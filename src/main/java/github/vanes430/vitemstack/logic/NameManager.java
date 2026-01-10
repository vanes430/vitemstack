package github.vanes430.vitemstack.logic;

import github.vanes430.vitemstack.VItemStackPlugin;
import github.vanes430.vitemstack.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class NameManager {
    private final ConfigManager config;

    public NameManager(ConfigManager config) {
        this.config = config;
    }

    public void updateName(Item item) {
        if (!config.get().getBoolean("custom-name.enabled", true)) return;

        ItemStack stack = item.getItemStack();
        int amount;
        
        if (item.getPersistentDataContainer().has(VItemStackPlugin.REAL_AMOUNT_KEY, PersistentDataType.INTEGER)) {
            amount = item.getPersistentDataContainer().get(VItemStackPlugin.REAL_AMOUNT_KEY, PersistentDataType.INTEGER);
        } else {
            amount = stack.getAmount();
        }

        String displayName = getNiceName(stack);

        String format = config.get().getString("custom-name.format", "{name} &cx{amount}");
        String finalName = format
                .replace("{name}", displayName)
                .replace("{amount}", String.valueOf(amount));

        item.setCustomName(ChatColor.translateAlternateColorCodes('&', finalName));
        item.setCustomNameVisible(true);
    }

    private String getNiceName(ItemStack stack) {
        if (stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            if (meta.hasDisplayName()) {
                return meta.getDisplayName();
            }
        }
        
        String rawName = stack.getType().name();
        StringBuilder niceName = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : rawName.toCharArray()) {
            if (c == '_') {
                nextTitleCase = true;
                niceName.append(" ");
                continue;
            }
            if (nextTitleCase) {
                niceName.append(Character.toUpperCase(c));
                nextTitleCase = false;
            } else {
                niceName.append(Character.toLowerCase(c));
            }
        }
        return niceName.toString();
    }
}
