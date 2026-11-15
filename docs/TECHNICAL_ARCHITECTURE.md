# Technical Architecture

## Runtime
Unity (Android target) is proposed for the real-time interactive prototype. Use a realistic asset pipeline: photogrammetry/scanned references, physically based materials, realistic lighting, and licensed motion-captured human animation. This does not mean cartoon or stylized 3D visuals.

## Modules
- **World:** small village scene first; town scene later. Stream only nearby content.
- **Player:** CharacterController movement, touch joystick, contextual action system.
- **Camera:** third-person follow camera with collision handling.
- **Avatar:** consent-based photo upload; initial implementation stores a private reference image and assigns a licensed rigged character. Photorealistic face reconstruction is a separate future service.
- **Recording:** Android MediaProjection / Unity-supported capture path; request permission explicitly and save to app media.
- **Accounts/backend:** add authentication and cloud profile storage only after the offline prototype is stable. Never put API keys or secrets in the app or repository.
- **Privacy:** photo deletion, upload consent, access controls, recording indicators, and safe multiplayer defaults.

## Milestones
1. Clean project foundation and player/camera prototype (current).
2. Unity project settings, touch controls, and a greybox test scene.
3. Realistic village assets and one rigged character.
4. Contextual actions and animation state machine.
5. Photo upload/profile workflow and privacy controls.
6. Recording/export and Android device testing.
7. Town expansion and multiplayer.

## Build status
No Android APK or successful Unity build is included in this commit. Unity Editor and Android Build Support are required to produce and test an APK.
