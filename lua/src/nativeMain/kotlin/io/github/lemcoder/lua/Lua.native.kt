package io.github.lemcoder.lua

import io.github.lemcoder.lua.value.LuaFunction
import io.github.lemcoder.lua.value.LuaValue
import lua.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

actual fun getLua(): Lua {
    TODO("Not yet implemented")
}

private class NativeLua : Lua {
    override fun checkStack(extra: Int) {
        TODO("Not yet implemented")
    }

    override fun push(`object`: Any?, degree: Lua.Conversion?) {
        TODO("Not yet implemented")
    }

    override fun pushNil() {
        TODO("Not yet implemented")
    }

    override fun push(bool: Boolean) {
        TODO("Not yet implemented")
    }

    override fun push(number: Number?) {
        TODO("Not yet implemented")
    }

    override fun push(integer: Long) {
        TODO("Not yet implemented")
    }

    override fun push(string: String?) {
        TODO("Not yet implemented")
    }

    override fun push(buffer: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun push(map: MutableMap<*, *>?) {
        TODO("Not yet implemented")
    }

    override fun push(collection: MutableCollection<*>?) {
        TODO("Not yet implemented")
    }

    override fun pushArray(array: Any?) {
        TODO("Not yet implemented")
    }

    override fun push(function: KFunction<*>?) {
        TODO("Not yet implemented")
    }

    override fun pushKotlinClass(clazz: KClass<*>?) {
        TODO("Not yet implemented")
    }

    override fun push(value: LuaValue?) {
        TODO("Not yet implemented")
    }

    override fun push(value: LuaFunction?) {
        TODO("Not yet implemented")
    }

    override fun pushJavaObject(`object`: Any?) {
        TODO("Not yet implemented")
    }

    override fun pushJavaArray(array: Any?) {
        TODO("Not yet implemented")
    }

    override fun toNumber(index: Int): Double {
        TODO("Not yet implemented")
    }

    override fun toInteger(index: Int): Long {
        TODO("Not yet implemented")
    }

    override fun toBoolean(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun toObject(index: Int): Any? {
        TODO("Not yet implemented")
    }

    override fun toObject(index: Int, type: KClass<*>?): Any? {
        TODO("Not yet implemented")
    }

    override fun toString(index: Int): String? {
        TODO("Not yet implemented")
    }

    override fun toBuffer(index: Int): ByteArray? {
        TODO("Not yet implemented")
    }

    override fun toDirectBuffer(index: Int): ByteArray? {
        TODO("Not yet implemented")
    }

    override fun toJavaObject(index: Int): Any? {
        TODO("Not yet implemented")
    }

    override fun toMap(index: Int): MutableMap<*, *>? {
        TODO("Not yet implemented")
    }

    override fun toList(index: Int): MutableList<*>? {
        TODO("Not yet implemented")
    }

    override fun isBoolean(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isFunction(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isJavaObject(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isNil(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isNone(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isNoneOrNil(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isNumber(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isInteger(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isString(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isTable(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isThread(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isUserdata(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun type(index: Int): Lua.LuaType? {
        TODO("Not yet implemented")
    }

    override fun equal(i1: Int, i2: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun rawLength(index: Int): Int {
        TODO("Not yet implemented")
    }

    override fun lessThan(i1: Int, i2: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun rawEqual(i1: Int, i2: Int): Boolean {
        TODO("Not yet implemented")
    }

    override var top: Int
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun insert(index: Int) {
        TODO("Not yet implemented")
    }

    override fun pop(n: Int) {
        TODO("Not yet implemented")
    }

    override fun pushValue(index: Int) {
        TODO("Not yet implemented")
    }

    override fun pushThread() {
        TODO("Not yet implemented")
    }

    override fun remove(index: Int) {
        TODO("Not yet implemented")
    }

    override fun replace(index: Int) {
        TODO("Not yet implemented")
    }

    override fun xMove(other: Lua?, n: Int) {
        TODO("Not yet implemented")
    }

    override fun load(script: String?) {
        TODO("Not yet implemented")
    }

    override fun load(buffer: ByteArray?, name: String?) {
        TODO("Not yet implemented")
    }

    override fun run(script: String?) {
        TODO("Not yet implemented")
    }

    override fun run(buffer: ByteArray?, name: String?) {
        TODO("Not yet implemented")
    }

    override fun dump(): ByteArray? {
        TODO("Not yet implemented")
    }

    override fun pCall(nArgs: Int, nResults: Int) {
        TODO("Not yet implemented")
    }

    override fun newThread(): Lua? {
        TODO("Not yet implemented")
    }

    override fun resume(nArgs: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun status(): LuaException.LuaError? {
        TODO("Not yet implemented")
    }

    override fun yield(n: Int) {
        TODO("Not yet implemented")
    }

    override fun createTable(nArr: Int, nRec: Int) {
        TODO("Not yet implemented")
    }

    override fun newTable() {
        TODO("Not yet implemented")
    }

    override fun getField(index: Int, key: String?) {
        TODO("Not yet implemented")
    }

    override fun setField(index: Int, key: String?) {
        TODO("Not yet implemented")
    }

    override fun getTable(index: Int) {
        TODO("Not yet implemented")
    }

    override fun setTable(index: Int) {
        TODO("Not yet implemented")
    }

    override fun next(n: Int): Int {
        TODO("Not yet implemented")
    }

    override fun rawGet(index: Int) {
        TODO("Not yet implemented")
    }

    override fun rawGetI(index: Int, n: Int) {
        TODO("Not yet implemented")
    }

    override fun rawSet(index: Int) {
        TODO("Not yet implemented")
    }

    override fun rawSetI(index: Int, n: Int) {
        TODO("Not yet implemented")
    }

    override fun ref(index: Int): Int {
        TODO("Not yet implemented")
    }

    override fun ref(): Int {
        TODO("Not yet implemented")
    }

    override fun refGet(ref: Int) {
        TODO("Not yet implemented")
    }

    override fun unRef(index: Int, ref: Int) {
        TODO("Not yet implemented")
    }

    override fun unref(ref: Int) {
        TODO("Not yet implemented")
    }

    override fun getGlobal(name: String?) {
        TODO("Not yet implemented")
    }

    override fun setGlobal(name: String?) {
        TODO("Not yet implemented")
    }

    override fun getMetatable(index: Int): Int {
        TODO("Not yet implemented")
    }

    override fun setMetatable(index: Int) {
        TODO("Not yet implemented")
    }

    override fun getMetaField(index: Int, field: String?): Int {
        TODO("Not yet implemented")
    }

    override fun getRegisteredMetatable(typeName: String?) {
        TODO("Not yet implemented")
    }

    override fun newRegisteredMetatable(typeName: String?): Int {
        TODO("Not yet implemented")
    }

    override fun openLibraries() {
        TODO("Not yet implemented")
    }

    override fun openLibrary(name: String?) {
        TODO("Not yet implemented")
    }

    override fun concat(n: Int) {
        TODO("Not yet implemented")
    }

    override fun gc() {
        TODO("Not yet implemented")
    }

    override fun error(message: String?) {
        TODO("Not yet implemented")
    }

    override fun createProxy(
        interfaces: Array<KClass<*>?>?,
        degree: Lua.Conversion?
    ): Any? {
        TODO("Not yet implemented")
    }

    override val mainState: Lua?
        get() = TODO("Not yet implemented")
    override val pointer: Long
        get() = TODO("Not yet implemented")
    override val id: Int
        get() = TODO("Not yet implemented")
    override val javaError: Throwable?
        get() = TODO("Not yet implemented")

    override fun error(e: Throwable?): Int {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun get(): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun set(key: String?, value: Any?) {
        TODO("Not yet implemented")
    }

    override fun get(globalName: String?): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun register(
        name: String?,
        function: LuaFunction?
    ) {
        TODO("Not yet implemented")
    }

    override fun eval(command: String?): Array<LuaValue?>? {
        TODO("Not yet implemented")
    }

    override fun require(module: String?): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun fromNull(): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun from(b: Boolean): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun from(n: Double): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun from(n: Long): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun from(s: String?): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun from(buffer: ByteArray?): LuaValue? {
        TODO("Not yet implemented")
    }

}