package io.github.lemcoder.lua

import io.github.lemcoder.lua.value.LuaFunction
import io.github.lemcoder.lua.value.LuaThread
import io.github.lemcoder.lua.value.LuaValue
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * A `lua_State *` wrapper, representing a Lua thread
 *
 *
 *
 * Most methods in this interface are wrappers around the corresponding Lua C API functions,
 * and requires a certain degree of familiarity with the Lua C API.
 * If you are not that familiar with the Lua C API, you may want to read the Lua manual first
 * or try out [LuaValue]-related API at the [LuaThread] interface.
 *
 */
interface Lua : AutoCloseable, LuaThread {
    /**
     * Ensures that there are at least `extra` free stack slots in the Lua stack
     *
     *
     *
     * It wraps `lua_checkstack`.
     *
     *
     * @param extra the extra slots to ensure
     * @throws RuntimeException when unable to grow the stack
     */
    @Throws(RuntimeException::class)
    fun checkStack(extra: Int)

    /* Push-something functions */
    /**
     * Push an object onto the stack, converting according to [Conversion].
     *
     * @param obj` the object to be pushed onto the stack
     * @param degree how the object is converted into lua values
     * @see Conversion
     */
    fun push(obj: Any?, degree: Conversion?)

    /**
     * Pushes a `nil` value onto the stack
     */
    fun pushNil()

    /**
     * Pushes a boolean value onto the stack
     *
     * @param bool the boolean value
     */
    fun push(bool: Boolean)

    /**
     * Pushes a floating-point number onto the stack
     *
     * @param number the number, whose [Number.doubleValue] will be pushed
     */
    fun push(number: Number?)

    /**
     * Pushes an integer onto the stack
     *
     *
     *
     * Please note that on some 32-bit platforms, 64-bit integers are likely to get
     * truncated instead of getting approximated into a floating-point number.
     * If you want to approximate an integer, cast it to double and use [.push].
     *
     *
     * @param integer the number
     */
    fun push(integer: Long)

    /**
     * Pushes a string onto the stack.
     *
     * @param string the string
     */
    fun push(string: String?)

    /**
     * Pushes a buffer as a raw string onto the stack
     *
     *
     *
     * The pushed bytes are from buffer[buffer.position()] to buffer[buffer.limit() - 1].
     * So remember to call [ByteBuffer.flip] or set the position and limit before pushing.
     *
     *
     * @param buffer the buffer, which might contain invalid UTF-8 characters and zeros
     */
    fun push(buffer: ByteArray)

    /**
     * Push the element onto the stack, converted to lua tables
     *
     *
     *
     * Inner elements are converted with [Conversion.FULL].
     *
     *
     * @param map the element to be pushed onto the stack
     */
    fun push(map: MutableMap<*, *>?)

    /**
     * Push the element onto the stack, converted to lua tables (index starting from 1)
     *
     *
     *
     * Inner elements are converted with [Conversion.FULL].
     *
     *
     * @param collection the element to be pushed onto the stack
     */
    fun push(collection: MutableCollection<*>?)

    /**
     * Push an array onto the stack, converted to luatable
     *
     * @param array a array
     * @throws IllegalArgumentException when the object is not array
     */
    @Throws(IllegalArgumentException::class)
    fun pushArray(array: Any?)

    /**
     * Push the function onto the stack, converted to a callable element
     *
     *
     *
     * The function is wrapped into a C closure, which means Lua will
     * treat the function as a C function. Checking [.isFunction] on the pushed
     * element will return true.
     *
     *
     * @param function the function to be pushed onto the stack
     */
    fun push(function: KFunction<*>?)

    /**
     * Push a class onto the stack, which may be used with `java.new` on the lua side
     *
     * @param clazz the class
     */
    fun pushKotlinClass(clazz: KClass<*>?)

    /**
     * Push a [LuaValue] onto the stack, equivalent to [LuaValue.push]
     *
     * @param value the value
     */
    fun push(value: LuaValue)

    /**
     * Push the function onto the stack, converted to a callable element
     *
     * @param function the function
     * @see .push
     */
    fun push(function: LuaFunction)

    /* Convert-something (into Java) functions */
    /**
     * Converts the Lua value at the given acceptable index to a number
     *
     *
     *
     * The Lua value must be a number or a string convertible to a number; otherwise,
     * `lua_tonumber` returns 0.
     *
     *
     * @param index the stack index
     * @return the converted value, zero if not convertible
     */
    fun toNumber(index: Int): Double

