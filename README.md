# FFAHotbarManager

Persistent hotbar layouts for a BuildFFA world.

## Status

Source reconstructed from the supplied JAR, prepared for Chillkb’s repository. Java source compilation passed against the available Spigot 1.8.8 API using JDK 17 with `--release 8`. No live server tests or new gameplay features are claimed in this package.

## Existing features

- GUI editor opened by `/ffa hotbar`.
- Reset with `/ffa hotbar reset`.
- Stores player layouts in config.yml and reapplies them after joining, teleporting and periodically in the configured world.

## Requirements

Runtime: Spigot-compatible 1.8.x and a compatible BuildFFA setup.

## Build and install

Follow [BUILDING.md](BUILDING.md), then place the resulting JAR in a test server’s `plugins/` directory and restart. Configure the generated files before running the test checklist. Back up existing configs and player data before replacing an installed plugin.

## Commands

| Command | Purpose |
| --- | --- |
| `/ffa hotbar` | Open the hotbar editor (intercepted player command). |
| `/ffa hotbar reset` | Reset the saved layout. |

## Permissions

No permission nodes declared in plugin.yml; inspect command handlers for access checks.

## Configuration

Default resources are in `src/main/resources/`. Keep live player records and passwords out of GitHub. The default resources in this package came from the uploaded JAR, not your running server.

## Known limitations

BuildFFA interactions and inventory behavior require live testing.

## Quick test

1. Set `world` in config.yml to your BuildFFA world.
2. Run `/ffa hotbar`, choose a layout and click Save.
3. Relog, restart the server and return to FFA; confirm the saved slots persist without duplicated or missing items.
4. Confirm inventories outside the configured world are unaffected.

## Source and credits

See [PROVENANCE.md](PROVENANCE.md) for original metadata, recovery details and credit handling. See [CHANGELOG.md](CHANGELOG.md) for this preparation pass. Only claim features and fixes that you can explain and demonstrate.
