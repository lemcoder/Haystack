package io.github.lemcoder.lua

class LuaException(val type: LuaError?, message: String?) : RuntimeException(message) {
    override fun toString(): String {
        return type.toString() + ": " + super.toString()
    }

    /**
     * Lua-relevant error types.
     *
     *
     *
     * Integer values of Lua error codes may vary between Lua versions.
     * This library handles the conversion from the Lua integers to interpretable Java enum values.
     *
     */
    enum class LuaError {
        /**
         * a file-related error
         */
        FILE,

        /**
         * error while running a __gc metamethod
         */
        GC,

        /**
         * error while running the message handler
         */
        HANDLER,

        /**
         * memory allocation error
         */
        MEMORY,

        /**
         * no errors
         */
        OK,

        /**
         * a runtime error
         */
        RUNTIME,

        /**
         * syntax error during precompilation
         */
        SYNTAX,

        /**
         * the thread (coroutine) yields
         */
        YIELD,

        /**
         * unknown error code
         */
        UNKNOWN,

        /**
         * a Java-side error
         */
        JAVA,
    }
}