    /**
     * Converts the Lua value at the given acceptable index to the signed integral type lua_Integer
     *
     *
     *
     * The Lua value must be a number or a string convertible to a number; otherwise,
     * `lua_tointeger` returns 0. If the number is not an integer, it is truncated
     * in some non-specified way.
     *
     *
     * @param index the stack index
     * @return the converted value, zero if not convertible
     */
    fun toInteger(index: Int): Long

    /**
     * Converts the Lua value at the given acceptable index to a boolean value
     *
     *
     *
     * Like all tests in Lua, `lua_toboolean` returns 1 for any Lua value different from
     * `false` and `nil`; otherwise it returns 0.
     * It also returns 0 when called with a non-valid index.
     *
     *
     * @param index the stack index
     * @return the converted value, `false` with and only with `false`, `nil` or `TNONE`
     */
    fun toBoolean(index: Int): Boolean

    /**
     * Automatically converts a value into a Java object
     *
     *
     *  1. ***nil*** is converted to `null`.
     *  1. ***boolean*** converted to `boolean` or the boxed `Boolean`.
     *  1. ***integer*** / ***number*** to any of `char` `byte` `short` `int` `long` `float` `double` or their boxed alternative.
     *  1. ***string*** to `String`.
     *  1. ***table*** to `Map<Object, Object>`, converted recursively.
     *  1. ***jclass*** to `Class<?>`.
     *  1. ***jobject*** to the underlying Java object.
     *  1. Other types are not converted and are `null` on the Java side.
     *
     *
     * @param index the stack index
     * @return the converted object, `null` if unable to converted
     */
    fun toObject(index: Int): Any?

    /**
     * Converts a value at the stack index
     *
     * @param index the stack index
     * @param type  the target type
     * @return the converted value, `null` if unable to converted
     * @see .toObject
     */
    fun toObject(index: Int, type: KClass<*>): Any?

    /**
     * Converts the Lua value at the given acceptable index to a string
     *
     *
     *
     * The Lua value must be a string or a number; otherwise, the function returns NULL.
     * If the value is a number, then lua_tolstring *also changes the actual value*
     * in the stack to a string.
     *
     *
     * @param index the stack index
     * @return the converted string
     */
    fun toString(index: Int): String?

    /**
     * Creates a [java.nio.ByteBuffer] from the string at the specific index
     *
     *
     *
     * You may want to use this instead of [.toString] when the string is binary
     * (e.g., those returned by `string.dump` and contains null characters).
     *
     *
     * @param index the stack index
     * @return the created buffer
     */
    fun toBuffer(index: Int): ByteArray?

    /**
     * Creates a read-only direct [java.nio.ByteBuffer] from the string at the specific index
     *
     *
     *
     * The memory of this buffer is managed by Lua.
     * So you should never use the buffer after popping the corresponding value
     * from the Lua stack.
     *
     *
     * @param index the stack index
     * @return the created read-only buffer
     */
    fun toDirectBuffer(index: Int): ByteArray?

    /**
     * Get the element at the specified stack position, if the element is a Java object / array / class
     *
     * @param index the stack position of the element
     * @return the Java object or null
     */
    fun toKotlinObject(index: Int): Any?

    /**
     * Get the element at the specified stack position, converted to a [Map]
     *
     *
     *
     * The element may be a Lua table or a Java [Map], or else, it returns null.
     *
     *
     * @param index the stack position of the element
     * @return the map or null
     */
    fun toMap(index: Int): MutableMap<*, *>?

    /**
     * Get the element at the specified stack position, converted to [List]
     *
     *
     *
     * The element may be a Lua table or a Java [List], or else, it returns null.
     *
     *
     * @param index the stack position of the element
     * @return the list or null
     */
    fun toList(index: Int): MutableList<*>?

    /* Type-checking function */
    /**
     * Returns true if the value at the given index is a boolean, and false otherwise
     *
     * @param index the stack index
     * @return true if the value at the given index is a boolean, and false otherwise
     */
    fun isBoolean(index: Int): Boolean

    /**
     * Returns true if the value at the given index is a function (either C or Lua), and false otherwise
     *
     *
     *
     * When one pushes a [JFunction] onto the stack using [.push],
     * the [JFunction] is wrapped into a C closure, so that it is treated as a C function in Lua.
     *
     *
     * @param index the stack index
     * @return true if the value at the given index is a function (either C or Lua), and false otherwise
     */
    fun isFunction(index: Int): Boolean

