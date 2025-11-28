# Haystack Needle System

## Overview

The Needle System is the core of Haystack - a Python tool management and execution framework for
Android. Needles are Python scripts that can be created manually or via LLM-assisted generation,
stored locally, and executed on-device via Chaquopy.

## Architecture

### 1. Data Model

#### Needle

Located: `core/model/needle/Needle.kt`

```kotlin
data class Needle(
    val id: String,                    // Unique identifier (UUID)
    val name: String,                  // Display name
    val description: String,           // What the needle does
    val pythonCode: String,           // The Python script content
    val args: List<Arg>,              // Input arguments
    val returnType: NeedleType,       // Output type
    val dependencies: List<String>,    // pip packages needed
    val tags: List<String>,           // For categorization
    val createdAt: Long,              // Timestamp
    val updatedAt: Long,              // Last modified
    val isLLMGenerated: Boolean       // Manual vs AI-generated
)
```

#### NeedleType

Supported types:

- `String` - Text data
- `Int` - Integer numbers
- `Float` - Decimal numbers
- `Boolean` - True/False
- `ByteArray` - Binary data
- `Any` - Generic type

### 2. Data Layer

#### NeedleRepository

Located: `core/data/NeedleRepository.kt`

Uses DataStore for persistence (JSON serialization of needle list).

**Operations:**

- `getAllNeedles()` - Get all stored needles
- `getNeedleById(id)` - Get specific needle
- `saveNeedle(needle)` - Create or update needle
- `updateNeedle(needle)` - Update existing needle
- `deleteNeedle(id)` - Remove needle
- `deleteAllNeedles()` - Clear all needles
- `needlesFlow` - Observable Flow of needles

**Storage Format:**
Stored as JSON in DataStore preferences:

```json
{
  "needles": "[{\"id\":\"...\",\"name\":\"...\",\"pythonCode\":\"...\"}]"
}
```

### 3. Use Cases

#### CreateNeedleUseCase

Creates a new needle, auto-generates UUID if needed.

#### ExecuteNeedleUseCase

Executes a needle with provided arguments:

1. Validates required arguments
2. Builds Python code with variable assignments
3. Executes via PythonExecutor
4. Returns result as string

**Argument Injection:**

```python
# If needle has args: a (Float), b (Float)
# And you call with: {"a": 5.0, "b": 3.0}

# Generated Python code:
a = 5.0
b = 3.0

# Needle code
result = a + b
print(f"The sum is: {result}")
```

#### GetAllNeedlesUseCase

Returns Flow of all needles for reactive UI updates.

#### DeleteNeedleUseCase

Safely deletes a needle by ID.

#### CreateSampleNeedlesUseCase

Creates sample needles on first launch:

- Calculate Sum
- Text Analyzer
- Temperature Converter
- List Sorter

### 4. Presentation Layer

#### NeedlesScreen

Modern Material 3 UI with:

- List of all needles with cards
- Tags, descriptions, timestamps
- "AI" badge for LLM-generated needles
- Delete confirmation dialog
- Empty state with prompt to create
- FAB for creating new needles

#### NeedlesViewModel

Manages:

- Loading needles (reactive with Flow)
- Delete operations with confirmation
- Navigation
- Sample needle initialization

**State:**

```kotlin
data class NeedlesState(
    val needles: List<Needle>,
    val isLoading: Boolean,
    val selectedNeedle: Needle?,
    val showCreateDialog: Boolean,
    val showDeleteDialog: Boolean,
    val needleToDelete: Needle?
)
```

**Events:**

- CreateNewNeedle
- SelectNeedle
- DeleteNeedle
- ConfirmDelete
- CancelDelete
- NavigateBack

### 5. Python Execution

#### PythonExecutor

Located: `core/python/PythonExecutor.kt`

Uses Chaquopy's Python interpreter with:

- Stdout capture
- Timeout handling (15 seconds via `interpreter.py`)
- Thread safety
- Error handling

**Usage:**

```kotlin
val result = PythonExecutor.executeSafe(pythonCode)
result.fold(
    onSuccess = { output -> /* handle output */ },
    onFailure = { error -> /* handle error */ }
)
```

## Sample Needles

### 1. Calculate Sum

Simple math operation demonstrating basic argument passing.

```python
result = a + b
print(f"The sum is: {result}")
```

### 2. Text Analyzer

String processing showing multiple output values.

```python
word_count = len(text.split())
char_count = len(text)
sentence_count = text.count('.') + text.count('!') + text.count('?')
print(f"Word count: {word_count}")
print(f"Character count: {char_count}")
print(f"Sentence count: {sentence_count}")
```

