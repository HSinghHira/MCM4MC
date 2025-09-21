package im.hira.tweaks.modules.Crash;

import im.hira.tweaks.MCM4MC;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.LecternScreen;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;

public class LecternCrash extends Module {

    public LecternCrash() {
        super(MCM4MC.JH_CRASH_CAT, "Lectern Crash", "Sends a funny packet when you open a lectern");
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!(event.screen instanceof LecternScreen) || mc.player == null || mc.getNetworkHandler() == null) {
            return;
        }

        try {
            Int2ObjectMap<ItemStackHash> emptyMap = Int2ObjectMaps.emptyMap();

            ClickSlotC2SPacket packet = new ClickSlotC2SPacket(
                mc.player.currentScreenHandler.syncId,
                mc.player.currentScreenHandler.getRevision(),
                (short) 0, // slot id
                (byte) 0, // button, cast to byte to match constructor
                SlotActionType.QUICK_MOVE,
                emptyMap,
                ItemStackHash.EMPTY
            );
            mc.getNetworkHandler().sendPacket(packet);
        } catch (Exception e) {
            error("Failed to send packet: " + e.getMessage());
        }
    }
}