    /**
     * Checks if the element is a Java object
     *
     *
     *
     * Note that a [JFunction] pushed with [.push] is not a Java object
     * any more, but a C function.
     *
     *
     * @param index the element to check type for
     * @return `true` if the element is a Java object, a Java class, or a Java array
     */
    fun isJavaObject(index: Int): Boolean

    /**
     * Returns true if the value at the given index is nil, and false otherwise
     *
     * @param index the stack index
     * @return true if the value at the given index is nil, and false otherwise
     */
    fun isNil(index: Int): Boolean

    /**
     * Returns true if the given index is not valid, and false otherwise
     *
     * @param index the stack index
     * @return true if the given index is not valid, and false otherwise
     */
    fun isNone(index: Int): Boolean

    /**
     * Returns true if the given index is not valid or if the value at this index is nil, and false otherwise
     *
     * @param index the stack index
     * @return true if the given index is not valid or if the value at this index is nil, and false otherwise
     */
    fun isNoneOrNil(index: Int): Boolean

    /**
     * Returns true if the value is a number or a string convertible to a number, and false otherwise
     *
     * @param index the stack index
     * @return true if the value is a number or a string convertible to a number, and false otherwise
     */
    fun isNumber(index: Int): Boolean

    /**
     * Returns true if the value at the given index is an integer, and false otherwise
     *
     *
     *
     * (that is, the value is a number and is represented as an integer)
     *
     *
     * @param index the stack index
     * @return true if the value is an integer, and false otherwise
     */
    fun isInteger(index: Int): Boolean

    /**
     * Returns true if the value at the given index is a string or a number
     *
     * @param index the stack index
     * @return true if the value at the given index is a string or a number, and false otherwise.
     */
    fun isString(index: Int): Boolean

    /**
     * Returns true if the value at the given index is a table, and false otherwise.
     *
     * @param index the stack index
     * @return Returns true if the value at the given index is a table, and false otherwise.
     */
    fun isTable(index: Int): Boolean

    /**
     * Returns true if the value at the given index is a thread, and false otherwise.
     *
     * @param index the stack index
     * @return true if the value at the given index is a thread, and 0 otherwise.
     */
    fun isThread(index: Int): Boolean

    /**
     * Returns true if the value at the given index is userdata (either full or light), and false otherwise.
     *
     * @param index the stack index
     * @return true if the value at the given index is userdata (either full or light), and false otherwise.
     */
    fun isUserdata(index: Int): Boolean

    /**
     * Returns the Lua type of the element at the given index.
     *
     * @param index the element to inspect
     * @return the lua type of the element, `null` if unrecognized (in, for example, incompatible lua versions)
     */
    fun type(index: Int): LuaType

    /* Measuring functions */
    /**
     * Returns true if the two values in acceptable indices i1 and i2 are equal
     *
     *
     *
     * Returns true if the two values in acceptable indices index1 and index2 are equal,
     * following the semantics of the Lua == operator (that is, may call metamethods).
     * Otherwise returns false. Also returns false if any of the indices is non valid.
     *
     *
     * @param i1 the index of the first element
     * @param i2 the index of the second element
     * @return true if the two values in acceptable indices i1 and i2 are equal
     */
    fun equal(i1: Int, i2: Int): Boolean

    /**
     * Returns the raw "length" of the value at the given index
     *
     *
     *
     * For strings, this is the string length;
     * for tables, this is the result of the length operator ('#') with no metamethods;
     * for userdata, this is the size of the block of memory allocated for the userdata.
     * For other values, this call returns 0.
     *
     *
     * @param index the stack index
     * @return the raw length of the element
     */
    fun rawLength(index: Int): Int

    /**
     * Returns true if the value at acceptable index i1 is smaller than the value at i2
     *
     *
     *
     * It follows the semantics of the Lua &lt; operator (that is, may call metamethods).
     * Otherwise returns false. Also returns false if any of the indices is non valid.
     *
     *
     * @param i1 the index of the first element
     * @param i2 the index of the second element
     * @return true if the value at acceptable index i1 is smaller than the value at i2
     */
    fun lessThan(i1: Int, i2: Int): Boolean

