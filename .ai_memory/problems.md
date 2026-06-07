# AI Memory: Build and Debug Issues

This folder stores recurring development problems so future sessions can avoid repeated debugging loops.

## Known problems

- Gradle wrapper missing in repository; whole-project builds must use system Gradle unless a wrapper is added.
- Build requires Java 17 or later, but the environment may default to Java 11.
- All built APKs should be placed in `.build_output/` and not ignored by Git.
- `.ai_memory/` is the local analysis cache and should remain ignored.
