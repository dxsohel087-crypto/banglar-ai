# Banglar AI — Product Roadmap

## Product principles
- Bengali-first, multilingual by design.
- Premium, calm, accessible interface.
- Fast on entry-level Android devices and low-bandwidth networks.
- Transparent permissions and user control.
- Privacy and security before monetization.
- Free core experience with optional, clearly disclosed paid features later.

## Current prototype
- Kotlin Android app and existing Gradle build.
- Groq chat integration using a user-configured key.
- Bengali speech recognition and text-to-speech.
- Local chat history.
- Floating shortcut.
- Optional user-triggered accessibility bridge.
- Basic phone shortcuts.

## Workstreams

### 1. Experience and interface
- Premium navy / cyan design system.
- Consistent typography, spacing, iconography, and component states.
- Responsive layouts for small and large phones.
- Conversation actions: copy, share, retry, and clear.
- Empty, loading, offline, and error states.
- Dark and light themes.
- Accessibility: scalable text, contrast, TalkBack labels.

### 2. AI platform
- Backend proxy for provider credentials.
- Provider abstraction so models can be changed without app updates.
- Context limits, timeouts, retries, streaming responses, and graceful errors.
- Usage quotas and abuse controls.
- Optional image understanding after a compatible backend is available.

### 3. Voice
- Explicit tap-to-talk as the reliable default.
- Clear microphone state and stop control.
- Background wake-word only after battery, privacy, foreground-service, and Play policy review.
- Do not imply always-listening when the feature is not active.

### 4. User data
- Local-first history by default.
- Optional account and cloud sync only after backend security design.
- Export and delete controls.
- Clear retention and provider-processing disclosures.

### 5. Android integrations
- File picker / Storage Access Framework rather than broad storage access.
- Optional notification access with a clear explanation and explicit opt-in.
- Accessibility only for a narrow, user-requested assistive purpose.
- Use supported Android intents for settings and app launching.

### 6. Release quality
- Unit and instrumentation tests.
- CI build and lint.
- Debug APK artifacts for testing.
- Release APK/AAB signed with protected credentials.
- Play Console declarations, Data safety form, privacy policy, content rating, and store listing.

## Not yet implemented / not yet verified
- Multi-user accounts and cloud sync.
- Banglar AI backend.
- Notification-reading service.
- Full offline language model.
- Production wake-word detection.
- Image upload / vision pipeline.
- Play Store release readiness.
