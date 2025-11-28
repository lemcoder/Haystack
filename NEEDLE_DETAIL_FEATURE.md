# Needle Detail & Execution Feature

## Overview

The Needle Detail screen provides a full-screen code viewer with syntax highlighting and the ability
to execute needles with interactive argument input and type-aware result display.

## Features

### ✅ Implemented

1. **Full-Screen Code Viewer**
    - Syntax highlighting for Python code
    - Scrollable horizontally and vertically
    - Color-coded keywords, strings, comments, numbers, and built-ins
    - Monospace font for code readability

2. **Needle Metadata Display**
    - Description
    - Arguments with types and descriptions
    - Tags for categorization
    - Required/optional indicators

3. **Interactive Execution**
    - Play button in top app bar
    - Arguments dialog for input collection
    - Type validation before execution
    - Loading indicator during execution

4. **Type-Aware Result Display**
    - **Text Results**: Dialog with scrollable text output
    - **Image Results**: Dialog with image preview (auto-detected from output)
    - **Error Results**: Dialog with error message in red

5. **Navigation**
    - Back button to return to needles list
    - Deep linking support via `NeedleDetail(needleId)` destination

## Architecture

### Files Created

```
presentation/screen/needleDetail/
├── NeedleDetailScreen.kt           # Main UI composables
├── NeedleDetailViewModel.kt        # Business logic & state
├── NeedleDetailState.kt           # State model
├── NeedleDetailEvent.kt           # User events
├── NeedleDetailRoute.kt           # Route wrapper
└── component/
    └── PythonCodeView.kt          # Syntax highlighting component
```

### State Model

```kotlin
data class NeedleDetailState(
    val needle: Needle?,                  // The needle being viewed
    val isLoading: Boolean,               // Loading needle from repo
    val isExecuting: Boolean,             // Executing Python code
    val executionResult: ExecutionResult?, // Result to display
    val showArgumentsDialog: Boolean,     // Show input dialog
    val argumentValues: Map<String, String>, // Current arg values
    val errorMessage: String?             // General errors
)

sealed class ExecutionResult {
    data class TextResult(val output: String)
    data class ImageResult(val imagePath: String)
    data class ErrorResult(val error: String)
}
```

### Events

```kotlin
sealed interface NeedleDetailEvent {
    object NavigateBack              // Return to needles list
    object ExecuteNeedle            // Start execution flow
    object DismissResult            // Close result dialog
    object ShowArgumentsDialog      // Open input dialog
    object DismissArgumentsDialog   // Close input dialog
    data class UpdateArgument(      // Update arg value
        val argName: String, 
        val value: String
    )
    object ConfirmAndExecute        // Execute with args
}
```

## Execution Flow

### 1. User Clicks Play Button

```
User clicks Play → ExecuteNeedle event
                 ↓
         Has arguments?
        /              \
      Yes               No
       ↓                ↓
  Show Dialog     Execute directly
       ↓
  User enters values
       ↓
  ConfirmAndExecute
       ↓
  Type validation & conversion
       ↓
  Execute via ExecuteNeedleUseCase
       ↓
  Display result based on type
```

### 2. Argument Collection

When a needle has arguments, a dialog appears with:

- One `OutlinedTextField` per argument
- Label with argument name (with `*` for required)
- Supporting text showing type and description
- Pre-filled with default values if available
- Scrollable for many arguments

### 3. Type Conversion

String inputs are converted to proper types:

```kotlin
when (type) {
    NeedleType.String -> value
    NeedleType.Int -> value.toIntOrNull() ?: throw error
    NeedleType.Float -> value.toFloatOrNull() ?: throw error
    NeedleType.Boolean -> value.lowercase() in ["true", "1", "yes"]
    NeedleType.ByteArray -> value.toByteArray()
    NeedleType.Any -> value
}
```

### 4. Result Detection

The system automatically determines result type:

**Image Detection:**

- Scans output for file paths ending in: `.png`, `.jpg`, `.jpeg`, `.bmp`, `.gif`
- Checks if file exists
- Displays in `ImageResultDialog`

**Text Output:**

- Everything else goes to `TextResultDialog`
- Preserves formatting
- Scrollable
- Monospace font

**Errors:**

- Exceptions from execution
- Type conversion errors
- Missing required arguments
- Displayed in `ErrorResultDialog` with red text

## Python Syntax Highlighting

### Implemented Features

- **Keywords**: `if`, `else`, `for`, `while`, `def`, `class`, etc. → Blue, Bold
- **Built-ins**: `print`, `len`, `str`, `range`, etc. → Yellow
- **Strings**: Single, double, and triple quotes → Orange
- **Comments**: Lines starting with `#` → Green
- **Numbers**: Integers and floats → Light green
- **Escape sequences**: Handled in strings
- **Default text**: White/theme color

### Color Scheme

```kotlin
val keywordColor = Color(0xFF569CD6)   // Blue
val stringColor = Color(0xFFCE9178)    // Orange
val commentColor = Color(0xFF6A9955)   // Green
val numberColor = Color(0xFFB5CEA8)    // Light green
val builtinColor = Color(0xFFDCDCAA)   // Yellow
```

Similar to VS Code's Dark+ theme.

## UI Components

### NeedleDetailScreen (Main)

- Top app bar with title, back button, play button
- Loading indicator (centered when loading)
- Scrollable content area
- Overlays for dialogs