    /**
     * Returns true if the two values in acceptable indices i1 and i2 are primitively equal
     *
     *
     *
     * It does not call metamethods.
     * Otherwise returns false. Also returns false if any of the indices are non valid.
     *
     *
     * @param i1 the index of the first element
     * @param i2 the index of the second element
     * @return true if the two values in acceptable indices i1 and i2 are primitively equal
     */
    fun rawEqual(i1: Int, i2: Int): Boolean

    /* Other stack manipulation functions */
    /**
     * Returns the index of the top element in the stack
     *
     *
     *
     * Because indices start at 1, this result is equal to the number of elements in the stack
     * (and so 0 means an empty stack).
     *
     *
     * @return the index of the top element in the stack
     */
    /**
     * Accepts any index, or 0, and sets the stack top to this index
     *
     *
     *
     * If the new top is greater than the old one, then the new elements are filled with nil.
     * If index is 0, then all stack elements are removed.
     *
     *
     * @param index the new top element index
     */
    fun getTop(): Int

    fun setTop(index: Int)

    /**
     * Moves the top element into the given valid index, shifting up the elements above this index
     *
     *
     *
     * Moves the top element into the given valid index,
     * shifting up the elements above this index to open space.
     * Cannot be called with a pseudo-index,
     * because a pseudo-index is not an actual stack position.
     *
     *
     * @param index the non-pseudo index
     * @see .pushValue
     * @see .replace
     */
    fun insert(index: Int)

    /**
     * Pops n elements from the stack
     *
     * @param n the number of elements to pop
     */
    fun pop(n: Int)

    /**
     * Pushes a copy of the element at the given valid index onto the stack
     *
     * @param index the index of the element to be copied
     * @see .insert
     * @see .replace
     */
    fun pushValue(index: Int)

    /**
     * Pushes the current thread onto the stack
     */
    fun pushThread()

    /**
     * Removes the element at the given valid index
     *
     *
     *
     * Removes the element at the given valid index,
     * shifting down the elements above this index to fill the gap.
     * Cannot be called with a pseudo-index,
     * because a pseudo-index is not an actual stack position.
     *
     *
     * @param index the index of the element to be removed
     */
    fun remove(index: Int)

    /**
     * Moves the top element into the given position (and pops it)
     *
     *
     *
     * Moves the top element into the given position (and pops it),
     * without shifting any element (therefore replacing the value at the given position).
     *
     *
     * @param index the index to move to
     * @see .insert
     * @see .pushValue
     */
    fun replace(index: Int)

    /* Executing functions */
    /**
     * Loads a string as a Lua chunk
     *
     *
     *
     * This function eventually uses `lua_load` to load the chunk in the string `script`.
     * **Also as lua_load, this function only loads the chunk; it does not run it.**
     *
     *
     * @param script the Lua chunk
     * @see .run
     * @see .pCall
     */
    @Throws(LuaException::class)
    fun load(script: String?)

    /**
     * Loads a buffer as a Lua chunk
     *
     *
     *
     * This function eventually uses `lua_load` to load the chunk in the string `script`.
     * **Also as lua_load, this function only loads the chunk; it does not run it.**
     *
     *
     *
     *
     * The used contents are from buffer[buffer.position()] to buffer[buffer.limit() - 1].
     * So remember to call [ByteBuffer.flip] or set the position and limit before pushing.
     *
     *
     * @param buffer the buffer, must be a direct buffer
     * @param name   the chunk name, used for debug information and error messages
     * @see .run
     * @see .pCall
     */
    @Throws(LuaException::class)
    fun load(buffer: ByteArray, name: String)

    /**
     * Loads and runs the given string
     *
     *
     *
     * It is equivalent to first calling [.load] and then [.pCall] the loaded chunk.
     *
     *
     * @param script the Lua chunk
     */
    @Throws(LuaException::class)
    fun run(script: String?)

    /**
     * Loads and runs a buffer
     *
     *
     *
     * It is equivalent to first calling [.load] and then [.pCall] the loaded chunk.
     *
     *
     *
     *
     * The used contents are from buffer[buffer.position()] to buffer[buffer.limit() - 1].
     * So remember to call [ByteBuffer.flip] or set the position and limit before pushing.
     *
     *
     * @param buffer the buffer, must be a direct buffer
     * @param name   the chunk name, used for debug information and error messages
     */
    @Throws(LuaException::class)
    fun run(buffer: ByteArray, name: String)

