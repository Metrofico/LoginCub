package me.metrofico.logincub.listeners;

import me.metrofico.logincub.Init;
import me.metrofico.logincub.objects.UserInLogin;
import me.metrofico.logincub.objects.UserManager;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class onChatPlayer implements Listener {
    private final Init plugin;

    public onChatPlayer(Init plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event) {
        Connection connection = event.getSender();
        if (connection instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) connection;
            UserInLogin userAuth = UserManager.getUserNotAuthenticated(player.getName());
            if (userAuth != null) {
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
