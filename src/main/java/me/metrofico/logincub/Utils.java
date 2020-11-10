package me.metrofico.logincub;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class Utils {
    public static BaseComponent[] chatColor(String text) {
        if (text == null) {
            return new BaseComponent[]{};
        }
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text));
    }

    public static BaseComponent[] replace(String text, String replace, String object) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text.replace(replace, object)));
    }
}
