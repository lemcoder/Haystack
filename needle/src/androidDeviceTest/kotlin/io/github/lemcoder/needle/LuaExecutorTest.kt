package io.github.lemcoder.needle

import android.util.Log
import io.github.lemcoder.needle.module.TestLuaFileSystemModule
import io.github.lemcoder.needle.module.TestLuaLoggingModule
import io.github.lemcoder.needle.module.TestLuaNetworkModule
import io.github.lemcoder.needle.util.createTestLuaExecutor
import kotlinx.coroutines.test.runTest
import party.iroiro.luajava.lua55.Lua55
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LuaExecutorTest {

    @Test
    fun shouldPerformBasicAddition() = runTest {
        val executor = createTestLuaExecutor()
        val result: Double? = executor.run("return a + b", mapOf("a" to 34, "b" to 35))
        assertTrue { result == 69.0 }
    }

    @Test
    fun testLuaTables() = runTest {
        val executor = createTestLuaExecutor()
        val table: Map<String, *> =
            executor.run("return { text = 'abc', children = { 'a', 'b', 'c' } }")!!
        assertEquals("abc", table["text"].toString())
        val children: MutableMap<Double, String> = table["children"] as MutableMap<Double, String>
        // Indices are 1-based.
        Log.e("LuaExecutorTest", "Children: $children")
        assertEquals("a", children[1.0].toString())
        assertEquals(3, children.size)
    }

    @Test
    fun shouldCallHttpGet() = runTest {
        val lua = Lua55()
        val networkModule = TestLuaNetworkModule(lua, this)
        networkModule.responseBody = "TEST"

        val executor = createTestLuaExecutor(lua = lua, networkModule = networkModule)

        val result: String? =
            executor.run(
                """
                local response = network:get("https://httpbin.org/get")
                for k, v in pairs(response) do
                    print(k, type(v))
                end
                if response.status == 200 then
                    return response.body
                else
                    return "error: " .. response.status
                end
                """
                    .trimIndent(),
                emptyMap(),
            )

        assertTrue(result != null && result.contains("TEST"))
    }

    @Test
    fun shouldCallHttpPost() = runTest {
        val lua = Lua55()
        val networkModule = TestLuaNetworkModule(lua, this)
        networkModule.status = 201
        networkModule.responseBody = """{"id": 123, "created": true}"""

        val executor = createTestLuaExecutor(lua = lua, networkModule = networkModule)

        val result: String? =
            executor.run(
                """
                local requestBody = '{"name": "John Doe", "email": "john@example.com"}'
                local response = network:post("https://httpbin.org/post", requestBody)

                if response.status == 201 then
                    return response.body
                else
                    return "error: " .. response.status
                end
                """
                    .trimIndent(),
                emptyMap(),
            )

        assertTrue(
            result != null && result.contains("\"id\": 123") && result.contains("\"created\": true")
        )
    }

    @Test
    fun shouldCallLoggingFunctions() = runTest {
        val lua = Lua55()
        val loggingModule = TestLuaLoggingModule(lua)

        var debugCalled = false
        var infoCalled = false
        var warnCalled = false
        var errorCalled = false

        loggingModule.onDebugCalled = { tag, message ->
            assertEquals("TestTag", tag)
            assertEquals("Debug message", message)
            debugCalled = true
        }

        loggingModule.onInfoCalled = { tag, message ->
            assertEquals("TestTag", tag)
            assertEquals("Info message", message)
            infoCalled = true
        }

        loggingModule.onWarnCalled = { tag, message ->
            assertEquals("TestTag", tag)
            assertEquals("Warning message", message)
            warnCalled = true
        }

        loggingModule.onErrorCalled = { tag, message ->
            assertEquals("TestTag", tag)
            assertEquals("Error message", message)
            errorCalled = true
        }

        val executor = createTestLuaExecutor(lua = lua, loggingModule = loggingModule)

        executor.run<Unit>(
            """
            log:d("TestTag", "Debug message")
            log:i("TestTag", "Info message")
            log:w("TestTag", "Warning message")
            log:e("TestTag", "Error message")
            """
                .trimIndent(),
            emptyMap(),
        )

        assertTrue(debugCalled, "Debug logging function was not called")
        assertTrue(infoCalled, "Info logging function was not called")
        assertTrue(warnCalled, "Warning logging function was not called")
        assertTrue(errorCalled, "Error logging function was not called")
    }

    @Test
    fun shouldWriteAndReadFile() = runTest {
        val lua = Lua55()
        val fileSystemModule = TestLuaFileSystemModule(lua)

        val executor = createTestLuaExecutor(lua = lua, fileSystemModule = fileSystemModule)

        val result: Boolean? =
            executor.run(
                """
                local success = fs:write("test.txt", "Hello, FileSystem!")
                if success then
                    local content = fs:read("test.txt")
                    return content == "Hello, FileSystem!"
                else
                    return false
                end
                """
                    .trimIndent(),
                emptyMap(),
            )

        assertTrue(result == true, "File write and read operation failed")
    }

    @Test
    fun shouldCheckFileExists() = runTest {
        val lua = Lua55()
        val fileSystemModule = TestLuaFileSystemModule(lua)
        fileSystemModule.setupFile("existing.txt", "Content")

        val executor = createTestLuaExecutor(lua = lua, fileSystemModule = fileSystemModule)

        val result: Boolean? =
            executor.run(
                """
                local exists = fs:exists("existing.txt")
                local notExists = fs:exists("missing.txt")
                return exists and not notExists
                """
                    .trimIndent(),
                emptyMap(),
            )

        assertTrue(result == true, "File exists check failed")
    }

    @Test
    fun shouldDeleteFile() = runTest {
        val lua = Lua55()
        val fileSystemModule = TestLuaFileSystemModule(lua)
        fileSystemModule.setupFile("todelete.txt", "Delete me")

        val executor = createTestLuaExecutor(lua = lua, fileSystemModule = fileSystemModule)

        val result: Boolean? =
            executor.run(
                """
                local existsBefore = fs:exists("todelete.txt")
                local deleted = fs:delete("todelete.txt")
                local existsAfter = fs:exists("todelete.txt")
                return existsBefore and deleted and not existsAfter
                """
                    .trimIndent(),
                emptyMap(),
            )

        assertTrue(result == true, "File delete operation failed")
    }

    @Test
    fun shouldListFiles() = runTest {
        val lua = Lua55()
        val fileSystemModule = TestLuaFileSystemModule(lua)
        fileSystemModule.setupFile("file1.txt", "Content 1")
        fileSystemModule.setupFile("file2.txt", "Content 2")
        fileSystemModule.setupFile("file3.txt", "Content 3")

        val executor = createTestLuaExecutor(lua = lua, fileSystemModule = fileSystemModule)

        val result: Double? =
            executor.run(
                """
                local files = fs:list("")
                local count = 0
                for _ in pairs(files) do
                    count = count + 1
                end
                return count
                """
                    .trimIndent(),
                emptyMap(),
            )

        assertEquals(3.0, result, "File list operation failed")
    }

    @Test
    fun shouldHandleReadNonExistentFile() = runTest {
        val lua = Lua55()
        val fileSystemModule = TestLuaFileSystemModule(lua)

        val executor = createTestLuaExecutor(lua = lua, fileSystemModule = fileSystemModule)

        val result: Boolean? =
            executor.run(
                """
                local content = fs:read("nonexistent.txt")
                return content == nil
                """
                    .trimIndent(),
                emptyMap(),
            )

        assertTrue(result == true, "Reading non-existent file should return nil")
    }

    @Test
    fun shouldCallFileSystemCallbacks() = runTest {
        val lua = Lua55()
        val fileSystemModule = TestLuaFileSystemModule(lua)

        var readCalled = false
        var writeCalled = false
        var deleteCalled = false
        var existsCalled = false
        var listCalled = false

        fileSystemModule.onReadCalled = { path ->
            assertEquals("test.txt", path)
            readCalled = true
        }

        fileSystemModule.onWriteCalled = { path, content ->
            assertEquals("test.txt", path)
            assertEquals("Test content", content)
            writeCalled = true
        }

        fileSystemModule.onDeleteCalled = { path ->
            assertEquals("test.txt", path)
            deleteCalled = true
        }

        fileSystemModule.onExistsCalled = { path ->
            assertEquals("test.txt", path)
            existsCalled = true
        }

        fileSystemModule.onListCalled = { path ->
            assertEquals("", path)
            listCalled = true
        }

        val executor = createTestLuaExecutor(lua = lua, fileSystemModule = fileSystemModule)

        executor.run<Unit>(
            """
            fs:write("test.txt", "Test content")
            fs:exists("test.txt")
            fs:read("test.txt")
            fs:list("")
            fs:delete("test.txt")
            """
                .trimIndent(),
            emptyMap(),
        )

        assertTrue(readCalled, "Read callback was not called")
        assertTrue(writeCalled, "Write callback was not called")
        assertTrue(deleteCalled, "Delete callback was not called")
        assertTrue(existsCalled, "Exists callback was not called")
        assertTrue(listCalled, "List callback was not called")
    }
}


