package me.metrofico.logincub.eventos;

import me.metrofico.logincub.objects.UserAuth;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class PlayerCrackedLogged extends Event {
    private final UserAuth userAuth;
    private final ProxiedPlayer connection;
    private boolean isNewUser;

    public PlayerCrackedLogged(UserAuth userAuth, ProxiedPlayer connection, boolean isNewUser) {
        this.userAuth = userAuth;
        this.isNewUser = isNewUser;
        this.connection = connection;
    }


    public ProxiedPlayer getPlayer() {
        return connection;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public void setNewUser(boolean newUser) {
        isNewUser = newUser;
    }

    public UserAuth getUserAuth() {
        return userAuth;
    }

}
