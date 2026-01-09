package github.vanes430.vitemstack.listeners;

import com.tcoded.folialib.FoliaLib;
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
            soundManager.playPickup((Player) event.getEntity());
            return; 
        }

        if (filterManager.shouldPreventPickup(event.getEntity().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemMerge(ItemMergeEvent event) {
        if (filterManager.isWorldDisabled(event.getEntity().getWorld().getName())) return;

        Item target = event.getTarget();
        // Sound removed per user request

        foliaLib.getScheduler().runAtEntity(target, (task) -> {
            if (target.isValid()) {
                nameManager.updateName(target);
            }
        });
    }
}
