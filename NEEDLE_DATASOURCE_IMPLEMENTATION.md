# NeedleDataSource Implementation

## Overview
This document describes the implementation of the NeedleDataSource pattern to automatically populate sample needles on first app launch.

## Problem
The needles list screen was showing "No needles yet" because the DataStore was empty by default. Users needed to manually create needles to see the functionality.

## Solution
Implemented a data source abstraction layer with automatic initialization of sample needles.

## Architecture

### 1. NeedleDataSource Interface
```kotlin
interface NeedleDataSource {
    suspend fun getNeedles(): List<Needle>
}
```
- Abstraction for providing needles from different sources
- Allows easy testing and swapping of implementations

### 2. DebugNeedleDataSource
```kotlin
class DebugNeedleDataSource : NeedleDataSource {
    override suspend fun getNeedles(): List<Needle> {
        return SampleNeedles.getAll()
    }
}
```
- Returns sample needles (weather, greeting, calculator)
- Used to populate DataStore on first launch

### 3. NeedleRepository Initialization
The repository now:
1. Uses Flow's `onStart` operator to check if DataStore is empty
2. If empty, initializes with sample needles from DebugNeedleDataSource
3. Uses Mutex for thread-safe initialization
4. Only initializes once per repository instance

## Sample Needles Included

### Weather Needle
- **ID**: `weather-api`
- **Name**: Get Weather
- **Function**: Fetches weather data from wttr.in API
- **Parameters**: city (String)
- **Returns**: JSON weather data

### Greeting Needle
- **ID**: `greeting`
- **Name**: Generate Greeting
- **Function**: Creates personalized greeting messages
- **Parameters**: name (String)
- **Returns**: Greeting message

### Calculator Needle
- **ID**: `calculator`
- **Name**: Calculate Sum
- **Function**: Adds two numbers
- **Parameters**: a (Int), b (Int)
- **Returns**: Sum (Int)

## Testing

### Unit Tests
✅ All tests passing (8 tests):
- `DebugNeedleDataSourceTest` - 4 tests
  - Verifies sample needles are returned
  - Checks each needle type is present
- `SampleNeedlesTest` - 9 tests  
  - Validates needle structure
  - Verifies Lua code syntax

### Manual Testing Steps
To verify the implementation:

1. **Fresh Install**
   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

2. **First Launch**
   - Open the app
   - Navigate to Needles screen
   - **Expected**: See 3 sample needles listed
   - **Previous**: "No needles yet" message

3. **Verify Needles**
   - Click on "Get Weather" needle
   - Should see: city parameter, Lua code for wttr.in API call
   - Click on "Generate Greeting" needle
   - Should see: name parameter, simple greeting logic
   - Click on "Calculate Sum" needle
   - Should see: a and b parameters, addition logic

4. **Persistence Test**
   - Toggle visibility of a needle
   - Close and reopen app
   - **Expected**: Visibility state is preserved

5. **User Needles Test**
   - Add a new needle manually
   - Restart app
   - **Expected**: User needle persists along with sample needles

## Implementation Details

### Thread Safety
- Uses `Mutex` from kotlinx.coroutines for thread-safe initialization
- Prevents race conditions when multiple collectors subscribe to needlesFlow
- Double-check pattern ensures initialization happens only once

### Performance
- Initialization happens asynchronously using Flow's `onStart`
- Does not block UI thread
- Lazy evaluation - only runs when needlesFlow is collected

### Logging
- Logs initialization events for debugging
- Tag: "NeedleRepository"
- Logs: "DataStore is empty, initializing with sample needles"
- Logs: "Initialized N sample needles"

## Files Changed

### Created
1. `core/src/commonMain/kotlin/io/github/lemcoder/core/data/source/NeedleDataSource.kt`
2. `core/src/commonMain/kotlin/io/github/lemcoder/core/data/source/DebugNeedleDataSource.kt`
3. `core/src/commonTest/kotlin/io/github/lemcoder/core/data/source/DebugNeedleDataSourceTest.kt`

### Modified
1. `core/src/commonMain/kotlin/io/github/lemcoder/core/data/repository/NeedleRepository.kt`
   - Added `debugDataSource` parameter
   - Added `initializeIfNeeded()` method
   - Modified `needlesFlow` to call initialization

## Future Enhancements

### Potential Improvements
1. **Configuration**: Add ability to disable sample needles via BuildConfig
2. **More Samples**: Add additional sample needles showcasing different features
3. **Import/Export**: Allow users to export needles and import sample packs
4. **Templates**: Convert sample needles into templates that users can customize

### Alternative Approaches
1. **Migration**: Could use a migration system instead of initialization
2. **First Run Flag**: Could use SharedPreferences to track first run
3. **Remote Config**: Could fetch sample needles from a remote server

## Conclusion
The implementation successfully addresses the issue by automatically populating sample needles on first launch, providing users with immediate examples of the needle functionality.
