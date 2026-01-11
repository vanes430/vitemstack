package github.vanes430.vitemstack.logic;

import com.tcoded.folialib.FoliaLib;
import github.vanes430.vitemstack.VItemStackPlugin;
import github.vanes430.vitemstack.config.ConfigManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;

public class StackingManager {
    private final ConfigManager config;
    private final FilterManager filterManager;
    private final NameManager nameManager;
    private final SoundManager soundManager;
    private final FoliaLib foliaLib;

    public StackingManager(ConfigManager config, FilterManager filterManager, NameManager nameManager, SoundManager soundManager, FoliaLib foliaLib) {
        this.config = config;
        this.filterManager = filterManager;
        this.nameManager = nameManager;
        this.soundManager = soundManager;
        this.foliaLib = foliaLib;
    }

    public void stackItem(Item item) {
        if (filterManager.isBlacklisted(item.getItemStack())) return;
        
        // Ensure the item has the correct real amount set initially if not present
        if (!item.getPersistentDataContainer().has(VItemStackPlugin.REAL_AMOUNT_KEY, PersistentDataType.INTEGER)) {
            int amount = item.getItemStack().getAmount();
            setRealAmount(item, amount);
            if (config.isGlowingEnabled()) {
                item.setGlowing(amount > config.getGlowingThreshold());
            }
        }

        double range = config.getMergeRadius();

        // Optimized: Only get Item entities within range to avoid iterating over mobs/players
        Collection<Entity> nearbyEntities = item.getWorld().getNearbyEntities(
            item.getLocation(), range, range, range, 
            entity -> entity instanceof Item && entity.isValid()
        );
        
        for (Entity entity : nearbyEntities) {
            Item target = (Item) entity;

            if (target.getUniqueId().equals(item.getUniqueId())) continue;

            // Schedule safe access to target
            foliaLib.getScheduler().runAtEntity(target, (task) -> {
                // Verify validity of both entities inside the task
                if (!target.isValid() || !item.isValid()) return;
                
                // Re-check distance/world just in case
                if (!target.getWorld().equals(item.getWorld())) return;
                if (target.getLocation().distanceSquared(item.getLocation()) > (range * range)) return;

                if (filterManager.isBlacklisted(target.getItemStack())) return;

                ItemStack itemStack = item.getItemStack();
                ItemStack targetStack = target.getItemStack();

                if (itemStack.isSimilar(targetStack)) {
                    int maxStackSize = config.getMaxStackSize();
                    int amount1 = getRealAmount(item);
                    int amount2 = getRealAmount(target);
                    int totalAmount = amount1 + amount2;

                    if (totalAmount <= maxStackSize) {
                        // Full merge into target
                        int targetTicks = target.getTicksLived();
                        int itemTicks = item.getTicksLived();
                        int newTicks = ((targetTicks * amount2) + (itemTicks * amount1)) / totalAmount;
                        target.setTicksLived(Math.max(1, newTicks));

                        setRealAmount(target, totalAmount);
                        
                        if (config.isGlowingEnabled()) {
                            target.setGlowing(totalAmount > config.getGlowingThreshold());
                        }

                        nameManager.updateName(target);
                        // Sound removed per user request
                        
                        // Remove item on its own thread
                        foliaLib.getScheduler().runAtEntity(item, (t) -> item.remove());
                    } else {
                        // Partial merge
                        int transfer = maxStackSize - amount2;
                        if (transfer > 0) {
                            int targetTicks = target.getTicksLived();
                            int itemTicks = item.getTicksLived();
                            int newTicks = ((targetTicks * amount2) + (itemTicks * transfer)) / maxStackSize;
                            target.setTicksLived(Math.max(1, newTicks));

                            setRealAmount(target, maxStackSize);
                            if (config.isGlowingEnabled()) {
                                target.setGlowing(maxStackSize > config.getGlowingThreshold());
                            }
                            nameManager.updateName(target);
                            
                            // Update item on its own thread
                            foliaLib.getScheduler().runAtEntity(item, (t) -> {
                                if (!item.isValid()) return;
                                int currentReal = getRealAmount(item); 
                                // Recalculate based on current state (though strictly we used snapshots)
                                // Better to just subtract transfer
                                int newAmount = currentReal - transfer;
                                if (newAmount <= 0) {
                                    item.remove();
                                } else {
                                    setRealAmount(item, newAmount);
                                    if (config.isGlowingEnabled()) {
                                        item.setGlowing(newAmount > config.getGlowingThreshold());
                                    }
                                    nameManager.updateName(item);
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    public int getRealAmount(Item item) {
        if (item.getPersistentDataContainer().has(VItemStackPlugin.REAL_AMOUNT_KEY, PersistentDataType.INTEGER)) {
            return item.getPersistentDataContainer().get(VItemStackPlugin.REAL_AMOUNT_KEY, PersistentDataType.INTEGER);
        }
        return item.getItemStack().getAmount();
    }

    public void setRealAmount(Item item, int amount) {
        item.getPersistentDataContainer().set(VItemStackPlugin.REAL_AMOUNT_KEY, PersistentDataType.INTEGER, amount);
        ItemStack stack = item.getItemStack();
        stack.setAmount(1);
        item.setItemStack(stack);
    }
}