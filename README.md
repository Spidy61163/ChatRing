# 📱 Chatting App

A modern real-time **chatting application** built with **Java** in Android Studio.  
It supports **one-to-one chats**, **group chats**, and **audio/video calls** powered by [Agora](https://www.agora.io/).  
The backend and data storage are powered by **Google Firebase**.

---

## ✨ Features

### 💬 Chatting
- **One-to-One Chat** – Send and receive messages instantly.
- **Group Chat** – Create and join groups (text & media only; calls not yet available).
- **Message Reactions** – React to messages with emojis.
- **Media Sharing** – Send images alongside text messages.
- **Status Updates** – Share your status with contacts.

### 📞 Calls
- **Audio Calls** – Crystal-clear real-time audio.
- **Video Calls** – High-quality video calling.
- **Agora API Integration** – Low-latency and reliable call experience.

> 🔹 **Note:** Group calls are not available yet — calls are limited to one-on-one chats.

### ☁️ Cloud Integration
- **Firebase Authentication** – Secure login & signup.
- **Firebase Realtime Database** – Store and sync messages instantly.
- **Firebase Storage** – Host images, media, and status updates.

---

## 🛠️ Tech Stack

- **Frontend:** Java (Android Studio)
- **Backend / Services:**
  - [Firebase](https://firebase.google.com/) (Authentication, Realtime Database, Storage)
  - [Agora](https://www.agora.io/) (Audio & Video calls)
- **Architecture:** MVVM (Model-View-ViewModel) *(recommended for maintainability)*
- **Networking:** Retrofit (for API integration, if required)
- **UI:** Material Design components

---


---

## 🚀 Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) installed.
- A valid **Firebase** project configured.
- An **Agora** developer account and App ID.

### Setup Steps
1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/chatting-app.git
   cd chatting-app

2. **Open in Android Studio**
   - Launch Android Studio.  
   - Go to `File → Open` and select the project folder.

3. **Firebase Setup**
   - Create a Firebase project in the [Firebase Console](https://console.firebase.google.com/).  
   - Enable **Authentication**, **Realtime Database**, and **Storage**.  
   - Download the `google-services.json` file and place it in the `app/` folder of the project.

4. **Agora Setup**
   - Sign in at the [Agora Console](https://console.agora.io/).  
   - Create a new App and obtain the **App ID**.  
   - Add your Agora App ID into the app’s configuration file or constants.

5. **Build & Run**
   - Connect your Android device or start an emulator.  
   - Click **Run ▶** in Android Studio to build and start the app.

## 📌 Roadmap

- ✅ One-on-one audio & video calls  
- ✅ Text, image sharing, reactions  
- ✅ Group text chat  
- 🔜 Group audio/video calls  
- 🔜 Message read receipts  
- 🔜 Push notifications  
