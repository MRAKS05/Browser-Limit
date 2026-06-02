# Security Policy

## Supported Versions

| Version | Supported          |
|---------|--------------------|
| 1.1.x   | :white_check_mark: |
| < 1.1   | :x:                |

## Reporting a Vulnerability

Browser Limit handles sensitive device operations — Shizuku-based uninstall, Gemini API communication, and parental lock PIN storage. We take security seriously.

**Do not file a public GitHub issue for security vulnerabilities.**

Instead, please report vulnerabilities privately via **GitHub's private vulnerability reporting system**:

1. Go to https://github.com/MRAKS05/Browser-Limit/security/advisories/new
2. Provide a detailed description of the vulnerability
3. Include steps to reproduce and any proof of concept
4. If possible, suggest a fix or mitigation

You can expect an acknowledgment within 72 hours. We will coordinate disclosure and release a fix before making details public.

## What to Report

- PIN storage or authentication bypass
- Unauthorized Shizuku command execution
- Gemini API key exposure or leak
- Insecure data storage (SharedPreferences, databases)
- Any other security-relevant issue

## Scope

The following are in scope:
- The Android application (`com.aistudio.browserlimit.abxyz`)
- The documentation site (https://browser-limit-docs.pages.dev)
- Build and CI pipeline

The following are out of scope:
- Shizuku itself (report to rikka/Shizuku)
- Gemini API (report to Google)
- Third-party library vulnerabilities already addressed in their respective projects
