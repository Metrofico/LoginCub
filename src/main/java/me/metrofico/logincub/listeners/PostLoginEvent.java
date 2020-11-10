package me.metrofico.logincub.listeners;


import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import me.metrofico.logincub.Init;
import me.metrofico.logincub.objects.UserAuth;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostLoginEvent implements Listener {
    private Init plugin;
    private final ExecutorService service;

    public PostLoginEvent(Init plugin) {
        this.plugin = plugin;
        service = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPost(net.md_5.bungee.api.event.PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UserAuth userAuth = UserAuth.getUser(player.getUniqueId());
        if (userAuth != null) {
            if (userAuth.isLoginPremium()) {
                if (!userAuth.isTimeOnFinishJoinPremium()) {
                    userAuth.sendMessage(plugin.getLanguage().getPremiumVerified());
                    userAuth.sendMessage(plugin.getLanguage().getLogingSuccessful());
                    service.submit(() -> {
                        plugin.getPlayerDatabase().updatePremiumMode(userAuth.getUuid(), true);
                    });
                    userAuth.setTimeoutJoinPremium(0);
                } else {
                    userAuth.sendMessage(plugin.getLanguage().getLogingSuccessful());
                }
            } else {
                if (userAuth.getPasswordHashed() != null) {
                    userAuth.sendMessage(plugin.getLanguage().getLoginWelcome());
                } else {
                    userAuth.sendMessage(plugin.getLanguage().getRegisterWelcome());
                }
            }
            return;
        }
        player.disconnect(new ComponentBuilder(plugin.getLanguage().getErrorLogin()).create());
    }
}
