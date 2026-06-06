# BrowserLimit

<p align="center">
  <img src="app_icon.svg" width="128" height="128" alt="BrowserLimit Icon" />
</p>

<p align="center">
  <a href="https://github.com/MRAKS05/Browser-Limit/releases/latest">
    <img src="https://img.shields.io/badge/Latest%20Release-v1.3.0--beta--2-blue?style=for-the-badge" alt="Latest Release">
  </a>
  <a href="https://browser-limit-docs.pages.dev/">
    <img src="https://img.shields.io/badge/Documentation-8A2BE2?style=for-the-badge&logo=gitbook&logoColor=white" alt="Documentation">
  </a>
  <img src="https://img.shields.io/badge/Platform-Android_12+-3DDC84?style=for-the-badge&logo=android" alt="Platform">
  <a href="https://github.com/MRAKS05/Browser-Limit/actions/workflows/build.yml">
    <img src="https://img.shields.io/badge/Build-GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions" alt="Build">
  </a>
</p>

BrowserLimit is an advanced Android utility engineered to enforce digital minimalism by automatically detecting and eliminating web browsers from the device environment. Built upon the Shizuku API for system-level operations, it combines automated detection with AI-powered analysis to maintain a browser-free environment.

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

## Build from Source

```bash
./gradlew :app:assembleRelease
```

The unsigned APK will be at `app/build/outputs/apk/release/app-release-unsigned.apk`. Sign it with any Android keystore before installing.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## AI Development Disclosure

This software was developed autonomously by Artificial Intelligence. The human developer acted in a supervisory capacity with minimal direct intervention in the codebase. This project demonstrates the capabilities of AI-driven software development.
