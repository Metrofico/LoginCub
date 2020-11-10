package me.metrofico.logincub.eventos;

import me.metrofico.logincub.objects.UserAuth;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Event;

public class PlayerAuthentication extends Event {
    private final UserAuth userAuth;
    private final PendingConnection connection;
    private boolean isNewUser;
    private String kickReason;

    public PlayerAuthentication(UserAuth userAuth, PendingConnection connection, boolean isNewUser) {
        this.userAuth = userAuth;
        this.isNewUser = isNewUser;
        this.connection = connection;
        kickReason = null;
    }

    public void setKickReason(String kickReason) {
        this.kickReason = kickReason;
    }

    public String getKickReason() {
        return kickReason;
    }

    public PendingConnection getConnection() {
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
