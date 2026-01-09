package github.vanes430.vitemstack.logic;

import github.vanes430.vitemstack.config.ConfigManager;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ChunkManager {
    private final ConfigManager config;

    public ChunkManager(ConfigManager config) {
        this.config = config;
    }

    public void checkChunkLimit(Item triggerItem) {
        if (!config.isChunkLimitEnabled()) return;

        Chunk chunk = triggerItem.getLocation().getChunk();
        // Optimasi: Arrays.stream bisa lambat di hot-path, pakai manual loop
        Entity[] entities = chunk.getEntities();
        
        List<Item> items = new ArrayList<>();
        int itemCount = 0;

        for (Entity entity : entities) {
            if (entity instanceof Item && entity.isValid()) {
                items.add((Item) entity);
                itemCount++;
            }
        }

        int maxItems = config.getChunkLimitMax();
        if (itemCount <= maxItems) return;

        int toRemove = itemCount - maxItems;
        
        // Sorting: Prioritaskan item yang paling lama ada di tanah (TicksLived terbesar) untuk dihapus
        items.sort(Comparator.comparingInt(Entity::getTicksLived).reversed());

        for (Item item : items) {
            if (toRemove <= 0) break;

            // PROTEKSI: Jangan hapus item yang punya Meta (Enchant, Name, Lore)
            if (item.getItemStack().hasItemMeta()) continue;

            // Hapus item polos
            item.remove();
            toRemove--;
        }
    }
}
