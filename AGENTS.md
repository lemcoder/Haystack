# Coding Agent Instructions for Haystack

## Project Overview

**Haystack** is an Android application that allows users to create custom Python-based tools (called *Needles*) and interact with them through a conversational AI interface powered by a local LLM. The project uses **Kotlin Multiplatform** to share code between Android and potentially iOS in the future.

### Technology Stack

- **Language**: Kotlin 2.3.0
- **Build System**: Gradle with Kotlin DSL
- **Architecture**: MVI + Clean Architecture
- **UI Framework**: Jetpack Compose with Material 3
- **Python Integration**: Chaquopy (Android only)
- **AI/ML Framework**: Koog 0.6.0
- **Scripting**: Lua via native interop
- **Min SDK**: 31 (Android 12)
- **Target SDK**: 36
- **JVM Toolchain**: 17

## Project Structure

The project consists of three Gradle modules:

### 1. `:app` - Android Application
- **Type**: Standard Android application (not multiplatform)
- **Location**: `app/`
- **Purpose**: Main Android UI using Jetpack Compose
- **Dependencies**: Depends on `:core` module
- **Key Features**: 
  - Compose UI with Material 3
  - MVI architecture
  - ViewModel integration

### 2. `:core` - Kotlin Multiplatform Library
- **Type**: Kotlin Multiplatform Module
- **Location**: `core/`
- **Targets**: 
  - `androidLibrary` - Android implementation
  - `iosArm64` - iOS device (commented out)
  - `iosSimulatorArm64` - iOS simulator (commented out)
- **Purpose**: Shared business logic, AI agents, and data management
- **Key Dependencies**:
  - Koog agents and clients (AI framework)
  - kotlinx.coroutines
  - kotlinx.serialization
  - androidx.datastore
- **Source Sets**:
  - `commonMain/kotlin/` - Shared Kotlin code
  - `commonTest/kotlin/` - Shared test code
  - `androidMain/kotlin/` - Android-specific implementations
  - `androidDeviceTest/` - Android instrumentation tests
  - `iosMain/kotlin/` - iOS-specific implementations

### 3. `:scriptEngine` - Kotlin Multiplatform Library with Native Interop
- **Type**: Kotlin Multiplatform Module with C interop
- **Location**: `scriptEngine/`
- **Targets**: 
  - `androidLibrary` - Uses LuaJ (Java implementation)
  - `iosArm64` / `iosSimulatorArm64` - Uses native Lua via C interop
- **Purpose**: Lua script execution engine
- **Native Components**:
  - C headers in `scriptEngine/native/include/`
  - C source in `scriptEngine/native/src/`
  - Compiled libraries in `scriptEngine/native/lib/`
- **Android Implementation**: Uses LuaJ and LuaJava libraries
- **iOS Implementation**: Uses native Lua library via Kotlin/Native cinterop

## Kotlin Best Practices

### Code Style

