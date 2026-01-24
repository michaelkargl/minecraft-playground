package at.osa.minecraftplayground;
import net.minecraft.world.item.ItemStack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.LeverBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;

// structure name is <classname>.<testname>.nbt (all lowercase)
// create a structure by
// 1. creating a @GameTest annotated test function
// 2. running the dev server
// 3. connecting via dev client
// 4. run /test create <classname>.<testname>
@GameTestHolder("minecraftplayground")
public class SimpleGameTests extends GameTestFramework  {

    @GameTest
    public static void coordinatesTest(GameTestHelper helper) {
        //
        //   x-axis: horizontal (west ← → east)
        //   y-axis: vertical (down ↓ ↑ up)
        //   z-axis: depth (north ↑ ↓ south)
        //
        // Visual representation from above (bird's eye view):
        //        x →
        //      0   1   2   3   4
        //    ┌─────────────────────
        // z 0│ BW  LBW  DB  RW  BrW
        // ↓ 1│ LBW  BW  DB  BrW  RW
        //   2│ ISB ISB  BlW RNB RNB
        //   3│ YW  OW   Br  GW  LW
        //   4│ OW  YW   Br  LW  GW
        //
        // Legend: BW=Blue Wool, LBW=Light Blue Wool, DB=Deepslate Bricks,
        //         RW=Red Wool, BrW=Brown Wool, ISB=Infested Stone Bricks,
        //         BlW=Black Wool, RNB=Red Nether Bricks, YW=Yellow Wool,
        //         OW=Orange Wool, Br=Bricks, GW=Green Wool, LW=Lime Wool

        // Test origin and reference points
        assertBlockNameAtPosition(helper, "Structure Block", 0, 0, 0); // (0,0,0) - structure origin
        assertBlockNameAtPosition(helper, "Air", 0, 2, 0);  // (0,2,0) - two blocks above origin

        String[][] expectedGrid = {
            {"Blue Wool", "Light Blue Wool", "Deepslate Bricks", "Red Wool", "Brown Wool"},                    // z=0
            {"Light Blue Wool", "Blue Wool", "Deepslate Bricks", "Brown Wool", "Red Wool"},                    // z=1
            {"Infested Stone Bricks", "Infested Stone Bricks", "Black Wool", "Red Nether Bricks", "Red Nether Bricks"}, // z=2
            {"Yellow Wool", "Orange Wool", "Bricks", "Green Wool", "Lime Wool"},                               // z=3
            {"Orange Wool", "Yellow Wool", "Bricks", "Lime Wool", "Green Wool"}                                // z=4
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

    @GameTest(template="leverinputoutputtest")
    public static void leverinputoutputtest(GameTestHelper helper) {
        var inputChainBlockPosition = new BlockPos(1, 2, 1);
        var outputChainBlockPosition = new BlockPos(3, 2, 3);

        // Verify the structure has the correct blocks
        assertBlockNameAtPosition(helper, "Redstone Chain", inputChainBlockPosition);
        assertBlockNameAtPosition(helper, "Redstone Chain", outputChainBlockPosition);

        // Create a chain connector item
        var connectorItem = new ItemStack(MinecraftPlayground.REDSTONE_CHAIN_CONNECTOR.get());

        // Schedule the connection process
        helper.runAtTickTime(1, () -> {
            // First click: crouch-click the first chain block to save its position
            crouchClickBlock(helper, inputChainBlockPosition, connectorItem);
        });

        helper.runAtTickTime(3, () -> {
            // Second click: crouch-click the second chain block to create the connection
            crouchClickBlock(helper, outputChainBlockPosition, connectorItem);
        });

        // Verify the connection was created (after both clicks complete)
        helper.runAtTickTime(5, () -> {
            assertChainBlocksAreConnected(helper, inputChainBlockPosition, outputChainBlockPosition);
            helper.succeed();
        });
    }
}
