package com.itzacat.generator;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Generator {
    private final UUID uuid;
    private final Location location;
    private GeneratorTier tier;
    private final List<ItemStack> collectedItems;
    private final UUID owner;
    private int maxStorage;
    // Booster state: multiplier and expiry (millis)
    private int boosterMultiplier = 1;
    private long boosterExpiry = 0L;

    public Generator(UUID uuid, Location location, GeneratorTier tier, UUID owner, int maxStorage) {
        this.uuid = uuid;
        this.location = location;
        this.tier = tier;
        this.owner = owner;
        this.maxStorage = maxStorage;
        this.collectedItems = new ArrayList<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public Location getLocation() {
        return location;
    }

    public GeneratorTier getTier() {
        return tier;
    }

    public void setTier(GeneratorTier tier) {
        this.tier = tier;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<ItemStack> getCollectedItems() {
        return collectedItems;
    }

    public int getMaxStorage() {
        return maxStorage;
    }

    public void setMaxStorage(int maxStorage) {
        this.maxStorage = maxStorage;
    }

    public boolean addItem(ItemStack item) {
        int totalItems = collectedItems.stream().mapToInt(ItemStack::getAmount).sum();
        if (totalItems >= maxStorage) {
            return false;
        }
        collectedItems.add(item);
        return true;
    }

    public void setBooster(int multiplier, int durationMinutes) {
        if (multiplier < 1) multiplier = 1;
        this.boosterMultiplier = multiplier;
        if (durationMinutes <= 0) {
            this.boosterExpiry = 0L;
        } else {
            this.boosterExpiry = System.currentTimeMillis() + (durationMinutes * 60L * 1000L);
        }
    }

    public boolean isBoosterActive() {
        return boosterMultiplier > 1 && boosterExpiry > System.currentTimeMillis();
    }

    public int getBoosterMultiplier() {
        if (isBoosterActive()) return boosterMultiplier;
        return 1;
    }

    public void clearBooster() {
        this.boosterMultiplier = 1;
        this.boosterExpiry = 0L;
    }

    public void clearItems() {
        collectedItems.clear();
    }

    public int getTotalItems() {
        return collectedItems.stream().mapToInt(ItemStack::getAmount).sum();
    }
}
