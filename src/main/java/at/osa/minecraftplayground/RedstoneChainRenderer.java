package at.osa.minecraftplayground;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Renders cables between connected RedstoneChainEntity blocks.
 * Based on OverheadRedstoneWires cable rendering with power-based coloring.
 * The cables sag realistically like overhead wires would under gravity.
 */
public class RedstoneChainRenderer implements BlockEntityRenderer<RedstoneChainEntity> {

    public static final RenderType CABLE_RENDER_TYPE = RenderType.create(
            "redstone_cable_render",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.TRIANGLES,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorLightmapShader))
                    .setCullState(new RenderStateShard.CullStateShard(false)) // Disable culling
                    .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                    .setOverlayState(new RenderStateShard.OverlayStateShard(true))
                    .createCompositeState(false)
    );

    public RedstoneChainRenderer(BlockEntityRendererProvider.Context ctx) {
        super();
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public void render(RedstoneChainEntity entity, float partialTicks, PoseStack stack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Vec3 center = Vec3.atCenterOf(entity.getBlockPos()).subtract(Vec3.atCenterOf(entity.getBlockPos()));
        BlockPos blockPos = entity.getBlockPos();

        int power = entity.getSignal();

        for (BlockPos connection : entity.getConnections()) {
            // Only render if this block's position is "less than" the connection
            // This prevents rendering the same cable twice from both ends
            if (blockPos.compareTo(connection) < 0) {
                Vec3 end = Vec3.atCenterOf(connection).subtract(Vec3.atCenterOf(blockPos));
                renderCurvedCuboid(stack, buffer, center.add(0.5, 0.5, 0.5), end.add(0.5, 0.5, 0.5),
                        packedLight, packedOverlay, power);
            }
        }
    }

    /**
     * Renders a curved cable between two points (from OverheadRedstoneWires).
     * Enhanced with power-based coloring.
     */
    public static void renderCurvedCuboid(PoseStack poseStack, MultiBufferSource buffer,
                                          Vec3 from, Vec3 to, int light, int overlay, int power) {
        VertexConsumer builder = buffer.getBuffer(CABLE_RENDER_TYPE);

        int segments = 12;
        float thickness = 0.03F;

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        // Enhanced cable colors - looks more like insulated wire
        float red, green, blue;
        if (power > 0) {
            // Powered: Bright red insulation (like hot wire)
            red = 0.9f + (power / 15.0f) * 0.1f;  // 0.9 to 1.0
            green = 0.0f;
            blue = 0.0f;
        } else {
            // Unpowered: Dark gray/black insulation
            red = 0.2f;
            green = 0.2f;
            blue = 0.2f;
        }

        for (int i = 0; i < segments; i++) {
            float t1 = i / (float) segments;
            float t2 = (i + 1) / (float) segments;

            Vec3 p1 = interpolateCurved(from, to, t1);
            Vec3 p2 = interpolateCurved(from, to, t2);

            drawThickSegment(builder, matrix, normal, p1, p2, thickness, light, overlay, red, green, blue);
        }
    }

    /**
     * Interpolates along a curved path (from OverheadRedstoneWires).
     * Creates realistic cable sag.
     */
    private static Vec3 interpolateCurved(Vec3 from, Vec3 to, float t) {
        Vec3 linear = from.lerp(to, t);
        if (Math.abs(from.x - to.x) < 0.001 && Math.abs(from.z - to.z) < 0.001) {
            return linear;
        }
        double curveAmplitude = 0.4;
        double curve = Math.sin(t * Math.PI) * -curveAmplitude; // Sag downward
        return new Vec3(linear.x, linear.y + curve, linear.z);
    }

    /**
     * Draws a smooth cylindrical cable segment.
     * Creates a round cable appearance instead of rectangular segments.
     */
    private static void drawThickSegment(VertexConsumer builder, Matrix4f matrix, Matrix3f normal,
                                         Vec3 p1, Vec3 p2, float thickness, int light, int overlay,
                                         float r, float g, float b) {
        // Calculate direction vector
        Vec3 dir = p2.subtract(p1).normalize();
        Vec3 up = Math.abs(dir.y) > 0.999 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = dir.cross(up).normalize();
        Vec3 forward = dir.cross(right).normalize();

        // Number of sides for the cylinder (more = smoother, but more vertices)
        int sides = 8; // Octagonal cross-section looks smooth enough

        // Generate circle vertices at both ends
        Vec3[] circle1 = new Vec3[sides];
        Vec3[] circle2 = new Vec3[sides];

        for (int i = 0; i < sides; i++) {
            double angle = (Math.PI * 2.0 * i) / sides;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            Vec3 offset = right.scale(cos * thickness).add(forward.scale(sin * thickness));
            circle1[i] = p1.add(offset);
            circle2[i] = p2.add(offset);
        }

        // Draw the cylinder surface as quads between the two circles
        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides;

            // Calculate normal for this face (pointing outward from cylinder center)
            double angle = (Math.PI * 2.0 * i) / sides;
            Vec3 normalVec = right.scale(Math.cos(angle)).add(forward.scale(Math.sin(angle))).normalize();

            // Calculate shading based on angle (top brighter, bottom darker)
            // This makes it look like a round cable catching light from above
            double angleFromTop = Math.abs(angle - Math.PI / 2);
            float shadeFactor = (float) (0.7 + 0.3 * Math.cos(angleFromTop));

            // UV coordinates wrap around the cylinder
            float u1 = (float) i / sides;
            float u2 = (float) next / sides;

            // Draw quad as two triangles in BOTH winding orders
            // This ensures visibility from all angles (prevents disappearing)

            // FRONT-FACING triangles (clockwise winding)
            // Triangle 1: circle1[i], circle2[i], circle2[next]
            builder.addVertex(matrix, (float) circle1[i].x, (float) circle1[i].y, (float) circle1[i].z)
                    .setColor(r * shadeFactor, g * shadeFactor, b * shadeFactor, 1.0f)
                    .setUv(u1, 0)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal((float) normalVec.x, (float) normalVec.y, (float) normalVec.z);

            builder.addVertex(matrix, (float) circle2[i].x, (float) circle2[i].y, (float) circle2[i].z)
                    .setColor(r * shadeFactor, g * shadeFactor, b * shadeFactor, 1.0f)
                    .setUv(u1, 1)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal((float) normalVec.x, (float) normalVec.y, (float) normalVec.z);

            builder.addVertex(matrix, (float) circle2[next].x, (float) circle2[next].y, (float) circle2[next].z)
                    .setColor(r * shadeFactor, g * shadeFactor, b * shadeFactor, 1.0f)
                    .setUv(u2, 1)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal((float) normalVec.x, (float) normalVec.y, (float) normalVec.z);

            // Triangle 2: circle1[i], circle2[next], circle1[next]
            builder.addVertex(matrix, (float) circle1[i].x, (float) circle1[i].y, (float) circle1[i].z)
                    .setColor(r * shadeFactor, g * shadeFactor, b * shadeFactor, 1.0f)
                    .setUv(u1, 0)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal((float) normalVec.x, (float) normalVec.y, (float) normalVec.z);

            builder.addVertex(matrix, (float) circle2[next].x, (float) circle2[next].y, (float) circle2[next].z)
                    .setColor(r * shadeFactor, g * shadeFactor, b * shadeFactor, 1.0f)
                    .setUv(u2, 1)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal((float) normalVec.x, (float) normalVec.y, (float) normalVec.z);

            builder.addVertex(matrix, (float) circle1[next].x, (float) circle1[next].y, (float) circle1[next].z)
                    .setColor(r * shadeFactor, g * shadeFactor, b * shadeFactor, 1.0f)
                    .setUv(u2, 0)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal((float) normalVec.x, (float) normalVec.y, (float) normalVec.z);

            // BACK-FACING triangles (counter-clockwise winding)
            // Same triangles but reversed vertex order
            // Triangle 1 reversed: circle2[next], circle2[i], circle1[i]
            builder.addVertex(matrix, (float) circle2[next].x, (float) circle2[next].y, (float) circle2[next].z)
                    .setColor(r * shadeFactor, g * shadeFactor, b * shadeFactor, 1.0f)
                    .setUv(u2, 1)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal((float) -normalVec.x, (float) -normalVec.y, (float) -normalVec.z);

            builder.addVertex(matrix, (float) circle2[i].x, (float) circle2[i].y, (float) circle2[i].z)
                    .setColor(r * shadeFactor, g * shadeFactor, b * shadeFactor, 1.0f)
                    .setUv(u1, 1)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal((float) -normalVec.x, (float) -normalVec.y, (float) -normalVec.z);

            builder.addVertex(matrix, (float) circle1[i].x, (float) circle1[i].y, (float) circle1[i].z)
                    .setColor(r * shadeFactor, g * shadeFactor, b * shadeFactor, 1.0f)
                    .setUv(u1, 0)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal((float) -normalVec.x, (float) -normalVec.y, (float) -normalVec.z);

            // Triangle 2 reversed: circle1[next], circle2[next], circle1[i]
            builder.addVertex(matrix, (float) circle1[next].x, (float) circle1[next].y, (float) circle1[next].z)
                    .setColor(r * shadeFactor, g * shadeFactor, b * shadeFactor, 1.0f)
                    .setUv(u2, 0)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal((float) -normalVec.x, (float) -normalVec.y, (float) -normalVec.z);

            builder.addVertex(matrix, (float) circle2[next].x, (float) circle2[next].y, (float) circle2[next].z)
                    .setColor(r * shadeFactor, g * shadeFactor, b * shadeFactor, 1.0f)
                    .setUv(u2, 1)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal((float) -normalVec.x, (float) -normalVec.y, (float) -normalVec.z);

            builder.addVertex(matrix, (float) circle1[i].x, (float) circle1[i].y, (float) circle1[i].z)
                    .setColor(r * shadeFactor, g * shadeFactor, b * shadeFactor, 1.0f)
                    .setUv(u1, 0)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal((float) -normalVec.x, (float) -normalVec.y, (float) -normalVec.z);
        }
    }
}