    /**
     * Dumps a function as a binary chunk
     *
     *
     *
     * Receives a Lua function on the top of the stack
     * and produces a binary chunk that,
     * if loaded again, results in a function equivalent to the one dumped.
     *
     *
     * @return the binary chunk, null if an error occurred
     */
    fun dump(): ByteArray

    /**
     * Calls a function in protected mode
     *
     *
     *
     * To call a function you must use the following protocol:
     * first, the function to be called is pushed onto the stack;
     * then, the arguments to the function are pushed in direct order;
     * that is, the first argument is pushed first.
     * Finally you call lua_call; nargs is the number of arguments that you pushed onto the stack.
     * All arguments and the function value are popped from the stack when the function is called.
     * The function results are pushed onto the stack when the function returns.
     * The number of results is adjusted to nresults, unless nresults is LUA_MULTRET.
     * In this case, all results from the function are pushed.
     * Lua takes care that the returned values fit into the stack space.
     * The function results are pushed onto the stack in direct order (the first result is pushed first),
     * so that after the call the last result is on the top of the stack.
     *
     *
     * @param nArgs    the number of arguments that you pushed onto the stack
     * @param nResults the number of results to adjust to
     */
    @Throws(LuaException::class)
    fun pCall(nArgs: Int, nResults: Int)

    /* Thread functions */
    /**
     * Creates a new thread, pushes it on the stack
     *
     *
     *
     * The new state returned by this function shares with the original state
     * all global objects (such as tables), but has an independent execution stack.
     *
     *
     * @return a new thread
     */
    fun newThread(): Lua?

    /**
     * Starts and resumes a coroutine in a given thread
     *
     *
     *
     * To start a coroutine, you first create a new thread (see lua_newthread or [.newThread]);
     * then you push onto its stack the main function plus any arguments;
     * then you call resume, with narg being the number of arguments.
     * This call returns when the coroutine suspends or finishes its execution.
     * When it returns, the stack contains all values passed to lua_yield,
     * or all values returned by the body function.
     * lua_resume returns LUA_YIELD if the coroutine yields,
     * 0 if the coroutine finishes its execution without errors,
     * or an error code in case of errors (see lua_pcall).
     * In case of errors, the stack is not unwound,
     * so you can use the debug API over it.
     * The error message is on the top of the stack.
     * To restart a coroutine, you put on its stack only the values to be passed as results from yield,
     * and then call lua_resume.
     *
     *
     * @param nArgs the number of arguments
     * @return `true` if the thread yielded, or `false` if it ended execution
     */
    @Throws(LuaException::class)
    fun resume(nArgs: Int): Boolean

    /**
     * Returns the status of the thread
     *
     * @return the status of the thread
     */
    fun status(): LuaException.LuaError

    /**
     * Yields a coroutine
     *
     *
     *
     * This is not implemented because we have no way to resume execution from a Java stack through a C stack
     * back to a Java stack.
     *
     *
     * @param n the number of values from the stack that are passed as results to lua_resume
     * @throws UnsupportedOperationException always
     */
    fun yield(n: Int)

    /* Table functions */
    /**
     * Creates a new empty table and pushes it onto the stack
     *
     *
     *
     * The new table has space pre-allocated for narr array elements and nrec non-array elements.
     * This pre-allocation is useful when you know exactly how many elements the table will have.
     *
     *
     * @param nArr pre-allocated array elements
     * @param nRec pre-allocated non-array elements
     */
    fun createTable(nArr: Int, nRec: Int)

    /**
     * Creates a new empty table and pushes it onto the stack
     *
     *
     *
     * It is equivalent to [createTable(0, 0)][.createTable].
     *
     */
    fun newTable()

    /**
     * Pushes onto the stack the value t[key]
     *
     *
     *
     * Pushes onto the stack the value t[key], where t is the value at the given valid index.
     * As in Lua, this function may trigger a metamethod for the "index" event.
     *
     *
     * @param index the index of the table-like element
     * @param key   the key to look up
     */
    fun getField(index: Int, key: String?)

    /**
     * Does the equivalent to t[key] = v
     *
     *
     *
     * Does the equivalent to t[key] = v,
     * where t is the value at the given valid index and v is the value at the top of the stack.
     * This function pops the value from the stack.
     * As in Lua, this function may trigger a metamethod for the "newindex" event.
     *
     *
     * @param index the index of the table-like element
     * @param key   the key to assign to
     */
    fun setField(index: Int, key: String?)

