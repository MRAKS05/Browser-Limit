# BrowserLimit

<p align="center">
  <img src="app_icon.svg" width="128" height="128" alt="BrowserLimit Icon" />
</p>

<p align="center">
  <a href="https://github.com/MRAKS05/Browser-Limit/releases/latest">
    <img src="https://img.shields.io/github/v/release/MRAKS05/Browser-Limit?style=for-the-badge&color=blue&label=Latest%20Release" alt="Latest Release">
  </a>
  <img src="https://img.shields.io/badge/Platform-Android_12+-3DDC84?style=for-the-badge&logo=android" alt="Platform">
</p>

BrowserLimit is an advanced Android utility engineered to enforce digital minimalism by automatically detecting and eliminating web browsers from the device environment. Built upon the Shizuku API for system-level execution and Gemini AI for heuristic package evaluation, BrowserLimit provides a rigorously controlled, focused environment.

## Core Capabilities

- **Automated Rootless Uninstallation**: Utilizes the Shizuku API to execute system-level commands, enabling the silent removal of detected browsers without requiring root access.
- **Heuristic AI Evaluation**: Integrates Google Gemini AI to analyze application semantics, dynamically identifying browsers or unrestricted access tools that bypass static blocklists.
- **System Browser Management**: Specialized protocols for the removal of pre-installed system browsers, ensuring a comprehensive zero-browser ecosystem.
- **Enforced Security Protocols**: Configuration states are secured via cryptographic PIN protection, featuring a mandatory waiting period for configuration changes to deter impulsive bypass attempts.
- **Offline Resilience**: Maintains a local evaluation cache and explicit exception arrays to ensure continuous operation without an active internet connection.
- **Execution Transparency**: Provides a granular audit trail of all system actions, including AI inference rationale and execution outputs.

## Infrastructure

- **Language**: Kotlin
- **Architecture**: Jetpack Compose / MVVM
- **Database**: Room (Local Persistence)
- **Execution**: Shizuku API

## License

This project is licensed under the Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0) License. See the [LICENSE](LICENSE) file for details.

## AI Development Disclosure

This software was developed autonomously by Artificial Intelligence. The human developer acted in a supervisory capacity with minimal direct intervention in the codebase. This project demonstrates the efficacy of AI-driven end-to-end software engineering.
