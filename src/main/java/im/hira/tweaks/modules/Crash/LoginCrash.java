package im.hira.tweaks.modules.Crash;

import meteordevelopment.meteorclient.systems.modules.Module;
import im.hira.tweaks.MCM4MC;

public class LoginCrash extends Module {
    public LoginCrash() {
        super(MCM4MC.JH_CRASH_CAT, "Login Crash", "Tries to crash the server on login using null packets.");
    }
}
