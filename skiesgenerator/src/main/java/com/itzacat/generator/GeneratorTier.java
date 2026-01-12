package com.itzacat.generator;

import org.bukkit.Material;

public enum GeneratorTier {
    OAK_WOOD(Material.OAK_WOOD, Material.DIRT, 1, 1000),
    SMOOTH_STONE(Material.SMOOTH_STONE, Material.COBBLESTONE, 2, 2000),
    REINFORCED_DEEPSLATE(Material.REINFORCED_DEEPSLATE, Material.COBBLED_DEEPSLATE, 3, 5000),
    SCULK_BLOCK(Material.SCULK, Material.ECHO_SHARD, 4, 10000),
    COAL_BLOCK(Material.COAL_BLOCK, Material.COAL, 5, 20000),
    COPPER_BLOCK(Material.COPPER_BLOCK, Material.COPPER_INGOT, 6, 50000),
    IRON_BLOCK(Material.IRON_BLOCK, Material.IRON_INGOT, 7, 100000),
    GOLD_BLOCK(Material.GOLD_BLOCK, Material.GOLD_INGOT, 8, 200000),
    REDSTONE_BLOCK(Material.REDSTONE_BLOCK, Material.REDSTONE, 9, 500000),
    AMETHYST_BLOCK(Material.AMETHYST_BLOCK, Material.AMETHYST_SHARD, 10, 1000000),
    EMERALD_BLOCK(Material.EMERALD_BLOCK, Material.EMERALD, 11, 2000000),
    BEDROCK(Material.BEDROCK, Material.PRISMARINE_SHARD, 12, 5000000);

    private final Material blockMaterial;
    private final Material dropMaterial;
    private final int tier;
    private final double upgradeCost;

    GeneratorTier(Material blockMaterial, Material dropMaterial, int tier, double upgradeCost) {
        this.blockMaterial = blockMaterial;
        this.dropMaterial = dropMaterial;
        this.tier = tier;
        this.upgradeCost = upgradeCost;
    }

    public Material getBlockMaterial() {
        return blockMaterial;
    }

    public Material getDropMaterial() {
        return dropMaterial;
    }

    public int getTier() {
        return tier;
    }

    public double getUpgradeCost() {
        return upgradeCost;
    }

    public GeneratorTier getNextTier() {
        for (GeneratorTier tier : values()) {
            if (tier.getTier() == this.tier + 1) {
                return tier;
            }
        }
        return null;
    }

    public static GeneratorTier fromMaterial(Material material) {
        for (GeneratorTier tier : values()) {
            if (tier.getBlockMaterial() == material) {
                return tier;
            }
        }
        return null;
    }
}
