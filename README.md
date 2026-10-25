# Kiko Calistung (Voice-First Socratic Learning Companion)

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Tests](https://img.shields.io/badge/Tests-Robolectric%20JVM-success?style=flat)](https://robolectric.org/)

**Kiko Calistung** is a restorative, voice-first Socratic learning companion designed for early childhood literacy and numeracy (*Membaca, Menulis, Berhitung* / Calistung, ages 3–6). 

Built on the **"Calm Scaffolding"** and **Tactile Warm Minimalism** design philosophy, Kiko rejects commercial preschool gamification (neon fills, casino dopamine loops, loud sound effects, and intrusive ads) in favor of unhurried conversational inquiry, physical-digital parity, and total parental data sovereignty.

---

## Key Features & Architecture

### 1. Socratic Voice Companion (`Suara`)
* **3-Tier Step-Down Ladder**: Never penalizes or says "wrong". When an answer is incorrect or hesitant, Kiko introduces progressive hints, inducing questions, and tactile analogies.
* **15-Minute Protected Time Architecture**: Enforces a strict daily session cap that transitions seamlessly into a peaceful twilight wind-down ritual to prevent screen fatigue and habituation.
* **Multimodal Low-SNR Fallback**: Automatically senses ambient background noise or low speech confidence, immediately rendering large visual Montessori tap cards so learning continues without frustration.
* **Voice Calibration & Simulated Test Lab**: In-app development controls allow testing different SNR audio levels, step-down hints, and wind-down triggers locally.

### 2. Curriculum & Tactile Modules (`Pilihan`)
* **Phonemic Awareness & Syllable Synthesis**: Modular voice tasks focusing on vowel sounds, consonants, and word building.
* **Montessori Numeracy**: Concrete counting, one-to-one correspondence, and number sense up to 10.
* **Offline Material Bundles**: Downloadable, zero-cloud puzzle packs for offline car rides, classrooms, and quiet time.

### 3. Rest & Data Sovereignty Ops (`Istirahat`)
* **Zero Persistent Voice Storage**: Temporary raw audio recordings are ephemeral and cryptographically purged within 24 hours.
* **On-Demand Voice Purge**: Parents can inspect the audit ledger and permanently purge stored voice cache at any moment.
* **Unit Economics & Token Transparency**: Complete disclosure of API costs, token usage, and inference efficiency for total operational honesty.

### 4. Parental Cognitive Telemetry Hub (`Laporan`)
* **Pedagogical Triad Metrics** (styled in tabular monospaced format):
  * **URR (Unprompted Retention Rate)**: Tracks unassisted recall across multiple learning days.
  * **TCR (Response Time & Hesitation)**: Evaluates cognitive processing speed without pressure.
  * **RBS (Resistance-to-Bait Score)**: Measures logical conviction when gently challenged by Kiko's counter-questions.
* **Cognitive Trajectory**: Visual indicators showing phonemic fluency and early numeracy milestones.

---

## Design System: "Calm Scaffolding"

Rooted in Scandinavian wooden toy craft, pressed card stock, and natural earth pigments:

| Token | Hex | Role |
|---|---|---|
| **Calm Primary** | `#9D3E1A` | Terracotta Clay – focal interactive anchors, primary affirmative triggers |
| **Calm Secondary** | `#3F674D` | Sage Earth – pedagogical progress, calm mastery indicators |
| **Calm Tertiary** | `#7B5500` | Honey Ochre – supportive scaffolding, timer chips, highlight accents |
| **Surface Cream** | `#FFF8F6` | Warm natural linen canvas, replacing sterile cold whites |
| **Surface Container** | `#FAEBE6` | Pressed card background for tactile depth |
| **Card Border Tactile** | `#E2D7CC` | Subtle debossed outline imitating natural beechwood and cardboard |

### Typography
* **Nunito Sans**: Non-intimidating, rounded geometric forms for child-facing voice prompts and titles.
* **Plus Jakarta Sans**: Crisp, high-legibility sans-serif for parent telemetry, metrics, and data audit logs.

---

## Tech Stack

* **Language**: Kotlin 2.0+
* **UI**: Jetpack Compose with Material 3 (M3)
* **Local Persistence**: Android Room Database (`CalistungDatabase`)
* **State Management**: `ViewModel` with Kotlin Coroutines `StateFlow` and Compose state hoisting
* **Testing**: Robolectric local JVM testing for end-to-end critical user journeys
* **Architecture**: Clean MVVM (Model-View-ViewModel) + Offline-first repository pattern

---

## Project Structure

```
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt               # Root Scaffold & 4-Tab Navigation
│   │   │   │   ├── audio/                        # Audio processing & SNR detection
│   │   │   │   ├── data/                         # Room entities, DAOs, & curriculum definitions
│   │   │   │   ├── network/                      # Network & API client abstractions
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/                  # Suara, Pilihan, Istirahat, Laporan
│   │   │   │   │   └── theme/                    # Color, Type, and Theme design tokens
│   │   │   │   └── viewmodel/                    # Socratic learning session ViewModel
│   │   │   └── res/                              # Strings, local fonts, and vector drawables
│   │   └── test/
│   │       └── java/com/example/
│   │           └── ExampleRobolectricTest.kt     # End-to-end Robolectric test suite
├── metadata.json                                 # AI Studio platform configuration
├── build.gradle.kts                              # Root Gradle script
└── settings.gradle.kts                           # Module settings & dependency catalogs
```

---

## Running the Application

### Build & Run
Run the app using the standard Gradle assemble command or via Android Studio:
```bash
gradle assembleDebug
```

### Run Unit & Robolectric Tests
Execute the local JVM test suite (covering Socratic ladder progression, SNR fallback cards, session wind-down, and parent hub telemetry):
```bash
gradle :app:testDebugUnitTest
```

---

## Privacy & Safety Principles

1. **COPPA & GDPR-K Aligned**: No third-party ad tracking, fingerprinting, or biometric identification.
2. **Acoustic Rest**: Active session caps ensure young children do not engage in unbounded device usage.
3. **Parental Consent**: Explicit disclosures regarding audio handling with one-tap zero-trace purging.
