# Bangladesh Life Simulator — Prototype

This is a separate Unity game prototype inside the existing repository. It does not modify the Banglar AI Android app.

## First playable milestone
- Third-person human character controller using Unity's CharacterController.
- On-screen virtual joystick for Android movement.
- Camera-relative walking and running.
- Jump action and optional Animator parameters.
- A small, original Bangladesh-inspired village test scene will be added after the controller is verified.

## Recommended setup
- Unity 2022.3 LTS or newer LTS release.
- 3D (URP) project.
- Android Build Support (SDK, NDK and OpenJDK modules).
- Add the scripts under `Assets/BangladeshLifeSimulator/Scripts/`.
- Create a Player GameObject with a CharacterController, Animator (optional), and PlayerMovementController.
- Add a Canvas with a joystick background/handle and attach SimpleMobileJoystick to the joystick root.
- Assign the joystick and camera transform in the Inspector.
- Add UI Buttons and connect their OnClick events to `PlayerMovementController.Jump` and `SetRunning(true/false)` (use Pointer Down/Up event triggers for running).

## Important scope notes
The initial milestone is a gameplay foundation, not a finished game or APK. Realistic photo-based avatars, online multiplayer, a full Bangladesh map, NPC social interactions, and video export require later dedicated systems and testing. Uploaded face photos must be used only with the person's permission and stored securely.

## Performance target
Start with a small village block and scalable URP settings. Profile on a real Android device before increasing world size or visual quality.
