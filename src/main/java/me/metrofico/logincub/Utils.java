package me.metrofico.logincub;

import com.google.common.base.Charsets;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {
    public static BaseComponent[] chatColor(String text) {
        if (text == null) {
            return new BaseComponent[]{};
        }
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text));
    }

    public static String md5(String password) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(password.getBytes(Charsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static BaseComponent[] replace(String text, String replace, String object) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text.replace(replace, object)));
    }
}
