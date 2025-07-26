Project Kisan — AI-Powered Personal Farming Assistant

**Google Agentic AI Day 2025 Project**

Project Kisan is an Android app that empowers small-scale farmers by combining AI, voice-first interaction, and real-time market intelligence.


## ✅ Key Features:
- **Voice-Based Commodity Queries (Speech-to-Text)**
- **Real-Time Market Price Analysis (Ktor API + Clean Architecture)**
- **Farming-Inspired Modern UI (Jetpack Compose)**
- **Firebase Analytics Integrated (Ready for Authentication & Crashlytics)**

---

## ✅ Technology Stack:
| Layer       | Technology                          |
|-------------|------------------------------------|
| UI          | Jetpack Compose, Material 3 Theme   |
| Architecture| MVVM + Clean Architecture + Hilt    |
| Networking  | Ktor, Kotlinx Serialization         |
| Voice Input | Android SpeechRecognizer API        |
| Backend     | Firebase (Analytics, Auth Ready)    |


## ✅ App Flow:
1. App launches directly to **Market Price Screen**.
2. User taps **"Speak Commodity Name"** → Speaks crop name.
3. App transcribes speech → Auto-fills TextField.
4. User taps **Fetch Market Price** → API fetches price via Ktor.
5. Price displayed with beautiful farming-themed UI.
   

## ✅ Future Scope:
- Vertex AI Speech-to-Text for advanced voice input.
- Google Sign-In / Phone Authentication (Firebase).
- Crop Disease Detection via Gemini Multimodal Models.
- Government Scheme Recommendations via AI Agents.

---

## ✅ Setup Instructions:
1. Clone the repository.
2. Add your `google-services.json` in `/app`.
3. Update Ktor API endpoints as needed.
4. Build & run on Android Studio (API 24+).

---

## ✅ License:
MIT License.
