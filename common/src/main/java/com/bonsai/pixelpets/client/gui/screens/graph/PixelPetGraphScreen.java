package com.bonsai.pixelpets.client.gui.screens.graph;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.client.PixelPetScreenRenderingUtil;
import com.bonsai.pixelpets.pixelpets.registration.PixelPetData;
import com.bonsai.pixelpets.pixelpets.registration.data.Rarity;
import com.bonsai.pixelpets.util.IGraphLayout;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

// TODO sfx
public class PixelPetGraphScreen extends Screen {
    private static final Component TITLE = Component.translatable("gui.pixelpets.pet_graph");
    private static final Component EMPTY_LABEL = Component.translatable("gui.pixelpets.pet_graph.empty");
    private static final Component EMPTY_HINT = Component.translatable("gui.pixelpets.pet_graph.empty_hint");

    private static final int NODE_SIZE = PixelPetScreenRenderingUtil.NODE_SIZE;
    private static final int MARGIN = 20;
    private static final int KEEP = 40;
    private static final int RENDER_MARGIN = 40;
    private static final double SCROLL_SPEED = 16.0;

    private static final double MIN_SCALE = 0.4;
    private static final double MAX_SCALE = 2.0;
    private static final double ZOOM_FACTOR = 1.15;

    private static final int BACKGROUND = 0xFF000000; // TODO replace with texture

    private static final int EDGE_DASH_LENGTH = 4;
    private static final int EDGE_ALTERNATE_LENGTH = 2;

    private static final double EDGE_THICKNESS = 3.0;
    private static final double EDGE_ENDPOINT_OVERLAP = 3.0;
    private static final double MITER_LIMIT = 4.0;

