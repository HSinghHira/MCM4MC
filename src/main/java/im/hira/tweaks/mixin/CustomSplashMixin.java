package im.hira.tweaks.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;

@Environment(value = EnvType.CLIENT)
@Mixin(value = {TitleScreen.class})
public class CustomSplashMixin extends Screen {

    @Shadow
    @Nullable
    private SplashTextRenderer splashText;

    private static final Random random = new Random();
    private final List<String> customSplashes = getCustomSplashes();

    protected CustomSplashMixin(Text title) {
        super(title);
    }

    @Inject(method = {"mouseClicked"}, at = {@At(value = "RETURN")})
    private void changeSplashText(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        // Change splash text to a new random one from our custom list
        if (customSplashes.size() > 0) {
            String newSplash = customSplashes.get(random.nextInt(customSplashes.size()));
            this.splashText = new SplashTextRenderer(newSplash);
        }
    }

    private static List<String> getCustomSplashes() {
        return List.of(
            "Harman Singh Hira",
            "Meteor Client is God",
            "2b2t - Too Boring To Try",
            "@h1ggsk on GitHub",
            "Free Palestine"
        );
    }
}
