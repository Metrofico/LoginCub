package me.metrofico.logincub.controllers;

import me.metrofico.logincub.Init;
import me.metrofico.logincub.objects.UserAuth;

import java.util.Iterator;

public class AuthCheck implements Runnable {

    private final Init plugin;

    public AuthCheck(Init plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Iterator<UserAuth> userIterator = UserAuth.getUsersNotLogged();
        while (userIterator.hasNext()) {
            UserAuth user = userIterator.next();
            if (user.timeOut()) {
                user.disconnect(plugin.getLanguage().getTimeOut());
                UserAuth.removeUser(user.getUserName());
                continue;
            }
            if (user.getPasswordHashed() == null) {
                user.sendMessage(plugin.getLanguage().getRegisterController());
            } else {
                user.sendMessage(plugin.getLanguage().getLoginController());
            }
        }
    }
}
