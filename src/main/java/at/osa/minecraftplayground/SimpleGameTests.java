package at.osa.minecraftplayground;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("minecraftplayground")
public class SimpleGameTests {
    @GameTest(template = "minecraft-playground-test-structure")
    public static void simpleMathTest(GameTestHelper helper) {
        int result = 1 + 2;
        if (result == 4) {
            helper.succeed();
        } else {
            helper.fail("Expected 1+2 to equal 3, but got " + result);
        }
    }
}
