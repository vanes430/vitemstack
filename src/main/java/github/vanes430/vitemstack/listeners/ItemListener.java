package github.vanes430.vitemstack.listeners;

import com.tcoded.folialib.FoliaLib;
import github.vanes430.vitemstack.VItemStackPlugin;
import github.vanes430.vitemstack.config.ConfigManager;
import github.vanes430.vitemstack.logic.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class ItemListener implements Listener {

    private final FoliaLib foliaLib;
    private final ConfigManager config;
    private final FilterManager filterManager;
    private final StackingManager stackingManager;
    private final NameManager nameManager;
    private final SoundManager soundManager;
    private final ChunkManager chunkManager;

    public ItemListener(FoliaLib foliaLib, ConfigManager config, FilterManager filterManager, StackingManager stackingManager, NameManager nameManager, SoundManager soundManager, ChunkManager chunkManager) {
        this.foliaLib = foliaLib;
        this.config = config;
        this.filterManager = filterManager;
        this.stackingManager = stackingManager;
        this.nameManager = nameManager;
        this.soundManager = soundManager;
        this.chunkManager = chunkManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        if (filterManager.isWorldDisabled(item.getWorld().getName())) return;
        
        foliaLib.getScheduler().runAtEntityLater(item, (task) -> {
            if (!item.isValid()) return;
            
            // Cek limit chunk sebelum stacking
            chunkManager.checkChunkLimit(item);
            if (!item.isValid()) return; // Jika item dihapus oleh limit, stop.

            nameManager.updateName(item);
            stackingManager.stackItem(item);
        }, config.getStackDelay());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityPickup(EntityPickupItemEvent event) {
        if (filterManager.isWorldDisabled(event.getEntity().getWorld().getName())) return;
        
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Item item = event.getItem();
            
            int realAmount = stackingManager.getRealAmount(item);
            
            // Check if we need to handle custom stack amount
            if (realAmount > item.getItemStack().getAmount() || (realAmount > 1 && item.getItemStack().getAmount() == 1)) {
                event.setCancelled(true);
                soundManager.playPickup(player);
                
                ItemStack stack = item.getItemStack().clone();
                int remaining = realAmount;
                int maxStack = stack.getMaxStackSize();
                
                // Try to add to inventory
                while (remaining > 0) {
                    int toAdd = Math.min(remaining, maxStack);
                    ItemStack addStack = stack.clone();
                    addStack.setAmount(toAdd);
                    
                    Map<Integer, ItemStack> leftovers = player.getInventory().addItem(addStack);
                    
                    // If everything added, leftovers is empty. 
                    // If some failed, leftover contains the stack that couldn't be added.
                    if (!leftovers.isEmpty()) {
                        // Calculate what was NOT added
                        int notAdded = leftovers.get(0).getAmount();
                        int added = toAdd - notAdded;
                        remaining -= added;
                        break; // Inventory full
                    } else {
                        remaining -= toAdd;
                    }
                }
                
                if (remaining <= 0) {
                    foliaLib.getScheduler().runAtEntity(item, (t) -> item.remove());
                } else {
                    int finalRemaining = remaining;
                    foliaLib.getScheduler().runAtEntity(item, (t) -> {
                        if (!item.isValid()) return;
                        stackingManager.setRealAmount(item, finalRemaining);
                        nameManager.updateName(item);
                    });
                }
            } else {
                // Normal pickup logic (just sound)
                soundManager.playPickup(player);
            }
            return;
        }

        if (filterManager.shouldPreventPickup(event.getEntity().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemMerge(ItemMergeEvent event) {
        if (filterManager.isWorldDisabled(event.getEntity().getWorld().getName())) return;

        Item target = event.getTarget();
        Item source = event.getEntity();
        
        if (filterManager.isBlacklisted(target.getItemStack()) || filterManager.isBlacklisted(source.getItemStack())) return;

        // Visual Stacking Logic: Cancel vanilla merge and do it manually
        event.setCancelled(true);
        
        // Schedule safe merge logic on target
        foliaLib.getScheduler().runAtEntity(target, (task) -> {
            if (!target.isValid() || !source.isValid()) return;

            int maxStackSize = config.getMaxStackSize();
            int amount1 = stackingManager.getRealAmount(target);
            int amount2 = stackingManager.getRealAmount(source);
            int totalAmount = amount1 + amount2;

            if (totalAmount <= maxStackSize) {
                // Average ticks calculation
                int targetTicks = target.getTicksLived();
                int itemTicks = source.getTicksLived();
                int newTicks = ((targetTicks * amount1) + (itemTicks * amount2)) / totalAmount;
                target.setTicksLived(Math.max(1, newTicks));

                stackingManager.setRealAmount(target, totalAmount);
                
                if (config.isGlowingEnabled()) {
                    target.setGlowing(totalAmount > config.getGlowingThreshold());
                }

                nameManager.updateName(target);
                
                // Remove source on its own thread
                foliaLib.getScheduler().runAtEntity(source, (t) -> source.remove());
            } else {
                int transfer = maxStackSize - amount1;
                if (transfer > 0) {
                    // Fill target to max
                    stackingManager.setRealAmount(target, maxStackSize);
                    if (config.isGlowingEnabled()) {
                        target.setGlowing(maxStackSize > config.getGlowingThreshold());
                    }
                    nameManager.updateName(target);
                    
                    // Update source on its own thread
                    foliaLib.getScheduler().runAtEntity(source, (t) -> {
                        if (!source.isValid()) return;
                        stackingManager.setRealAmount(source, amount2 - transfer);
                        if (config.isGlowingEnabled()) {
                            source.setGlowing((amount2 - transfer) > config.getGlowingThreshold());
                        }
                        nameManager.updateName(source);
                    });
                }
            }
        });
    }
}
