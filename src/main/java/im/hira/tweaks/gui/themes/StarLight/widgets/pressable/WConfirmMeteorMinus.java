package im.hira.tweaks.gui.themes.StarLight.widgets.pressable;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WConfirmedMinus;
import meteordevelopment.meteorclient.utils.render.color.Color;
import im.hira.tweaks.gui.themes.StarLight.Starlight;

public class WConfirmMeteorMinus extends WConfirmedMinus {
    public WConfirmMeteorMinus() {
        super();
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        Starlight theme = theme();

        renderBackground(renderer, pressed, mouseOver);

        Color color = theme.minusColor.get();
        // Dim the color when pressed to show interaction
        if (pressed) color = theme.textSecondaryColor();

        double w = width;
        double h = height;

        // Draw minus icon
        double minusWidth = w * 0.6;
        double minusHeight = theme.scale(1);
        double minusX = x + (w - minusWidth) / 2;
        double minusY = y + (h - minusHeight) / 2;

        renderer.quad(minusX, minusY, minusWidth, minusHeight, color);
    }

    private void renderBackground(GuiRenderer renderer, boolean pressed, boolean hovered) {
        Starlight theme = theme();
        int round = theme.roundAmount();

        Color outlineColor = theme.outlineColor.get(pressed, hovered);
        Color backgroundColor = theme.backgroundColor.get(pressed, hovered);

        if (round > 0) {
            // Use regular quad methods since rounded methods might not be available
            renderer.quad(x, y, width, height, backgroundColor);
            // Draw border manually if needed
            renderer.quad(x, y, width, 1, outlineColor); // top
            renderer.quad(x, y + height - 1, width, 1, outlineColor); // bottom
            renderer.quad(x, y, 1, height, outlineColor); // left
            renderer.quad(x + width - 1, y, 1, height, outlineColor); // right
        } else {
            renderer.quad(x, y, width, height, backgroundColor);
            // Draw border
            renderer.quad(x, y, width, 1, outlineColor); // top
            renderer.quad(x, y + height - 1, width, 1, outlineColor); // bottom
            renderer.quad(x, y, 1, height, outlineColor); // left
            renderer.quad(x + width - 1, y, 1, height, outlineColor); // right
        }
    }

    private Starlight theme() {
        return (Starlight) getTheme();
    }
}
