package one.armelin.igcmda;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Configuration {
    public Texts texts = new Texts();

    public static class Texts {
        @Comment(value = """
                Memory message
                Available placeholders:
                {playername} | Player name
                {memory} | Memory""")
        public String memoryMessage = "{playername} has discovered a new memory: {memory}";
    }

    @Comment(value = """
                Colors configuration
                All colors should be in hex format, e.g. #RRGGBB
            """)
    public Colors colors = new Colors();

    public static class Colors {
        public String messageColor = "#FFFFFF";
        public String playerNameColor = "#C5D9E8";
        public String memoryColor = "#8AB2D0";
    }

    /**
     * Load configuration from file or create default if it doesn't exist
     * @return Loaded or default configuration
     */
    public static Configuration getConfig(Path configPath) {
        var jankson = Jankson.builder().build();
        Configuration config;

        try {
            Path configFile = configPath.resolve("igcda.json5");

            // Try to load existing config
            if (Files.exists(configFile)) {
                JsonObject configJson = jankson.load(configFile.toFile());
                config = jankson.fromJson(configJson, Configuration.class);
                IGCMDA.LOGGER.atInfo().log("Configuration loaded from: " + configFile);
            } else {
                config = new Configuration();
                IGCMDA.LOGGER.atInfo().log("Creating default configuration at: " + configFile);
            }

            // Save/update config file
            Files.writeString(configFile, jankson.toJson(config).toJson(true, true));

        } catch (IOException | SyntaxError e) {
            IGCMDA.LOGGER.atSevere().withCause(e).log("Failed to load configuration, using defaults");
            config = new Configuration();
        }

        return config;
    }
}
