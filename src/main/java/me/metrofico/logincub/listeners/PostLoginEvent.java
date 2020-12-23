package me.metrofico.logincub.listeners;


import me.metrofico.logincub.ConsoleUtil;
import me.metrofico.logincub.Init;
import me.metrofico.logincub.objects.UserInLogin;
import me.metrofico.logincub.objects.UserManager;
import me.metrofico.logincub.objects.UserPremiumValidate;
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
        UserManager.removeUserNotAuthenticated(player.getName());
        UserManager.removeUserAuthenticated(player.getName());
    }

    /*
     Se ejecuta antes de ServerConnectEvent
     Cambia la UUID directamente desde la network
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPostUuid(net.md_5.bungee.api.event.LoginEvent event) {
        event.registerIntent(plugin);
        service.submit(() -> {
            try {
                PendingConnection player = event.getConnection();
                UserInLogin userInLogin = UserManager.getUserNotAuthenticated(player.getName());
                if (userInLogin != null) {
                    if (!userInLogin.isLogged()) {
                        UUID userUid = userInLogin.getUuid();
                        if (player.getUniqueId() != userUid) {
                            InitialHandler handler = (InitialHandler) player;
                            handler.setUniqueId(userUid);
                            event.completeIntent(plugin);
                            return;
                        }
                    }
                }
                event.completeIntent(plugin);
            } catch (Throwable w) {
                w.printStackTrace();
                event.completeIntent(plugin);
            }
        });

    }

    /*
    Se ejecuta antes de ServerConnectEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPost(net.md_5.bungee.api.event.PostLoginEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();
            UserInLogin userInLog = UserManager.getUserNotAuthenticated(player.getName());
            if (userInLog != null) {
                if (userInLog.isEnableLoginPremium()) {

                    UserManager.setUserAuthenticated(player.getName(), userInLog);
                    UserManager.removeUserNotAuthenticated(player.getName());
                    UserPremiumValidate validate = UserManager.getPremiumValidate(player.getName());
                        /*player.connect(plugin.getProxy().getServerInfo("lobby"), new Callback<Boolean>() {
                            @Override
                            public void done(Boolean aBoolean, Throwable throwable) {
                                if(throwable != null){
                                    throwable.printStackTrace();
                                }
                                if (aBoolean) {
                                    userInLog.setLogged(true);
                                }

                            }
                        });*/
                    if (validate != null) {
                        if (!validate.isFinishedPremiumValidate()) {
                            userInLog.sendMessage(plugin.getLanguage().getPremiumVerified());
                            userInLog.sendMessage(plugin.getLanguage().getLogingSuccessful());
                            UUID userUid = userInLog.getUuid();
                            service.submit(() -> {
                                plugin.getPlayerDatabase().updatePremiumMode(player.getName(),
                                        userUid, true);
                            });
                        }
                        UserManager.removePremiumValidate(player.getName());
                    } else {
                        userInLog.sendMessage(plugin.getLanguage().getLogingSuccessful());
                    }
                } else {
                    if (userInLog.getPasswordHashed() != null) {
                        userInLog.sendMessage(plugin.getLanguage().getLoginWelcome());
                    } else {
                        userInLog.sendMessage(plugin.getLanguage().getRegisterWelcome());
                    }
                }
                return;
            }
            ConsoleUtil.Console("Error no esperado al iniciar sesion con el jugador: " + player.getName());
            player.disconnect(new ComponentBuilder(plugin.getLanguage().getErrorLogin()).create());
        } catch (Throwable w) {
            w.printStackTrace();
        }

    }
}
