# ⚡ GLANCEFLOW

> **Ambient Offline-First AI Productivity System for Next-Generation Smartphones**  
> *Transform real-world noise into clear, actionable physical artifacts.*

---

```
   ┌─────────────────────────────────────────────────────────────┐
   │  REAL WORLD ──▶ CAPTURE ──▶ UNDERSTAND ──▶ STRUCTURE ──▶ GLANCE │
   └─────────────────────────────────────────────────────────────┘
```

GlanceFlow reimagines mobile productivity. Instead of forcing users into conventional dashboards, cluttered calendars, and generic SaaS interfaces, GlanceFlow is designed as a **series of art-directed digital posters** driven by on-device intelligence. 

Snap a photo of a messy classroom blackboard, record a fast voice memo, or paste lecture notes — GlanceFlow extracts what matters, normalizes deadlines, classifies priorities, and presents a **tangible physical information artifact** floating over giant Swiss typography.

---

## 🌟 Key Highlights

- **100% Offline-First Neural Architecture**: Powered by an on-device **Google Gemma 2B-IT (INT4)** model and on-device **ML Kit Optical Character Recognition (OCR)**. Operates in complete Airplane Mode with zero cloud dependencies.
- **Digital Poster & Physical Artifact Design Language**: Inspired by high-contrast editorial typography, full-screen color fields, tactile ticket cutouts, asymmetric rotation, and layer depth.
- **Optical CameraX Viewfinder**: High-performance camera preview with technical crosshairs, dynamic laser reticle, front/rear lens switching, torch/flash controls, and image enhancement curves.
- **Kinetic AI Synthesis**: Live multi-stage kinetic transformation: `CAPTURED` ➔ `READING YOUR WORLD` ➔ `UNDERSTANDING` ➔ `STRUCTURING` ➔ `I FOUND WHAT MATTERS`.
- **Zero-Cloud Companion Sync**: Embedded lightweight HTTP server serving a live web dashboard directly to your laptop over local Wi-Fi without third-party servers.
- **Smart Unfinished Assignment Notification Engine**: Monitors coursework and pending tasks, scheduling background `AlarmManager` alerts with interactive notification actions (`MARK DONE` & `SNOOZE 1H`) directly in the notification shade.
- **Room Database Local Persistence**: SQLite database caching active, completed, and archived glances with instant reactive Kotlin Flows.

---

## 🎨 Visual Design Philosophy

GlanceFlow avoids "AI slop" and conventional Material cards in favor of **Digital Posters**:

| Screen / Mode | Dominant Color Field | Visual Role |
| :--- | :--- | :--- |
| **Glance Home** | Void Black / Warm Sand | Giant condensed typography (`YOUR DAY AT A GLANCE`) with floating artifact & circular instruments |
| **Next Action (Active)** | Deep Red (`#C73627`) | Urgent milestone focal poster (`DO IT NOW`) with immediate resolution triggers |
| **Upcoming (Timeline)** | Vivid Turquoise (`#14968E`) | Chronological timeline (`UPCOMING GLANCES`) with deadline markers and milestone queue |
| **AI Synthesis** | Acid Yellow (`#E2F028`) | Kinetic typographic metamorphosis from raw text/image into a structured card |
| **Voice Capture** | Warm Orange (`#DD5320`) | Acoustic waveform interface (`TELL ME WHAT TO DO`) morphing spoken audio to artifact |
| **Archive Repository** | Deep Charcoal (`#0C0E14`) | Layered, overlapping document repository (`ALL GLANCES` & `DONE.`) |

---

## 🧠 Dual-Tier AI Engine

```
                        ┌─────────────────────────────────┐
                        │   OPTICAL / AUDIO CAPTURE INPUT │
                        └────────────────┬────────────────┘
                                         │
                         ┌───────────────▼──────────────┐
                         │   On-Device ML Kit OCR / ASR │
                         └───────────────┬──────────────┘
                                         │
               ┌─────────────────────────┴─────────────────────────┐
               │                                                   │
    [Airplane / Offline Mode]                            [Optional Cloud Boost]
               │                                                   │
  ┌────────────▼──────────────┐                       ┌────────────▼──────────────┐
  │ Google Gemma 2B-IT (Local)│                       │   Cloud Gemini 3.5-Flash  │
  │ • 4-Bit INT4 Quantization │                       │   • Deep contextual NLU   │
  │ • NPU/GPU Acceleration    │                       │   • Auto-fallback to local│
  │ • Zero Cloud Transmission │                       └───────────────────────────┘
  └────────────┬──────────────┘
               │
  ┌────────────▼──────────────┐
  │ Heuristic Fallback Engine │
  │ • Regex & Date Parsers    │
  │ • Zero-battery extraction │
  └────────────┬──────────────┘
               │
 ┌─────────────▼───────────────────────────────────────────────┐
 │ STRUCTURED PHYSICAL ARTIFACT (Category, Priority, Due, Todo)│
 └─────────────────────────────────────────────────────────────┘
```

