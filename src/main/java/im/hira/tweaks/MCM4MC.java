package im.hira.tweaks;

import im.hira.tweaks.category.CustomCategoryManager;
import im.hira.tweaks.modules.Buildings.*;
import im.hira.tweaks.modules.Combats.*;
import im.hira.tweaks.modules.Movements.*;
import im.hira.tweaks.modules.Utilities.*;
import im.hira.tweaks.modules.Render.*;
import im.hira.tweaks.modules.Dupes.*;
import im.hira.tweaks.modules.Crash.*;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.gui.GuiThemes;
import org.slf4j.Logger;

public class MCM4MC extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    // public static final HudGroup HUD_GROUP = new HudGroup("JH HUD");

    // Categories
    public static final Category JH_BUILD_CAT = new Category("JH Build/World");
    public static final Category JH_UTILITIES_CAT = new Category("JH Utilities");
    public static final Category JH_COMBAT_CAT = new Category("JH Combat");
    public static final Category JH_MOVEMENT_CAT = new Category("JH Movement");
    public static final Category JH_RENDER_CAT = new Category("JH Render");
    public static final Category JH_DUPES_CAT = new Category("JH Dupes");
    public static final Category JH_CRASH_CAT = new Category("JH Crash");

    @Override
    public void onInitialize() {
        LOG.info("Initializing MCM4MC addon");

        // Initialize custom categories
        CustomCategoryManager.init();

        // JH Build/World Modules
        Modules.get().add(new AutoDirtPath());
        Modules.get().add(new AutoFarmLand());
        Modules.get().add(new AutoLogStrip());

        // JH Utilities Modules
        Modules.get().add(new ChatUtility());
        Modules.get().add(new HotkeyUtility());
        Modules.get().add(new ChestIndex());
        Modules.get().add(new StuffStealer());
        Modules.get().add(new DisableMods());

        // JH Render Modules
        Modules.get().add(new BigCavesESP());
        Modules.get().add(new OreESP());
        Modules.get().add(new PlayerVisual());

        // JH Combat Modules
        Modules.get().add(new LegitMaceKill());
        Modules.get().add(new WindChargeJump());
        Modules.get().add(new MaceCombo());

        // JH Movement Modules
        Modules.get().add(new AFKVanillaFly());

        // JH Dupes Modules
        Modules.get().add(new TridentDupe());

        // JH Crash Modules
        Modules.get().add(new AACCrash());
        Modules.get().add(new BoatCrash());
        Modules.get().add(new BookCrash());
        Modules.get().add(new ContainerCrash());
        Modules.get().add(new CraftingCrash());
        Modules.get().add(new EntityCrash());
        Modules.get().add(new InvalidPositionCrash());
        Modules.get().add(new LecternCrash());
        Modules.get().add(new LoginCrash());
        Modules.get().add(new MessageLagger());
        Modules.get().add(new MovementCrash());
        Modules.get().add(new NoComCrash());
        Modules.get().add(new PacketSpammer());
        Modules.get().add(new SignCrash());
        Modules.get().add(new TryUseCrash());


        LOG.info("MCM4MC addon initialization complete");
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(JH_BUILD_CAT);
        Modules.registerCategory(JH_UTILITIES_CAT);
        Modules.registerCategory(JH_COMBAT_CAT);
        Modules.registerCategory(JH_MOVEMENT_CAT);
        Modules.registerCategory(JH_RENDER_CAT);
        Modules.registerCategory(JH_DUPES_CAT);
        Modules.registerCategory(JH_CRASH_CAT);
    }

    @Override
    public String getPackage() {
        return "im.hira.tweaks";
    }
}
