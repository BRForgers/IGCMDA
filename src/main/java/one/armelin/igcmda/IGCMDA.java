package one.armelin.igcmda;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import one.armelin.igcmda.systems.BeforeGatherMemoriesSystem;

import java.nio.file.Files;

public class IGCMDA extends JavaPlugin {

    public static final String NAME = "IGCMDA";
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static Configuration config;
    public static Universe universe;

    private static IGCMDA instance;

    public IGCMDA(JavaPluginInit init) {
        super(init);
        instance = this;

        LOGGER.atInfo().log("IGCDA initializing...");

        if(!Files.exists(getDataDirectory())){
            try {
                Files.createDirectories(getDataDirectory());
            } catch (Exception e) {
                LOGGER.atSevere().withCause(e).log("Failed to create plugin data directory");
            }
        }
        config = Configuration.getConfig(getDataDirectory());

        LOGGER.atInfo().log("IGCDA initialized successfully!");
    }

    @Override
    protected void setup() {
        super.setup();
        getEntityStoreRegistry().registerSystem(new BeforeGatherMemoriesSystem());
    }

    @Override
    protected void start() {
        super.start();
        universe = Universe.get();
    }

    @Override
    protected void shutdown() {
        super.shutdown();
    }

    public static IGCMDA getInstance() {
        return instance;
    }
}
