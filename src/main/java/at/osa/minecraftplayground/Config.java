package at.osa.minecraftplayground;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_CONNECTION_DISTANCE = BUILDER
            .comment("Maximum distance (in blocks) between two connected chain blocks. Connections beyond this distance are rejected.")
            .defineInRange("maxConnectionDistance", 24, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MAX_CONNECTIONS_PER_CHAIN = BUILDER
            .comment("Maximum number of connections allowed per chain block. Prevents visual clutter and performance issues.")
            .defineInRange("maxConnectionsPerChain", 5, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue UPDATE_INTERVAL_TICKS = BUILDER
            .comment("How often to perform periodic network updates (in ticks). 20 ticks = 1 second. This acts as a backup to event-driven updates.")
            .defineInRange("updateIntervalTicks", 20, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue SIGNAL_LOSS_DELAY_TICKS = BUILDER
            .comment("How many ticks to wait before clearing cached signal after input is lost. Prevents flickering when power briefly turns off.")
            .defineInRange("signalLossDelayTicks", 1, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
