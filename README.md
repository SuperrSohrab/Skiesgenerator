# SkiesGenerator

**Description:** Lightweight Bukkit/Paper plugin that provides upgradable, configurable item generators with GUI management, economy upgrades, automatic update checking, and protections to prevent abuse.

## Features

✨ **Core Functionality**
- Upgradable item generators with multiple tiers
- Dual drop modes: collect items in GUI or spawn on block
- Temporary multiplier boosters for enhanced production
- Comprehensive GUI for generator management

🔒 **Protection System**
- NBT-tagged generator items prevent placement, crafting, and trading
- Separation between placeable generators and produced items
- Configurable restrictions for selling generator drops

💰 **Economy Integration**
- Dual economy provider support: **Vault** and **SkiesEconomy**
- Automatic fallback from SkiesEconomy to Vault if unavailable
- Configurable upgrade prices via `prices.yml`
- Unified economy API using reflection (no compile-time dependencies)

📊 **Analytics & Updates**
- **bStats** integration for anonymous usage statistics
- **Automatic update checker** via Modrinth API
- OP players notified on join when updates are available
- Asynchronous checking to avoid server lag

## Installation

1. **Build:** `mvn -DskipTests package`
2. **Install:** Place the generated JAR (`target/skiesgenerator-1.jar`) into your server `plugins/` folder
3. **Restart** the server
4. Configure `config.yml` to your preferences

## Configuration (`config.yml`)

### Drop Mode & Storage
- `drop-mode`: `collect` or `block` — whether drops are collected into the generator or spawned on the block. Default: `block`.
  - **collect mode**: Items stored in generator GUI (beta - may have bugs)
  - **block mode**: Items spawn on top of the generator block
- `max-storage`: Maximum items stored per generator (collect mode only). Default: `64`.

### Generation Settings
- `generation-interval`: Ticks between item generation (20 ticks = 1 second). Default: `100`.

### Economy Provider
- `economy-provider`: Choose between `vault` or `skies-economy`. Default: `skies-economy`.
  - Automatically falls back to Vault if SkiesEconomy is not found
  - See Economy Integration section in the wiki for details

### Generator Restrictions
- `prevent-placement`: If true, generator blocks cannot be placed normally. Default: `true`.
- `sellable-only-via-command`: If true, generator drops are restricted from normal shops. Default: `true`.

## Commands

- `/generator`: Manage generators (requires `skiesgenerator.use`)
- `/givegenerator <player> <tier> [amount]`: Give a placeable generator (requires `skiesgenerator.give`)
  - Aliases: `/givegens`, `/ggen`
- `/sellgen`: Sell all generator drops in your storage and inventory (requires `skiesgenerator.sell`)
- `/boostgen <player> <multiplier> <minutes>`: Grant a temporary generator production booster (requires `skiesgenerator.boost`)
  - Example: `/boostgen Alice 2 30` gives x2 output for 30 minutes
- `/skiesgenerator help`: Shows SkiesGenerator commands (requires `skiesgenerator.help`)

## Permissions

- `skiesgenerator.*`: Grants access to all SkiesGenerator commands
- `skiesgenerator.use`: Allows using basic generator commands (default: false)
- `skiesgenerator.sell`: Allows selling generator items (default: true)
- `skiesgenerator.give`: Allows giving generator items to players (default: op)
- `skiesgenerator.upgrade`: Allows upgrading generators (default: true)
- `skiesgenerator.boost`: Allows giving multiplier boosters (default: op)
- `skiesgenerator.help`: Allows viewing SkiesGenerator help (default: true)

## GUI Features

- Collector GUI: Shows stored items, `Collect All`, `Upgrade` and `Take Generator` buttons. `Take Generator` transfers the placeable generator item and stored items directly to the player and removes the block from the world.
- Interaction GUI (block mode): Provides `Upgrade` and `Take Generator` when interacting with a placed generator.

## Protections

- Generator-produced items are tagged internally (NBT) and:
  - Cannot be placed, crafted, or traded with villagers
  - Are recognized separately from placeable generator items (different NBT tags)
  - Must be sold via `/sellgen` (when `sellable-only-via-command` is enabled)
- Generator blocks cannot be placed normally (when `prevent-placement` is enabled)

## Economy Integration

SkiesGenerator supports **two economy providers**: Vault and SkiesEconomy.

### Configuration

In `config.yml`, choose your economy provider:

```yaml
economy-provider: skies-economy  # or 'vault'
```

- If set to `skies-economy` but the plugin isn't found, automatically falls back to Vault
- Uses reflection for SkiesEconomy integration (no compile-time dependency required)