1. **Naming Conventions**:
   - Classes: `PascalCase` (e.g., `NeedleRepository`, `ScriptExecutor`)
   - Functions/Properties: `camelCase` (e.g., `executeScript`, `isEnabled`)
   - Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_TIMEOUT`, `DEFAULT_VALUE`)
   - Package names: lowercase with dots (e.g., `io.github.lemcoder.core.needle`)

2. **File Organization**:
   - One public class per file
   - File name matches the class name
   - Organize by feature, not by layer (e.g., `needle/`, not `models/`)

3. **Imports**:
   - Remove unused imports
   - Use explicit imports, avoid wildcard imports (`*`)
   - Order: stdlib → Android → third-party → project

4. **Null Safety**:
   - Prefer non-null types when possible
   - Use `?.` for safe calls, `?:` for null coalescing
   - Use `!!` only when absolutely certain (rare)
   - Prefer `let`, `also`, `run`, `apply` for null-safe operations

5. **Immutability**:
   - Prefer `val` over `var` whenever possible
   - Use immutable collections (`List`, `Map`, `Set`) by default
   - Use mutable collections only when necessary

6. **Functions**:
   - Keep functions small and focused (single responsibility)
   - Use expression bodies for simple functions: `fun double(x: Int) = x * 2`
   - Use named parameters for clarity when calling functions with multiple parameters
   - Use default parameters instead of overloading

7. **Flows**:
    - prefer `StateFlow` for state management
    - use `SharedFlow` for events
    - collect flows in lifecycle-aware scopes (e.g., `viewModelScope`, `lifecycleScope`)
    - use .update { } for modifying StateFlow values

### Kotlin Multiplatform Specifics

1. **Source Set Structure**:
   ```
   src/
   ├── commonMain/kotlin/       # Shared code
   ├── commonTest/kotlin/       # Shared tests
   ├── androidMain/kotlin/      # Android implementations
   ├── androidDeviceTest/       # Android instrumentation tests
   ├── iosMain/kotlin/          # iOS implementations
   └── nativeMain/kotlin/       # Native (iOS/macOS) implementations
   ```

2. **Expect/Actual Pattern**:
   - Use `expect` declarations in `commonMain` for platform-specific APIs
   - Provide `actual` implementations in platform source sets
   - Example:
     ```kotlin
     // commonMain
     expect fun getPlatformName(): String
     
     // androidMain
     actual fun getPlatformName(): String = "Android"
     
     // iosMain
     actual fun getPlatformName(): String = "iOS"
     ```

3. **Platform-Specific Code**:
   - Minimize platform-specific code in `commonMain`
   - Use interfaces to abstract platform differences
   - Keep most business logic in `commonMain`

4. **Dependencies**:
   - Use multiplatform dependencies when available
   - Declare dependencies in appropriate source sets:
     ```kotlin
     commonMain.dependencies {
         implementation(libs.kotlinx.coroutines.core)
     }
     androidMain.dependencies {
         implementation(libs.androidx.core.ktx)
     }
     ```

5. **Testing**:
   - Write tests in `commonTest` for shared code
   - Use `kotlin-test` library (provides common assertions)
   - Platform-specific tests go in `androidDeviceTest` or iOS test source sets

### Coroutines Best Practices

1. **Structured Concurrency**:
   - Always use structured concurrency with proper scopes
   - Use `viewModelScope`, `lifecycleScope`, or create custom `CoroutineScope`
   - Never use `GlobalScope` (except for rare cases)

2. **Dispatchers**:
   - `Dispatchers.Main` - UI updates (Android)
   - `Dispatchers.IO` - I/O operations (file, network)
   - `Dispatchers.Default` - CPU-intensive work
   - Don't hardcode dispatchers in shared code (use dependency injection)

3. **Error Handling**:
   - Use `try-catch` blocks within coroutines
   - Use `CoroutineExceptionHandler` for scope-level error handling
   - Consider using `Result` or sealed classes for error propagation

4. **Testing**:
   - Use `runTest` from `kotlinx-coroutines-test` for testing suspending functions
   - Example from codebase:
     ```kotlin
     @Test
     fun shouldPerformBasicAddition() = runTest {
         val executor = createTestScriptExecutor()
         val result: Double? = executor.run("return a + b", mapOf("a" to 34, "b" to 35))
         assertTrue { result == 69.0 }
     }
     ```

### Serialization

1. **kotlinx.serialization**:
   - Annotate data classes with `@Serializable`
   - Use `Json.encodeToString()` and `Json.decodeFromString()`
   - Configure Json instance with appropriate settings:
     ```kotlin
     val json = Json {
         prettyPrint = true
         isLenient = true
         ignoreUnknownKeys = true
     }
     ```

2. **DataStore**:
   - Use DataStore for preferences and small data
   - Define preference keys properly
   - Use coroutines for reading/writing

## Building and Running

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (if signing configured)
./gradlew assembleRelease

# Clean build
./gradlew clean

# Build specific module
./gradlew :core:build
./gradlew :scriptEngine:build
```

