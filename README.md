# Voxel Craft — 3D First-Person Voxel Survival Sandbox for Android (v1.1)

## 1. Project Overview & Architecture
**Voxel Craft v1.1** is a complete, native first-person 3D voxel sandbox survival game developed for Android. It features a high-performance procedural Voxel engine built with OpenGL ES 2.0 (`GLSurfaceView`) paired seamlessly with Jetpack Compose for the responsive touch HUD, inventory, crafting table, chest containers, villager trading, creator tools, settings, mod browser, and central player account database.

- **Package**: `com.example.voxel`
- **Application ID**: `com.aistudio.voxelcraft.bqwz`
- **Version**: `1.1` (versionCode 2)
- **Minimum SDK**: Android 24+ (Nougat) | **Target SDK**: Android 36
- **Architecture**: Kotlin + Android OpenGL ES 2.0 (Direct VBO / Vertex Arrays with chunk meshing and hidden-face culling) + Jetpack Compose M3 UI.

---

## 2. New in Version 1.1: Mod Engine, Marketplace & Account System

### 🌐 Mod Browser & Online Repository (https://menafex.xo.je/index)
- **Embedded Web Browser**: Interactive in-game WebView pointed directly to `https://menafex.xo.je/index` with JavaScript, DOM storage, zoom, and live reload controls.
- **1-Click Mod Marketplace**: Pre-loaded catalog of popular community mods from the site with instant one-click download & activate buttons.
- **Unified Mod Runtime**:
  - **Bedrock Mod Engine**: Parses `.mcaddon`, `.mcpack`, `manifest.json`, Bedrock JSON blocks (`minecraft:block`), items (`minecraft:item`), and entities (`minecraft:entity`).
  - **Java Mod & DataPack Engine**: Parses `.jar`, `fabric.mod.json`, `pack.mcmeta`, shaped/shapeless JSON recipes, and custom tools.
- **In-World Integration**: Installed mod contents appear physically inside the 3D world:
  - **Lucky Block Mod**: Golden glowing Lucky Blocks that drop surprise gifts (Ruby Gems, Lucky Pickaxes, iron, or spawn mobs).
  - **Ruby Ores & Legendary Weapons**: Deep underground Ruby Ore, Ruby Gems, 8x speed Ruby Pickaxes, and 9.5-damage Mythic Ruby Swords.
  - **DecoCraft & Modern Furniture**: Velvet Sofas, Kitchen Fridges, and Tables.
  - **Mythical Dragons & Flying Bosses**: 45 HP Crimson Fire Dragons roaming the skies and attacking in melee range.

### 🎨 Texture Converter & Shaders
- Dynamic palette converter that re-meshes the 3D world without reload:
  - **Vanilla Classic** (16x16 standard palette)
  - **Faithful HD** (32x32 high-saturation contrast)
  - **Fantasy Medieval** (32x32 warm rustic autumnal tones)
  - **Cyber Voxel** (64x64 neon vibrant highlights)

### 🗺️ Adventure Maps & Worlds Importer
- **Medieval Fortress & Village**: Fortified settlement with castle walls and torch battlements.
- **SkyBlock Challenge**: Floating 4x4 dirt island in the sky with a single tree and chest.
- **Survival Island 1.1**: Isolated sandy tropical island surrounded by ocean.

### 👤 Central Player Account & Database System (Stage 14)
- **Authentication**: User registration and login with SHA-256 hashed credentials.
- **Player Stats**: Tracks Blocks Mined, Blocks Placed, Mobs Defeated, Player Level, and High Scores.
- **Skin Selector**: 6 custom 3D player skins:
  - Steve the Miner, Alex the Explorer, Iron Knight, Shadow Ninja, Arcane Wizard, and Cyber Miner.
- **Central Persistence**: Atomic JSON database storing user profiles and world cloud states.

---

## 3. Core Survival Gameplay & World Engine

### A. Procedural Continuous Voxel World
- **Chunk System**: 16x48x16 chunks streamed dynamically around the player.
- **Hidden Face Culling & Ambient Occlusion (AO)**: Only exposed block faces are meshed, reducing rendered geometry by ~85% for a stable 60 FPS on mobile.
- **Biomes & Landscape**: Grassy plains, oak forests, sandy beaches with water level at Y=14, stone mountain peaks, and natural subterranean caves with Coal and Iron ores.
- **Starting Area & Village**:
  - Deterministic default Seed (`133742`) spawns the player safely on a grassy scenic bluff overlooking the village, water lake, nearby trees, and starter cave.
  - Fully formed village with 4 distinct buildings, gravel pathways, a central well with water, and living villagers.

### B. Mobile Touch Controls & First-Person Physics
- **Left Virtual Joystick**: Smooth movement forward/backward and strafe.
- **Right Viewport Drag**: Direct 360-degree look rotation (Yaw & Pitch) with configurable sensitivity.
- **Action Buttons**:
  - Jump (Smooth stepping up 0.5-height blocks and jump up full blocks).
  - Mine / Attack (Hold to mine with tool-based speed multipliers, progress indicator, particles, and haptic vibration).
  - Place / Interact (Tap to place held block, open Crafting Table, open Chests, or trade with Villagers).
- **Collision Detection**: Full 3D AABB bounding box collision preventing the player from clipping into voxels or placing blocks inside themselves.
- **Water Physics**: Buoyancy and swimming mechanics when wading into water.

### C. Survival Loop, Inventory & Crafting
- **9-Slot Hotbar**: Accessible at bottom of screen with direct touch selection.
- **36-Slot Player Inventory**: Drag-and-drop / tap-to-swap and split stack operations.
- **Crafting Recipes**:
  - Oak Logs → 4 Oak Planks | 2 Oak Planks → 4 Sticks
  - 4 Oak Planks → Crafting Table | 1 Coal + 1 Stick → 4 Torches
  - 8 Oak Planks → Storage Chest | Wooden & Stone Pickaxes / Axes / Swords
  - Lucky Blocks (4 Planks + 1 Torch) | Velvet Sofas (2 Wool + 2 Planks) | Ruby Swords (2 Cobblestone + 1 Stick)
- **Storage Chests**: 27-slot persistent chest storage with tap-to-deposit and tap-to-withdraw.
- **Health & Damage**: 10 hearts (20 HP), fall damage calculations, zombie melee damage, red damage flash vignette, and full respawn lifecycle.

### D. Living Mobs & AI
- **Animals**: Sheep, Cows, Pigs with 3D blocky models, wandering AI, sounds, and meat/wool drops.
- **Villagers**: Animated walking between village homes, fleeing from hostile zombies, and opening resource trade dialogs when interacted with.
- **Zombies**: Spawning during nighttime or dark areas, actively tracking and pursuing the player and villagers, attacking within melee range.
- **Fire Dragons**: Flying winged drakes dropping Ruby Gems.
- **Day / Night Cycle**: Dynamic celestial orbit with Sun and Moon, color transitions (Dawn → Noon → Sunset → Deep Midnight), and torch point illumination.

### E. Creator Tools & Recording Suite
- Time-of-day slider (Dawn, Noon, Sunset, Midnight scrub).
- Free flight / camera mode toggle.
- Invulnerability (God Mode) toggle.
- Mob Spawner (Spawn Sheep, Cow, Pig, Villager, Zombie, Dragon right in front of player).
- Quick builder starter kit grant.
- Teleport shortcuts (Scenic Spawn, Village Center, Starter Cave).
- HUD Toggle for clean cinematic recording.
