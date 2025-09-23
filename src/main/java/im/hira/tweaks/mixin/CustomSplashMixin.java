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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Inject(method = {"init"}, at = {@At(value = "TAIL")})
    private void initCustomSplash(CallbackInfo ci) {
        // Override the default splash text with our custom one immediately after init
        if (customSplashes.size() > 0) {
            String randomSplash = customSplashes.get(random.nextInt(customSplashes.size()));
            this.splashText = new SplashTextRenderer(randomSplash);
        }
    }

    @Inject(method = {"mouseClicked"}, at = {@At(value = "RETURN")})
    private void changeSplashText(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        // Change splash text to a new random one from our custom list when clicked
        if (customSplashes.size() > 0) {
            String newSplash = customSplashes.get(random.nextInt(customSplashes.size()));
            this.splashText = new SplashTextRenderer(newSplash);
        }
    }

    private static List<String> getCustomSplashes() {
        List<String> splashes = new ArrayList<>();

        // Try to fetch from URL first
        try {
            List<String> fetchedSplashes = fetchSplashesFromURL("https://cdn.jsdelivr.net/gh/HSinghHira/MCM4MC@main/src/main/resources/assets/jatthira/splash.txt");
            if (!fetchedSplashes.isEmpty()) {
                splashes.addAll(fetchedSplashes);
                return splashes;
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch splash texts from URL: " + e.getMessage());
        }

        // Fallback to hardcoded splashes if URL fetch fails
        splashes.addAll(getFallbackSplashes());
        return splashes;
    }

    private static List<String> fetchSplashesFromURL(String urlString) throws Exception {
        List<String> splashes = new ArrayList<>();
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000); // 5 second timeout
        connection.setReadTimeout(5000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    splashes.add(line);
                }
            }
        } finally {
            connection.disconnect();
        }

        return splashes;
    }

    private static List<String> getFallbackSplashes() {
        return Arrays.asList(
            "Harman Singh Hira",
            "Fix your internet Mate",
            "Get a Broadband & Life",
            "@HSinghHira"
        );
    }
}