### Running the App

```bash
# Install and run on connected device/emulator
./gradlew installDebug

# Run from Android Studio: Click Run button or Shift+F10
```

## Testing

### Unit Tests (JVM)

Unit tests run on the JVM and are much faster than instrumentation tests. They are located in:
- `core/src/commonTest/kotlin/` - Multiplatform tests
- `app/src/test/kotlin/` - Android-specific JVM tests

**Run all unit tests:**
```bash
./gradlew test
```

**Run tests for specific module:**
```bash
./gradlew :core:test
./gradlew :scriptEngine:test
./gradlew :app:testDebugUnitTest
```

**Run tests with coverage:**
```bash
./gradlew test jacocoTestReport
```

### Instrumentation Tests (Android)

Instrumentation tests run on Android devices/emulators. They are located in:
- `core/src/androidDeviceTest/` - Core module device tests
- `app/src/androidTest/` - App module instrumentation tests

**Prerequisites:**
- An Android emulator must be running before executing instrumentation tests
- Recommended: API 34 emulator named "test"

**Start emulator:**
```bash
# List available emulators
emulator -list-avds

# Start specific emulator
emulator -avd test &
```

**Run instrumentation tests:**
```bash
# Run all instrumentation tests on connected device
./gradlew connectedDebugAndroidTest

# Run tests for specific module
./gradlew :core:connectedDebugAndroidTest
./gradlew :app:connectedDebugAndroidTest
```

**Run specific test class:**
```bash
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.MyTestClass
```

### Writing Tests

#### 1. Common (Multiplatform) Tests

Located in `src/commonTest/kotlin/`, these tests run on all platforms:

```kotlin
class MyFeatureTest {
    
    @Test
    fun shouldDoSomething() = runTest {
        // Given
        val input = "test"
        
        // When
        val result = processInput(input)
        
        // Then
        assertEquals("expected", result)
        assertTrue { result.isNotEmpty() }
    }
}
```

#### 2. Key Testing Patterns from Codebase

**Use test utilities:**
```kotlin
// Create reusable test setup functions
fun createTestScriptExecutor(
    engine: ScriptEngine = instantiateScriptEngine(),
    networkModule: NetworkModule = TestLuaNetworkModule(engine),
    loggingModule: LoggingModule = TestLuaLoggingModule(engine),
    fileSystemModule: FileSystemModule = TestLuaFileSystemModule(engine)
): ScriptExecutor {
    // Setup and return test instance
}
```

**Test with mocks/fakes:**
```kotlin
@Test
fun shouldCallHttpPost() = runTest {
    val engine = instantiateScriptEngine()
    val networkModule = TestLuaNetworkModule(engine, this)
    networkModule.status = 201  // Mock response
    networkModule.responseBody = """{"id": 123}"""
    
    val executor = createTestScriptExecutor(engine = engine, networkModule = networkModule)
    val result = executor.run(script, emptyMap())
    
    assertTrue(result != null && result.contains("\"id\": 123"))
}
```

**Test callbacks:**
```kotlin
@Test
fun shouldCallLoggingFunctions() = runTest {
    val loggingModule = TestLuaLoggingModule(engine)
    var debugCalled = false
    
    loggingModule.onDebugCalled = { tag, message ->
        assertEquals("TestTag", tag)
        assertEquals("Debug message", message)
        debugCalled = true
    }
    
    executor.run<Unit>("log:d('TestTag', 'Debug message')", emptyMap())
    
    assertTrue(debugCalled, "Debug logging function was not called")
}
```

### Test Coverage Goals

- Aim for >80% coverage in `commonMain` (shared business logic)
- Test all public APIs
- Test edge cases and error handling
- Test platform-specific implementations separately

## Compose UI Best Practices

