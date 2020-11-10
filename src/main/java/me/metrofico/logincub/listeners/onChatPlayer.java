package me.metrofico.logincub.listeners;

import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import me.metrofico.logincub.Init;
import me.metrofico.logincub.objects.UserAuth;

public class onChatPlayer implements Listener {
    private Init plugin;

    public onChatPlayer(Init plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event) {
        Connection connection = event.getSender();
        if (connection instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) connection;
            UserAuth userAuth = UserAuth.getUser(player.getUniqueId());
            if (userAuth != null) {
                if (!userAuth.isLogged()) {
                    String message = event.getMessage().trim();
                    if (!message.isEmpty()) {
                        String command = message.split(" ")[0];
                        if (plugin.matchCommandAllowed(command.toLowerCase())) {
                            return;
                        }
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

}
