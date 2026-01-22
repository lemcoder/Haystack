package io.github.lemcoder.scriptEngine

import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import lua.*

actual fun instantiateScriptEngine(): ScriptEngine {
    return NativeScriptEngine()
}

@OptIn(ExperimentalForeignApi::class)
class NativeScriptEngine : ScriptEngine {
    
    private val L: CPointer<lua_State>? = luaL_newstate()
    
    // Store registered functions to prevent them from being garbage collected
    private val registeredFunctions = mutableMapOf<String, Pair<ScriptFunction, CPointer<*>>>()
    
    init {
        if (L == null) {
            throw RuntimeException("Failed to create Lua state")
        }
        // Open standard libraries
        LuaMacros.luaL_openlibs(L)
    }
    
    override fun eval(script: String): ScriptValue {
        if (L == null) {
            throw RuntimeException("Lua state is null")
        }
        
        return try {
            // Load and execute the script
            val result = luaL_loadstring(L, script)
            if (result != LUA_OK) {
                val error = LuaMacros.lua_tostring(L, -1)?.toKString() ?: "Unknown error"
                LuaMacros.lua_pop(L, 1)
                throw RuntimeException("Lua compilation error: $error")
            }
            
            // Execute the loaded chunk
            val execResult = LuaMacros.lua_pcall(L, 0, LUA_MULTRET, 0)
            if (execResult != LUA_OK) {
                val error = LuaMacros.lua_tostring(L, -1)?.toKString() ?: "Unknown error"
                LuaMacros.lua_pop(L, 1)
                throw RuntimeException("Lua execution error: $error")
            }
            
            // Get the top of the stack (result count)
            val top = lua_gettop(L)
            if (top == 0) {
                ScriptValue.Nil
            } else {
                // Convert the top value to ScriptValue
                val result = ScriptValueConverter.toScriptValue(L, -1)
                LuaMacros.lua_pop(L, top) // Clean up the stack
                result
            }
        } catch (e: Exception) {
            throw RuntimeException("Lua execution error: ${e.message}", e)
        }
    }

    override fun setGlobal(name: String, value: ScriptValue) {
        if (L == null) {
            throw RuntimeException("Lua state is null")
        }
        
        ScriptValueConverter.pushToLua(L, value)
        lua_setglobal(L, name)
    }

    override fun registerFunction(name: String, fn: ScriptFunction) {
        if (L == null) {
            throw RuntimeException("Lua state is null")
        }
        
        // Create a stable reference to the function and its wrapper
        val functionRef = StableRef.create(fn)
        
        // Create a C function that bridges to our Kotlin function
        val cFunction = staticCFunction { luaState: CPointer<lua_State>? ->
            if (luaState == null) return@staticCFunction 0
            
            try {
                // Get the function from upvalue
                val fnRef = lua_touserdata(luaState, LuaMacros.lua_upvalueindex(1))
                    ?.asStableRef<ScriptFunction>()
                    ?: return@staticCFunction 0
                
                val kotlinFunction = fnRef.get()
                
                // Get the number of arguments
                val nArgs = lua_gettop(luaState)
                
                // Convert arguments from Lua to ScriptValue
                val args = mutableListOf<ScriptValue>()
                for (i in 1..nArgs) {
                    args.add(ScriptValueConverter.toScriptValue(luaState, i))
                }
                
                // Call the Kotlin function
                val result = runBlocking {
                    kotlinFunction.invoke(args)
                }
                
                // Push the result back to Lua
                ScriptValueConverter.pushToLua(luaState, result)
                
                // Return 1 to indicate one return value
                1
            } catch (e: Exception) {
                // Push error message
                lua_pushstring(luaState, "Error in function: ${e.message}")
                lua_error(luaState)
                0
            }
        }
        
        // Push the function reference as a light userdata (upvalue)
        lua_pushlightuserdata(L, functionRef.asCPointer())
        
        // Push the C closure with the upvalue
        lua_pushcclosure(L, cFunction, 1)
        
        // Set it as a global function
        lua_setglobal(L, name)
        
        // Store the reference to prevent garbage collection
        registeredFunctions[name] = Pair(fn, functionRef.asCPointer())
    }

    override fun close() {
        if (L != null) {
            // Dispose all registered function references
            registeredFunctions.values.forEach { (_, ptr) ->
                ptr.asStableRef<ScriptFunction>().dispose()
            }
            registeredFunctions.clear()
            
            // Close the Lua state
            lua_close(L)
        }
    }
}
