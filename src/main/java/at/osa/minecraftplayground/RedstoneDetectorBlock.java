package at.osa.minecraftplayground;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;

public class RedstoneDetectorBlock extends Block {

    public RedstoneDetectorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        if (!level.isClientSide) {
            int signalStrength = level.getBestNeighborSignal(pos);
            MinecraftPlayground.LOGGER.info("Redstone Detector at {} received signal strength: {}", pos, signalStrength);
            // log to client console as well
            level.players().forEach(player -> {
                player.sendSystemMessage(Component.literal("@"+player.getName()+": Redstone Detector at " + pos + " received signal strength: " + signalStrength));
            });
        }
    }
}
