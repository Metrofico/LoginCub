package me.metrofico.logincub.eventos;

import me.metrofico.logincub.objects.UserAuth;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class SecurePostLogin extends Event {
    ProxiedPlayer player;
    UserAuth auth;

    public SecurePostLogin(UserAuth auth, ProxiedPlayer player) {
        this.auth = auth;
        this.player = player;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    public UserAuth getAuth() {
        return auth;
    }
}
