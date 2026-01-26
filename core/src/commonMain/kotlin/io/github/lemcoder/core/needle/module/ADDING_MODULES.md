# Adding New Modules to Needle

This guide explains how to add new modules to the Needle Lua executor framework following the established patterns.

## Overview

Modules in Needle extend the Lua runtime with platform-specific functionality. Each module follows a three-layer architecture:

1. **Common Interface** - Platform-agnostic API definition
2. **Platform Implementation** - Platform-specific implementation (e.g., Android)
3. **Test Mock** - In-memory mock for testing

## Module Architecture

### 1. Common Interface (commonMain)

Location: `needle/src/commonMain/kotlin/io/github/lemcoder/needle/module/`

Create an interface that extends `Module`:

```kotlin
package io.github.lemcoder.core.needle.module

interface YourModule: Module {
    override val name: String
        get() = "yourmodule"

    override fun install() { }

    // Define your module's API methods
    fun doSomething(param: String): Result
}
```

**Key points:**
- Extend `Module` interface
- Provide default `name` property
- Keep `install()` empty (implemented in platform layer)
- Define only the API contract, no implementation

### 2. Platform Implementation (androidMain)

Location: `needle/src/androidMain/kotlin/io/github/lemcoder/needle/module/`

Implement the module for the target platform:

```kotlin
package io.github.lemcoder.core.needle.module

import party.iroiro.luajava.AbstractLua

internal class LuaYourModule(
    private val lua: AbstractLua,
    // Add any platform dependencies (e.g., Context, File)
) : YourModule {
    
    /**
     * Helper object to expose functions to Lua
     */
    private val yourModuleApi = object {
        fun doSomething(param: String) = this@LuaYourModule.doSomething(param)
    }

    override fun install() = with(lua) {
        // If your module returns complex types, add converter functions
        // For Maps:
        push { lua ->
            val javaMap = lua.toJavaObject(1) as? Map<*, *>
                ?: throw IllegalArgumentException("Expected Map object")
            lua.pushMap(javaMap)
            1
        }
        setGlobal("__convertMapToTable")
        
        // For Lists:
        push { lua ->
            val javaList = lua.toJavaObject(1) as? List<*>
                ?: throw IllegalArgumentException("Expected List object")
            lua.pushList(javaList)
            1
        }
        setGlobal("__convertListToTable")

        // Expose the API object to Lua
        set("__yourmodule_api", yourModuleApi)

        // Create Lua-side wrapper
        run(
            """
                yourmodule = {}
                function yourmodule:doSomething(param)
                    local result = __yourmodule_api:doSomething(param)
                    return __convertMapToTable(result)  -- if returning Map
                end
            """.trimIndent()
        )
    }

    override fun doSomething(param: String): Result {
        // Implement the actual functionality
        return Result()
    }
}
```

**Key patterns:**
- Use `internal` visibility
- Create a helper object that delegates to module methods
- In `install()`, expose the API object and create Lua wrapper functions
- Use converter functions for complex return types (Maps, Lists)
- Lua table access uses colon syntax: `yourmodule:doSomething()`

### 3. Test Mock Implementation (androidDeviceTest)

Location: `needle/src/androidDeviceTest/kotlin/io/github/lemcoder/needle/module/`

Create a test double for your module:

```kotlin
package io.github.lemcoder.core.needle.module

import party.iroiro.luajava.AbstractLua

internal class TestLuaYourModule(
    private val lua: AbstractLua
): YourModule {
    
    // Add callback hooks for test assertions
    var onDoSomethingCalled: ((param: String) -> Unit)? = null
    
    // In-memory state for testing
    private val storage = mutableMapOf<String, Any>()

    private val yourModuleApi = object {
        fun doSomething(param: String) = this@TestLuaYourModule.doSomething(param)
    }

    override fun install() = with(lua) {
        // Same converter setup as platform implementation
        set("__yourmodule_api", yourModuleApi)
        
        run(
            """
                yourmodule = {}
                function yourmodule:doSomething(param)
                    return __yourmodule_api:doSomething(param)
                end
            """.trimIndent()
        )
    }

    override fun doSomething(param: String): Result {
        onDoSomethingCalled?.invoke(param)
        // Return test data
        return Result()
    }
    
    // Helper methods for test setup
    fun setupTestData(key: String, value: Any) {
        storage[key] = value
    }
    
    fun clear() {
        storage.clear()
    }
}
```

**Key patterns:**
- Implement same interface as platform module
- Add callback properties for verifying calls in tests
- Use in-memory storage instead of real I/O
- Provide helper methods for test setup

### 4. Integration with Executor

Update `LuaExecutor.android.kt`:

