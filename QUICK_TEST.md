# Quick Test Guide

## Build & Run

```bash
cd /Users/mikolaj/src/Haystack
./gradlew clean build
./gradlew installDebug
```

## Test Scenarios

### 1. Basic Math

**Input:** "What is 25 + 17?"

**Expected:** Agent calls `Calculate Sum` Needle and returns "42"

### 2. Temperature Conversion

**Input:** "Convert 100°F to Celsius"

**Expected:** Agent calls `Temperature Converter` and returns "37.8°C"

### 3. Text Analysis

**Input:** "Analyze this text: The quick brown fox jumps over the lazy dog"

**Expected:** Agent calls `Text Analyzer` and returns word count, character count, etc.

### 4. List Sorting

**Input:** "Sort these numbers: 42, 17, 8, 23, 91, 5"

**Expected:** Agent calls `List Sorter` and returns sorted list

### 5. Weather (Mock Data)

**Input:** "What's the weather in Tokyo?"

**Expected:** Agent calls `Weather Fetcher` and returns mock weather data

### 6. Data Visualization

**Input:** "Create a bar chart with these values: 15, 30, 22, 45, 18"

**Expected:** Agent calls `Data Visualizer` and creates a chart image

### 7. Chart Generation

**Input:** "Generate a line chart"

**Expected:** Agent calls `Chart Generator` and creates a sample chart

### 8. Multi-Step (Advanced)

**Input:** "What's 50 + 30, then convert that result from Fahrenheit to Celsius?"

**Expected:**

1. Agent calls `Calculate Sum` → 80
2. Agent calls `Temperature Converter` with 80°F → 26.7°C
3. Returns final result

## Troubleshooting

### Agent Not Initializing

- Check if model is downloaded (Settings → Download Model)
- Verify koog-edge dependency is correct version
- Check logs: `adb logcat | grep ChatAgentService`

### Tools Not Being Called

- Verify Needles are created (check Needles screen)
- Check agent system prompt includes tool descriptions
- Look for tool call events in logs: `adb logcat | grep "Tool called"`

### Python Execution Errors

- Ensure matplotlib is installed (Chaquopy pip)
- Check Python code syntax in Needle definitions
- Verify file paths are accessible (use getFilesDir())

### UI Not Updating

- Check if StateFlow is being collected
- Verify agent state changes are emitted
- Look for exceptions in ViewModel

## Debug Commands

```bash
# View all logs
adb logcat | grep Haystack

# View agent-specific logs
adb logcat | grep ChatAgentService

# View tool execution logs
adb logcat | grep NeedleToolAdapter

# View Python execution
adb logcat | grep PythonExecutor

# Clear app data
adb shell pm clear io.github.lemcoder.haystack
```

## Expected Log Output

When sending a message, you should see:

```
ChatViewModel: Sending message: "What is 5 + 3?"
ChatAgentService: Processing user input
ChatAgentService: Tool called: calculate_sum
NeedleToolAdapter: Executing needle: Calculate Sum
PythonExecutor: Running Python code
PythonExecutor: Result: The sum is: 8
ChatAgentService: Agent completed
ChatViewModel: Response received
```

## Navigation Test

1. Launch app → Should show **Chat screen**
2. Tap "Needles" icon → Shows list of 7 sample Needles
3. Tap a Needle → Shows detail view with Python code
4. Back to Chat → Send a message
5. Tap "Settings" → Shows LLM configuration
6. Tap "Clear" → Clears chat history

## Performance Notes

- First message may be slow (agent initialization)
- Subsequent messages should be faster
- Tool execution time depends on Python complexity
- Chart generation ~1-2 seconds
- Math operations ~100-200ms

## Known Issues

### Current Limitations

1. No streaming responses (full response only)
2. No conversation persistence between app restarts
3. Images not displayed in chat yet (path returned)
4. No parallel tool calls (sequential only)
5. No conversation context (each message independent)

### Future Fixes

- Add streaming support
- Persist chat history to DataStore
- Display images inline in chat
- Implement parallel tool execution
- Add conversation memory/context

## Success Criteria

✅ App launches to Chat screen
✅ Chat UI displays properly
✅ Can type and send messages
✅ Agent initializes with 7 Needles
✅ Basic math tool works
✅ Temperature conversion works
✅ Error messages display correctly
✅ Can navigate to Needles screen
✅ Can navigate to Settings screen
✅ Can clear chat history

## Demo Preparation

1. **Clean Install**
   ```bash
   adb uninstall io.github.lemcoder.haystack
   ./gradlew installDebug
   ```

2. **Pre-load Model**
    - Launch app
    - Go through model download if needed
    - Return to Chat screen

3. **Test All Needles**
    - Run through test scenarios above
    - Note any failures
    - Check timing for demo

4. **Prepare Demo Script**
    - Choose 3-4 best examples
    - Practice the flow
    - Have backup examples ready

## Quick Fixes

### If Agent Won't Initialize

```kotlin
// Check ChatAgentService.initializeAgent()
// Add more logging
// Verify needles are loaded
```

### If UI Freezes

```kotlin
// Ensure all heavy operations in viewModelScope.launch
// Check for blocking calls on main thread
// Add try-catch around suspend functions
```

### If Tools Don't Execute

```kotlin
// Verify NeedleToolAdapter.doExecute() is called
// Check Python code syntax
// Test Python code in isolation
```

## Contact for Issues

Check logs first, then review:

1. ChatAgentService implementation
2. NeedleToolAdapter tool conversion
3. PythonExecutor integration
4. Agent strategy configuration