### Usage in Code

The plugin provides unified economy methods that work with both systems:

```java
// Get the plugin instance
Plugin plugin = (Plugin) getPlugin();

// Get player balance
double balance = plugin.getBalance(player);

// Deposit money
if (plugin.deposit(player, 100.0)) {
    player.sendMessage("You received $100!");
}

// Withdraw money
if (plugin.withdraw(player, 50.0)) {
    player.sendMessage("$50 has been deducted!");
}

// Check which economy system is active
Plugin.EconomyType economyType = plugin.getActiveEconomyType();
if (economyType == Plugin.EconomyType.SKIES_ECONOMY) {
    // Using SkiesEconomy
} else if (economyType == Plugin.EconomyType.VAULT) {
    // Using Vault
}
```

### Upgrade Prices

Prices are defined in `prices.yml` using Material names:

```yaml
DIAMOND: 100.0
IRON_INGOT: 10.0
```

## Update Checker

SkiesGenerator automatically checks for updates on startup via the Modrinth API:

- **OP players** are notified on join when an update is available
- Shows current version, latest version, and download link
- Runs asynchronously to avoid server lag
- Project ID is embedded in `plugin.yml` (users cannot modify it)

The update checker queries: `https://api.modrinth.com/v2/project/GO47EhMF/version`

## Analytics

This plugin uses **bStats** to collect anonymous usage statistics:

- Plugin ID: `29144`
- View stats: [https://bstats.org/plugin/bukkit/skiesgenerator/29144](https://bstats.org/plugin/bukkit/skiesgenerator/29144)
- Helps developers understand plugin usage and improve features
- No personal data is collected

## Economy / Upgrades

- Integrates with Vault or SkiesEconomy for upgrades
- Use `Upgrade` button in GUI or appropriate admin commands (permission `skiesgenerator.upgrade`)
- Prices are defined in `prices.yml` (MATERIAL: price)

## Developer Notes

### Technical Stack
- **Java 21**: Project targets Java 21 — ensure server/build environment supports it
- **Build Tool**: Maven with Shade plugin for dependency shading
- **Dependencies**: 
  - Spigot API 1.21.1
  - Vault API (provided)
  - bStats Bukkit 3.1.0
  - Gson 2.10.1 (for JSON parsing)

### Architecture
- **Persistence**: Generators and their stored items are saved via `GeneratorStorage`
- **Boosters**: Applied in-memory only (not persisted across restarts)
- **Economy**: Reflection-based integration with SkiesEconomy (no compile-time dependency)
- **Update Checking**: Asynchronous HTTP requests to Modrinth API

### Key Classes
- `Plugin`: Main plugin class with economy integration
- `Generator`, `GeneratorManager`: Core generator logic
- `GeneratorTask`: Handles item generation intervals
- `GeneratorMenu`, `GeneratorInteractionMenu`: GUI implementations
- `ItemProtectionListener`: NBT-based item protection
- `UpdateChecker`: Modrinth API integration for version checking
- `UpdateNotificationListener`: OP player notifications
- `PricesConfig`: Economy pricing configuration

### Maven Shade Plugin

The plugin uses Maven Shade to relocate bStats:

```xml
<relocation>
    <pattern>org.bstats</pattern>
    <shadedPattern>your.package</shadedPattern>
</relocation>
```

This prevents conflicts with other plugins using bStats.

## Troubleshooting

### Common Issues

**Generator block remains inert after taking it**
- Ensure you're using the latest plugin build
- Verify you have the `skiesgenerator.use` permission

**Materials can't be sold**
- Add them to `prices.yml` using their exact `Material` name
- Example: `DIAMOND: 100.0`

**Economy not working**
- Check that either Vault or SkiesEconomy is installed
- Verify `economy-provider` in `config.yml` is set correctly
- Check console for economy setup messages on startup

**Update notifications not showing**
- Ensure the player has OP status
- Check console for update check results
- Verify internet connectivity for Modrinth API access

**Collect mode issues**
- Collect mode is still in beta and may have bugs
- Consider using `block` mode for production servers
- Report issues on the GitHub repository

## Links

- **Modrinth**: [https://modrinth.com/plugin/skiesgenerator](https://modrinth.com/plugin/skiesgenerator)
- **bStats**: [https://bstats.org/plugin/bukkit/skiesgenerator/29144](https://bstats.org/plugin/bukkit/skiesgenerator/29144)
- **Authors**: ItzAcat, SuperrSohrab

## Contribution

Fork the repo, submit PRs for features or fixes, or open issues with reproduction steps.
