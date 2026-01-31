package at.osa.minecraftplayground;

import net.minecraft.gametest.framework.GameTestHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A thin SpecFlow-like base class for Java GameTests.
 * Provides Given, When, Then functionality using Runnables.
 */
public class SpecFlowBase extends GameTestFramework {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final GameTestHelper helper;
    private int tick = 0;

    public SpecFlowBase(GameTestHelper helper) {
        this.helper = helper;
    }

    public SpecFlowBase given(String description, Runnable action) {
        LOGGER.info("GIVEN: {}", description);
        helper.runAtTickTime(tick++, action);
        return this;
    }

    public SpecFlowBase when(String description, Runnable action) {
        LOGGER.info("WHEN: {}", description);
        helper.runAtTickTime(tick++, action);
        return this;
    }

    public SpecFlowBase then(String description, Runnable action) {
        LOGGER.info("THEN: {}", description);
        helper.runAtTickTime(tick++, action);
        return this;
    }

    /**
     * A terminal THEN step that completes the test.
     */
    public void thenSucceed(String description, Runnable action) {
        LOGGER.info("THEN (Final): {}", description);
        helper.runAtTickTime(tick++, () -> {
            action.run();
            helper.succeed();
        });
    }

    /**
     * Simply succeed after the last step.
     */
    public void succeed() {
        helper.runAtTickTime(tick++, helper::succeed);
    }
}
