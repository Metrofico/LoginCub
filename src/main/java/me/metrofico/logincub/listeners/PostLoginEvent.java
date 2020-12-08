package me.metrofico.logincub.listeners;


import me.metrofico.logincub.Init;
import me.metrofico.logincub.objects.UserAuth;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostLoginEvent implements Listener {
    private final Init plugin;
    private final ExecutorService service;

    public PostLoginEvent(Init plugin) {
        this.plugin = plugin;
        service = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UserAuth auth = UserAuth.getUser(player.getName());
        if (auth != null) {
            if (auth.isFinishedPremiumValidate()) {
                UserAuth.removeUser(player.getName());
            }
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPostUuid(net.md_5.bungee.api.event.LoginEvent event) {
        PendingConnection player = event.getConnection();
        UserAuth userAuth = UserAuth.getUser(player.getName());
        if (userAuth != null) {
            userAuth = userAuth.clone();
            if (!userAuth.isLogged()) {
                UUID uuidNew = userAuth.getUuid();
                if (player.getUniqueId() != uuidNew) {
                    InitialHandler handler = (InitialHandler) player;
                    handler.setUniqueId(userAuth.getUuid());
                    UserAuth.updateUser(player.getName(), userAuth);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPost(net.md_5.bungee.api.event.PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UserAuth userAuth = UserAuth.getUser(player.getName());
        if (userAuth != null) {
            if (userAuth.isLoginPremium()) {
                if (!userAuth.isFinishedPremiumValidate()) {
                    userAuth.sendMessage(plugin.getLanguage().getPremiumVerified());
                    userAuth.sendMessage(plugin.getLanguage().getLogingSuccessful());
                    service.submit(() -> {
                        plugin.getPlayerDatabase().updatePremiumMode(player.getName(), userAuth.getUuid(), true);
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