```kotlin
actual fun createLuaExecutor(): Executor {
    val lua = Lua55()
    val logModule = LuaLoggingModule(lua)
    val networkModule = LuaNetworkModule(lua)
    val fileSystemModule = LuaFileSystemModule(lua, File(System.getProperty("java.io.tmpdir")))
    val yourModule = LuaYourModule(lua) // Add your module

    return AndroidExecutor(lua, logModule, networkModule, fileSystemModule, yourModule)
}

internal class AndroidExecutor(
    private val lua: AbstractLua,
    private val logModule: LoggingModule,
    private val networkModule: NetworkModule,
    private val fileSystemModule: FileSystemModule,
    private val yourModule: YourModule // Add parameter
) : Executor {

    override fun <OUT> run(code: String, args: Map<String, Any?>): OUT? {
        lua.use { lua ->
            lua.openLibraries()

            logModule.install()
            networkModule.install()
            fileSystemModule.install()
            yourModule.install() // Install your module
            
            // ... rest of execution
        }
    }
}
```

### 5. Update Test Utilities

Update `TestUtils.kt`:

```kotlin
fun TestScope.createTestLuaExecutor(
    lua: AbstractLua = Lua55(),
    loggingModule: LoggingModule = TestLuaLoggingModule(lua),
    networkModule: NetworkModule = TestLuaNetworkModule(lua, this),
    fileSystemModule: FileSystemModule = TestLuaFileSystemModule(lua),
    yourModule: YourModule = TestLuaYourModule(lua) // Add parameter
): Executor = AndroidExecutor(lua, loggingModule, networkModule, fileSystemModule, yourModule)
```

### 6. Add Tests

Add tests in `LuaExecutorTest.kt`:

```kotlin
@Test
fun shouldCallYourModuleFunction() = runTest {
    val lua = Lua55()
    val yourModule = TestLuaYourModule(lua)
    
    var functionCalled = false
    yourModule.onDoSomethingCalled = { param ->
        assertEquals("expected", param)
        functionCalled = true
    }
    
    val executor = createTestLuaExecutor(
        lua = lua,
        yourModule = yourModule
    )

    val result: String? = executor.run(
        """
        local result = yourmodule:doSomething("expected")
        return result
        """.trimIndent(),
        emptyMap()
    )

    assertTrue(functionCalled, "Module function was not called")
    assertNotNull(result)
}
```

## Common Patterns

### Converting Kotlin Collections to Lua Tables

**For Maps:** Use `lua.pushMap()` (defined in `LuaExt.kt`)
```kotlin
// In Lua wrapper:
local result = __yourmodule_api:getData()
return __convertMapToTable(result)
```

**For Lists:** Use `lua.pushList()` (defined in `LuaExt.kt`)
```kotlin
// In Lua wrapper:
local result = __yourmodule_api:getList()
return __convertListToTable(result)
```

### Error Handling

Wrap platform operations in try-catch:

```kotlin
override fun doSomething(param: String): Result? {
    return try {
        // Platform-specific operation
        performOperation(param)
    } catch (e: Exception) {
        null // or throw specific error
    }
}
```

### Lua Table Indexing

Remember Lua arrays are 1-indexed:

```kotlin
// Kotlin
list.forEachIndexed { index, value ->
    lua.push(index + 1) // Lua index starts at 1
    lua.push(value)
    lua.setTable(-3)
}
```

## Examples

See existing modules for reference:
- **LoggingModule** - Simple void methods, no return values
- **NetworkModule** - Async operations, Map return types
- **FileSystemModule** - CRUD operations, List return types

## Checklist

When adding a new module:

- [ ] Create common interface in `commonMain/module/`
- [ ] Implement platform version in `androidMain/module/`
- [ ] Create test mock in `androidDeviceTest/module/`
- [ ] Add module to `LuaExecutor.android.kt`
- [ ] Update `TestUtils.kt` with new parameter
- [ ] Add comprehensive tests in `LuaExecutorTest.kt`
- [ ] Test all CRUD operations (if applicable)
- [ ] Verify callback hooks work in tests
- [ ] Run linter and fix any errors
- [ ] Document Lua API usage

## Tips for AI Coding Agents

When asked to add a new module:

1. **Read existing modules first** - LoggingModule, NetworkModule, and FileSystemModule are good templates
2. **Follow the naming convention** - `YourModule`, `LuaYourModule`, `TestLuaYourModule`
3. **Keep install() in common interface empty** - Platform layer handles it
4. **Use internal visibility** for implementation classes
5. **Add callback hooks to test mocks** - Essential for verifying behavior
6. **Use pushMap/pushList utilities** - Don't reimplement conversion logic
7. **Remember Lua 1-indexing** - Arrays start at 1, not 0
8. **Test all public methods** - Each API method should have at least one test
9. **Check linter errors** - Run `read_lints` after creating files
10. **Update all integration points** - Executor, TestUtils, and tests

## Common Issues

**Issue:** Linter error "Too many arguments for rawSet"
- **Solution:** Use `push(index) + push(value) + setTable(-3)` pattern

**Issue:** Lua can't access module methods
- **Solution:** Verify `install()` was called and API object is exposed

**Issue:** Maps/Lists not converting properly
- **Solution:** Use `__convertMapToTable` or `__convertListToTable` in Lua wrapper

**Issue:** Tests can't verify calls
- **Solution:** Add callback properties to test mock and invoke them in methods
