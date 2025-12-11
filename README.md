# Scene Assist

Scene Assist is an accessibility-focused Android application designed to help visually impaired users understand their surroundings through camera-based scene description and question-answering capabilities.

## Features
- **Real-time Camera Preview**: Continuous frame analysis using CameraX
- **Scene Description**: AI-powered descriptions of captured scenes
- **Voice Interaction**: Speech-to-text for questions and text-to-speech for responses
- **Image Enhancement**: 4x super-resolution using ESRGAN TensorFlow Lite model
- **Smart Visibility Check**: Pre-validates if requested information is visible in frame

## Architecture

The app follows a two-activity architecture with a five-stage image processing pipeline:

### Core Components

| Component             | File                       | Purpose                                      |
| --------------------- | -------------------------- | -------------------------------------------- |
| `MainActivity`        | `MainActivity.java`        | Camera preview, frame capture, speech input  |
| `DescribeSceneWindow` | `DescribeSceneWindow.java` | AI processing orchestration, results display |
| `ESRGANEnhancer`      | `ESRGANEnhancer.java`      | 4x image super-resolution                    |
| `GlobalBitmap`        | `GlobalBitmap.java`        | Static bitmap storage between activities     |
| `BitmapUtils`         | `BitmapUtils.java`         | ImageProxy to Bitmap conversion              |

### Data Flow

<img width="1170" height="304" alt="Screenshot 2025-12-10 184423" src="https://github.com/user-attachments/assets/7ad1dd3d-0ae3-4be5-8cb2-e5312df401c5" />

## Project Structure

```
app/src/main/java/com/example/scenceassist/
├── MainActivity.java              # Main camera interface
├── DescribeSceneWindow.java       # AI processing & results
├── ESRGANEnhancer.java           # Image enhancement
├── GlobalBitmap.java             # Shared bitmap storage
└── BitmapUtils.java              # Image conversion utilities
```

The application is built using Java with a package structure under `com.example.scenceassist` and contains 5 main Java files:

### 1. **MainActivity.java** - Primary Camera Interface
This is the launcher activity that implements the camera preview and image analysis pipeline

**Key Responsibilities:**
- Manages camera permissions and initialization
- Implements `ImageAnalysis.Analyzer` to continuously capture camera frames 
- Handles two primary user interactions: "Describe" and "Ask" buttons
- Implements Text-to-Speech (TTS) for audio feedback
- Performs visibility checking using Gemini API before proceeding to scene description

**User Flow Handling:**
- **Describe Mode**: Captures current frame and navigates to DescribeSceneWindow with a default prompt 
- **Ask Mode**: Uses Speech-to-Text to capture user's question, then validates if the queried object is visible in frame using Gemini AI, providing directional guidance if partially visible

### 2. **DescribeSceneWindow.java** - Results Display Activity
This secondary activity displays the captured image and AI-generated description

**Key Responsibilities:**
- Retrieves the captured bitmap from GlobalBitmap
- Enhances image quality using ESRGAN before sending to AI 
- Sends enhanced image to Gemini API with user's question or default prompt
- Displays results both visually and via TTS
- Allows follow-up questions through a floating action button

### 3. **ESRGANEnhancer.java** - Image Enhancement Utility
Implements Real-ESRGAN for 4x super-resolution image enhancement using TensorFlow Lite

**Key Responsibilities:**
- Loads the TFLite model from assets
- Performs offline 4x upscaling of input images
- Converts bitmaps to normalized float tensors for model input
- Converts model output back to ARGB bitmap
- Falls back to original image if enhancement fails

### 4. **GlobalBitmap.java** - State Management
A simple singleton pattern for sharing bitmap data between activities

**Purpose:**
- Provides static getter and setter methods to pass captured frames between MainActivity and DescribeSceneWindow without using Intent extras (which have size limitations)

### 5. **BitmapUtils.java** - Image Conversion Utility
Contains utility methods for converting CameraX ImageProxy objects to Bitmap format

**Note:** This utility appears to be unused in the current implementation, as MainActivity uses `imageProxy.toBitmap()` directly

## Key Implementation Details

### Camera Integration
- Uses CameraX API with `ImageAnalysis` for continuous frame processing
- Implements `ImageAnalysis.Analyzer` interface for frame capture
- Background executor prevents UI blocking 

### AI Processing Pipeline
1. **Image Enhancement**: ESRGAN model for 4x upscaling
2. **Gemini AI Integration**: Scene analysis with custom prompts
3. **Response Processing**: Text cleaning and TTS output

### Threading Model
- **Camera Thread**: Frame analysis and conversion
- **AI Executor**: Image enhancement and API calls
- **UI Thread**: Display updates and user interactions

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 21+ (Android 5.0)
- Physical Android device with camera

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/manavukani/scene-assist.git
   cd scene-assist
   ```

2. **Configure API Keys**
   - Replace `API_KEY_HERE` in `MainActivity.java` line 167
   - Replace `API_KEY_HERE` in `DescribeSceneWindow.java` line 104
    
3. **Build and Run with Android Studio**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

## Usage

1. **Describe Scene**: Tap "Describe" button for automatic scene description
2. **Ask Questions**: Tap "Ask" button and speak your question about the scene
3. **Voice Guidance**: The app provides audio feedback for all interactions

## Dependencies and APIs
- **Core Android Libraries**
	- AndroidX AppCompat & Material Design
	- AndroidX ConstraintLayout
-  **Camera Functionality**
	- CameraX Libraries (version 1.3.4) for modern camera implementation
- **Machine Learning & AI**
	- TensorFlow Lite (version 2.14.0) for offline image enhancement
	- TensorFlow Lite Support (version 0.4.4) for additional utilities
	- Google Generative AI SDK (version 0.1.2) for Gemini API integration
- **Utilities**
	- Google Guava (version 31.1-android) for concurrent utilities and ListenableFuture support 
- **Android System APIs**
	- Speech Recognition (RecognizerIntent) for voice input
	- Text-to-Speech for audio output

### Required Permissions
- **CAMERA**: For capturing live camera feed
- **RECORD_AUDIO**: For speech-to-text functionality
- **READ_EXTERNAL_STORAGE**: For accessing media
- **WRITE_EXTERNAL_STORAGE**: For devices below Android 10
