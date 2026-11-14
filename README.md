# 🇧🇩 Banglar AI

**A Bengali-first, multilingual AI assistant for everyone.**

Banglar AI is an Android assistant project built with Kotlin and Android native APIs. The project is being developed from its existing Android application and GitHub Actions build pipeline.

## Current application foundation

- Native Android app (Kotlin)
- Groq-compatible chat completion integration
- Bengali speech recognition and text-to-speech
- Local chat history on the device
- Optional floating assistant bubble
- Explicit, user-triggered accessibility actions for reading visible screen text or clicking a visible item
- Flashlight, camera, gallery, and Android settings shortcuts
- API key encrypted at rest with Android Keystore on the device
- GitHub Actions debug APK build

## Build

The GitHub Actions workflow builds a debug APK on pushes to `main` / `master` and on manual dispatch. Open **Actions → Banglar AI APK Build** to inspect build results and download the workflow artifact.

## Important: public-release readiness

The current application is a development prototype, **not yet ready for public Play Store release**.

Before public launch, the project must:
1. Move shared AI credentials behind a secured backend. Never ship a shared provider key in an APK.
2. Add a privacy policy, in-app privacy disclosures, data deletion controls, and a support contact.
3. Review every Android permission and foreground-service use against current Google Play policies.
4. Implement secure multi-user authentication only if cloud accounts are genuinely needed.
5. Add rate limiting, abuse prevention, cost controls, and server-side validation.
6. Add automated tests, release signing, versioning, accessibility checks, and device testing.
7. Clearly disclose that accessibility actions are optional and user-triggered.

## Privacy notes for the current prototype

- Chat history is stored in app-private local preferences on the device.
- The user-provided Groq API key is encrypted at rest using Android Keystore.
- When a user sends a prompt, the app sends the conversation context to the configured Groq API endpoint.
- The current prototype does not yet include a Banglar AI account system or a Banglar AI-operated cloud backup service.
- Accessibility is an optional, user-enabled feature; screen text is inspected only when an explicit command invokes it.

Review the policy draft in `docs/PRIVACY_POLICY_DRAFT.md` before any public distribution.
