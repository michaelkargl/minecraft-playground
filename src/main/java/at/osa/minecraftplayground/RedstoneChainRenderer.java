package at.osa.minecraftplayground;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Renders cables between connected RedstoneChainEntity blocks.
 * Cables sag realistically and are colored based on power level.
 *
 * @credit Create Crafts & Additions: https://github.com/mrh0/createaddition
 * @credit Overhead Redstone Wires: https://github.com/MaxLegend/OverheadRedstoneWires
 */
public class RedstoneChainRenderer implements BlockEntityRenderer<RedstoneChainEntity> {

    public RedstoneChainRenderer(BlockEntityRendererProvider.Context ctx) {
        super();
    }

    @Override
    public void render(RedstoneChainEntity entity, float partialTicks, PoseStack stack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockPos blockPos = entity.getBlockPos();
        int power = entity.getSignal();

        for (BlockPos connection : entity.getConnections()) {
            if (blockPos.compareTo(connection) < 0) {
                Vec3 start = new Vec3(0.5, 0.5, 0.5);
                Vec3 end = Vec3.atCenterOf(connection)
                        .subtract(Vec3.atCenterOf(blockPos))
                        .add(0.5, 0.5, 0.5);

                renderCable(stack, buffer, start, end, power, packedLight, packedOverlay);
            }
        }
    }

    /**
     * Renders a cable as segments with quad geometry.
     */
    private void renderCable(PoseStack stack, MultiBufferSource buffer, Vec3 from, Vec3 to,
                            int power, int light, int overlay) {
        VertexConsumer builder = buffer.getBuffer(RedstoneRenderType.CABLE);
        Matrix4f matrix = stack.last().pose();

        int segments = Config.getCableSegments();
        for (int i = 0; i < segments; i++) {
            float t1 = i / (float) segments;
            float t2 = (i + 1) / (float) segments;

            Vec3 p1 = interpolateCurved(from, to, t1);
            Vec3 p2 = interpolateCurved(from, to, t2);

            // Select color set based on odd/even segment
            float r, g, b;
            if (i % 2 == 0) {
                // Even segments: primary color
                r = (float) getColorComponent(power, Config.getUnpoweredRed(), Config.getPoweredRedBase(), Config.getPoweredRedBonus(), true);
                g = (float) Config.getGreenValue();
                b = (float) Config.getBlueValue();
            } else {
                // Odd segments: alternate color
                r = (float) getColorComponent(power, Config.getUnpoweredRedAlt(), Config.getPoweredRedBaseAlt(), Config.getPoweredRedBonusAlt(), true);
                g = (float) Config.getGreenValueAlt();
                b = (float) Config.getBlueValueAlt();
            }

            drawSegment(builder, matrix, p1, p2, r, g, b, light, overlay);
        }
    }

    /**
     * Draw a single cable segment as a rounded cylinder with many radial faces.
     * Creates a rope-like appearance with outward-facing normals on all faces.
     */
    private void drawSegment(VertexConsumer builder, Matrix4f matrix, Vec3 p1, Vec3 p2,
                            float r, float g, float b, int light, int overlay) {
        Vec3 direction = p2.subtract(p1).normalize();

        // Create base perpendicular vector
        Vec3 up = Math.abs(direction.y) > 0.999 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 basePerp = direction.cross(up).normalize().scale(Config.getCableThickness());

        // Create multiple perpendicular vectors around the cable (12 faces for smooth cylinder)
        int sides = Config.getCableSides();
        Vec3[] perpVectors = new Vec3[sides];
        for (int i = 0; i < sides; i++) {
            double angle = (i / (double) sides) * 2 * Math.PI;
            perpVectors[i] = rotateAroundAxis(basePerp, direction, Math.toDegrees(angle));
        }

        // Draw faces between consecutive perpendicular vectors
        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides;

            // 4 corners of this quad strip
            Vec3 p1_current = p1.add(perpVectors[i]);
            Vec3 p1_next = p1.add(perpVectors[next]);
            Vec3 p2_current = p2.add(perpVectors[i]);
            Vec3 p2_next = p2.add(perpVectors[next]);

            // Draw quad - vertices ordered so normal points outward
            drawQuad(builder, matrix, p1_current, p1_next, p2_next, p2_current, r, g, b, light, overlay);
        }
    }

    /**
     * Rotates a vector around an arbitrary axis using Rodrigues' rotation formula.
     */
    private Vec3 rotateAroundAxis(Vec3 v, Vec3 k, double angleDegrees) {
        double angleRad = Math.toRadians(angleDegrees);
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);
        double oneMinusCos = 1.0 - cos;

        Vec3 term1 = v.scale(cos);
        Vec3 term2 = k.cross(v).scale(sin);
        double dotProduct = k.dot(v);
        Vec3 term3 = k.scale(dotProduct * oneMinusCos);

        return term1.add(term2).add(term3);
    }

    /**
     * Draw a single quad face (4 corners) as 2 triangles.
     */
    private void drawQuad(VertexConsumer builder, Matrix4f matrix,
                         Vec3 c0, Vec3 c1, Vec3 c2, Vec3 c3,
                         float r, float g, float b, int light, int overlay) {
        // Calculate normal from the first two edges
        Vec3 edge1 = c1.subtract(c0);
        Vec3 edge2 = c2.subtract(c1);
        Vec3 normal = edge1.cross(edge2).normalize();

        // First triangle
        addVertex(builder, matrix, c0, r, g, b, light, overlay, normal);
        addVertex(builder, matrix, c1, r, g, b, light, overlay, normal);
        addVertex(builder, matrix, c2, r, g, b, light, overlay, normal);

        // Second triangle
        addVertex(builder, matrix, c0, r, g, b, light, overlay, normal);
        addVertex(builder, matrix, c2, r, g, b, light, overlay, normal);
        addVertex(builder, matrix, c3, r, g, b, light, overlay, normal);
    }

    /**
     * Gets the color component based on power level.
     */
    private double getColorComponent(int power, double unpowered, double base, double bonus, boolean isRed) {
        if (isRed && power > 0) {
            return base + (power / 15.0f) * bonus;
        }
        return isRed ? unpowered : (isRed ? 0 : Config.getGreenValue());
    }

    /**
     * Interpolates along a curved cable path with sagging effect.
     */
    private static Vec3 interpolateCurved(Vec3 from, Vec3 to, float t) {
        Vec3 linear = from.lerp(to, t);
        double horizontalDist = Math.abs(from.x - to.x) + Math.abs(from.z - to.z);

        if (horizontalDist < 0.001) {
            return linear;
        }

        double sag = Math.sin(t * Math.PI) * Config.getCableSagAmount();
        return new Vec3(linear.x, linear.y + sag, linear.z);
    }

    /**
     * Adds a vertex to the builder with custom normal.
     */
    private void addVertex(VertexConsumer builder, Matrix4f matrix, Vec3 pos,
                          float r, float g, float b, int light, int overlay, Vec3 normal) {
        builder.addVertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(r, g, b, 1f)
                .setUv(0, 0)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal((float) normal.x, (float) normal.y, (float) normal.z);
    }


}