1. **Google Gemma 2B-IT (Local On-Device)**:
   - Quantized to 4-bit INT4 (LiteRT/AWQ) for mobile NPU/GPU execution.
   - Structured JSON output formatted via Gemma `<start_of_turn>user ... <end_of_turn>` templates.
   - Built-in on-device benchmark suite measuring tokens/sec and TTFT (time to first token).
   - Custom weights importer (`.bin` / `.task`) directly from the device file manager.
2. **Offline Rule-Based NLP**:
   - Deterministic keyword and date parsing for instant, low-battery processing.
3. **Cloud Gemini 3.5-Flash (Optional)**:
   - Cloud expansion mode that automatically falls back to Gemma if connectivity is severed.

---

## 📱 Navigation & Screen Architecture

The app uses **Jetpack Navigation** (`NavHost`) with clean backstack management and a custom floating capsule:

- **`home` (`HomeScreen`)**:
  - Precision circular data instruments (`14 GLANCES`, `02 DUE`, `01 URGENT`).
  - Hero condensed typography with floating, tilted artifact (`GlanceArtifact`).
  - Quick triggers for Scan, Voice, Text, and Classroom Presets.
- **`capture` (`CaptureScreen`)**:
  - Full-screen CameraX viewfinder with real-time laser scanning reticle.
  - Front/Back camera toggle, torch/flash controls, and gallery picker.
- **`upcoming` (`CalendarScreen`)**:
  - Turquoise color field featuring milestone countdowns and time-until-due badges.
- **`archive` (`GlancesScreen`)**:
  - Staggered, overlapping physical cards with interactive swipe-to-complete and swipe-to-archive gestures.
  - Monospace search bar with category filters (`Assignment`, `Exam`, `Meeting`, `Task`, `Event`).
- **`sync` (`SyncScreen`)**:
  - Embedded HTTP companion server with live laptop dashboard (`http://<ip>:8080`).
- **`settings` (`SettingsScreen`)**:
  - Gemma 2B model manager, live benchmark test, and Airplane Mode simulation switch.

---

## 🛠 Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Language** | Kotlin 2.0+ (100% Coroutines & Flows) |
| **UI Framework** | Jetpack Compose, Material 3, Custom Canvas Drawing |
| **Navigation** | AndroidX Navigation Compose (`NavHost`) |
| **Camera & Vision** | CameraX (`PreviewView`, `ImageCapture`), Google ML Kit Text Recognition |
| **Local AI** | Google Gemma 2B-IT (INT4), On-device benchmark harness |
| **Cloud AI (Optional)**| Google Gemini 3.5-Flash API via OkHttp |
| **Data Persistence** | Room Database (SQLite), Android DataStore |
| **Local Sync** | Embedded Ktor/NanoHTTPD local network server |
| **Notifications** | Android NotificationManager with custom channels |
| **Testing** | Robolectric, AndroidX JUnit, KotlinX Coroutines Test |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug / Meerkat or later
- JDK 17 or higher
- Android SDK 34 (Android 14) or higher (Min SDK: 26)

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/example/glanceflow.git
cd glanceflow

# Run unit and Robolectric tests
gradle :app:testDebugUnitTest

# Assemble Debug APK
gradle :app:assembleDebug
```

### Running on Device / Emulator

1. Open the project in Android Studio.
2. Select your physical Android device or emulator.
3. Click **Run (`Shift + F10`)**.
4. Grant Camera and Notification permissions when prompted.
5. In Settings, select **Gemma 2B (Local)** to experience 100% on-device AI!

---

## 🧪 Testing the Offline Flow (Hackathon Walkthrough)

1. **Launch App**: The Home screen displays the poster composition with pre-seeded classroom artifacts.
2. **Open Settings**: Toggle **Airplane Mode Simulation** ON. Notice the status indicator changes to `● AIRPLANE MODE (GEMMA)`.
3. **Capture**: Tap the central circular Camera button. Point at a whiteboard or tap **PRESETS** and choose *"Machine Learning Assignment"*.
4. **Observe Transformation**: Watch the kinetic synthesis overlay cycle through `READING YOUR WORLD` ➔ `UNDERSTANDING` ➔ `STRUCTURING` ➔ `I FOUND WHAT MATTERS`.
5. **Interact with the Card**:
   - Check off subtasks (`Implement CNN`, `Train with CIFAR-10`).
   - Swipe right to mark as completed.
   - Tap **REMIND** to schedule an offline notification.
6. **Laptop Companion Sync**: Go to the **SYNC** tab, start the local server, and open the generated IP URL in your laptop browser to view the real-time synced companion view.

---

## 📄 License

GlanceFlow is developed as an open-source ambient productivity prototype. Released under the [Apache 2.0 License](LICENSE).
