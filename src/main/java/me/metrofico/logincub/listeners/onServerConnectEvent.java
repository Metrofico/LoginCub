package me.metrofico.logincub.listeners;

import me.metrofico.logincub.Init;
import me.metrofico.logincub.objects.UserInLogin;
import me.metrofico.logincub.objects.UserManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class onServerConnectEvent implements Listener {
    private static final TextComponent NO_LOBBY_AVAILABLE = new TextComponent(ChatColor.RED + "¡No hay ningún lobby disponible actualmente!");
    private final Init plugin;

    public onServerConnectEvent(Init plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }
/*

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogged(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (player != null) {
            Server s = event.getServer();
            UserInLogin authenticated = UserManager.getUserAuthenticated(player.getName());
            if (authenticated != null && authenticated.isEnableLoginPremium()) {
                if (s != null) {
                    if (s.getInfo().getName().equalsIgnoreCase("lobby")) {
                        authenticated.setLogged(true);
                    }
                }
            }
        }
    }
*/

    /*
          Ocurre despues de PostLoginEvent
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChangeServer(ServerConnectEvent event) {
        if (event.isCancelled()) {
            return;
        }
        ProxiedPlayer player = event.getPlayer();
        UserInLogin user = UserManager.getUserNotAuthenticated(player.getName());
        if (user != null) {
            if (!user.isLogged()) {
                user.setTargetServer(event.getTarget());
                ServerInfo serverInfo = plugin.getProxy().getServerInfo(plugin.getObjectSettings().getServerNameAuth());
                event.setTarget(serverInfo);
                return;
            }
        }
        /*
        USUARIOS PREMIUM
         */
        UserInLogin authenticated = UserManager.getUserAuthenticated(player.getName());
        if (authenticated != null) {
            if (authenticated.isEnableLoginPremium()) {
                if (!authenticated.isSendedLobby()) {
                    authenticated.setLogged(true);
                    authenticated.setSendedLobby(true);
                    try {
                        event.setCancelled(false);
                        event.setTarget(plugin.getProxy().getServerInfo("lobby"));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        event.setCancelled(true);
                        authenticated.setLogged(false);
                        player.disconnect(NO_LOBBY_AVAILABLE);

                    }
                }
            }
        }
    }
}
