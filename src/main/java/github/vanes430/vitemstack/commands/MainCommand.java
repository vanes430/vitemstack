package github.vanes430.vitemstack.commands;

import com.tcoded.folialib.FoliaLib;
import github.vanes430.vitemstack.VItemStackPlugin;
import github.vanes430.vitemstack.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Item;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final VItemStackPlugin plugin;
    private final ConfigManager configManager;
    private final FoliaLib foliaLib;

    public MainCommand(VItemStackPlugin plugin, ConfigManager configManager, FoliaLib foliaLib) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.foliaLib = foliaLib;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "vitemstack v" + plugin.getDescription().getVersion() + " by vanes430");
            sender.sendMessage(ChatColor.YELLOW + "/vitemstack reload - Reload config");
            sender.sendMessage(ChatColor.YELLOW + "/vitemstack clear [world] - Clear ground items");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("vitemstack.admin")) {
                sender.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }
            configManager.reload();
            sender.sendMessage(ChatColor.GREEN + "vitemstack configuration reloaded!");
            return true;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            if (!sender.hasPermission("vitemstack.admin")) {
                sender.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }
            
            World targetWorld = null;
            if (args.length > 1) {
                targetWorld = Bukkit.getWorld(args[1]);
                if (targetWorld == null) {
                    sender.sendMessage(ChatColor.RED + "World '" + args[1] + "' not found.");
                    return true;
                }
            }

            final World finalWorld = targetWorld;
            sender.sendMessage(ChatColor.YELLOW + "Clearing items...");
            
            foliaLib.getScheduler().runNextTick((task) -> {
                int count = 0;
                List<World> worlds = (finalWorld != null) ? Collections.singletonList(finalWorld) : Bukkit.getWorlds();

                for (World world : worlds) {
                    for (Item item : world.getEntitiesByClass(Item.class)) {
                        foliaLib.getScheduler().runAtEntity(item, (t) -> item.remove());
                        count++;
                    }
                }
                sender.sendMessage(ChatColor.GREEN + "Cleared approximately " + count + " items.");
            });
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("vitemstack.admin")) return Collections.emptyList();

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("reload", "clear"), new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("clear")) {
            List<String> worldNames = Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[1], worldNames, new ArrayList<>());
        }

        return Collections.emptyList();
    }
}