    private static final ResourceLocation AND_EDGE_ACTIVE_TEXTURE = PixelPets.id("textures/gui/pet_graph/edge/and_edge_active.png");
    private static final ResourceLocation AND_EDGE_INACTIVE_TEXTURE = PixelPets.id("textures/gui/pet_graph/edge/and_edge_inactive.png");
    private static final ResourceLocation OR_EDGE_ACTIVE_TEXTURE = PixelPets.id("textures/gui/pet_graph/edge/or_edge_active.png");
    private static final ResourceLocation OR_EDGE_INACTIVE_TEXTURE = PixelPets.id("textures/gui/pet_graph/edge/or_edge_inactive.png");
    private static final ResourceLocation OR_EDGE_ALTERNATE_ACTIVE_TEXTURE = PixelPets.id("textures/gui/pet_graph/edge/or_edge_alternate_active.png");
    private static final ResourceLocation OR_EDGE_ALTERNATE_INACTIVE_TEXTURE = PixelPets.id("textures/gui/pet_graph/edge/or_edge_alternate_inactive.png");

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 33);
    private final IGraphLayout<ResourceLocation> graph;
    private final Set<ResourceLocation> caughtPets;

    private final double contentLeft;
    private final double contentTop;
    private final double contentRight;
    private final double contentBottom;

    private double panX;
    private double panY;
    private double scale = 1.0;
    private boolean panning;

    @Nullable
    private PixelPetSpawnInfoPanel infoPanel;

    public PixelPetGraphScreen(Set<ResourceLocation> caughtPets) {
        this(caughtPets, PixelPetScreenRenderingUtil.getGraph());
    }

    public PixelPetGraphScreen(Set<ResourceLocation> caughtPets, IGraphLayout<ResourceLocation> graph) {
        super(TITLE);
        this.caughtPets = caughtPets;
        this.graph = graph;

        IGraphLayout.Bounds bounds = graph.bounds();
        double half = NODE_SIZE * 0.5;

        contentLeft = bounds.minX() - half;
        contentTop = bounds.minY() - half;
        contentRight = bounds.maxX() + half;
        contentBottom = bounds.maxY() + half;
    }

    private static double initialPan(double viewMin, double viewMax, double contentMin, double contentMax, double scale) {
        double contentSize = (contentMax - contentMin) * scale;
        double room = (viewMax - viewMin) - contentSize;

        return room >= 0 ? viewMin + room * 0.5 - contentMin * scale : viewMin + MARGIN - contentMin * scale;
    }

    private static double clampPanAxis(double pan, double viewMin, double viewMax, double contentMin, double contentMax) {
        double size = contentMax - contentMin;
        double viewSize = viewMax - viewMin;

        if (size + 2.0 * KEEP <= viewSize) {
            return viewMin + (viewSize - size) * 0.5 - contentMin;
        }

        return Mth.clamp(pan, viewMax - KEEP - contentMax, viewMin + KEEP - contentMin);
    }

    private static int px(double value) {
        return (int) Math.round(value);
    }

    private static boolean samePoint(IGraphLayout.Point a, IGraphLayout.Point b) {
        return Math.abs(a.x() - b.x()) < 1e-9 && Math.abs(a.y() - b.y()) < 1e-9;
    }

    private static List<IGraphLayout.Point> clipPath(List<IGraphLayout.Point> original, double halfSize) {
        if (original.size() < 2) {
            return List.of();
        }

        List<IGraphLayout.Point> path = new ArrayList<>(original);

        int first = firstDistinct(path, 0, 1);
        int last = lastDistinct(path, path.size() - 1, path.size() - 2);

        if (first < 0 || last < 0) {
            return List.of();
        }

        path.set(0, clipFromCenter(path.getFirst(), path.get(first), halfSize));

        int end = path.size() - 1;
        path.set(end, clipFromCenter(path.get(end), path.get(last), halfSize));

        return path;
    }

    private static int firstDistinct(List<IGraphLayout.Point> path, int a, int b) {
        IGraphLayout.Point first = path.get(a);

        for (int i = b; i < path.size(); i++) {
            if (!samePoint(first, path.get(i))) {
                return i;
            }
        }

        return -1;
    }

    private static int lastDistinct(List<IGraphLayout.Point> path, int a, int b) {
        IGraphLayout.Point last = path.get(a);

        for (int i = b; i >= 0; i--) {
            if (!samePoint(last, path.get(i))) {
                return i;
            }
        }

        return -1;
    }

    private static IGraphLayout.Point clipFromCenter(IGraphLayout.Point center, IGraphLayout.Point toward, double halfSize) {
        double dx = toward.x() - center.x();
        double dy = toward.y() - center.y();
        double max = Math.max(Math.abs(dx), Math.abs(dy));

        if (max < 1e-9) {
            return center;
        }

        double factor = halfSize / max;

        return new IGraphLayout.Point(center.x() + dx * factor, center.y() + dy * factor);
    }

    private static IGraphLayout.Point lerp(IGraphLayout.Point a, IGraphLayout.Point b, double t) {
        return new IGraphLayout.Point(a.x() + (b.x() - a.x()) * t, a.y() + (b.y() - a.y()) * t);
    }

    private static void addEdgeVertex(VertexConsumer consumer, Matrix4f matrix, IGraphLayout.Point point, float u, float v) {
        consumer.addVertex(matrix, (float) point.x(), (float) point.y(), 0.0F).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0, 0, 1);
    }

    private static void addEdgeVertex(VertexConsumer consumer, Matrix4f matrix, IGraphLayout.Point point, float u, float v, int color) {
        consumer.addVertex(matrix, (float) point.x(), (float) point.y(), 0.0F).setColor(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color), 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0, 0, 1);
    }

    private static List<IGraphLayout.Point> simplifyPath(List<IGraphLayout.Point> path) {
        if (path.isEmpty()) {
            return List.of();
        }

        List<IGraphLayout.Point> result = new ArrayList<>(path.size());
        for (IGraphLayout.Point point : path) {
            if (result.isEmpty() || !samePoint(result.getLast(), point)) {
                result.add(point);
            }
        }
        return result;
    }

    @Override
    protected void init() {
        layout.addTitleHeader(TITLE, font);
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();

        scale = 1.0;
        panX = initialPan(viewLeft(), viewRight(), contentLeft, contentRight, scale);
        panY = initialPan(viewTop(), viewBottom(), contentTop, contentBottom, scale);
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        clampPan();
    }

    private int viewLeft() {
        return 0;
    }

    private int viewRight() {
        return infoPanel != null ? infoPanel.left(width) : width;
    }

    private int viewTop() {
        return layout.getHeaderHeight();
    }

    private int viewBottom() {
        return height - layout.getFooterHeight();
    }

    private boolean inViewport(double x, double y) {
        return x >= viewLeft() && x < viewRight() && y >= viewTop() && y < viewBottom();
    }

    private void pan(double dx, double dy) {
        panX += dx;
        panY += dy;
        clampPan();
    }

    private void zoom(double mouseX, double mouseY, double wheel) {
        if (Math.abs(wheel) < 1e-9) {
            return;
        }

        double oldScale = scale;
        double newScale = Mth.clamp(oldScale * Math.pow(ZOOM_FACTOR, wheel), MIN_SCALE, MAX_SCALE);

        if (Math.abs(newScale - oldScale) < 1e-9) {
            return;
        }

        double graphX = (mouseX - panX) / oldScale;
        double graphY = (mouseY - panY) / oldScale;

        scale = newScale;
        panX = mouseX - graphX * scale;
        panY = mouseY - graphY * scale;

        clampPan();
    }

    private void clampPan() {
        panX = clampPanAxis(panX, viewLeft(), viewRight(), contentLeft * scale, contentRight * scale);

        panY = clampPanAxis(panY, viewTop(), viewBottom(), contentTop * scale, contentBottom * scale);
    }

    private void setInfoPanel(@Nullable PixelPetSpawnInfoPanel panel) {
        double oldCenter = (viewLeft() + viewRight()) * 0.5;

        infoPanel = panel;

        double newCenter = (viewLeft() + viewRight()) * 0.5;
        panX += newCenter - oldCenter;

        clampPan();
    }

