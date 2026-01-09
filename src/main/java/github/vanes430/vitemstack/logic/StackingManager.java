package github.vanes430.vitemstack.logic;

import github.vanes430.vitemstack.config.ConfigManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class StackingManager {
    private final ConfigManager config;
    private final FilterManager filterManager;
    private final NameManager nameManager;
    private final SoundManager soundManager;

    public StackingManager(ConfigManager config, FilterManager filterManager, NameManager nameManager, SoundManager soundManager) {
        this.config = config;
        this.filterManager = filterManager;
        this.nameManager = nameManager;
        this.soundManager = soundManager;
    }

    public void stackItem(Item item) {
        if (filterManager.isBlacklisted(item.getItemStack())) return;

        double range = config.getMergeRadius();
        int maxStackSize = config.getMaxStackSize();

        // Optimized: Only get Item entities within range to avoid iterating over mobs/players
        Collection<Entity> nearbyEntities = item.getWorld().getNearbyEntities(
            item.getLocation(), range, range, range, 
            entity -> entity instanceof Item && entity.isValid()
        );
        
        for (Entity entity : nearbyEntities) {
            Item target = (Item) entity;

            if (target.getUniqueId().equals(item.getUniqueId())) continue;
            if (filterManager.isBlacklisted(target.getItemStack())) continue;

            ItemStack itemStack = item.getItemStack();
            ItemStack targetStack = target.getItemStack();

            if (itemStack.isSimilar(targetStack)) {
                int totalAmount = itemStack.getAmount() + targetStack.getAmount();

                if (totalAmount <= maxStackSize) {
                    targetStack.setAmount(totalAmount);
                    target.setItemStack(targetStack);
                    
                    nameManager.updateName(target);
                    soundManager.playMerge(target.getLocation());
                    
                    item.remove();
                    break; 
                } else {
                    int transfer = maxStackSize - targetStack.getAmount();
                    if (transfer > 0) {
                        targetStack.setAmount(maxStackSize);
                        target.setItemStack(targetStack);
                        nameManager.updateName(target);
                        
                        itemStack.setAmount(itemStack.getAmount() - transfer);
                        item.setItemStack(itemStack);
                        nameManager.updateName(item);
                        
                        soundManager.playMerge(target.getLocation());
                    }
                }
            }
        }
    }
}