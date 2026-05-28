# BrowserLimit

<p align="center">
  <img src="app_icon.png" width="128" height="128" alt="BrowserLimit Icon" />
</p>

[![Release](https://img.shields.io/github/v/release/yourusername/BrowserLimit?label=Release&style=flat-square)](#) 
[![Platform](https://img.shields.io/badge/Platform-Android_12+-3DDC84?style=flat-square&logo=android)](#)

BrowserLimit is an advanced, uncompromising Android utility engineered to enforce strict digital minimalism by automatically detecting and eliminating web browsers from your device.

Built on top of Shizuku for system-level execution and Gemini AI for intelligent package evaluation, BrowserLimit transforms any standard Android device into a rigorously locked-down, focused environment. 

## Core Capabilities

- **Automated Rootless Uninstallation (Shizuku)**
  Operates at the system level via Shizuku (ADB Wrapper). When a browser is detected, it is immediately and silently uninstalled (`pm uninstall -k --user 0`) without requiring root access or user interaction.
  
- **Heuristic AI Package Evaluation (Gemini)**
  Integrates Google's Gemini AI model to evaluate newly installed applications. The engine analyzes package semantics to determine if an app is a dedicated browser or designed for unrestricted open internet browsing, dynamically detecting workarounds that bypass static blocklists.
  
- **System Browser Eradication**
  Equipped with specialized handling to remove immutable, pre-installed system browsers like Google Chrome, ensuring a strict zero-browser ecosystem.
  
- **Enforced Parental Lock with Smart Delay**
  Configuration states and exception lists are cryptographically secured behind a PIN. Disabling the lock dynamically triggers a mandatory waiting period, drastically deterring impulsive bypass attempts.

- **Offline Inference & Exception Bypasses**
  Maintains a comprehensive local evaluation cache and explicit exception arrays, ensuring uninterrupted automated uninstallation sequences even without internet access.
  
- **Cryptographic & Execution Transparency**
  Maintains an auditable, granular execution database. Each event logs exact package identities, Gemini inference rationale, execution methods, and ADB shell outputs for complete system transparency.

## Infrastructure

- **Language:** Kotlin
- **Architecture:** Jetpack Compose / MVVM
- **Database:** Room Database (Local Persistence)
- **Execution:** Shizuku API (System-level management)