1. **Composable Functions**:
   - Name with `PascalCase` (like classes)
   - Keep composables small and reusable
   - Extract complex UI into separate composables

2. **State Management**:
   - Use `remember` for UI state
   - Use `rememberSaveable` for state that survives configuration changes
   - Hoist state when necessary
   - Use `StateFlow` or `State` for ViewModel state

3. **Previews**:
   - Always add `@Preview` annotations for composables
   - Create preview variants for different states
   - Example:
     ```kotlin
     @Preview(showBackground = true)
     @Composable
     fun MyScreenPreview() {
         MyScreen(state = MyScreenState.Loading)
     }
     ```

4. **Performance**:
   - Use `derivedStateOf` for computed state
   - Use `key()` in lists for proper recomposition
   - Avoid reading state in composition when possible
   - Use `LaunchedEffect` for side effects

## Dependency Management

The project uses Gradle version catalogs (`gradle/libs.versions.toml`):

### Adding a New Dependency

1. Add version to `[versions]` section:
   ```toml
   [versions]
   my-library = "1.2.3"
   ```

2. Add library to `[libraries]` section:
   ```toml
   [libraries]
   my-library = { group = "com.example", name = "my-library", version.ref = "my-library" }
   ```

3. Use in build.gradle.kts:
   ```kotlin
   commonMain.dependencies {
       implementation(libs.my.library)
   }
   ```

### Type-Safe Project Accessors

The project uses `TYPESAFE_PROJECT_ACCESSORS` feature:
```kotlin
// Instead of implementation(project(":core"))
implementation(projects.core)
```

## Common Tasks

### Adding a New Feature

1. Identify if feature is shared or platform-specific
2. Add code to appropriate source set (`commonMain` vs platform-specific)
3. Write tests in `commonTest` or platform test directories
4. Update UI in `app` module if needed
5. Run tests: `./gradlew test`
6. Build app: `./gradlew assembleDebug`

### Adding a New Module

1. Create module directory
2. Add to `settings.gradle.kts`: `include(":moduleName")`
3. Create `build.gradle.kts` in module directory
4. Set up appropriate source sets
5. Sync Gradle

### Debugging

1. **Android Studio Debugger**:
   - Set breakpoints in Kotlin code
   - Use "Debug" run configuration
   - Inspect variables, evaluate expressions

2. **Logging**:
   - Use `/Users/mikolaj/src/Haystack/core/src/commonMain/kotlin/io/github/lemcoder/core/utils/Log.kt` for commonMain in :core module
   - Use `Log.d()`, `Log.e()` for Android-specific code
   - Check Logcat in Android Studio

3. **Gradle Issues**:
   ```bash
   # Clear Gradle cache
   ./gradlew clean
   ./gradlew --stop
   rm -rf ~/.gradle/caches/
   
   # Sync project
   ./gradlew build --refresh-dependencies
   ```

## Performance Considerations

1. **Avoid blocking main thread**:
   - Use coroutines for long operations
   - Use `Dispatchers.IO` for I/O operations

2. **Memory Management**:
   - Close resources properly (use `use {}` for streams)
   - Avoid memory leaks in Activities/Fragments
   - Use `WeakReference` when holding references to contexts

3. **Build Performance**:
   - Use Gradle configuration cache: `./gradlew --configuration-cache`
   - Enable parallel builds in `gradle.properties`
   - Use build cache

## Quick Reference

| Task | Command |
|------|---------|
| Build debug APK | `./gradlew assembleDebug` |
| Run unit tests | `./gradlew test` |
| Run instrumentation tests | `./gradlew connectedDebugAndroidTest` |
| Run tests for core module | `./gradlew :core:test` |
| Clean build | `./gradlew clean` |
| Lint check | `./gradlew lint` |
| Install on device | `./gradlew installDebug` |
| Build all modules | `./gradlew build` |
| List tasks | `./gradlew tasks` |
| Check dependencies | `./gradlew dependencies` |
