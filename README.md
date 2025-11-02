# 🌐 Dynamic Internet Shutdown Tracking and Reporting Framework

A **real-time Internet Shutdown Detection and Reporting System** that monitors local connectivity disruptions, verifies them using a cloud-based backend, and reports confirmed shutdowns to a central dashboard.  
Built with **FastAPI**, **Python**, and an **Android mobile app**, this project empowers users and researchers to detect and track Internet disruptions dynamically across regions.

---

---

## 🚀 Introduction

During government-imposed or unintentional Internet shutdowns, users and journalists often struggle to verify if the outage is **local**, **regional**, or **nationwide**.  
This framework provides **dynamic, decentralized, and real-time detection** using mobile devices and edge nodes that monitor network connectivity, packet loss, and latency in the background.

Our **Android app** detects anomalies in Internet connectivity and sends structured telemetry to the **FastAPI backend**, which:
- Aggregates reports from multiple devices
- Cross-validates disruptions across regions
- Notifies a central dashboard for real-time visualization

This allows organizations and researchers to **accurately track, verify, and respond** to shutdown events.

---

## ⚙️ Features

### 📱 Android App (`src/`)
- Detects and logs Internet connectivity status.
- Measures latency, DNS resolution, and packet loss.
- Sends shutdown reports to the FastAPI backend.
- Operates with minimal battery and data usage.

### 🧠 Backend API (`test_ping/`)
- Built with **FastAPI** for high-performance, async endpoints.
- Verifies incoming reports using independent network checks.
- Stores validated disruptions in a structured database.
- Provides REST APIs for dashboard integration and analytics.

### 🤖 Chatbot Interface
- Implemented via `chatbot.py` and `prompts.yaml`.
- Uses LLM to:
  - Query historical shutdowns.
  - Explain trends and causes.
  - Interact with backend data through natural language.

---

## 🧩 Tech Stack

| Component | Technology |
|------------|-------------|
| **Mobile Client** | Android (Java / Kotlin) |
| **Backend API** | FastAPI (Python 3.10+) |
| **Database** | PostgreSQL / SQLite |
| **Networking** | ICMP, Ping, DNS, HTTP checks |
| **Chatbot** | Python + OpenAI LLM |
| **Environment Management** | `.env` for secrets |
| **Version Control** | Git & GitHub |

---

## 🧠 Architecture Overview
+-------------------+ +--------------------+ +----------------------+
| Android App (src) | ---> | FastAPI Backend | ---> | Dashboard / Database |
| - Detects issues | | (test_ping/) | | - Data visualization |
| - Sends reports | | - Verifies reports | | - Analytics, alerts |
+-------------------+ +--------------------+ +----------------------+


---

## 🛠️ Setup Instructions

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/sarankumar007/Dynamic-Internet-Shutdown-Tracking-and-Reporting-Framework.git
cd Dynamic-Internet-Shutdown-Tracking-and-Reporting-Framework

2️⃣ Set Up Backend (FastAPI)
cd test_ping
pip install -r ../requirements.txt
cp ../secrets.env .env
uvicorn main:app --reload

3️⃣ Run Android App

Open the src folder in Android Studio.

Connect your Android device or start an emulator.

Build and run the app.

4️⃣ Run Chatbot (Optional)
python chatbot.py

📊 Future Enhancements

🌍 Interactive map-based dashboard (React / Next.js)

🛰️ Distributed global probe network for verification

📡 Integration with RIPE Atlas and Cloudflare Radar

🔔 Push notifications for verified shutdowns

📈 AI-based pattern recognition for anomaly detection

❤️ Acknowledgments

Open Observatory of Network Interference (OONI) – For open datasets and inspiration.

FastAPI & Android Developer Docs – For framework integration and reference examples.

⭐ Support

If you find this project useful, please give it a ⭐ on GitHub!
Contributions, issues, and pull requests are warmly welcomed.











