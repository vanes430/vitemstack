package github.vanes430.vitemstack.config;

import github.vanes430.vitemstack.VItemStackPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {
    private final VItemStackPlugin plugin;
    
    // Volatile memastikan thread lain melihat perubahan variabel ini segera setelah reload
    private volatile Set<Material> blacklistedMaterials = Collections.emptySet();
    private volatile Set<String> blacklistedNbtTags = Collections.emptySet();
    private volatile Set<String> pickupWhitelist = Collections.emptySet();
    private volatile Set<String> disabledWorlds = Collections.emptySet();

    public ConfigManager(VItemStackPlugin plugin) {
        this.plugin = plugin;
        loadCache();
    }

    public void reload() {
        plugin.reloadConfig();
        loadCache();
    }
    
    private void loadCache() {
        // Create NEW sets (thread-safe technique: Copy-on-Write / Immutable Swap)
        // We do not modify the existing sets directly to avoid ConcurrentModificationException
        // during reads from other threads (Folia regions).
        
        Set<Material> newMaterials = new HashSet<>();
        Set<String> newNbtTags = new HashSet<>();
        Set<String> newWhitelist = new HashSet<>();
        Set<String> newDisabledWorlds = new HashSet<>();

        // Cache Materials
        List<String> matNames = get().getStringList("blacklist.materials");
        for (String name : matNames) {
            try {
                Material mat = Material.matchMaterial(name);
                if (mat != null) newMaterials.add(mat);
            } catch (Exception ignored) {}
        }

        // Cache NBT Tags
        newNbtTags.addAll(get().getStringList("blacklist.nbt-tags"));

        // Cache Whitelist
        newWhitelist.addAll(get().getStringList("settings.pickup-whitelist"));
        
        // Cache Disabled Worlds
        newDisabledWorlds.addAll(get().getStringList("settings.disabled-worlds"));
        
        // Atomically swap references
        this.blacklistedMaterials = newMaterials;
        this.blacklistedNbtTags = newNbtTags;
        this.pickupWhitelist = newWhitelist;
        this.disabledWorlds = newDisabledWorlds;
    }

    public FileConfiguration get() {
        return plugin.getConfig();
    }

    public int getMaxStackSize() {
        return get().getInt("settings.max-stack-size", 64);
    }

    public int getStackDelay() {
        return get().getInt("settings.stack-delay-ticks", 5);
    }

    public boolean isGlowingEnabled() {
        return get().getBoolean("visuals.glowing.enabled", false);
    }

    public int getGlowingThreshold() {
        return get().getInt("visuals.glowing.threshold", 32);
    }

    public double getMergeRadius() {
        return get().getDouble("settings.merge-radius", 5.0);
    }


    public boolean isMobPickupPrevented() {
        return get().getBoolean("settings.prevent-mob-pickup", true);
    }

    // Direct access to cached Sets (Safe because we swapped references)
    public Set<String> getPickupWhitelistCache() {
        return pickupWhitelist;
    }

    public Set<String> getDisabledWorldsCache() {
        return disabledWorlds;
    }

    public Set<Material> getBlacklistedMaterialsCache() {
        return blacklistedMaterials;
    }

    public Set<String> getBlacklistedNbtTagsCache() {
        return blacklistedNbtTags;
    }
}
