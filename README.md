# Unstable PvP Auto Spawner — Fabric 1.21.11

**Source project, not a precompiled JAR. Not yet compiled or tested in-game.**

## Dependencies
- Fabric Loader 0.19.5+
- Fabric API 0.141.6+1.21.11
- Carpet PvP Practice release 19+ for Minecraft 1.21.11 (its own mod JAR)
- Java 21+ for this project; Carpet PvP release notes list Java 25+ for release 19, so use Java 25 for the complete setup.

## Behavior
- `/bots start` enables spawning; `/bots stop` disables it.
- Every 2400 server ticks (~2 minutes at 20 TPS), tries to spawn an `smp average` bot near each online player.
- Random horizontal offset 20–35 blocks. Carpet PvP's spawn command may add an additional offset.
- No artificial bot cap. Be careful: bot accumulation can lag/crash servers.
- No bot cleanup, terrain safety validation, or reliable fake-player filtering yet.
- Spawning resets to OFF after server restart.

## Build
Run `gradle build` with an appropriate Gradle version or open the project in IntelliJ IDEA and run the Gradle `build` task. Place the resulting normal JAR (not `-sources.jar`) in the Fabric server `mods` folder alongside the dependencies.

## Integration caveat
The implementation invokes Carpet PvP's `/bot spawn <name> smp average` via `/execute as <player> at <player> positioned ...`. This assumes its command accepts a player executor and uses the executor position. The integration has **not been verified** against Carpet PvP release 19; it may need adaptation. Minecraft 1.21.11 Mojang mappings and command API signatures may also require build fixes.