### NeedleDetailContent

- Metadata card showing:
    - Description
    - Arguments with types
    - Tags
- Code card with syntax-highlighted Python

### ArgumentsDialog

- Modal dialog
- Scrollable list of text fields
- Validation on confirm
- Cancel option

### TextResultDialog

- Full-width scrollable text
- Monospace font
- Close button

### ImageResultDialog

- Bitmap decoded from file path
- Full-width image display
- Fallback for failed decode
- Close button

### ErrorResultDialog

- Red text for errors
- Monospace font
- Scrollable
- Close button

## Example Execution

### Calculate Sum Needle

**Code:**

```python
result = a + b
print(f"The sum is: {result}")
```

**User Flow:**

1. Click Play button
2. Dialog appears with:
    - `a` field (Float) *
    - `b` field (Float) *
3. Enter: `a = 5`, `b = 3`
4. Click Execute
5. See result dialog: "The sum is: 8.0"

### Temperature Converter Needle

**Code:**

```python
if unit.lower() == 'c':
    result = (temperature * 9/5) + 32
    print(f"{temperature}°C = {result:.2f}°F")
```

**User Flow:**

1. Click Play
2. Enter: `temperature = 25`, `unit = C`
3. Execute
4. Result: "25.0°C = 77.00°F"

### Chart Generator (with Image)

**Code:**

```python
import matplotlib.pyplot as plt
plt.plot([1,2,3], [4,5,6])
plt.savefig('/path/to/chart.png')
print('/path/to/chart.png')
```

**Result:**

- System detects `.png` in output
- Verifies file exists
- Opens `ImageResultDialog`
- Displays chart image

## Error Handling

### Common Errors

1. **Missing Required Argument**
   ```
   Error: Required argument 'a' is missing
   ```

2. **Invalid Type Conversion**
   ```
   Error: Invalid integer: "abc"
   ```

3. **Python Execution Error**
   ```
   Error: NameError: name 'x' is not defined
   ```

4. **Timeout (15 seconds)**
   ```
   Error: TimeoutError
   ```

All errors are caught and displayed in `ErrorResultDialog`.

## Performance Considerations

- **Syntax Highlighting**: Computed once per render, not character-by-character
- **Execution**: Runs on IO dispatcher to avoid blocking UI
- **Image Loading**: Only loads when dialog opens, not proactively
- **Scrolling**: Efficient with `rememberScrollState()`

## Future Enhancements

### Potential Features

1. **Code Editing**: Allow inline editing of needle code
2. **Multiple Outputs**: Support multiple images or mixed output
3. **Export Results**: Save/share execution results
4. **Execution History**: Show past executions with timestamp
5. **Favorites**: Star frequently used needles
6. **Rich Output**: Support for tables, plots, HTML
7. **Live Preview**: Execute on argument change (for simple needles)
8. **Breakpoints**: Debug mode with step execution
9. **Performance Metrics**: Show execution time, memory usage

### Advanced Syntax Highlighting

- **Indentation guides**
- **Line numbers**
- **Error underlining**
- **Autocomplete in code editor**
- **Function/class collapsing**

### Result Type Extensions

- **JSON Viewer**: Pretty-print JSON with collapsible trees
- **CSV Viewer**: Table display for CSV output
- **HTML Renderer**: Display HTML output
- **Audio Player**: For audio generation needles
- **Video Player**: For video processing needles
- **Interactive Plots**: Zoom/pan for matplotlib figures

## Testing Needles

### Manual Testing Checklist

- [ ] Navigate from Needles list to detail
- [ ] Verify syntax highlighting works
- [ ] Execute needle without arguments
- [ ] Execute needle with required arguments
- [ ] Execute needle with optional arguments
- [ ] Test with missing required argument
- [ ] Test with invalid type input
- [ ] Test needle that outputs text
- [ ] Test needle that outputs image
- [ ] Test needle that throws error
- [ ] Verify back navigation works
- [ ] Check loading states display correctly

### Sample Test Cases

**Test 1: No Arguments**

```kotlin
Needle(
    name = "Hello World",
    pythonCode = "print('Hello, World!')",
    args = emptyList()
)
// Expected: Direct execution, text result
```

**Test 2: Type Validation**

```kotlin
// Input: "abc" for Int argument
// Expected: Error dialog "Invalid integer: abc"
```

**Test 3: Optional Arguments**

```kotlin
// Provide only required args, skip optional
// Expected: Executes with defaults
```

## Integration with Chat System

When integrating with the chat interface:

```kotlin
// LLM detects need to use needle
val toolCall = detectToolCall(llmResponse)

// Execute needle
val result = executeNeedleUseCase(
    needleId = toolCall.needleId,
    args = toolCall.extractedArgs
)

// Feed result back to LLM
val followUpPrompt = """
Previous result:
${result.getOrNull()}

User question: ${originalQuestion}
"""
```

## Dependencies

- **Compose**: UI framework
- **Material3**: Design system
- **Chaquopy**: Python execution
- **ViewModel**: State management
- **Coroutines**: Async operations

No additional libraries needed for syntax highlighting (implemented from scratch).

## File Size

- `NeedleDetailScreen.kt`: ~400 lines
- `NeedleDetailViewModel.kt`: ~230 lines
- `PythonCodeView.kt`: ~180 lines
- **Total**: ~800 lines of code

Compact and maintainable!
