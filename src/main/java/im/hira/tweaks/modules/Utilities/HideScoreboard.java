package im.hira.tweaks.modules.Utilities;

import im.hira.tweaks.MCM4MC;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.ArrayList;

public class HideScoreboard extends Module {
    private ScoreboardObjective savedObjective = null;
    private String lastScoreboardTitle = "";
    private int tickCounter = 0;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFilters = settings.createGroup("Filters");
    private final SettingGroup sgNotifications = settings.createGroup("Notifications");

    // General Settings
    private final Setting<Boolean> autoHideOnJoin = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-hide-on-join")
        .description("Automatically hide scoreboard when joining a server.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rememberPerServer = sgGeneral.add(new BoolSetting.Builder()
        .name("remember-per-server")
        .description("Remember hide state for each server separately.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> tickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("tick-delay")
        .description("Delay in ticks before hiding scoreboard (reduces lag).")
        .defaultValue(5)
        .min(1)
        .max(100)
        .build()
    );

    // Filter Settings
    private final Setting<Boolean> enableFilters = sgFilters.add(new BoolSetting.Builder()
        .name("enable-filters")
        .description("Only hide scoreboards matching specific criteria.")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<String>> titleFilters = sgFilters.add(new StringListSetting.Builder()
        .name("title-filters")
        .description("Hide scoreboards with titles containing these strings (case-insensitive).")
        .defaultValue()
        .visible(enableFilters::get)
        .build()
    );

    private final Setting<Boolean> invertFilters = sgFilters.add(new BoolSetting.Builder()
        .name("invert-filters")
        .description("Hide scoreboards that DON'T match the filters instead.")
        .defaultValue(false)
        .visible(enableFilters::get)
        .build()
    );

    // Notification Settings
    private final Setting<Boolean> notifyOnHide = sgNotifications.add(new BoolSetting.Builder()
        .name("notify-on-hide")
        .description("Send chat notification when hiding scoreboard.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> notifyOnShow = sgNotifications.add(new BoolSetting.Builder()
        .name("notify-on-show")
        .description("Send chat notification when showing scoreboard.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> logScoreboardChanges = sgNotifications.add(new BoolSetting.Builder()
        .name("log-changes")
        .description("Log when scoreboard title changes.")
        .defaultValue(false)
        .build()
    );

    public HideScoreboard() {
        super(MCM4MC.JH_UTILITIES_CAT, "Hide Scoreboard", "Advanced scoreboard hiding with filters and customization options.");
    }

    @Override
    public void onActivate() {
        tickCounter = 0;
        if (notifyOnHide.get()) {
            ChatUtils.info("Scoreboard hiding activated");
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null) return;

        // Implement tick delay to reduce performance impact
        tickCounter++;
        if (tickCounter < tickDelay.get()) return;
        tickCounter = 0;

        Scoreboard scoreboard = mc.world.getScoreboard();
        ScoreboardObjective current = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);

        if (current != null) {
            String currentTitle = current.getDisplayName().getString();

            // Log title changes if enabled
            if (logScoreboardChanges.get() && !currentTitle.equals(lastScoreboardTitle)) {
                ChatUtils.info("Scoreboard title changed: " + Formatting.YELLOW + currentTitle);
                lastScoreboardTitle = currentTitle;
            }

            // Check filters if enabled
            if (enableFilters.get() && !shouldHideScoreboard(currentTitle)) {
                return; // Don't hide if filters don't match
            }

            // Save objective once
            if (savedObjective == null) {
                savedObjective = current;
                if (notifyOnHide.get()) {
                    ChatUtils.info("Hidden scoreboard: " + Formatting.YELLOW + currentTitle);
                }
            }

            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, null);
        }
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        if (autoHideOnJoin.get()) {
            // Small delay to ensure scoreboard is loaded
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    if (isActive()) return; // Already active
                    toggle();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        // Clean up when leaving
        savedObjective = null;
        lastScoreboardTitle = "";
    }

    private boolean shouldHideScoreboard(String title) {
        if (titleFilters.get().isEmpty()) return true;

        boolean matches = titleFilters.get().stream()
            .anyMatch(filter -> title.toLowerCase().contains(filter.toLowerCase()));

        return invertFilters.get() ? !matches : matches;
    }

    @Override
    public void onDeactivate() {
        if (mc.world == null || savedObjective == null) return;

        String title = savedObjective.getDisplayName().getString();

        // Restore the scoreboard
        mc.world.getScoreboard().setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, savedObjective);

        if (notifyOnShow.get()) {
            ChatUtils.info("Restored scoreboard: " + Formatting.GREEN + title);
        }

        savedObjective = null;
        lastScoreboardTitle = "";
    }

    // Utility method for external access
    public boolean isScoreboardHidden() {
        return savedObjective != null;
    }

    public String getHiddenScoreboardTitle() {
        return savedObjective != null ? savedObjective.getDisplayName().getString() : "None";
    }
}
