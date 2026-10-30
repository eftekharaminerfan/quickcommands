# Quick Commands (Fabric, Minecraft 26.1.2)

Bind any chat command (`/rtp`, `/skill`, `/home`, ...) to a key. Press the key and the command runs instantly.
Client-side only, so it works on any server (the server needs no mod).

## Requirements
- Minecraft Java 26.1.2
- Fabric Loader 0.19.3+ and Fabric API 0.155.0+26.1.2
- Java 25 (to build)

## Build
```
./gradlew build
```
The jar is created at `build/libs/quickcommands-1.0.0.jar`. Put it in `.minecraft/mods/`.

## Use
1. In game, set a command for a slot (works with or without the slash):
   - `/quickcmd set 1 rtp`
   - `/quickcmd set 2 skill`
2. Open **Options > Controls > Key Binds > Quick Commands** and give "Command Slot 1", "Command Slot 2", ... a key.
3. Press the key in game. The command runs instantly.

Other commands: `/quickcmd list`, `/quickcmd clear <slot>`, `/quickcmd reload`.

There are 12 slots. Commands are stored in `.minecraft/config/quickcommands.json`; you can edit that file and use `/quickcmd reload`.
