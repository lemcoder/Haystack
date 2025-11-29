# Haystack

---

## 🚀 Overview

Haystack is an Android application that allows you to create custom Python-based tools (called
*Needles*) and interact with them through a conversational AI interface powered by a local LLM. All
processing happens on-device for complete privacy and offline capability.

### What makes Haystack special?

- 🤖 **Natural Language Interface** - Chat with an AI assistant that automatically uses your tools
- 🔧 **Custom Python Tools** - Create "Needles" to perform any task
- 📱 **Runs On-Device** - Complete privacy with local LLM execution
- 🎨 **Modern UI** - Beautiful Material Design 3 interface with Jetpack Compose
- ⚡ **Real-Time Execution** - Python code runs instantly via Chaquopy
- 🔒 **No Server Required** - Everything runs locally on your device

## Video

https://github.com/user-attachments/assets/8b38ac9b-115a-4ec6-b487-2535dd38bda3

## APK link

https://we.tl/t-OQIx5BAipD
Password: cactus2025


## ✨ Features

### Core Features

- **Chat Interface**: Conversational AI powered by Koog framework
- **Needle Management**: Create, view, edit, and delete Python-based tools
- **Python Execution**: Run Python code on Android using Chaquopy
- **Syntax Highlighting**: Beautiful code display with color-coded Python syntax
- **Type-Safe Arguments**: Strongly-typed argument system with validation
- **Smart Result Display**: Automatic detection of text, images, and error outputs
- **Local LLM**: Fully on-device AI using Cactus and koog-edge
- **Sample Tools**: Pre-loaded examples to get you started quickly

### Included Sample Needles

1. **Calculate Sum** - Add two numbers
2. **Text Analyzer** - Count words, characters, and sentences
3. **Temperature Converter** - Convert between Celsius and Fahrenheit
4. **List Sorter** - Sort comma-separated lists in ascending/descending order
5. **Chart Generator** - Create line charts with matplotlib
6. **Data Visualizer** - Generate bar charts from data
7. **Weather Fetcher** - Mock weather API demonstration

## 🛠 Technology Stack

- **Language**: Kotlin 2.2.21
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVI + Clean Architecture with Use Cases
- **Python Integration**: Chaquopy 16.1.0
- **AI/ML Framework**: Koog 0.5.3 + koog-edge 0.0.3
- **Local LLM**: Cactus 1.0.2-beta
- **Data Persistence**: DataStore Preferences
- **Serialization**: Kotlinx Serialization
- **Build System**: Gradle with Kotlin DSL
- **Min SDK**: 31 (Android 12)
- **Target SDK**: 36

## 📋 Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android device or emulator running Android 12+ (API 31+)
- Python 3 installed on your development machine (for Chaquopy)

## 🚦 Getting Started

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/lemcoder/Haystack.git
   cd Haystack
   ```

2. **Open in Android Studio**
    - Open Android Studio
    - Select "Open an existing project"
    - Navigate to the cloned repository

3. **Sync Gradle**
    - Android Studio will automatically sync Gradle dependencies
    - Wait for the sync to complete

4. **Build and Run**
    - Connect your Android device or start an emulator
    - Click the "Run" button or press `Shift + F10`


## 🤝 Contributing

While this is currently a personal project, contributions and feedback are welcome!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 🐛 Known Issues

- Python execution timeout is currently 15 seconds
- Large LLM model downloads can take time on first launch
- Pip packages available are matplotlib and requests

## 🛣 Roadmap

### Phase 1: Enhanced Needle Creation ✅ DONE

- [x] Needle data model
- [x] Repository with DataStore
- [x] CRUD operations
- [x] Needles list UI
- [x] Detail and execution UI

### Phase 2: Chat Interface ✅ DONE

- [x] Chat screen with message history
- [x] Local LLM integration with Koog
- [x] Tool calling system
- [x] Needle execution in chat
- [x] Result formatting
- [x] AI-assisted Needle generation

## 👤 Author

**lemcoder**

- GitHub: [@lemcoder](https://github.com/lemcoder)

## 🙏 Acknowledgments

- [Koog](https://github.com/koog-ai) - Kotlin-first AI framework
- [Chaquopy](https://chaquo.com/chaquopy/) - Python for Android
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI
- [Material Design 3](https://m3.material.io/) - Design system

## 📞 Support

If you encounter any issues or have questions:

1. Check the [documentation](#documentation) files
2. Look through [existing issues](https://github.com/lemcoder/Haystack/issues)
3. Create a new issue with detailed information

---

<p align="center">
  Made with ❤️ and Kotlin
</p>

<p align="center">
  <sub>Built for innovation, powered by AI</sub>
</p>
