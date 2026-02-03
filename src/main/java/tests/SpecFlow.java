package tests;

import net.minecraft.gametest.framework.GameTestHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SpecFlow implements ISpecFlow {
    private static final Logger LOGGER = LogManager.getLogger();
    private final GameTestHelper helper;
    private int tick = 0;

    public SpecFlow(GameTestHelper helper) {
        this.helper = helper;
    }

    public ISpecFlow given(String description, Runnable action) {
        LOGGER.info("GIVEN: {}", description);
        helper.runAtTickTime(tick++, action);
        return this;
    }

    public ISpecFlow when(String description, Runnable action) {
        LOGGER.info("WHEN: {}", description);
        helper.runAtTickTime(tick++, action);
        return this;
    }

    public ISpecFlow then(String description, Runnable action) {
        LOGGER.info("THEN: {}", description);
        helper.runAtTickTime(tick++, action);
        return this;
    }

    public ISpecFlow and(String description, Runnable action) {
        LOGGER.info("   AND: {}", description);
        helper.runAtTickTime(tick++, action);
        return this;
    }
}
