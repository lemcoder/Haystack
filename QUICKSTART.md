# Haystack - Quick Start Guide

## 🚀 What You Just Built

A complete **Needle Management & Execution System** for your hackathon project!

## 📱 User Flow

### 1. Launch App

- App opens to Home screen
- Navigate to **Needles** (click Add icon in top bar)

### 2. Browse Needles

- See 4 sample needles pre-loaded:
    - **Calculate Sum** - Add two numbers
    - **Text Analyzer** - Count words/chars/sentences
    - **Temperature Converter** - C↔F conversion
    - **List Sorter** - Sort comma-separated numbers

### 3. View Needle Details

- Click any needle card
- See full-screen Python code with **syntax highlighting**
- View metadata: description, arguments, tags

### 4. Execute a Needle

#### Example: Calculate Sum

**Step 1:** Click the **Play button** (▶) in top bar

**Step 2:** Arguments dialog appears:

```
a (Float) *
[      5      ]

b (Float) *
[      3      ]

[Cancel]  [Execute]
```

**Step 3:** Click Execute

**Step 4:** Result dialog shows:

```
Execution Result
─────────────────
The sum is: 8.0

        [Close]
```

#### Example: Temperature Converter

**Input:**

```
temperature: 25
unit: C
```

**Output:**

```
25.0°C = 77.00°F
```

#### Example: Text Analyzer

**Input:**

```
text: Hello world! This is a test.
```

**Output:**

```
Word count: 6
Character count: 28
Sentence count: 2
```

### 5. Execute Needles with Images

If a needle outputs an image path (e.g., matplotlib chart):

- System auto-detects `.png`, `.jpg`, etc.
- Opens image viewer dialog
- Displays the generated image

### 6. Delete Needles

- Click trash icon on any needle card
- Confirm deletion

## 🎨 Features Showcase

### Syntax Highlighting

Python code is beautifully color-coded:

- **Blue**: `if`, `for`, `def`, `class`
- **Yellow**: `print`, `len`, `range`
- **Orange**: `"strings"`
- **Green**: `# comments`
- **Light Green**: `123` numbers

### Type-Aware Results

Three result types automatically detected:

| Type | Detection | Display |
|------|-----------|---------|
| Text | Default | Scrollable text dialog |
| Image | `.png`, `.jpg` in output | Image preview dialog |
| Error | Exception thrown | Red error message |

### Argument Validation

- Required arguments marked with `*`
- Type conversion (String → Int/Float/Boolean)
- Default values pre-filled
- Error if required argument missing

## 🛠 For Developers

### Add New Needle Programmatically

```kotlin
val needle = Needle(
    id = UUID.randomUUID().toString(),
    name = "My Tool",
    description = "Does something cool",
    pythonCode = """
        result = input_value * 2
        print(f"Result: {result}")
    """.trimIndent(),
    args = listOf(
        Needle.Arg(
            name = "input_value",
            type = NeedleType.Int,
            description = "A number to double",
            required = true
        )
    ),
    returnType = NeedleType.String,
    tags = listOf("math", "utility")
)

// Save it
val createNeedle = CreateNeedleUseCase()
createNeedle(needle)
```

### Execute Needle from Code

```kotlin
val execute = ExecuteNeedleUseCase()
val result = execute(
    needleId = "needle-uuid",
    args = mapOf("input_value" to 42)
)

result.fold(
    onSuccess = { output -> println(output) },
    onFailure = { error -> println(error) }
)
```

### Repository Access

```kotlin
// Get all needles
val needles = NeedleRepository.Instance.getAllNeedles()

// Get specific needle
val needle = NeedleRepository.Instance.getNeedleById("uuid")

// Delete needle
NeedleRepository.Instance.deleteNeedle("uuid")

// Observe changes
NeedleRepository.Instance.needlesFlow.collect { needles ->
    // UI updates automatically
}
```

## 📋 Architecture Recap

```
┌─────────────────────────────────────────┐
│           Needles Screen                │
│  ┌─────────────────────────────────┐   │
│  │ ▶ Calculate Sum                 │◄──┼── Click
│  │   Add two numbers               │   │
│  │   [math] [calculator]           │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│        Needle Detail Screen             │
│  ┌─────────────────────────────────┐   │
│  │ Python Code (Syntax Highlighted)│   │
│  │                                 │   │
│  │ result = a + b                  │   │
│  │ print(f"Sum: {result}")         │   │
│  └─────────────────────────────────┘   │
│                                  [▶]◄───┼── Click Play
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│        Arguments Dialog                 │
│  ┌─────────────────────────────────┐   │
│  │ a (Float) *: [    5    ]        │   │
│  │ b (Float) *: [    3    ]        │   │
│  └─────────────────────────────────┘   │
│         [Cancel]  [Execute]◄────────────┼── Click Execute
└─────────────────────────────────────────┘
                    │
                    ▼
         ┌──────────────────┐
         │  PythonExecutor  │
         │   (Chaquopy)     │
         └──────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│         Result Dialog                   │
│  ┌─────────────────────────────────┐   │
│  │ Execution Result                │   │
│  │                                 │   │
│  │ The sum is: 8.0                 │   │
│  │                                 │   │
│  └─────────────────────────────────┘   │
│              [Close]                    │
└─────────────────────────────────────────┘
```

## 🎯 Next Steps for Your Hackathon

### Immediate (30 min)

1. ✅ Test the app - run all sample needles
2. ✅ Add your own custom needle for demo
3. ✅ Practice the user flow

### Short-term (2-4 hours)

1. **Needle Creation UI**: Build form to create needles manually
2. **OpenRouter Integration**: Add API client for large LLM
3. **Chat Screen**: Basic message list with input field

### Medium-term (4-8 hours)

1. **LLM-Assisted Generation**: Natural language → Python needle
2. **Tool Calling**: LLM detects when to use needles
3. **Execution in Chat**: Results flow back to conversation

### Demo Script

**"Let me show you Haystack - an AI-powered Python tool builder"**

1. **Show needle list**: "Here are some tools I created"
2. **Open Calculator**: "This adds two numbers - see the code?"
3. **Execute**: "Let me run it with 5 and 3... Result: 8!"
4. **Show Temperature Converter**: "This one does C to F conversion"
5. **Execute**: "25 Celsius is 77 Fahrenheit"
6. **Future vision**: "Next, I'll use GPT-4 to generate these needles on-demand, and my on-device
   LLM will call them during chat conversations"

## 🐛 Troubleshooting

### No needles appear

- Check logs for deserialization errors
- Clear app data and restart

### Execution fails

- Check Python syntax in needle code
- Verify argument types match
- Look for timeout errors (>15 sec)

### Syntax highlighting looks wrong

- Check for unmatched quotes
- Verify code is valid Python

### Image not displayed

- Ensure file path is absolute
- Check file exists after execution
- Verify file has image extension

## 📚 Documentation Files

- `NEEDLE_SYSTEM.md` - Complete system architecture
- `NEEDLE_DETAIL_FEATURE.md` - Detail screen deep dive
- `QUICKSTART.md` - This file!

## 🎉 You're Ready!

You now have:

- ✅ Needle storage with DataStore
- ✅ Beautiful UI with Material 3
- ✅ Syntax highlighting
- ✅ Execution with argument collection
- ✅ Type-aware result display
- ✅ 4 working sample needles

**Go build something awesome! 🚀**
