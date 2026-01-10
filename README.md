<div align="center">

# vitemstack 📦

[![Build Status](https://github.com/vanes430/vitemstack/actions/workflows/build.yml/badge.svg?branch=master&style=flat-square)](https://github.com/vanes430/vitemstack/actions/workflows/build.yml)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/vanes430/vitemstack?style=flat-square&logo=github)](https://github.com/vanes430/vitemstack/releases)
[![License](https://img.shields.io/github/license/vanes430/vitemstack?style=flat-square)](LICENSE)
<br>
[![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Platform](https://img.shields.io/badge/Platform-Spigot_|_Paper_|_Folia-373737?style=flat-square&logo=spigotmc&logoColor=white)](https://papermc.io/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.14_--_1.21%2B-46D168?style=flat-square&logo=minecraft&logoColor=white)](https://minecraft.net/)

**vitemstack** is a lightweight, high-performance, and Folia-ready item stacking solution designed for modern Minecraft servers. Built on the robust Spigot API 1.14 foundation, it ensures compatibility with advanced NBT features while remaining incredibly efficient.

</div>

---

## ✨ Key Features

*   ⚡ **Folia Native Support**: Fully optimized for region-based threading using `FoliaLib`. No more async errors or thread safety issues on Folia/Paper.
*   📦 **Visual Stacking**: Items visually appear as a **single entity** (`Amount: 1`) but contain the true stack amount (e.g., x64) internally. This drastically reduces client-side FPS lag and server-side physics calculations while keeping the "cool" aesthetic of single items.
*   💾 **Thread-Safe & Leak-Free**: Uses `PersistentDataContainer` (PDC) for data storage, ensuring zero memory leaks. Full Folia support with region-safe scheduling (`FoliaLib`) guarantees no thread race conditions.
*   🧱 **Smart Stacking**: Intelligently merges nearby items. Supports custom stack limits (default: **128**), allowing you to go beyond the vanilla 64 limit.
*   🛡️ **Smart Chunk Limiter**: Prevents lag machines by limiting item entities per chunk (default: **64**). 
*   ⚓**Anti-Loss Protection**: Unlike other cleaners, it only removes "plain" items (no custom name, no enchantments, no lore). Your valuable gear and rare loot are always safe!
*   ⏱️ **Performance Delay**: Configurable delay before stacking starts (default: 5 ticks). This prevents lag spikes during massive item drops like explosions or breaking double chests.
*   ⏳ **Fair Despawn Logic**: Merging items calculates the **average** ticks lived. This ensures item timers are fair and prevents "infinite" item stacking exploits where items never despawn.
*   ✨ **Visual Indicators**: Large stacks of items (configurable threshold) will automatically **glow**, making it easy for players to spot valuable loot piles from a distance.
*   🏷️ **Native Holograms**: Displays clean, lag-free text display above items (e.g., `Diamond x64`) using native entity metadata. No ArmorStands or extra entities involved.
*   🛡️ **Anti-Theft**: Configurable prevention for mobs (Zombies, Foxes, etc.) from picking up items on the ground.
*   ✅ **Breeding Friendly**: Includes a configurable **Whitelist** (default: Villagers) so your automatic farms and breeding mechanics keep working perfectly.
*   🚫 **Smart Blacklist**: Filter items by **Material** or **NBT Tags**. Perfect for ignoring rare items or custom items from plugins like MMOItems, Oraxen, or MythicMobs.
*   🔊 **Audio Feedback**: Satisfying sound effects for item pickup (with internal cooldowns to prevent noise spam). Merge sounds are silenced for a smoother experience.
*   🧹 **Optimization Tools**: Built-in command to clear ground items efficiently across worlds.

---

## 📥 Installation

1.  Download the `vitemstack-1.0.0.jar`.
2.  Drop it into your server's `plugins` folder.
3.  Restart your server.
4.  (Optional) Configure `config.yml` to your liking and run `/vitemstack reload`.

---

## 🛠 Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/vitemstack` | Shows version & author info. | `None` |
| `/vitemstack reload` | Reloads `config.yml` instantly. | `vitemstack.admin` |
| `/vitemstack clear [world]` | Removes all items on the ground safely. | `vitemstack.admin` |

---

## ⚙️ Configuration

```yaml
settings:
  # Radius (in blocks) to search for items to merge
  merge-radius: 5.0
  # Maximum stack size per entity (Vanilla is 64, we support more!)
  max-stack-size: 128
  # Delay (in ticks) before attempting to stack newly spawned items.
  # Helps performance during explosions or breaking containers.
  stack-delay-ticks: 5
  
  # Prevent mobs from picking up items?
  prevent-mob-pickup: true
  
  # Mobs allowed to pickup items (keep VILLAGER for breeding)
  pickup-whitelist:
    - "VILLAGER"
    
  # Disable features in specific worlds
  disabled-worlds:
    - "spawn_hub"

# Limits the number of item entities per chunk to prevent lag machines.
chunk-limit:
  enabled: true
  # Maximum number of item entities allowed in a chunk.
  max-items: 64

visuals:
  glowing:
    enabled: true
    # Items will glow if stack amount is greater than this value
    threshold: 32

blacklist:
  # Items to NEVER stack (e.g. rare items)
  materials:
    - "NETHER_STAR"
    - "DRAGON_EGG"
    - "BEACON"
  
  # Advanced: Don't stack items with these NBT keys
  # Compatible with MMOItems ("MMOITEMS_ITEM_ID"), MythicMobs, etc.
  nbt-tags:
    - "MMOITEMS_ITEM_ID"
    - "MYTHIC_TYPE"

sounds:
  # Sound when player picks up item
  pickup:
    enabled: true
    sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
    volume: 0.3
    pitch: 1.0

custom-name:
  # Show holograms above items?
  enabled: true
  # Format: {name} = Item Name, {amount} = Count
  # Color codes (&) supported.
  format: "{name} &cx{amount}"
```

---

## ❓ FAQ

**Q: Does the Chunk Limiter delete my Diamond Armor?**
A: **No.** The Smart Limiter specifically checks for `ItemMeta`. Items that are enchanted, renamed, or have special lore are ignored by the limiter. It only purges "plain" items like Cobblestone, Dirt, or common mob drops when the chunk is overloaded.

**Q: Does this work on 1.21?**
A: Yes! It works on versions 1.14 up to the latest 1.21+ (including Folia).

**Q: Why do my stacked items look like just one item?**
A: This is our **Visual Stacking** feature! It reduces client lag by rendering only one item model, but the real amount (e.g., 64) is safely stored and given to you when picked up.

**Q: Why don't items stack INSTANTLY when I break a chest?**
A: We added a small delay (default 5 ticks) to improve server performance. This allows the physics engine to settle items before we run the stacking logic, preventing lag spikes.

**Q: Will it lag my server?**
A: No. It uses **Immutable Swap Caching** and **Predicate Filtering**, making it extremely fast (O(1) lookups). It's designed for high-player-count servers.

**Q: Can I use this with custom item plugins?**
A: Absolutely. Use the `blacklist.nbt-tags` section to prevent your custom RPG items from stacking and losing their unique data.

**Q: Is it safe against duplication (dupe) exploits?**
A: Yes. We use the vanilla pickup logic. No magic inventory manipulation means no dupes.

---

Made with ❤️ by **vanes430**
