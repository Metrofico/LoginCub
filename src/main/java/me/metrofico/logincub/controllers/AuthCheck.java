package me.metrofico.logincub.controllers;

import me.metrofico.logincub.Init;
import me.metrofico.logincub.objects.UserInLogin;
import me.metrofico.logincub.objects.UserManager;

public class AuthCheck implements Runnable {

    private final Init plugin;

    public AuthCheck(Init plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (UserInLogin user : UserManager.getUsersNotLogged()) {
            if (user.timeOutAuthentication()) {
                user.disconnect(plugin.getLanguage().getTimeOut());
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