@Override
public boolean mouseClicked(double mouseX, double mouseY, int button) {
    // The panel gets first chance to handle the click.
    if (infoPanel != null && infoPanel.mouseClicked(mouseX, mouseY, button)) {
        return true;
    }

    if (super.mouseClicked(mouseX, mouseY, button)) {
        return true;
    }

    if (button != 0 || !inViewport(mouseX, mouseY)) {
        return false;
    }

    ResourceLocation node = hoveredNode(mouseX, mouseY);

    if (node != null) {
        // TODO uh-uh sfx
        if (!caughtPets.contains(node) && !graph.dependenciesMet(node, caughtPets)) {
            return true;
        }

        if (infoPanel != null && infoPanel.petId().equals(node)) {
            setInfoPanel(null);
        } else if (infoPanel != null) {
            infoPanel.setPetId(node);
        } else {
            setInfoPanel(new PixelPetSpawnInfoPanel(node, () -> setInfoPanel(null)));
        }

        return true;
    }

    panning = true;
    return true;
}

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            panning = false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (panning && button == 0) {
            pan(dragX, dragY);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        if (infoPanel != null && infoPanel.contains(mouseX, mouseY)) {
            return true;
        }

        if (!inViewport(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        if (Math.abs(scrollY) > 1e-9) {
            zoom(mouseX, mouseY, scrollY);
        } else {
            pan(scrollX * SCROLL_SPEED, 0);
        }

        return true;
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        g.fill(viewLeft(), viewTop(), viewRight(), viewBottom(), BACKGROUND);

        if (graph.nodes().isEmpty() || PixelPetScreenRenderingUtil.getPetRegistry() == null) {
            renderEmpty(g);
            return;
        }

        ResourceLocation hovered = hoveredNode(mouseX, mouseY);

        g.enableScissor(viewLeft(), viewTop(), viewRight(), viewBottom());
        g.pose().pushPose();
        g.pose().translate(panX, panY, 0);
        g.pose().scale((float) scale, (float) scale, 1.0f);

        Map<ResourceLocation, Integer> incomingCounts = incomingEdgeCounts();
        for (IGraphLayout.Edge<ResourceLocation> edge : graph.edges()) {
            List<IGraphLayout.Point> path = clipPath(edge.points(), NODE_SIZE * 0.5 - EDGE_ENDPOINT_OVERLAP);

            if (path.size() < 2 || !pathNearViewport(path)) {
                continue;
            }

            boolean solid = graph.requireAll(edge.target()) || incomingCounts.getOrDefault(edge.target(), 0) == 1;
            boolean active = caughtPets.contains(edge.source());
            renderTexturedPath(g, path, !solid, active);
        }
        g.flush();

        for (ResourceLocation node : graph.nodes()) {
            IGraphLayout.Point point = graph.position(node);
            int x = px(point.x()) - NODE_SIZE / 2;
            int y = px(point.y()) - NODE_SIZE / 2;

            if (isNearViewport(x, y, NODE_SIZE * MAX_SCALE)) {
                renderNode(g, node, x, y, node.equals(hovered));
            }
        }
        g.flush();
        g.pose().popPose();
        g.disableScissor();

        if (hovered != null) {
            renderNodeTooltip(g, hovered, mouseX, mouseY);
        }

        if (infoPanel != null) {
            infoPanel.render(g, mouseX, mouseY, width, height);
        }
    }

    private boolean pathNearViewport(List<IGraphLayout.Point> path) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (IGraphLayout.Point point : path) {
            minX = Math.min(minX, point.x());
            minY = Math.min(minY, point.y());
            maxX = Math.max(maxX, point.x());
            maxY = Math.max(maxY, point.y());
        }

        double left = panX + minX * scale;
        double top = panY + minY * scale;
        double right = panX + maxX * scale;
        double bottom = panY + maxY * scale;

        return right >= viewLeft() - RENDER_MARGIN && bottom >= viewTop() - RENDER_MARGIN && left <= viewRight() + RENDER_MARGIN && top <= viewBottom() + RENDER_MARGIN;
    }

    private boolean isNearViewport(double x, double y, double margin) {
        double left = panX + x * scale;
        double top = panY + y * scale;
        double right = left + NODE_SIZE * scale;
        double bottom = top + NODE_SIZE * scale;

        return right >= viewLeft() - margin && bottom >= viewTop() - margin && left <= viewRight() + margin && top <= viewBottom() + margin;
    }

    private Map<ResourceLocation, Integer> incomingEdgeCounts() {
        Map<ResourceLocation, Integer> counts = new HashMap<>();

        for (IGraphLayout.Edge<ResourceLocation> edge : graph.edges()) {
            counts.merge(edge.target(), 1, Integer::sum);
        }

        return counts;
    }

    private void renderTexturedPath(GuiGraphics g, List<IGraphLayout.Point> path, boolean alternating, boolean active) {
        List<IGraphLayout.Point> cleanPath = simplifyPath(path);
        if (cleanPath.size() >= 2) {
            renderStrokeMesh(g, cleanPath, alternating, active);
        }
    }

    private void renderStrokeMesh(GuiGraphics g, List<IGraphLayout.Point> points, boolean alternating, boolean active) {
        int count = points.size();
        if (count < 2) {
            return;
        }

        double[] segmentLengths = new double[count - 1];
        double[] cumulativeLengths = new double[count];
        double[] normalX = new double[count - 1];
        double[] normalY = new double[count - 1];

        for (int i = 0; i < count - 1; i++) {
            IGraphLayout.Point a = points.get(i);
            IGraphLayout.Point b = points.get(i + 1);
            double dx = b.x() - a.x();
            double dy = b.y() - a.y();
            double length = Math.hypot(dx, dy);

            if (length < 1.0e-9) {
                return;
            }

            segmentLengths[i] = length;
            cumulativeLengths[i + 1] = cumulativeLengths[i] + length;
            normalX[i] = -dy / length;
            normalY[i] = dx / length;
        }

        IGraphLayout.Point[] left = new IGraphLayout.Point[count];
        IGraphLayout.Point[] right = new IGraphLayout.Point[count];
        double halfThickness = EDGE_THICKNESS * 0.5;
        double maxMiterLength = halfThickness * MITER_LIMIT;

        for (int i = 0; i < count; i++) {
            double offsetX;
            double offsetY;

            if (i == 0) {
                offsetX = normalX[0] * halfThickness;
                offsetY = normalY[0] * halfThickness;
            } else if (i == count - 1) {
                offsetX = normalX[count - 2] * halfThickness;
                offsetY = normalY[count - 2] * halfThickness;
            } else {
                double mx = normalX[i - 1] + normalX[i];
                double my = normalY[i - 1] + normalY[i];
                double miterLength = Math.hypot(mx, my);

                if (miterLength < 1.0e-9) {
                    offsetX = normalX[i] * halfThickness;
                    offsetY = normalY[i] * halfThickness;
                } else {
                    mx /= miterLength;
                    my /= miterLength;

                    double denominator = mx * normalX[i] + my * normalY[i];
                    if (Math.abs(denominator) < 1.0e-6) {
                        offsetX = normalX[i] * halfThickness;
                        offsetY = normalY[i] * halfThickness;
                    } else {
                        double length = Mth.clamp(halfThickness / denominator, -maxMiterLength, maxMiterLength);
                        offsetX = mx * length;
                        offsetY = my * length;
                    }
                }
            }

            IGraphLayout.Point point = points.get(i);
            left[i] = new IGraphLayout.Point(point.x() + offsetX, point.y() + offsetY);
            right[i] = new IGraphLayout.Point(point.x() - offsetX, point.y() - offsetY);
        }

        PoseStack pose = g.pose();
        Matrix4f matrix = pose.last().pose();
        double period = EDGE_DASH_LENGTH + EDGE_ALTERNATE_LENGTH;

        for (int i = 0; i < count - 1; i++) {
            double segmentLength = segmentLengths[i];
            double segmentStart = cumulativeLengths[i];
            double travelled = 0.0;

            while (travelled < segmentLength - 1.0e-9) {
                double absoluteStart = segmentStart + travelled;
                double absoluteEnd;
                ResourceLocation texture;

                if (!alternating) {
                    absoluteEnd = segmentStart + segmentLength;
                    texture = active ? AND_EDGE_ACTIVE_TEXTURE : AND_EDGE_INACTIVE_TEXTURE;
                } else {
                    double phase = absoluteStart % period;
                    boolean primary = phase < EDGE_DASH_LENGTH - 1.0e-9;
                    double periodStart = Math.floor(absoluteStart / period) * period;
                    double boundary = periodStart + (primary ? EDGE_DASH_LENGTH : period);
                    absoluteEnd = Math.min(segmentStart + segmentLength, boundary);
                    texture = primary ?
                            (active ? OR_EDGE_ACTIVE_TEXTURE : OR_EDGE_INACTIVE_TEXTURE) :
                            (active ? OR_EDGE_ALTERNATE_ACTIVE_TEXTURE : OR_EDGE_ALTERNATE_INACTIVE_TEXTURE);

                    if (absoluteEnd <= absoluteStart + 1.0e-9) {
                        absoluteEnd = Math.min(segmentStart + segmentLength, absoluteStart + 1.0e-6);
                    }
                }

                double nextTravelled = absoluteEnd - segmentStart;
                double t0 = travelled / segmentLength;
                double t1 = Math.min(1.0, nextTravelled / segmentLength);

                IGraphLayout.Point left0 = lerp(left[i], left[i + 1], t0);
                IGraphLayout.Point right0 = lerp(right[i], right[i + 1], t0);
                IGraphLayout.Point left1 = lerp(left[i], left[i + 1], t1);
                IGraphLayout.Point right1 = lerp(right[i], right[i + 1], t1);

                VertexConsumer consumer = g.bufferSource().getBuffer(RenderType.text(texture));
                addEdgeVertex(consumer, matrix, left0, 0.0F, 0.0F);
                addEdgeVertex(consumer, matrix, left1, 1.0F, 0.0F);
                addEdgeVertex(consumer, matrix, right1, 1.0F, 1.0F);
                addEdgeVertex(consumer, matrix, right0, 0.0F, 1.0F);

                travelled = nextTravelled;
            }
        }
    }

    // TODO do smth with hover to make it obvious can be clicked
    private void renderNode(GuiGraphics g, ResourceLocation id, int x, int y, boolean hovered) {
        Registry<PixelPetData> registry = PixelPetScreenRenderingUtil.getPetRegistry();
        PixelPetData data = registry != null ? registry.get(id) : null;
        boolean catchable = graph.dependenciesMet(id, caughtPets);

        ResourceLocation texture = data != null ? data.rarity().getCaughtFrameTexture() : Rarity.COMMON.getCaughtFrameTexture();
        if (!caughtPets.contains(id)) {
            if (catchable) {
                texture = data != null ? data.rarity().getCatchableFrameTexture() : Rarity.COMMON.getCatchableFrameTexture();
            } else {
                texture = data != null ? data.rarity().getUncatchableFrameTexture() : Rarity.COMMON.getUncatchableFrameTexture();
            }
        }

        g.blit(texture, x, y, 0, 0, NODE_SIZE, NODE_SIZE, NODE_SIZE, NODE_SIZE);

        LivingEntity entity = PixelPetScreenRenderingUtil.getEntity(id);
        if (entity == null) {
            return;
        }
        InventoryScreen.renderEntityInInventory(g, x + NODE_SIZE / 2.0F, y + NODE_SIZE / 2.0F, (float) NODE_SIZE, new Vector3f(0.0F, 0.3F, 0.0F), new Quaternionf().rotationXYZ((float) Math.toRadians(10), (float) Math.toRadians(225), (float) Math.toRadians(180)), null, entity);

        if (!caughtPets.contains(id) && !catchable) {
            // TODO render padlock or smth obvious to show cant be clicked?
        }
    }

    private void renderNodeTooltip(GuiGraphics g, ResourceLocation id, int mouseX, int mouseY) {
        Registry<PixelPetData> registry = PixelPetScreenRenderingUtil.getPetRegistry();
        PixelPetData data = registry != null ? registry.get(id) : null;

        String name = data != null ? data.genericName() : id.toString();
        Rarity rarity = data != null ? data.rarity() : Rarity.COMMON;

        g.renderTooltip(font, Component.literal(name).withStyle(rarity.color()), mouseX, mouseY);
    }

    private void renderEmpty(GuiGraphics g) {
        int centerX = width / 2;
        int centerY = (viewTop() + viewBottom()) / 2;

        g.drawCenteredString(font, EMPTY_LABEL, centerX, centerY - 4, -1);
        g.drawCenteredString(font, EMPTY_HINT, centerX, centerY + 8, 0xFFAAAAAA);
    }

    @Nullable
    private ResourceLocation hoveredNode(double mouseX, double mouseY) {
        if (!inViewport(mouseX, mouseY)) {
            return null;
        }

        double gx = (mouseX - panX) / scale;
        double gy = (mouseY - panY) / scale;
        double half = NODE_SIZE * 0.5;

        for (ResourceLocation node : graph.nodes()) {
            IGraphLayout.Point point = graph.position(node);

            if (Math.abs(gx - point.x()) <= half && Math.abs(gy - point.y()) <= half) {
                return node;
            }
        }

        return null;
    }
}
