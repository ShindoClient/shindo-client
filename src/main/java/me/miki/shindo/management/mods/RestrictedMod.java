package me.miki.shindo.management.mods;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.notification.NotificationType;
import me.miki.shindo.management.remote.blacklists.BlacklistManager;
import me.miki.shindo.management.remote.blacklists.Server;
import me.miki.shindo.utils.ServerUtils;

import java.util.List;

public class RestrictedMod {

    public Boolean shouldCheck = true;
    String currentServerIP = "";
    BlacklistManager blm = Shindo.getInstance().getBlacklistManager();

    public boolean checkAllowed(Mod m) {
        if (shouldCheck) {
            List<Server> servers = blm.getBlacklist();
            for (Server server : servers) {
                if (currentServerIP.contains(server.getServerIp())) {
                    List<String> blacklistedMods = server.getMods();
                    if (blacklistedMods.contains(m.getNameKey())) {
                        m.setAllowed(false);
                        return false;
                    }
                }
            }
        }
        m.setAllowed(true);
        return true;
    }

    public void joinServer(String ip) {
        blm.check();
    }

    public void joinWorld() {
        this.currentServerIP = ServerUtils.getServerIP();
        for (Mod m : Shindo.getInstance().getModManager().getMods()) {
            if (!checkAllowed(m) && m.isToggled()) {
                m.setToggled(false);
                Shindo.getInstance().getNotificationManager().post(m.getName(), "Disabled due to serverside blacklist", NotificationType.INFO);
            }
        }
    }

}
