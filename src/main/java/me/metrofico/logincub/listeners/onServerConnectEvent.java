package me.metrofico.logincub.listeners;

import me.metrofico.logincub.Init;
import me.metrofico.logincub.objects.UserAuth;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
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

    @EventHandler(priority = EventPriority.LOW)
    public void onChangeServer(ServerConnectEvent event) {
        if (event.isCancelled()) {
            return;
        }
        ProxiedPlayer player = event.getPlayer();
        UserAuth user = UserAuth.getUser(player.getName());
        if (user != null) {
            if (user.isLoginPremium()) {
                if (!user.isLogged()) {
                    try {
                        user.setLogged(true);
                        event.setTarget(plugin.getProxy().getServerInfo("lobby"));
                    } catch (Throwable throwable) {
                        player.disconnect(NO_LOBBY_AVAILABLE);
                        event.setCancelled(true);
                        user.setLogged(false);
                        return;
                    }
                    return;
                }
            }
            if (!user.isLogged()) {
                user.setTargetServer(event.getTarget());
                ServerInfo serverInfo = plugin.getProxy().getServerInfo(plugin.getObjectSettings().getServerNameAuth());
                event.setTarget(serverInfo);
            }
            return;
        }
        player.disconnect(new ComponentBuilder("Error unexpected").create());
    }
}
