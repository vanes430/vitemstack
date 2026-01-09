package github.vanes430.vitemstack.logic;

import github.vanes430.vitemstack.config.ConfigManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Set;

public class FilterManager {
    private final ConfigManager config;

    public FilterManager(ConfigManager config) {
        this.config = config;
    }

    public boolean isWorldDisabled(String worldName) {
        return config.getDisabledWorldsCache().contains(worldName);
    }

    public boolean shouldPreventPickup(EntityType type) {
        if (!config.isMobPickupPrevented()) return false;
        return !config.getPickupWhitelistCache().contains(type.name());
    }

    public boolean isBlacklisted(ItemStack stack) {
        // Material Check (Fast Set Lookup)
        if (config.getBlacklistedMaterialsCache().contains(stack.getType())) {
            return true;
        }

        // NBT/PDC Check
        if (stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            Set<String> restrictedTags = config.getBlacklistedNbtTagsCache();
            
            if (!restrictedTags.isEmpty()) {
                PersistentDataContainer container = meta.getPersistentDataContainer();
                // Namespaces common for custom items
                String[] commonNamespaces = {"mmoitems", "mythicmobs", "oraxen", "minecraft", "custom"};
                
                for (String tag : restrictedTags) {
                    if (tag.contains(":")) {
                        String[] split = tag.split(":");
                        try {
                            NamespacedKey key = new NamespacedKey(split[0], split[1]);
                            if (hasKey(container, key)) return true;
                        } catch (Exception ignored) {}
                    } else {
                        for (String ns : commonNamespaces) {
                            try {
                                NamespacedKey key = new NamespacedKey(ns, tag.toLowerCase());
                                if (hasKey(container, key)) return true;
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasKey(PersistentDataContainer container, NamespacedKey key) {
        return container.has(key, org.bukkit.persistence.PersistentDataType.STRING) ||
               container.has(key, org.bukkit.persistence.PersistentDataType.INTEGER) ||
               container.has(key, org.bukkit.persistence.PersistentDataType.DOUBLE);
    }
}