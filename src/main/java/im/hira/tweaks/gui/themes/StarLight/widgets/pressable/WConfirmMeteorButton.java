package im.hira.tweaks.gui.themes.StarLight.widgets.pressable;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WConfirmedButton;
import meteordevelopment.meteorclient.utils.render.color.Color;
import im.hira.tweaks.gui.themes.StarLight.Starlight;

public class WConfirmMeteorButton extends WConfirmedButton {
    public WConfirmMeteorButton(String text, String confirmText) {
        super(text, confirmText, null);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        Starlight theme = theme();

        renderBackground(renderer, pressed, mouseOver);

        Color color = theme.textColor();
        // Dim the text color when pressed to show interaction
        if (pressed) color = theme.textSecondaryColor();

        String currentText = getText();
        double textWidth = theme.textRenderer().getWidth(currentText);
        double textHeight = theme.textRenderer().getHeight();

        double w = width;
        double h = height;
        double ix = x + w / 2 - textWidth / 2;
        double iy = y + h / 2 - textHeight / 2;

        renderer.text(currentText, ix, iy, color, false);
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
