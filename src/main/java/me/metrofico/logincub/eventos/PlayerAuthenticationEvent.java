package me.metrofico.logincub.eventos;

import me.metrofico.logincub.objects.UserInLogin;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.AsyncEvent;

public class PlayerAuthenticationEvent extends AsyncEvent<PlayerAuthenticationEvent> {

    UserInLogin userInLogin;
    PendingConnection pendingConnection;
    boolean isNewUser;
    String kickReason;

    public PlayerAuthenticationEvent(UserInLogin userInLogin, PendingConnection pendingConnection, boolean isNewUser,
                                     Callback<PlayerAuthenticationEvent> done) {
        super(done);
        this.userInLogin = userInLogin;
        this.pendingConnection = pendingConnection;
        this.isNewUser = isNewUser;
        this.kickReason = null;
    }

    public UserInLogin getUserInLogin() {
        return userInLogin;
    }

    public PendingConnection getPendingConnection() {
        return pendingConnection;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public String getKickReason() {
        return kickReason;
    }

    public void setKickReason(String kickReason) {
        this.kickReason = kickReason;
    }
}