    /**
     * Pushes onto the stack the value t[k]
     *
     *
     *
     * Pushes onto the stack the value t[k],
     * where t is the value at the given valid index and k is the value at the top of the stack.
     * This function pops the key from the stack (putting the resulting value in its place).
     * As in Lua, this function may trigger a metamethod for the "index" event.
     *
     *
     * @param index the index of the table-like element
     */
    fun getTable(index: Int)

    /**
     * Does the equivalent to t[k] = v
     *
     *
     *
     * Does the equivalent to t[k] = v, where t is the value at the given valid index,
     * v is the value at the top of the stack, and k is the value just below the top.
     * This function pops both the key and the value from the stack.
     * As in Lua, this function may trigger a metamethod for the "newindex" event.
     *
     *
     * @param index the index of the table-like element
     */
    fun setTable(index: Int)

    /**
     * Pops a key from the stack, and pushes a key-value pair from the table at the given index
     *
     *
     *
     * Pops a key from the stack, and pushes a key-value pair from the table at the given index
     * (the "next" pair after the given key). If there are no more elements in the table,
     * then lua_next returns 0 (and pushes nothing).
     *
     *
     *
     *
     * A typical traversal looks like this:
     *
     *
     * <pre>`
     * / * table is in the stack at index 't' *&#47;
     * lua_pushnil(L);  / * first key *&#47;
     * while (lua_next(L, t) != 0) {
     * / * uses 'key' (at index -2) and 'value' (at index -1) *&#47;
     * printf("%s - %s\n",
     * lua_typename(L, lua_type(L, -2)),
     * lua_typename(L, lua_type(L, -1)));
     * / * removes 'value'; keeps 'key' for next iteration *&#47;
     * lua_pop(L, 1);
     * }
    `</pre> *
     *
     *
     *
     * While traversing a table, do not call [.toString] directly on a key,
     * unless you know that the key is actually a string.
     * Recall that [.toString] changes the value at the given index;
     * this confuses the next call to lua_next.
     *
     *
     * @param n the index of the table
     * @return 0 if there are no more elements
     */
    fun next(n: Int): Int

    /**
     * Similar to [.getTable], but does a raw access (i.e., without metamethods)
     *
     * @param index the index of the table
     */
    fun rawGet(index: Int)

    /**
     * Pushes onto the stack the value t[n], where t is the value at the given valid index
     *
     *
     *
     * The access is raw; that is, it does not invoke metamethods.
     *
     *
     * @param index the index of the table
     * @param n     the key
     */
    fun rawGetI(index: Int, n: Int)

    /**
     * Similar to [.setTable], but does a raw assignment (i.e., without metamethods)
     *
     * @param index the index of the table
     */
    fun rawSet(index: Int)

    /**
     * Does the equivalent of t[n] = v
     *
     *
     *
     * Does the equivalent of t[n] = v,
     * where t is the value at the given valid index and v is the value at the top of the stack.
     *
     *
     * This function pops the value from the stack. The assignment is raw;
     * that is, it does not invoke metamethods.
     *
     *
     * @param index the index of the table
     * @param n     the key
     */
    fun rawSetI(index: Int, n: Int)

    /**
     * Creates and returns a reference, in the table at index `index`
     *
     *
     *
     * Creates and returns a reference, in the table at index t, for the object at the top of the stack (and pops the object).
     *
     *
     *
     * A reference is a unique integer key.
     * As long as you do not manually add integer keys into table t,
     * luaL_ref ensures the uniqueness of the key it returns.
     * You can retrieve an object referred by reference r by calling [.rawGetI].
     * Function [.unRef] frees a reference and its associated object.
     *
     *
     * @param index the index of the table
     * @return the created reference
     */
    fun ref(index: Int): Int

    /**
     * Calls [.ref] with the pseudo-index `LUA_REGISTRYINDEX`
     *
     * @return the created reference
     */
    fun ref(): Int

    /**
     * Calls [.rawGetI] with the pseudo-index `LUA_REGISTRYINDEX` and the given `ref`
     *
     * @param ref the reference on `LUA_REGISTRYINDEX` table
     */
    fun refGet(ref: Int)

    /**
     * Releases reference ref from the table at index `index`
     *
     *
     *
     * The entry is removed from the table, so that the referred object can be collected.
     * The reference ref is also freed to be used again.
     *
     *
     * @param index the index of the table
     * @param ref   the reference to be freed
     */
    fun unRef(index: Int, ref: Int)

