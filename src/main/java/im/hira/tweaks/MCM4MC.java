package im.hira.tweaks;

import im.hira.tweaks.category.CustomCategoryManager;
import im.hira.tweaks.modules.Buildings.*;
import im.hira.tweaks.modules.Combats.*;
import im.hira.tweaks.modules.Movements.*;
import im.hira.tweaks.modules.Utilities.*;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class MCM4MC extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();

    // Categories
    public static final Category JH_BUILD_CAT = new Category("JH Build/World");
    public static final Category JH_UTILITIES_CAT = new Category("JH Utilities");
    public static final Category JH_COMBAT_CAT = new Category("JH Combat");
    public static final Category JH_MOVEMENT_CAT = new Category("JH Movement");

    @Override
    public void onInitialize() {
        LOG.info("Initializing");

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

        // JH Combat Modules
        Modules.get().add(new LegitMaceKill());
        Modules.get().add(new WindChargeJump());
        Modules.get().add(new MaceCombo());

        // JH Movement Modules
        Modules.get().add(new AFKVanillaFly());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(JH_BUILD_CAT);
        Modules.registerCategory(JH_UTILITIES_CAT);
        Modules.registerCategory(JH_COMBAT_CAT);
        Modules.registerCategory(JH_MOVEMENT_CAT); // Register new category
    }

    @Override
    public String getPackage() {
        return "im.hira.tweaks";
    }
}
