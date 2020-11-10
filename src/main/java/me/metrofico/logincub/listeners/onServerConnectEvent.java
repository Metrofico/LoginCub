package me.metrofico.logincub.listeners;

import me.metrofico.logincub.Init;
import me.metrofico.logincub.objects.UserAuth;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class onServerConnectEvent implements Listener {
    private Init plugin;

    public onServerConnectEvent(Init plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChangeServer(ServerConnectEvent event) {
        if (event.isCancelled()) {
            return;
        }
        ProxiedPlayer player = event.getPlayer();
        UserAuth user = UserAuth.getUser(player.getUniqueId());
        if (user != null) {
            if (user.isLoginPremium()) {
                event.setTarget(plugin.getProxy().getServerInfo("lobby"));
                return;
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
