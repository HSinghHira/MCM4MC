package im.hira.tweaks.modules.Utilities;

import im.hira.tweaks.MCM4MC;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.client.util.InputUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DisableMods extends Module {
    // Core collections
    private final Set<Module> disabledModules = ConcurrentHashMap.newKeySet();
    private final Set<Module> queuedModules = ConcurrentHashMap.newKeySet();
    private final Set<Module> trackedModules = ConcurrentHashMap.newKeySet();
    private final Set<Module> disabledModulesOnDisconnect = ConcurrentHashMap.newKeySet();
    private final Set<String> detectedStaff = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> playerJoinTimes = new ConcurrentHashMap<>();

    // Settings groups
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAutoDisable = settings.createGroup("Auto Disable");
    private final SettingGroup sgProfiles = settings.createGroup("Profiles");
    private final SettingGroup sgSafety = settings.createGroup("Safety");
    private final SettingGroup sgNotifications = settings.createGroup("Notifications");

    public enum ModuleActivationBehavior {
        Allow("Allow activation"),
        Queue("Queue for later"),
        Block("Block activation");

        private final String description;
        ModuleActivationBehavior(String description) { this.description = description; }
        @Override public String toString() { return description; }
    }

    public enum DisableMode {
        All("All modules"),
        Category("By category"),
        Whitelist("Use whitelist"),
        Profile("Use profile");

        private final String description;
        DisableMode(String description) { this.description = description; }
        @Override public String toString() { return description; }
    }

    public enum StaffDetectionMode {
        Off("Disabled"),
        Username("Username patterns"),
        Tab("Tab list monitoring"),
        Both("Username + Tab list");

        private final String description;
        StaffDetectionMode(String description) { this.description = description; }
        @Override public String toString() { return description; }
    }

    // General Settings
    private final Setting<ModuleActivationBehavior> activationBehavior = sgGeneral.add(new EnumSetting.Builder<ModuleActivationBehavior>()
        .name("activation-behavior")
        .description("What happens when a module is enabled while DisableMods is active.")
        .defaultValue(ModuleActivationBehavior.Block)
        .build()
    );

    private final Setting<DisableMode> disableMode = sgGeneral.add(new EnumSetting.Builder<DisableMode>()
        .name("disable-mode")
        .description("How to determine which modules to disable.")
        .defaultValue(DisableMode.Whitelist)
        .build()
    );

    private final Setting<List<Module>> whitelistModules = sgGeneral.add(new ModuleListSetting.Builder()
        .name("whitelist-modules")
        .description("Modules that won't be disabled (whitelist mode).")
        .defaultValue(new ArrayList<>())
        .visible(() -> disableMode.get() == DisableMode.Whitelist)
        .build()
    );

    private final Setting<Boolean> disableOnServerJoin = sgGeneral.add(new BoolSetting.Builder()
        .name("disable-on-join")
        .description("Automatically disable modules when joining a server.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> enableOnServerLeave = sgGeneral.add(new BoolSetting.Builder()
        .name("enable-on-leave")
        .description("Re-enable modules when leaving a server.")
        .defaultValue(true)
        .build()
    );

    // Auto Disable Settings
    private final Setting<StaffDetectionMode> staffDetection = sgAutoDisable.add(new EnumSetting.Builder<StaffDetectionMode>()
        .name("staff-detection")
        .description("Automatically disable when staff members are detected.")
        .defaultValue(StaffDetectionMode.Both)
        .build()
    );

    private final Setting<List<String>> staffKeywords = sgAutoDisable.add(new StringListSetting.Builder()
        .name("staff-keywords")
        .description("Keywords to detect staff members (case insensitive).")
        .defaultValue(Arrays.asList("admin", "mod", "moderator", "staff", "helper", "owner"))
        .visible(() -> staffDetection.get() != StaffDetectionMode.Off)
        .build()
    );

    private final Setting<Boolean> disableOnPlayerNearby = sgAutoDisable.add(new BoolSetting.Builder()
        .name("disable-on-players")
        .description("Disable when other players are nearby.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> playerDetectionRange = sgAutoDisable.add(new DoubleSetting.Builder()
        .name("detection-range")
        .description("Range to detect nearby players.")
        .defaultValue(50.0)
        .min(10.0)
        .max(200.0)
        .visible(() -> disableOnPlayerNearby.get())
        .build()
    );

    private final Setting<Integer> playerDetectionDelay = sgAutoDisable.add(new IntSetting.Builder()
        .name("detection-delay")
        .description("Delay in seconds before disabling after player detection.")
        .defaultValue(3)
        .min(0)
        .max(30)
        .visible(() -> disableOnPlayerNearby.get())
        .build()
    );

    // Safety Settings
    private final Setting<Boolean> autoReEnable = sgSafety.add(new BoolSetting.Builder()
        .name("auto-re-enable")
        .description("Automatically re-enable modules when DisableMods is turned off.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> emergencyKey = sgSafety.add(new BoolSetting.Builder()
        .name("emergency-key")
        .description("Enable emergency disable key (press F1 3 times quickly).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> panicMode = sgSafety.add(new BoolSetting.Builder()
        .name("panic-mode")
        .description("When enabled, clears chat and disconnects from server.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> autoReEnableDelay = sgSafety.add(new IntSetting.Builder()
        .name("auto-reenable-delay")
        .description("Delay in seconds before re-enabling modules (0 = instant).")
        .defaultValue(5)
        .min(0)
        .max(300)
        .visible(() -> autoReEnable.get())
        .build()
    );

    // Notification Settings
    private final Setting<Boolean> chatNotifications = sgNotifications.add(new BoolSetting.Builder()
        .name("chat-notifications")
        .description("Show notifications in chat.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> soundNotifications = sgNotifications.add(new BoolSetting.Builder()
        .name("sound-notifications")
        .description("Play sounds for important events.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> verboseLogging = sgNotifications.add(new BoolSetting.Builder()
        .name("verbose-logging")
        .description("Show detailed information about module states.")
        .defaultValue(false)
        .build()
    );

    // Profile Settings
    private final Setting<String> currentProfile = sgProfiles.add(new StringSetting.Builder()
        .name("current-profile")
        .description("Current active profile name.")
        .defaultValue("default")
        .visible(() -> disableMode.get() == DisableMode.Profile)
        .build()
    );

    // Internal state
    private long lastEmergencyKeyPress = 0;
    private int emergencyKeyPresses = 0;
    private boolean isEmergencyDisabled = false;
    private Timer reEnableTimer = new Timer();

    public DisableMods() {
        super(MCM4MC.JH_UTILITIES_CAT, "Disable Mods", "Advanced module management with staff detection and safety features.");

        try {
            MeteorClient.EVENT_BUS.subscribe(this);
            MCM4MC.LOG.info("DisableMods initialized successfully");
        } catch (Exception e) {
            MCM4MC.LOG.error("Failed to initialize DisableMods", e);
            throw e;
        }
    }

    @Override
    public void onActivate() {
        clearState();
        disableModulesBasedOnMode();

        if (chatNotifications.get()) {
            info("§6DisableMods activated - Disabled " + disabledModules.size() + " modules");
        }

        if (verboseLogging.get()) {
            logModuleStates();
        }
    }

    @Override
    public void onDeactivate() {
        if (autoReEnable.get()) {
            if (autoReEnableDelay.get() > 0) {
                scheduleReEnable();
            } else {
                reEnableModules();
            }
        } else {
            if (chatNotifications.get()) {
                info("§eAuto re-enable is disabled - modules will stay off");
            }
        }

        clearState();
        isEmergencyDisabled = false;
    }

    private void clearState() {
        queuedModules.clear();
        trackedModules.clear();
        detectedStaff.clear();
        playerJoinTimes.clear();
    }

    private void disableModulesBasedOnMode() {
        disabledModules.clear();
        List<Module> toDisable = new ArrayList<>();

        switch (disableMode.get()) {
            case All:
                toDisable = Modules.get().getAll().stream()
                    .filter(module -> module != this && module.isActive())
                    .toList();
                break;

            case Whitelist:
                List<Module> whitelist = whitelistModules.get();
                toDisable = Modules.get().getAll().stream()
                    .filter(module -> module != this && module.isActive() && !whitelist.contains(module))
                    .toList();
                break;

            case Profile:
                // Profile-based disabling would be implemented here
                // For now, fall back to whitelist behavior
                toDisable = Modules.get().getAll().stream()
                    .filter(module -> module != this && module.isActive())
                    .toList();
                break;
        }

        for (Module module : toDisable) {
            trackedModules.add(module);
            if (module.isActive()) {
                module.toggle();
                disabledModules.add(module);
            }
        }
    }

    private void reEnableModules() {
        int reEnabledCount = 0;

        for (Module module : disabledModules) {
            if (!module.isActive()) {
                module.toggle();
                reEnabledCount++;
            }
        }

        for (Module module : queuedModules) {
            if (!module.isActive()) {
                module.toggle();
                reEnabledCount++;
            }
        }

        if (chatNotifications.get()) {
            info("§aRe-enabled " + reEnabledCount + " modules");
        }

        if (!queuedModules.isEmpty() && verboseLogging.get()) {
            info("§eEnabled " + queuedModules.size() + " queued modules");
        }
    }

    private void scheduleReEnable() {
        if (chatNotifications.get()) {
            info("§eRe-enabling modules in " + autoReEnableDelay.get() + " seconds...");
        }

        reEnableTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mc.player != null) {
                    reEnableModules();
                }
            }
        }, autoReEnableDelay.get() * 1000L);
    }

    private void emergencyDisable() {
        if (isEmergencyDisabled) return;

        isEmergencyDisabled = true;
        if (!isActive()) toggle();

        if (panicMode.get() && mc.player != null) {
            // Clear chat
            if (mc.inGameHud != null && mc.inGameHud.getChatHud() != null) {
                mc.inGameHud.getChatHud().clear(false);
            }

            // Disconnect from server
            if (mc.world != null && mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().getConnection().disconnect(
                    net.minecraft.text.Text.literal("Emergency disconnect")
                );
            }
        }

        if (chatNotifications.get()) {
            ChatUtils.error("§c§lEMERGENCY DISABLE ACTIVATED!");
        }

        MCM4MC.LOG.warn("Emergency disable activated!");
    }

    private void logModuleStates() {
        if (verboseLogging.get()) {
            info("§7--- Module States ---");
            info("§7Disabled: " + disabledModules.size() + " modules");
            info("§7Queued: " + queuedModules.size() + " modules");
            info("§7Tracked: " + trackedModules.size() + " modules");
        }
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        try {
            // Re-add modules that were disabled on disconnect
            for (Module module : disabledModulesOnDisconnect) {
                if (module != this) {
                    trackedModules.add(module);
                    disabledModules.add(module);
                }
            }
            disabledModulesOnDisconnect.clear();

            // Auto-disable on server join
            if (disableOnServerJoin.get() && !isActive()) {
                toggle();
            }

            if (chatNotifications.get()) {
                info("§6Server joined - DisableMods monitoring active");
            }
        } catch (Exception e) {
            MCM4MC.LOG.error("Error in onGameJoined", e);
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        try {
            if (enableOnServerLeave.get()) {
                List<Module> whitelist = whitelistModules.get();

                for (Module module : Modules.get().getAll()) {
                    if (module != this && module.isActive() && !whitelist.contains(module)) {
                        module.toggle();
                        disabledModulesOnDisconnect.add(module);
                    }
                }

                if (chatNotifications.get()) {
                    info("§eDisabled all modules for server disconnect");
                }
            }
        } catch (Exception e) {
            MCM4MC.LOG.error("Error in onGameLeft", e);
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (staffDetection.get() == StaffDetectionMode.Off) return;

        try {
            if (event.packet instanceof PlayerListS2CPacket packet) {
                for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
                    if (entry.profile() != null) {
                        String username = entry.profile().getName().toLowerCase();
                        checkForStaff(username);
                    }
                }
            }
        } catch (Exception e) {
            MCM4MC.LOG.error("Error in packet handling", e);
        }
    }

    private void checkForStaff(String username) {
        if (staffDetection.get() == StaffDetectionMode.Off) return;

        for (String keyword : staffKeywords.get()) {
            if (username.contains(keyword.toLowerCase())) {
                if (!detectedStaff.contains(username)) {
                    detectedStaff.add(username);

                    if (chatNotifications.get()) {
                        ChatUtils.warning("§c§lSTAFF DETECTED: §r§c" + username);
                    }

                    if (!isActive()) {
                        toggle();
                        if (chatNotifications.get()) {
                            info("§c§lAuto-disabled due to staff detection!");
                        }
                    }

                    MCM4MC.LOG.warn("Staff member detected: " + username);
                }
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (!isActive()) return;

        try {
            handleModuleActivation();
            handleEmergencyKey();

            if (disableOnPlayerNearby.get()) {
                checkNearbyPlayers();
            }
        } catch (Exception e) {
            MCM4MC.LOG.error("Error in onTick", e);
        }
    }

    private void handleModuleActivation() {
        ModuleActivationBehavior behavior = activationBehavior.get();
        List<Module> whitelist = whitelistModules.get();

        for (Module module : Modules.get().getAll()) {
            if (module == this || whitelist.contains(module)) continue;

            if (module.isActive() && !disabledModules.contains(module) && trackedModules.contains(module)) {
                switch (behavior) {
                    case Allow:
                        break;
                    case Queue:
                        module.toggle();
                        queuedModules.add(module);
                        if (verboseLogging.get()) {
                            info("§eQueued " + module.title + " for activation");
                        }
                        break;
                    case Block:
                        module.toggle();
                        if (verboseLogging.get()) {
                            info("§cBlocked activation of " + module.title);
                        }
                        break;
                }
            }
        }
    }

    private void handleEmergencyKey() {
        if (!emergencyKey.get()) return;

        // Use F1 key for emergency disable
        if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.GLFW_KEY_F1)) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastEmergencyKeyPress < 1000) {
                emergencyKeyPresses++;
            } else {
                emergencyKeyPresses = 1;
            }

            lastEmergencyKeyPress = currentTime;

            if (emergencyKeyPresses >= 3) {
                emergencyDisable();
                emergencyKeyPresses = 0;
            }
        }
    }

    private void checkNearbyPlayers() {
        if (mc.world == null || mc.player == null) return;

        long nearbyPlayers = mc.world.getPlayers().stream()
            .filter(player -> player != mc.player)
            .filter(player -> mc.player.distanceTo(player) <= playerDetectionRange.get())
            .count();

        if (nearbyPlayers > 0 && !isActive()) {
            if (playerDetectionDelay.get() > 0) {
                // Implementation for delayed activation would go here
            } else {
                toggle();
                if (chatNotifications.get()) {
                    info("§c§lAuto-disabled: " + nearbyPlayers + " players nearby!");
                }
            }
        }
    }
}