// Interview

/**
 * Imagine you write a microservice for an online retailer.
 * Your responsibility is to write a component (method in Java)
 * that calculates the total price of the user's basket.
 *
 *
 * The user's basket contains positions — product and quantity.
 *
 * The product has a name (String) and a unit price (number).
 *
 * For simplicity, let's assume there are four products hardcoded somewhere in the code:
 *
 * Product	Price
 * bread	1.99
 * butter	3.20
 * milk	2.40
 * chocolate	5.15
 *
 *
 * The total price is the sum of unit price × quantity of basket positions.
 * All validations are out of scope — we assume input is correct.
 *
 * Extension 1
 * If the user buys products for a total price above 50, apply a 10% discount.
 *
 * Extension 2
 * Add a promotion "3 for the price of 2".
 * The user that buys 3 items of the same product pays for only 2.
 * When the user buys 6, they pay for 4, and so on.
 * This promotion applies to all products and works together with Extension 1.
 *
 * Extension 3
 * Add an ability to enter a coupon code (String).
 * When provided (non-empty), subtract 5 from the total price.
 * This promotion should be applied BEFORE Extension 2 but AFTER Extension 1.
 */

data class Basket(
    val products: List<Product>
)

sealed class Product(
    open val price: BigDecimal
) {
    data class Butter(
        override val price: BigDecimal
    ) : Product(price)

    data class Bread(
        override val price: BigDecimal
    ) : Product(price)

    data class Milk(
        override val price: BigDecimal,
    ) : Product(price)

    data class Chocolate(
        override val price: BigDecimal
    ) : Product(price)
}

fun totalPrice(basket: Basket): BigDecimal {
    return basket.products.sumOf { it.price }
}

fun calculateDiscountedSum(basket: Basket): BigDecimal {
    val sum = totalPrice(basket)
    return if (sum > BigDecimal("50.0")) {
        sum * BigDecimal("0.1")
    } else {
        sum
    }
}

fun applyThreeForPriceOfTwo(basket: Basket): Basket {
    val productsAfterPromo = basket.products.groupBy { product ->
        product::class
    }.flatMap { (_, list) ->
        list.drop(list.size / 3)
    }

    return Basket(productsAfterPromo)
}

fun applyCupon(cupon: String, totalSum: BigDecimal): BigDecimal =
    if (cupon.isEmpty()) totalSum else totalSum - BigDecimal("5.0")

fun main() {
    val basket = Basket(
        products = listOf(
            Product.Butter(BigDecimal("15.0")),
            Product.Milk(BigDecimal("3.33")),
            Product.Milk(BigDecimal("3.33")),
            Product.Milk(BigDecimal("3.33")),
            Product.Milk(BigDecimal("3.33")),
        )
    )

    print(applyCupon("BACD", calculateDiscountedSum(applyThreeForPriceOfTwo(basket))))
}


































