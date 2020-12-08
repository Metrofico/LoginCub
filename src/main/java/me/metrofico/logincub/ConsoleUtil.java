package me.metrofico.logincub;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class ConsoleUtil {
    boolean DEBUG = false;

    public static void prefixConsole(String content) {
        if (content == null) {
            return;
        }
        BungeeCord.getInstance().getConsole().sendMessage(new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&2[SessionAuth]&a " + content)).create());
    }

    public static void Console(String content) {
        if (content == null) {
            return;
        }
        BungeeCord.getInstance().getConsole().sendMessage(new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&a" + content)).create());
    }
}
