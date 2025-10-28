# LearnPlay

> **Learn while you play. Play while you learn.** 🎮📚

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20.4-brightgreen.svg)](https://www.minecraft.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://www.oracle.com/java/)

## 💡 The Philosophy

Educational games are usually terribly boring. People still play popular games as usual, ignoring the "educational" ones. So why try to gamify education when we can **educate the gameplay** instead?

LearnPlay doesn't interrupt your gaming experience with forced learning sessions. Instead, it seamlessly weaves learning into the natural flow of Minecraft gameplay through a **trigger-based learning system**. You're not playing an educational game—you're playing Minecraft while genuinely learning something on the side.

The goal? Achieve that perfect **flow state** where you're half-learning, half-playing, and they don't interfere with each other. Games have everything needed for flow—challenge, feedback, progression. Learning is hard, but it doesn't have to feel that way.

## 🎯 Features

### 📚 Simple Spaced Repetition System
- **Fixed Interval Progression**: Simple, predictable intervals (10min → 20min → 1d → 3d → 7d → 14d → 30d → 60d → 120d → 240d)
- **Two-Button System**: Just "Forgot" or "Remember"—no complicated ratings
- **Automatic Scheduling**: Cards appear when they're due for review
- **Progress Tracking**: Per-player progress saved with NBT data persistence
- **Review Statistics**: Track your learning progress with detailed stats

### 🎮 Gameplay Integration
LearnPlay triggers flashcard reviews during natural gameplay moments:

- **Death Trigger**: Review cards after dying (configurable frequency)
- **Timer Trigger**: Periodic reviews at set intervals
- **Block Break Trigger**: Review after breaking blocks (with whitelist support)
- **Block Place Trigger**: Review after placing blocks (with whitelist support)
- **Entity Kill Trigger**: Review after killing mobs (with whitelist support)
- **Advancement Trigger**: Review when earning achievements
- **Chat Trigger**: Review based on chat patterns

### 🗂️ Deck & Category Management
- **Hierarchical Organization**: Categories → Subcategories → Decks → Cards
- **Category Browser**: Navigate through your learning materials with an intuitive interface
- **Deck Editor**: Create and edit decks with a user-friendly GUI
- **Card Editor**: Add, edit, and manage flashcards
- **Enable/Disable Decks**: Control which decks are active for reviews
- **Import/Export**: JSON-based storage for easy sharing and backup

### ⚙️ Customization
- **Configurable Triggers**: Enable/disable and customize each trigger type
- **Review Limits**: Set daily limits for new cards and reviews
- **Keybindings**: Customizable hotkeys (default: 'I' for manual review)
- **HUD Stats**: Optional on-screen statistics display
- **Pause on Review**: Optionally pause the game during flashcard reviews

### 🌐 Multi-Platform Support
Built with [Architectury](https://github.com/architectury/architectury-api), LearnPlay supports:
- **Fabric**
- **Forge**
- **NeoForge**
- **Quilt**

## 📦 Installation

1. Download the appropriate version for your mod loader from the [Releases](../../releases) page
2. Install the required dependencies:
   - [Architectury API](https://www.curseforge.com/minecraft/mc-mods/architectury-api)
   - [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) (Fabric/Quilt only)
3. Place the mod JAR file in your `mods` folder
4. Launch Minecraft 1.20.4

## 🚀 Getting Started

### Creating Your First Deck

1. Press **'I'** (or your configured keybind) to open the config screen
2. Click **"Browse Categories & Decks"**
3. Click **"+ Deck"** to create a new deck
4. Add cards using the **"+ Card"** button
5. Enable the deck using the toggle button
6. Configure triggers in the config screen

### Reviewing Cards

Cards will automatically appear based on your configured triggers. You can also:
- Press **'I'** to manually start a review session
- Click **"Reveal Answer"** to see the answer
- Choose your response:
  - **Forgot**: Resets the card to 10 minutes
  - **Remember**: Advances to the next interval in the progression

### Understanding the Interval System

The system uses a simple fixed progression:
- **Level 0**: 10 minutes
- **Level 1**: 20 minutes
- **Level 2**: 1 day
- **Level 3**: 3 days
- **Level 4**: 7 days
- **Level 5**: 14 days
- **Level 6**: 30 days
- **Level 7**: 60 days
- **Level 8**: 120 days
- **Level 9**: 240 days (maximum)

Each time you click "Remember", the card advances one level. Click "Forgot" and it resets to 10 minutes.

## 📁 File Structure

```
config/learnplay/
├── config.json              # Main configuration
├── categories/              # Category definitions
│   └── *.json
├── decks/                   # User-created decks
│   └── *.json
└── player_progress/         # Per-player SRS data
    └── <player_uuid>.dat
```

## ⚙️ Configuration

Configuration is stored in `config/learnplay/config.json`:

```json
{
  "triggers": {
    "enableDeathTrigger": true,
    "enableTimerTrigger": false,
    "deathTriggerEveryNDeaths": 2,
    "timerIntervalMinutes": 15,
    "blockBreakTriggerThreshold": 100,
    "blockBreakWhitelist": "stone,dirt,oak_log,iron_ore,diamond_ore",
    "entityKillTriggerThreshold": 10,
    "entityKillWhitelist": "zombie,skeleton,creeper,spider,enderman"
  },
  "maxCardsPerSession": 20,
  "maxNewCardsPerDay": 10,
  "maxReviewsPerDay": 100,
  "pauseGameDuringReview": true,
  "showHudStats": false,
  "keybindCode": 73
}
```

## 🎨 Screenshots

<!-- Add screenshots here -->

## 🛠️ Building from Source

### Prerequisites
- JDK 21 or higher
- Git

### Build Instructions

```bash
# Clone the repository
git clone https://github.com/yourusername/learnplay.git
cd learnplay

# Build all platforms
./gradlew build

# Build specific platform
./gradlew :fabric:build
./gradlew :forge:build
./gradlew :neoforge:build
./gradlew :quilt:build
```

Built JARs will be in `<platform>/build/libs/`

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Setup

1. Clone the repository
2. Import the project into your IDE (IntelliJ IDEA recommended)
3. Run the appropriate Gradle task:
   - `./gradlew :fabric:runClient` - Run Fabric client
   - `./gradlew :forge:runClient` - Run Forge client
   - `./gradlew :neoforge:runClient` - Run NeoForge client
   - `./gradlew :quilt:runClient` - Run Quilt client

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Spaced Repetition**: Inspired by proven learning science
- **Architectury**: For enabling multi-platform support
- **Minecraft Modding Community**: For tools, documentation, and support