### 3. Temperature Converter

Conditional logic with string and float inputs.

```python
if unit.lower() == 'c':
    result = (temperature * 9/5) + 32
    print(f"{temperature}°C = {result:.2f}°F")
elif unit.lower() == 'f':
    result = (temperature - 32) * 5/9
    print(f"{temperature}°F = {result:.2f}°C")
```

### 4. List Sorter

String parsing and list manipulation with optional arguments.

```python
numbers = [float(x.strip()) for x in numbers_str.split(',')]
sorted_numbers = sorted(numbers, reverse=(order.lower() == 'desc'))
print(f"Sorted list: {sorted_numbers}")
```

## Next Steps

### Phase 1: Manual Needle Creation ✅ DONE

- ✅ Needle data model
- ✅ Repository with DataStore
- ✅ CRUD use cases
- ✅ Needles list UI
- ⏳ Needle creation UI (dialog placeholder exists)
- ⏳ Needle detail/edit UI

### Phase 2: LLM-Assisted Generation

- [ ] OpenRouter API integration
- [ ] Needle generation prompt system
- [ ] Interactive refinement chat
- [ ] Code validation & testing
- [ ] Save generated needle

### Phase 3: Chat Interface

- [ ] Chat screen with message history
- [ ] Streaming responses from local LLM
- [ ] Tool calling detection
- [ ] Needle execution in chat context
- [ ] Result formatting and display

### Phase 4: Advanced Features

- [ ] Dependency management (pip install)
- [ ] Needle marketplace/sharing
- [ ] Python environment isolation
- [ ] Needle versioning
- [ ] Testing framework for needles
- [ ] Import/export needles

## Testing Needles

To test a needle:

```kotlin
val executeNeedle = ExecuteNeedleUseCase()
val result = executeNeedle(
    needleId = "needle-uuid",
    args = mapOf(
        "a" to 5.0,
        "b" to 3.0
    )
)
```

This provides Material Icons for the UI (Add, Delete, ArrowBack, etc.)

## File Structure

```
app/src/main/kotlin/io/github/lemcoder/haystack/
├── core/
│   ├── data/
│   │   └── NeedleRepository.kt          # DataStore persistence
│   ├── model/
│   │   └── needle/
│   │       └── Needle.kt                # Data models
│   ├── python/
│   │   └── PythonExecutor.kt            # Chaquopy wrapper
│   └── useCase/
│       ├── CreateNeedleUseCase.kt
│       ├── ExecuteNeedleUseCase.kt
│       ├── GetAllNeedlesUseCase.kt
│       ├── DeleteNeedleUseCase.kt
│       └── CreateSampleNeedlesUseCase.kt
├── navigation/
│   └── Destination.kt                   # Navigation destinations
└── presentation/
    └── screen/
        ├── needles/
        │   ├── NeedlesScreen.kt         # List UI
        │   ├── NeedlesViewModel.kt      # State management
        │   ├── NeedlesState.kt          # State model
        │   └── NeedlesEvent.kt          # Events
        └── needleDetail/
            ├── NeedleDetailScreen.kt    # Detail/execution UI
            ├── NeedleDetailViewModel.kt # Execution logic
            ├── NeedleDetailState.kt     # State model
            ├── NeedleDetailEvent.kt     # Events
            ├── NeedleDetailRoute.kt     # Route wrapper
            └── component/
                └── PythonCodeView.kt    # Syntax highlighting

app/src/main/python/
└── interpreter.py                       # Python execution wrapper
```

## Key Patterns

1. **MVI Architecture**: Model-View-Intent pattern for predictable state management
2. **Use Case Pattern**: Single responsibility, testable business logic
3. **Repository Pattern**: Abstract data source, easy to swap implementations
4. **Flow/StateFlow**: Reactive data streams for UI updates
5. **Result<T>**: Type-safe error handling
6. **Sealed Interfaces**: Type-safe events and states

## Performance Considerations

- DataStore is async and performant for small-medium datasets
- For large needle collections (>1000), consider migrating to Room
- Python execution is isolated in threads with timeout
- Needle list uses LazyColumn for efficient scrolling

## Security Notes

⚠️ **Important**: Python execution is NOT sandboxed. Needles have full access to:

- File system (via app's context)
- Network (if permission granted)
- Device resources

For production, consider:

- Code review/approval system
- Restricted Python environment
- Permission system for needles
- Code signing/verification
