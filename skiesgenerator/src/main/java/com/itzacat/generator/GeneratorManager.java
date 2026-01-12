package com.itzacat.generator;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.*;

public class GeneratorManager {
    private final Map<Location, Generator> generators;

    public GeneratorManager() {
        this.generators = new HashMap<>();
    }

    public void createGenerator(Location location, GeneratorTier tier, UUID owner, int maxStorage) {
        Generator generator = new Generator(UUID.randomUUID(), location, tier, owner, maxStorage);
        generators.put(location, generator);
    }

    public Generator getGenerator(Location location) {
        return generators.get(location);
    }

    public Generator getGenerator(Block block) {
        return getGenerator(block.getLocation());
    }

    public void removeGenerator(Location location) {
        generators.remove(location);
    }

    public boolean isGenerator(Location location) {
        return generators.containsKey(location);
    }

    public boolean isGenerator(Block block) {
        return isGenerator(block.getLocation());
    }

    public Collection<Generator> getAllGenerators() {
        return generators.values();
    }

    public List<Generator> getGeneratorsByOwner(UUID owner) {
        List<Generator> result = new ArrayList<>();
        for (Generator generator : generators.values()) {
            if (generator.getOwner().equals(owner)) {
                result.add(generator);
            }
        }
        return result;
    }

    public boolean upgradeGenerator(Location location, GeneratorTier newTier) {
        Generator generator = getGenerator(location);
        if (generator != null) {
            generator.setTier(newTier);
            return true;
        }
        return false;
    }
}
