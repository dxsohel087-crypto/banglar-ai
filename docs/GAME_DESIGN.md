# Game Design — Bangladesh Real Life Simulator

## Vision
A Bangladesh-focused life simulator in which a player creates a personal digital character and explores a believable village and town. The visual direction is photorealistic/live-action inspired: real-world materials, natural lighting, believable proportions, and natural human motion. No cartoon characters, toy-like proportions, or decorative game-art styling.

## First release: vertical slice
1. A compact village block with a road, homes, pond, fields, and a small market.
2. One controllable player character.
3. Walk, run, turn, and jump.
4. A third-person follow camera.
5. A small set of contextual actions: greet, talk, offer a proposal, sit, eat, sleep, and use a bathroom privately.
6. Record gameplay locally and save a shareable video.

The town map, multiplayer, advanced AI avatar generation, vehicles, and a full Bangladesh map are later milestones, not promises for the first prototype.

## Photo-based avatar
- The player may upload a clear photo of themselves or a person who has explicitly agreed.
- The photo is private by default and must not be used to create a deceptive real-person impersonation.
- First prototype: use a consent-confirmed photo as a profile/identity reference while a standard licensed human rig is used for movement.
- Later: evaluate a face-reconstruction provider and likeness pipeline. Do not claim a single photo can reliably generate a fully animated photorealistic body without additional processing or data.

## World interaction and safety
- Interactions are fictional and opt-in.
- Romantic proposals and social interactions require the other player's acceptance in multiplayer.
- Bathrooms and other intimate activities are represented non-graphically, with privacy boundaries (closed door / fade out); no voyeuristic camera access.
- User-generated recordings should have clear controls and consent protections for other players.

## Performance target
Start with a small, streamed scene and scalable quality settings. Test on actual Android devices before choosing final texture, lighting, and character budgets.
