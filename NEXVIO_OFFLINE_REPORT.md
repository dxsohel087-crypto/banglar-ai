# NEXVIO Offline Smart Control — Development Report

## Product direction
NEXVIO is an Android voice-operated phone controller. Bengali phrases are mapped to explicit, on-device Android actions.

## Required constraints
- No app backend, cloud AI, API key, or app network permission.
- Keep the source in the existing GitHub repository.
- Build APK with GitHub Actions.
- Keep command history and processing on the device.

## First implementation scope
- Use Android on-device speech recognition only when available; do not silently fall back to online recognition.
- Group command examples by system, each with a question and tappable commands.
- Local actions: flashlight, media volume, Wi-Fi/Bluetooth/Airplane-mode settings pages, display settings, camera, file picker, dialer, home screen, selected installed apps, local clock, and optional Accessibility text reading/clicking.
- Local conversation history and device TTS when Bengali speech data is installed.
- NEXVIO app branding and APK artifact naming.

## Platform limitations
- Offline Bengali speech recognition depends on the device's on-device recognition service and installed Bengali offline language pack.
- Android blocks ordinary apps from silently toggling several protected settings; NEXVIO opens the relevant Settings page.
- Opening an app such as YouTube or Facebook does not make its online content available offline.
- Accessibility is optional and must be enabled by the user. Actions are limited to visible UI elements and direct user commands.
- This is an initial offline command foundation, not a claim that every Android feature can be controlled.

## Verification
- Run GitHub Actions Kotlin compile and APK assembly.
- Install and test on the target Android phone with Wi-Fi and mobile data disabled.
- Verify Bengali offline speech, TTS, permissions, and commands on the actual device.
