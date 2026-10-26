# AutoMine SetPoint (Fabric 1.21.11) — source project

This is a Gradle/Fabric source project, not a ready-to-install `.jar`.

## Current prototype features
- J: set corner 1 to the block you're looking at
- K: set corner 2 to the block you're looking at
- B: toggle the mining loop
- Scans selected blocks within a small area and attempts to break the nearest reachable block

## Important limitations
- This prototype does **not** yet draw a visible selection outline.
- It does **not** implement AutoEat.
- It does not have the requested wooden-axe selection/menu UI.
- Auto-mining behavior may not work reliably in all Minecraft versions/servers and can violate server rules. Use only where permitted.

## Build the JAR without installing Gradle on your phone
1. Upload this project to a GitHub repository (create a repository, then upload the files/folders from this ZIP).
2. Open the repository's **Actions** tab and run **Build Fabric mod** (or push to `main`/`master`).
3. Open the completed workflow run and download the artifact named `automine-setpoint-jar`.
4. Extract the downloaded artifact ZIP. Put the resulting `.jar` in the `mods` folder for a Fabric 1.21.11 instance with Fabric API installed.

## Build locally
Use Java 21 and Gradle, then run `gradle build`. The JAR will be in `build/libs/`.
