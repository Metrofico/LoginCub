package me.metrofico.logincub;

import me.metrofico.logincub.listeners.AsyncPreLoginEvent;
import me.metrofico.logincub.listeners.PostLoginEvent;
import me.metrofico.logincub.listeners.onChatPlayer;
import me.metrofico.logincub.listeners.onServerConnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Events implements Listener {

    private final Init plugin;
    private final ExecutorService service;

    Events(Init plugin) {
        this.plugin = plugin;
        service = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
        new PostLoginEvent(plugin);
        new onChatPlayer(plugin);
        new onServerConnectEvent(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(PreLoginEvent event) {
        if (event.isCancelled()) {
            return;
        }
        event.registerIntent(plugin);
        service.submit(new AsyncPreLoginEvent(event, plugin));
    }

}
