package github.vanes430.vitemstack;

import com.tcoded.folialib.FoliaLib;
import github.vanes430.vitemstack.commands.MainCommand;
import github.vanes430.vitemstack.config.ConfigManager;
import github.vanes430.vitemstack.listeners.ItemListener;
import github.vanes430.vitemstack.logic.*;
import org.bukkit.plugin.java.JavaPlugin;

public class VItemStackPlugin extends JavaPlugin {

    private FoliaLib foliaLib;
    private static VItemStackPlugin instance;
    private ConfigManager configManager;
    
    public static org.bukkit.NamespacedKey REAL_AMOUNT_KEY;

    @Override
    public void onEnable() {
        instance = this;
        REAL_AMOUNT_KEY = new org.bukkit.NamespacedKey(this, "real_amount");
        this.foliaLib = new FoliaLib(this);

        saveDefaultConfig();

        // Initialize Managers (Dependency Injection)
        this.configManager = new ConfigManager(this);
        FilterManager filterManager = new FilterManager(configManager);
        NameManager nameManager = new NameManager(configManager);
        SoundManager soundManager = new SoundManager(configManager);
        ChunkManager chunkManager = new ChunkManager(configManager);
        StackingManager stackingManager = new StackingManager(configManager, filterManager, nameManager, soundManager, foliaLib);

        // Register Listeners
        getServer().getPluginManager().registerEvents(
                new ItemListener(foliaLib, configManager, filterManager, stackingManager, nameManager, soundManager, chunkManager), 
                this
        );

        // Register Commands
        MainCommand mainCommand = new MainCommand(this, configManager, foliaLib);
        getCommand("vitemstack").setExecutor(mainCommand);
        getCommand("vitemstack").setTabCompleter(mainCommand);

        getLogger().info("vitemstack enabled successfully!");
    }

    public FoliaLib getFoliaLib() {
        return foliaLib;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public static VItemStackPlugin getInstance() {
        return instance;
    }
}