package at.osa.minecraftplayground;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;


// structure name is <classname>.<testname>.nbt (all lowercase)
// create a structure by
// 1. creating a @GameTest annotated test function
// 2. running the dev server
// 3. connecting via dev client
// 4. run /test create <classname>.<testname>
@GameTestHolder("minecraftplayground")
public class SimpleGameTests {



    @GameTest
    public static void coordinatesTest(GameTestHelper helper) {
        //   x
        //   * ---->
        // z |\ (up/down)
        //   v y
        //
        // x = horizontal (left, right)
        // y = normal (up / down)
        // z = vertical (forward, backward)
        //
        // Test special positions
        assertBlockNameAtPosition(helper, "Structure Block", 0, 0, 0); // coordinate origin
        assertBlockNameAtPosition(helper, "Air", 0, 2, 0);  // above the blue wool

        // Define the expected 3x3 grid at y=1 (2D array indexed by [z][x])
        // This represents the horizontal plane one block above the structure block
        String[][] expectedGrid = {
            {"Blue Wool", "Light Blue Wool", "Deepslate Bricks", "Red Wool", "Brown Wool"},
            {"Light Blue Wool", "Blue Wool", "Deepslate Bricks", "Brown Wool", "Red Wool"},
            {"Infested Stone Bricks", "Infested Stone Bricks", "Black Wool", "Red Nether Bricks", "Red Nether Bricks"},
            {"Yellow Wool", "Orange Wool", "Bricks", "Green Wool", "Lime Wool"},
            {"Orange Wool", "Yellow Wool", "Bricks", "Lime Wool", "Green Wool"}
        };

        validate2DXZGrid(helper, expectedGrid, 0, 0, 1);

        helper.succeed();
    }

    @GameTest
    public static void leverActionTest(GameTestHelper helper) {
        var redstoneLampPosition = new BlockPos(2, 2, 2);
        var leverBlockPosition = new BlockPos(2, 3, 2);
        assertBlockNameAtPosition(helper, "Redstone Lamp", redstoneLampPosition); // 9
        assertBlockNameAtPosition(helper, "Lever", leverBlockPosition); // 10


        var leverBlockState = helper.getBlockState(leverBlockPosition);
        if (leverBlockState.getBlock() instanceof LeverBlock leverBlock) {
            // run the lever action at tick 1
            helper.runAtTickTime(1, () -> pullLever(helper, leverBlockPosition));

            // Schedule assertions after the lever action completes
            helper.runAtTickTime(2, () -> {
                // Re-fetch block state since BlockState is immutable
                var currentLeverState = helper.getBlockState(leverBlockPosition);
                assertLeverIsPowered(helper, currentLeverState);
                assertRedstoneLampIsLit(helper, redstoneLampPosition);
                helper.succeed();
            });
        } else {
            helper.fail("Block at lever position is not a lever");
        }
    }

    private static void pullLever(GameTestHelper helper, BlockPos relativePosition) {
        var absolutePos = helper.absolutePos(relativePosition);
        var currentState = helper.getBlockState(relativePosition);

        var block = currentState.getBlock();
        if (!(block instanceof LeverBlock leverBlock)) {
            throw new IllegalStateException(String.format(String.format("Block at lever position %s is not a lever but a %s", relativePosition, block.getName())));
        }

        // triggers block state change (typically on the next game tick, not immediately)
        leverBlock.pull(currentState, helper.getLevel(), absolutePos, null);
    }

    private static void assertLeverIsPowered(GameTestHelper helper, BlockState leverBlockState) {
        if (leverBlockState.getBlock() instanceof LeverBlock leverBlock) {
            var isPowered = leverBlockState.getValue(LeverBlock.POWERED);
            helper.assertTrue(isPowered.booleanValue(), "Lever is not powered");
        } else {
            helper.fail("Block at lever position is not a lever");
        }
    }

    private static void assertRedstoneLampIsLit(GameTestHelper helper, BlockState redstoneLampState) {
        if (redstoneLampState.getBlock() instanceof RedstoneLampBlock redstoneLampBlock) {
            var isLit = redstoneLampState.getValue(RedstoneLampBlock.LIT);
            helper.assertTrue(isLit, "Redstone Lamp is not lit");
        } else {
            helper.fail("Block at redstone lamp position is not a redstone lamp");
        }
    }

    private static void assertRedstoneLampIsLit(GameTestHelper helper, BlockPos position) {
        assertRedstoneLampIsLit(helper, helper.getBlockState(position));
    }

    private static String getBlockNameAtPosition(GameTestHelper helper, int x, int y, int z) {
        var pos = new BlockPos(x, y, z);
        var state = helper.getBlockState(pos);
        var block = state.getBlock();
        return block.getName().getString();
    }

    private static void assertBlockNameAtPosition(GameTestHelper helper, String expected, int x, int y, int z) {
        var blockName = getBlockNameAtPosition(helper, x, y, z);
        helper.assertTrue(blockName.equals(expected), "Block at position is not a %s but a %s".formatted(expected, blockName));
    }

    private static void assertBlockNameAtPosition(GameTestHelper helper, String expected, BlockPos pos) {
        assertBlockNameAtPosition(helper, expected, pos.getX(), pos.getY(), pos.getZ());
    }

    private static void validate2DXZGrid(GameTestHelper helper, String[][] expectedGrid, int startX, int startZ, int y) {
        for (int z = startZ; z < expectedGrid.length; z++) {
            for (int x = startX; x < expectedGrid[z].length; x++) {
                assertBlockNameAtPosition(helper, expectedGrid[z][x], x, y, z);
            }
        }
    }
}
