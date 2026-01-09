# vitemstack 📦

![Java](https://img.shields.io/badge/Java-21+-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Platform](https://img.shields.io/badge/Platform-Spigot%20%7C%20Paper%20%7C%20Folia-blue?style=for-the-badge&logo=spigotmc)
![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg?style=for-the-badge)

**vitemstack** is a lightweight, high-performance, and Folia-ready item stacking solution designed for modern Minecraft servers. Built on the robust Spigot API 1.14 foundation, it ensures compatibility with advanced NBT features while remaining incredibly efficient.

---

## ✨ Key Features

*   ⚡ **Folia Native Support**: Fully optimized for region-based threading using `FoliaLib`. No more async errors!
*   🧱 **Smart Stacking**: Intelligently merges nearby items. Supports custom stack limits (default: **128**).
*   ⏱️ **Performance Delay**: Configurable delay before stacking starts to prevent lag spikes during massive item drops (e.g., explosions, breaking chests).
*   ⏳ **Fair Despawn Logic**: Merging items calculates the **average** ticks lived. This prevents "infinite" item stacking exploits while ensuring fairness for players.
*   ✨ **Visual Indicators**: Large stacks of items (configurable, default >32) will **glow**, making it easy to spot valuable loot piles.
*   🏷️ **Native Holograms**: Displays clean, lag-free text above items (e.g., `Diamond Sword x1`) using native entity metadata. No ArmorStands involved.
*   🛡️ **Anti-Theft**: Prevents mobs (Zombies, Foxes, etc.) from stealing your loot.
*   ✅ **Breeding Friendly**: Includes a configurable **Whitelist** (default: Villagers) so your farms keep working.
*   🚫 **Smart Blacklist**: Filter items by **Material** or **NBT Tags** (perfect for MMOItems, Oraxen, etc.).
*   🔊 **Audio Feedback**: Satisfying sound effects for item pickup (throttled to prevent ear-rape). Merge sounds are silenced for a cleaner experience.
*   🧹 **Optimization Tools**: Built-in command to clear ground items efficiently.

---

## 📥 Installation

1.  Download the `vitemstack-1.0.0.jar`.
2.  Drop it into your server's `plugins` folder.
3.  Restart your server.
4.  (Optional) Configure `config.yml` to your liking.

---

## 🛠 Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/vitemstack` | Shows version & author info. | `None` |
| `/vitemstack reload` | Reloads `config.yml` instantly. | `vitemstack.admin` |
| `/vitemstack clear [world]` | Removes all items on the ground. | `vitemstack.admin` |

> **Note:** The `clear` command is thread-safe and safe to use on Folia servers.

---

## ⚙️ Configuration

The `config.yml` is simple yet powerful. Here is the default configuration:

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

**Q: Does this work on 1.21?**
A: Yes! It works on versions 1.14 up to the latest 1.21+ (including Folia).

**Q: Why don't items stack INSTANTLY when I break a chest?**
A: We added a small delay (default 5 ticks) to improve server performance. This allows the physics engine to settle items before we run the stacking logic, preventing lag spikes.

**Q: Will it lag my server?**
A: No. It uses **Immutable Swap Caching** and **Predicate Filtering**, making it extremely fast (O(1) lookups). It's designed for high-player-count servers.

**Q: Can I use this with custom item plugins?**
A: Absolutely. Use the `blacklist.nbt-tags` section to prevent your custom RPG items from stacking and losing their unique data.

**Q: Is it safe against duplication (dupe) exploits?**
A: Yes. We use the vanilla pickup logic. If a stack is 128, you pick up 64, and 64 stay on the ground. No magic inventory manipulation means no dupes.

---

Made with ❤️ by **vanes430**