    /**
     * Calls [.unRef] with the pseudo-index `LUA_REGISTRYINDEX` and the given `ref`
     *
     * @param ref the reference to be freed
     */
    fun unref(ref: Int)

    /* Meta functions */
    /**
     * Pushes onto the stack the value of the global `name`
     *
     * @param name the global name
     */
    fun getGlobal(name: String?)

    /**
     * Pops a value from the stack and sets it as the new value of global `name`
     *
     * @param name the global name
     */
    fun setGlobal(name: String?)

    /**
     * Pushes onto the stack the metatable of the value at the given acceptable index
     *
     *
     *
     * Pushes onto the stack the metatable of the value at the given acceptable index.
     * If the index is not valid, or if the value does not have a metatable,
     * the function returns 0 and pushes nothing on the stack.
     *
     *
     * @param index the index of the element
     * @return 0 if the value does not have a metatable
     */
    fun getMetatable(index: Int): Int

    /**
     * Pops a table from the stack and sets it as the new metatable for the value at the given acceptable index
     *
     * @param index the index of the element
     */
    fun setMetatable(index: Int)

    /**
     * Pushes onto the stack the field `field` from the metatable of the object at index `index`
     *
     *
     *
     * If the object does not have a metatable,
     * or if the metatable does not have this field, returns 0 and pushes nothing.
     *
     *
     * @param index the index of the element
     * @param field the meta field
     * @return 0 if no such field
     */
    fun getMetaField(index: Int, field: String?): Int

    /**
     * Pushes onto the stack the metatable associated with name tname in the registry
     *
     * @param typeName the name of the user-defined type
     * @see .newRegisteredMetatable
     */
    fun getRegisteredMetatable(typeName: String?)

    /**
     * Creates a new table to be used as a metatable for userdata, adds it to the registry
     *
     *
     *
     * If the registry already has the key `typeName`, returns 0.
     * Otherwise, creates a new table to be used as a metatable for userdata,
     * adds it to the registry with key `typeName`, and returns 1.
     *
     *
     *
     * In both cases pushes onto the stack the final value associated with tname in the registry.
     *
     *
     * @param typeName the name of the user-defined type
     * @return 1 if added to registry, 0 if already registered
     */
    fun newRegisteredMetatable(typeName: String?): Int

    /* Libraries */
    /**
     * Opens all standard Lua libraries into the given state
     *
     *
     * See [.openLibrary] for more info.
     */
    fun openLibraries()

    /**
     * Opens a specific library into the given state
     *
     *
     *
     * See the corresponding Lua manual for available library. For example,
     * in [Lua 5.4](https://lua.org/manual/5.4/manual.html#6),
     * calling `L.openLibrary("base")` will call the `luaopen_base`
     * C function for the base library.
     *
     *
     *
     *
     * Note that opening a library may modify global variables. For example,
     * opening the `base` library in some specific Lua versions will override
     * built-in functions like `print`. So make sure to make your modifications
     * after opening all the libraries.
     *
     *
     * @param name the library name
     */
    fun openLibrary(name: String?)

    /**
     * Concatenates the n values at the top of the stack, pops them, and leaves the result at the top
     *
     *
     *
     * If n is 1, the result is the single value on the stack (that is, the function does nothing);
     * if n is 0, the result is the empty string.
     * Concatenation is performed following the usual semantics of Lua.
     *
     *
     * @param n the number of values on top of the stack to concatenate
     */
    fun concat(n: Int)

    /**
     * Performs a full garbage-collection cycle
     *
     *
     *
     * This also removes unneeded references created by finalized proxies and Lua values.
     *
     */
    fun gc()

    /**
     * Throws an error inside a Lua environment
     *
     *
     *
     * It currently just throws a [RuntimeException].
     *
     *
     * @param message the error message
     */
    fun error(message: String?)

    /**
     * Creates a proxy object, implementing all the specified interfaces, with a Lua table / function on top of the stack
     *
     *
     *
     * This method pops the value on top on the stack and creates reference to it with [.ref].
     *
     *
     *
     *
     * When invoking methods, the created Java object, instead of the backing Lua table, is passed
     * as the first parameter to the Lua function.
     *
     *
     * @param interfaces the interfaces to implement
     * @param degree     the conversion degree when passing parameters and return values
     * @return a proxy object, calls to which are proxied to the underlying Lua table
     * @throws IllegalArgumentException if not all classes are interfaces
     */
    @Throws(IllegalArgumentException::class)
    fun createProxy(interfaces: Array<KClass<*>>, degree: Conversion): Any?

