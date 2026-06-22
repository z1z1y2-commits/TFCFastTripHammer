# New Mod NeoForge Template

This project is a reusable NeoForge `1.21.1` starter template pinned to `21.1.219`.

## Included

- Java 21 Gradle workspace
- `ModDevGradle` based NeoForge setup
- Base package `com.z1z1y2.newmod`
- Main mod class `com.z1z1y2.newmod.NewMod`
- Separate client entrypoint
- Empty block, item, and creative tab registries ready for expansion
- GitHub Actions build workflow

## Quick Start

Open the project as a Gradle project in IntelliJ IDEA or Eclipse, then use these commands as needed:

```powershell
.\gradlew.bat runClient
.\gradlew.bat runServer
.\gradlew.bat runData
.\gradlew.bat build
```

> Place the TerraFirmaCraft jar file in a new `libs/` directory at the project root before running Gradle. The build is configured to load `libs/[群峦传说] TerraFirmaCraft-NeoForge-1.21.1-4.1.0.jar` as a local dependency.
>
> Use `.\gradlew.bat runClient` to start the client with TerraFirmaCraft loaded.
>
> Use `.\n> gradlew.bat runClient` to start the client with the TerraFirmaCraft dependency loaded.

## Where To Edit First

- `gradle.properties`: core mod metadata such as version, display name, authors, and package group
- `src/main/templates/META-INF/neoforge.mods.toml`: mod metadata shown by NeoForge
- `src/main/java/com/z1z1y2/newmod`: your Java source entrypoints and registries
- `src/main/resources/assets/newmod`: textures, models, lang, sounds, and other assets
- `src/generated/resources`: data generation output

## Notes

- This template keeps the starter code intentionally clean and does not ship demo blocks or items.
- `TEMPLATE_LICENSE.txt` is still a placeholder. Replace it with your real project license before publishing.
- If your IDE misses dependencies, run `.\gradlew.bat --refresh-dependencies`.

## References

- NeoForged docs: https://docs.neoforged.net/
- ModDevGradle: https://github.com/neoforged/ModDevGradle
