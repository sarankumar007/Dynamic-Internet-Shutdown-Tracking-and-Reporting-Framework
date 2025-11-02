# Dynamic Internet Shutdown Tracking and Reporting Framework

A real-time Internet shutdown detection and reporting system monitors local connectivity disruptions, verifies them using a cloud-based backend, and reports confirmed shutdowns to a central dashboard. Built with FastAPI, Python, and an Android mobile app, this project helps users and researchers track Internet disruptions across regions.

## Introduction

During government-imposed or unintentional Internet shutdowns, users and journalists often struggle to verify if the outage is local, regional, or nationwide. This framework provides dynamic, decentralized, real-time detection using mobile devices and edge nodes that monitor network connectivity, packet loss, and latency in the background.

Our Android app detects anomalies in Internet connectivity and sends structured data to the FastAPI backend, which:
- Aggregates reports from multiple devices
- Validates disruptions across regions
- Notifies a central dashboard that supports user interactions with an AI bot

This allows organizations and researchers to track, verify, and respond to shutdown events.

## Features

### Android App (`src/`)
- Detects and logs Internet connectivity status.
- Measures latency, DNS resolution, and packet loss.
- Sends shutdown reports to the FastAPI backend.
- Operates with minimal battery and data usage.
- <img width="300" height="700" alt="image" src="https://github.com/user-attachments/assets/61398b79-074d-4a32-9033-1077698e98b8" />
- <img width="300" height="700" alt="image" src="https://github.com/user-attachments/assets/cfc22b9c-e7d6-452e-9130-9c0fa69b774d" />

### Backend API (`test_ping/`)
- Built with FastAPI endpoints.
- Allows for manual checks by admins before confirming shutdowns.
- Stores validated disruptions in a structured database.
- Provides REST APIs for dashboard integration and analytics.
- <img width="1086" height="598" alt="image" src="https://github.com/user-attachments/assets/f98462f7-1932-4d2e-83c6-45c111c3798b" />

### Chatbot Interface
- Implemented via `chatbot.py` and `prompts.yaml`.
- Uses LLM to:
  - Query historical shutdowns.
  - Explain trends and causes.
  - Interact with backend data through natural language instead of SQL queries.
  - <img width="966" height="544" alt="image" src="https://github.com/user-attachments/assets/ef9ffcab-39f7-4810-b4b5-4e2733e4aacf" />


## Tech Stack

| Component | Technology |
|------------|-------------|
| Mobile Client | Android (Java / Kotlin) |
| Backend API | FastAPI (Python 3.10+) |
| Database | Render(for hosting) and PostgreSQL|
| Networking | ICMP, Ping, DNS, HTTP checks |
| Chatbot | Python + OLlama LLM |
| Cloud instance | Oracle VM |
| Environment Management |.env for secrets |
| Version Control | Git & GitHub |

## Architecture Overview
<img width="1047" height="1600" alt="image" src="https://github.com/user-attachments/assets/bdaa91a0-f249-48f7-b57b-78d27c1cd5ff" />


## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/sarankumar007/Dynamic-Internet-Shutdown-Tracking-and-Reporting-Framework.git
cd Dynamic-Internet-Shutdown-Tracking-and-Reporting-Framework
```

### 2. Set Up Backend (FastAPI)
```bash
cd test_ping
pip install -r ../requirements.txt
cp ../secrets.env .env
uvicorn main:app --reload
```

### 3. Run Android App

Open the src folder in Android Studio. Connect your Android device or start an emulator. Build and run the app.

### 4. Run Chatbot (Optional)
```bash
python chatbot.py
```

## Future Enhancements

- Interactive map-based dashboard (React / Next.js)
- Hosting the chatbot in a cloud based VM
- Distributed global probe network for verification
- AI-based pattern recognition for anomaly detection

## Acknowledgments

Open Observatory of Network Interference (OONI) – For open datasets and inspiration.

FastAPI & Android Developer Docs – For framework integration and reference examples.