    /**
     * Sets a [ExternalLoader] for the main state
     *
     *
     *
     * The provided external loader will be integrated into Lua's module resolution progress.
     * See [require (modname)](https://www.lua.org/manual/5.2/manual.html#pdf-require)
     * for an overview.
     *
     *
     *
     * We will register a new searcher by appending to `package.searchers` (or
     * `package.loaders` for Lua 5.1) to load Lua files with this [ExternalLoader].
     *
     *
     *
     * You need to load the `package` library to make the external loader effective.
     *
     *
     * @param loader the loader that will be used to find files
     */
//    fun setExternalLoader(loader: ExternalLoader?)

    /**
     * Loads a chunk from a [ExternalLoader] set by [.setExternalLoader]
     *
     * @param module the module
     */
//    @Throws(LuaException::class)
//    fun loadExternal(module: String?)

    /**
     * Returns the underlying native Lua interface.
     *
     * @return the underlying [LuaNatives] natives
     */
//    val luaNatives: LuaNatives?

    /**
     * Returns the main Lua state.
     *
     * @return the main Lua state
     */
    val mainState: Lua?

    /**
     * Returns the pointer to the internal Lua state.
     *
     * @return the pointer to the internal `lua_State`
     */
    val pointer: Long

    /**
     * Returns the unique identifier of this Lua thread.
     *
     * @return the unique identifier to the Lua thread
     */
    val id: Int

    /**
     * Fetches the most recent Java [Throwable] passed to Lua
     *
     * @return value of the Lua global [.GLOBAL_THROWABLE]
     */
    val javaError: Throwable?

    /**
     * Sets the Lua global [.GLOBAL_THROWABLE] to the throwable
     *
     *
     *
     * If the exception is `null`, it clears the global exception and pushes nothing.
     * Otherwise, it sets the Lua global [.GLOBAL_THROWABLE] to the throwable,
     * and pushes [Throwable.toString] onto the stack.
     *
     *
     * @param e the exception
     * @return 0 if e is null, -1 otherwise
     */
    fun error(e: Throwable?): Int

    /**
     * Closes the thread
     *
     *
     *
     * You need to make sure that you call this method no more than once,
     * or else the Lua binary may / will very likely just crash.
     *
     */
    override fun close()

    /**
     * Pops the value on top of the stack and return a LuaValue referring to it
     *
     * @return a reference to the value
     */
    fun get(): LuaValue?

    /**
     * Controls the degree of conversion from Java to Lua
     */
    enum class Conversion {
        /**
         * Converts everything possible, including the following classes:
         *
         *
         *  * Boolean -&gt; boolean
         *  * String -&gt; string
         *  * ByteBuffer -&gt; string
         *  * Number -&gt; lua_Number
         *  * Map / Collection / Array -&gt; table (recursive)
         *  * Object -&gt; Java object wrapped by a metatable [.pushJavaObject]
         *
         *
         *
         *
         * Note that this means luatable changes on the lua side will not get reflected
         * to the Java side.
         *
         */
        FULL,

        /**
         * Converts immutable types, including:
         *
         *  * Boolean
         *  * String
         *  * Number
         *
         *
         *
         *
         * [Map], [Collection], etc. are pushed with [.pushJavaObject].
         * Arrays are pushed with [.pushJavaArray].
         *
         */
        SEMI,

        /**
         * All objects, including [Integer], for example, are pushed as either
         * Java objects (with [.pushJavaObject]) or Java arrays
         * (with [.pushJavaArray]).
         */
        NONE
    }

    /**
     * Lua data types
     */
    enum class LuaType {
        /** Boolean type  */
        BOOLEAN,

        /** Function type  */
        FUNCTION,

        /** Light userdata type  */
        LIGHTUSERDATA,

        /** Nil type  */
        NIL,

        /** None/invalid type  */
        NONE,

        /** Number type  */
        NUMBER,

        /** String type  */
        STRING,

        /** Table type  */
        TABLE,

        /** Thread type  */
        THREAD,

        /** Userdata type  */
        USERDATA
    }

    companion object {
        /**
         * The global key used to store Java exceptions in Lua.
         */
        const val GLOBAL_THROWABLE: String = "__jthrowable__"
    }
}

expect fun getLua(): Lua