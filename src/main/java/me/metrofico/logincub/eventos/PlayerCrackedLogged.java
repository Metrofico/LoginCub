package me.metrofico.logincub.eventos;

import me.metrofico.logincub.objects.UserInLogin;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.AsyncEvent;

public class PlayerCrackedLogged extends AsyncEvent<PlayerCrackedLogged> {
    private final UserInLogin userAuth;
    private final ProxiedPlayer connection;
    private boolean isNewUser;

    public PlayerCrackedLogged(UserInLogin userAuth, ProxiedPlayer connection, boolean isNewUser, Callback<PlayerCrackedLogged> done) {
        super(done);
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

    public UserInLogin getUserAuth() {
        return userAuth;
    }

}
