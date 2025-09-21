package im.hira.tweaks.mixin;

import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerPositionLookS2CPacket.class)
public abstract class PlayerPositionLookS2CPacketAccessor {

    public abstract void setX(double x);

    public abstract void setY(double y);

    public abstract void setZ(double z);

    public abstract void setYaw(float yaw);

    public abstract void setPitch(float pitch);
}
