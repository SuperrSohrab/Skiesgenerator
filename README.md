# SkiesGenerator

**Description:** Lightweight Bukkit/Paper plugin that provides upgradable, configurable item generators with GUI management, economy upgrades, and protections to prevent abuse.

## Installation

- Build: `mvn -DskipTests package`
- Install: place the generated JAR (`target/skiesgenerator-1.jar`) into your server `plugins/` folder and restart the server.

## Configuration (`config.yml`)

- `drop-mode`: `collect` or `block` — whether drops are collected into the generator or spawned on the block. Default: `collect`.
- `max-storage`: Maximum items stored per generator (collect mode). Default: `64`.
- `generation-interval`: Ticks between item generation (20 ticks = 1s). Default: `100`.
- `max-generators-per-player`: Max placeable generators per player (0 = unlimited). Default: `5`.
- `generator-pickup-delay`: Pickup delay for block-mode spawned items. Default: `40`.
- `use-vault`: Enable economy integration (Vault). Default: `true`.
- `sellable-only-via-command`: If true, generator drops are restricted from normal shops. Default: `true`.

## Commands

- `/generator`: Manage generators (requires `skiesgenerator.use`).
- `/givegenerator <player> <tier> [amount]`: Give a placeable generator (requires `skiesgenerator.give`).
- `/sellgen`: Sell all generator drops in your storage and inventory (requires `skiesgenerator.sell`).
- `/boostgen <player> <multiplier> <minutes>`: Grant a temporary generator production booster to a player (requires `skiesgenerator.boost`). Example: `/boostgen Alice 2 30` gives x2 output for 30 minutes.

## GUI Features

- Collector GUI: Shows stored items, `Collect All`, `Upgrade` and `Take Generator` buttons. `Take Generator` transfers the placeable generator item and stored items directly to the player and removes the block from the world.
- Interaction GUI (block mode): Provides `Upgrade` and `Take Generator` when interacting with a placed generator.

## Protections

- Generator-produced items are tagged internally (NBT) and:
  - Cannot be placed, crafted, or traded with villagers.
  - Are recognized separately from placeable generator items (placeable items carry a different tag).
  - Must be sold via `/sellgen` (or configured behavior).

## Economy / Upgrades

- Integrates with Vault for upgrades. Use `Upgrade` in GUI or the appropriate admin commands (permission `skiesgenerator.upgrade`).
- Prices are defined in `prices.yml` (MATERIAL: price).

## Developer Notes

- Java: Project targets Java 21 — ensure server/build environment supports Java 21.
- Persistence: Generators and their stored items are saved via `GeneratorStorage`. Boosters are currently applied in-memory only (not persisted across restarts) — request persistence if desired.
- Key classes: `Generator`, `GeneratorManager`, `GeneratorTask`, `GeneratorMenu`, `GeneratorInteractionMenu`, `ItemProtectionListener`, `PricesConfig`.

## Troubleshooting

- If a generator block remains inert after taking it, ensure you are using the latest plugin build and that you have permissions to take it.
- If certain materials can't be sold, add them to `prices.yml` using their `Material` name.

## Contribution

Fork the repo, submit PRs for features or fixes, or open issues with reproduction steps.
