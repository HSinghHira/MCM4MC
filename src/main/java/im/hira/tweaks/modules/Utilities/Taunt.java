package im.hira.tweaks.modules.Utilities;

import im.hira.tweaks.MCM4MC;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Taunt extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Random random = new Random();

    private enum Intensity {
        Light, Medium, Toxic
    }

    private final Setting<Intensity> intensity = sgGeneral.add(new EnumSetting.Builder<Intensity>()
        .name("intensity")
        .description("How aggressive the taunts are.")
        .defaultValue(Intensity.Medium)
        .build()
    );

    private final Setting<Integer> cooldown = sgGeneral.add(new IntSetting.Builder()
        .name("cooldown-seconds")
        .description("Minimum time between taunts.")
        .defaultValue(5)
        .min(0)
        .sliderMax(30)
        .build()
    );

    private final Setting<List<String>> customTaunts = sgGeneral.add(new StringListSetting.Builder()
        .name("custom-taunts")
        .description("Custom taunts you can add manually.")
        .defaultValue()
        .build()
    );

    private final Map<Intensity, List<String>> killTaunts = new HashMap<>();
    private final Map<Intensity, List<String>> deathTaunts = new HashMap<>();
    private final Map<Intensity, List<String>> joinTaunts = new HashMap<>();
    private final Map<Intensity, List<String>> quitTaunts = new HashMap<>();

    private final Map<String, Long> lastTauntTime = new ConcurrentHashMap<>();
    private DamageSource lastDamageSource = null;

    public Taunt() {
        super(MCM4MC.JH_UTILITIES_CAT, "taunt", "Sends taunts on kill, death, join, and quit.");
        loadTaunts();
    }

    private void loadTaunts() {
        killTaunts.put(Intensity.Light, Arrays.asList(
            "GG {player}.",
            "Good fight {player}.",
            "That was close {player}.",
            "You did well {player}.",
            "Respect, {player}."
        ));
        killTaunts.put(Intensity.Medium, Arrays.asList(
            "Outplayed, {player}.",
            "{player} dropped harder than my FPS.",
            "Better luck next time {player}.",
            "Sit down {player}.",
            "You got styled on {player}.",
            "Take notes {player}.",
            "{player}, try harder.",
            "Pathetic attempt {player}."
        ));
        killTaunts.put(Intensity.Toxic, Arrays.asList(
            "Uninstall the game {player}.",
            "Trash {player}.",
            "{player}, you call that PvP?",
            "Free loot {player}.",
            "{player}, don't come back.",
            "You're embarrassing {player}.",
            "Keyboard warrior down: {player}.",
            "Clowned on {player}.",
            "{player}, that was painful to watch.",
            "Imagine dying like that {player}."
        ));

        deathTaunts.put(Intensity.Light, Arrays.asList(
            "Well played {killer}.",
            "Nice one {killer}.",
            "Fair fight {killer}.",
            "Respect {killer}.",
            "You got me {killer}."
        ));
        deathTaunts.put(Intensity.Medium, Arrays.asList(
            "Lag killed me, not {killer}.",
            "I wasn't trying.",
            "Cheap shot {killer}.",
            "Next round is mine.",
            "You only win once {killer}.",
            "Warm up death.",
            "That was luck {killer}."
        ));
        deathTaunts.put(Intensity.Toxic, Arrays.asList(
            "You're bad even when you win {killer}.",
            "Imagine needing help {killer}.",
            "That was RNG, not skill {killer}.",
            "Clown kill {killer}.",
            "Keep dreaming {killer}.",
            "Your win means nothing {killer}.",
            "Garbage kill {killer}."
        ));

        joinTaunts.put(Intensity.Light, Arrays.asList(
            "Hello everyone.",
            "Good to be here.",
            "Let's play.",
            "Back again.",
            "Hope you're ready."
        ));
        joinTaunts.put(Intensity.Medium, Arrays.asList(
            "Hide your loot, I'm here.",
            "Guess who's back.",
            "The fun starts now.",
            "Time to farm some kills.",
            "Let's stir things up."
        ));
        joinTaunts.put(Intensity.Toxic, Arrays.asList(
            "Your nightmare has arrived.",
            "Server's about to get wrecked.",
            "I own this place.",
            "Prepare to suffer.",
            "Easy farm incoming."
        ));

        quitTaunts.put(Intensity.Light, Arrays.asList(
            "GG everyone.",
            "Thanks for the games.",
            "See you soon.",
            "Logging out, good luck.",
            "That was fun."
        ));
        quitTaunts.put(Intensity.Medium, Arrays.asList(
            "Too easy, I'm out.",
            "Server cleared.",
            "Nothing left to prove.",
            "I'll be back stronger.",
            "Done for today."
        ));
        quitTaunts.put(Intensity.Toxic, Arrays.asList(
            "Boring server, cya.",
            "No competition here.",
            "Waste of time, later.",
            "Pathetic players, I'm gone.",
            "Uninstalled this trash server."
        ));
    }

    private String getRandomMessage(List<String> base, String player, String killer) {
        List<String> combined = new ArrayList<>(base);
        combined.addAll(customTaunts.get());

        if (combined.isEmpty()) return null;

        String msg = combined.get(random.nextInt(combined.size()));
        if (player != null) msg = msg.replace("{player}", player);
        if (killer != null) msg = msg.replace("{killer}", killer);
        return msg;
    }

    private void sendTaunt(String key, String message) {
        if (message == null || message.isEmpty()) return;

        long now = System.currentTimeMillis();
        long last = lastTauntTime.getOrDefault(key, 0L);

        if ((now - last) / 1000 < cooldown.get()) return;

        ChatUtils.sendPlayerMsg(message);
        lastTauntTime.put(key, now);
    }

    private String getKillerName(DamageSource damageSource) {
        if (damageSource != null && damageSource.getAttacker() instanceof LivingEntity) {
            return ((LivingEntity) damageSource.getAttacker()).getName().getString();
        }
        return "unknown";
    }

    @EventHandler
    private void onEntityRemoved(EntityRemovedEvent event) {
        if (!(event.entity instanceof PlayerEntity)) return;

        PlayerEntity player = (PlayerEntity) event.entity;
        if (player.getHealth() <= 0) {
            if (player != mc.player) {
                // Kill taunt - another player died
                String msg = getRandomMessage(killTaunts.get(intensity.get()), player.getName().getString(), null);
                sendTaunt("kill", msg);
            } else {
                // Death taunt - we died
                String killer = getKillerName(lastDamageSource);
                String msg = getRandomMessage(deathTaunts.get(intensity.get()), null, killer);
                sendTaunt("death", msg);
                lastDamageSource = null; // Reset after use
            }
        }
    }

    @EventHandler
    private void onJoin(GameJoinedEvent event) {
        String msg = getRandomMessage(joinTaunts.get(intensity.get()), null, null);
        sendTaunt("join", msg);
    }

    @EventHandler
    private void onQuit(GameLeftEvent event) {
        String msg = getRandomMessage(quitTaunts.get(intensity.get()), null, null);
        sendTaunt("quit", msg);
    }

    @Override
    public void onActivate() {
        lastDamageSource = null;
    }

    @Override
    public void onDeactivate() {
        lastDamageSource = null;
    }
}
