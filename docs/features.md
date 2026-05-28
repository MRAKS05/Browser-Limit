# Features & Security Model

## Core Features

- **Rootless Uninstall**: Uses Shizuku to uninstall browsers without root.
- **AI Heuristics**: Google Gemini integration for semantic detection of browsers and bypass tools.
- **System Browser Management**: The only effective way to fully remove pre-installed browsers on most Androids.
- **Parental Lock**: PIN-protected configuration with cooldown.
- **Offline Cache & Exception Lists**: Works reliably even without internet.
- **Audit Trail**: Logs all removals, AI reasoning, and admin actions.

## Privacy

- **No Data Sent to Cloud**: All analysis and actions are local unless you enable Gemini (uses Google APIs).
- **Cryptographic Operations**: Parental lock PINs are hashed. No plaintext PINs stored.

See [API & Internals](api.md) for code-level reference.
