# Validation

Java source compilation passed against the available Spigot 1.8.8 API using JDK 17 with `--release 8`.

- JAR entries and plugin descriptor inspected.
- Only plugin-owned classes were reconstructed; bundled SQLite implementation was not copied as project source.
- Default resources retained; original manifest signatures and compiled artifacts excluded from source.
- Maven POM syntax checked; Maven/Gradle tasks not executed because the build tools/dependency network were unavailable.
- No Minecraft server or live authentication test was run.

BuildFFA interactions and inventory behavior require live testing.
