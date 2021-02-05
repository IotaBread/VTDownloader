package io.github.bymartrixx.vtd.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.bymartrixx.vtd.gui.widget.MainWidget;
import io.github.bymartrixx.vtd.gui.widget.TabButtonWidget;
import io.github.bymartrixx.vtd.object.PackCategories;
import io.github.bymartrixx.vtd.object.PackCategory;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * VanillaTweaks Resource Pack downloading screen.
 */
public class VanillaTweaksResourcePackScreen extends Screen {
    private final Screen previousScreen;
    private final Text subtitle;
    private MainWidget mainWidget;
    private final Map<Text, PackCategory<?>> categoryMap = new LinkedHashMap<>();
    private final List<Pair<Text, Integer>> tabs;
    private final List<TabButtonWidget> tabButtons = Lists.newArrayList();
    private int tabIndex = 0;
    private int tabScrollAmount = 0;

    /**
     * Create a new {@link VanillaTweaksResourcePackScreen}.
     *
     * @param previousScreen the screen that was opened before this one.
     * @param subtitle the screen subtitle.
     */
    public VanillaTweaksResourcePackScreen(Screen previousScreen, Text subtitle,
                                           PackCategories<?> categories) {
        super(new TranslatableText("vtd.title"));
        this.previousScreen = previousScreen;
        this.subtitle = subtitle;

        for (PackCategory<?> category : categories) {
            this.categoryMap.put(new LiteralText(category.name()), category);
        }

        this.tabs = this.categoryMap.keySet().stream().map(text -> new Pair<>(text, this.textRenderer.getWidth(text) + 8)).collect(Collectors.toList());
    }

    protected void init() {
        this.tabButtons.clear();

        this.mainWidget = new MainWidget(this.client, this.width, this.height, 60,
                this.height - 40, 32);

        for (Pair<Text, Integer> tab : this.tabs) {
            this.tabButtons.add(new TabButtonWidget(-100, 32, tab.getRight(), 20, tab.getLeft(), button -> {/* TODO */}, this.categoryMap.get(tab.getLeft())));
        }

        this.children.addAll(this.tabButtons);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onClose() {
        // Return to the previous screen
        this.client.openScreen(this.previousScreen);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // Render background
        this.renderBackgroundTexture(0);

        // Render main widget
        this.mainWidget.render(matrices, mouseX, mouseY, delta);

        this.renderTabs(matrices, mouseX, mouseY, delta);

        // Render title and subtitle
        DrawableHelper.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2,
                8, 16777215);
        DrawableHelper.drawCenteredText(matrices, this.textRenderer, this.subtitle, this.width / 2,
                20, 16777215);

        super.render(matrices, mouseX, mouseY, delta);
    }

    @SuppressWarnings({"deprecation", "ConstantConditions"})
    private void renderTabs(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        // Render darker background part
        this.client.getTextureManager().bindTexture(DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        // Corner positions
        int left = 70;
        int right = this.width - 40;
        int top = 30;
        int bottom = 54;
        bufferBuilder.vertex(left, bottom, 0)
                .texture(left / 32.0F, bottom / 32.0F)
                .color(32, 32, 32, 255).next();
        bufferBuilder.vertex(right, bottom, 0)
                .texture(right / 32.0F, bottom / 32.0F)
                .color(32, 32, 32, 255).next();
        bufferBuilder.vertex(right, top, 0)
                .texture(right / 32.0F, top / 32.0F)
                .color(32, 32, 32, 255).next();
        bufferBuilder.vertex(left, top, 0)
                .texture(left / 32.0F, top / 32.0F)
                .color(32, 32, 32, 255).next();
        tessellator.draw();

        // Position the buttons on the x axis
        int buttonLeft = left;
        for (TabButtonWidget tabButton : this.tabButtons) {
            tabButton.x = buttonLeft;
            buttonLeft += tabButton.getWidth() + 10;
        }

        // Render the tabButtons
        for (TabButtonWidget tabButton : this.tabButtons) {
            if (tabButton.x >= left && tabButton.getRight() <= right) {
                tabButton.render(matrices, mouseX, mouseY, delta);
            }
        }
    }
}
