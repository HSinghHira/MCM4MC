package im.hira.tweaks.modules.Render;

import im.hira.tweaks.MCM4MC;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class PlayerVisual extends Module{

    public PlayerVisual() {
        super(MCM4MC.JH_RENDER_CAT, "Player Visual CMD", "Run commands when specific players enter visual range.");
    }
    private final SettingGroup sg = settings.getDefaultGroup();

    private final StringListSetting players = (StringListSetting) sg.add(new StringListSetting.Builder()
        .name("players")
        .description("Players to watch for visual range")
        .defaultValue()
        .build()
    );

    private final StringListSetting commands = (StringListSetting) sg.add(new StringListSetting.Builder()
        .name("commands")
        .description("Commands to run when each player enters visual range")
        .defaultValue()
        .build()
    );

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (!(event.entity instanceof PlayerEntity player)) return;
        if (mc.player == null || mc.player.networkHandler == null) return;

        List<String> playerList = players.get();
        List<String> commandList = commands.get();

        for (int i = 0; i < playerList.size(); i++) {
            if (player.getName().getString().equals(playerList.get(i))) {
                if (i < commandList.size()) {
                    mc.player.networkHandler.sendChatMessage(commandList.get(i));
                }
            }
        }
    }
}
