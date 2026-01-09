package github.vanes430.vitemstack.logic;

import github.vanes430.vitemstack.config.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoundManager {
    private final ConfigManager config;
    private final Map<UUID, Long> soundCooldowns = new HashMap<>();

    public SoundManager(ConfigManager config) {
        this.config = config;
    }

    public void playPickup(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        // Cooldown 100ms (0.1 detik) agar tidak berisik saat ambil banyak item
        if (soundCooldowns.getOrDefault(uuid, 0L) > now) return;
        
        playSound(player.getLocation(), player, "sounds.pickup");
        soundCooldowns.put(uuid, now + 100);
    }

    private void playSound(Location loc, Player specificPlayer, String path) {
        if (!config.get().getBoolean(path + ".enabled", false)) return;

        try {
            String soundName = config.get().getString(path + ".sound");
            float volume = (float) config.get().getDouble(path + ".volume", 1.0);
            float pitch = (float) config.get().getDouble(path + ".pitch", 1.0);
            
            Sound sound = Sound.valueOf(soundName);
            
            if (specificPlayer != null) {
                specificPlayer.playSound(loc, sound, volume, pitch);
            } else {
                loc.getWorld().playSound(loc, sound, volume, pitch);
            }
        } catch (Exception ignored) {}
    }
}