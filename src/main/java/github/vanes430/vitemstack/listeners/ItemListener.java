package github.vanes430.vitemstack.listeners;

import com.tcoded.folialib.FoliaLib;
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
    private final FilterManager filterManager;
    private final StackingManager stackingManager;
    private final NameManager nameManager;
    private final SoundManager soundManager;

    public ItemListener(FoliaLib foliaLib, FilterManager filterManager, StackingManager stackingManager, NameManager nameManager, SoundManager soundManager) {
        this.foliaLib = foliaLib;
        this.filterManager = filterManager;
        this.stackingManager = stackingManager;
        this.nameManager = nameManager;
        this.soundManager = soundManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        if (filterManager.isWorldDisabled(item.getWorld().getName())) return;
        
        foliaLib.getScheduler().runAtEntity(item, (task) -> {
            if (!item.isValid()) return;
            nameManager.updateName(item);
            stackingManager.stackItem(item);
        });
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
        soundManager.playMerge(target.getLocation());

        foliaLib.getScheduler().runAtEntity(target, (task) -> {
            if (target.isValid()) {
                nameManager.updateName(target);
            }
        });
    }
}