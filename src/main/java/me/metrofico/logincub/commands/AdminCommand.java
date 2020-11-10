package me.metrofico.logincub.commands;

import me.metrofico.logincub.Init;
import me.metrofico.logincub.Utils;
import me.metrofico.logincub.objects.UserAuth;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class AdminCommand extends Command {
    private Init plugin;

    public AdminCommand(Init plugin, String name) {
        super(name, null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (!player.hasPermission("logincuub.admin")) {
                return;
            }
            UserAuth userAuth = UserAuth.getUser(player.getUniqueId());
            if (userAuth.isLogged()) {
                if (strings.length < 1) {
                    userAuth.sendMessage(plugin.getLanguage().getAdminUsage());
                    return;
                }
                String subcommand = strings[0];
                if (subcommand.toLowerCase().equals("reload")) {
                    if (plugin.getMojangAPI().isLoadingProxies()) {
                        userAuth.sendMessage("&a - [LoginCub] is loading proxies.");
                        return;
                    }
                    plugin.reloadObjectSettings();
                    userAuth.sendMessage("&a - [LoginCub] settings reloaded!");
                    return;
                }
                if (subcommand.toLowerCase().equals("changepass")) {
                    if (strings.length < 3) {
                        userAuth.sendMessage(plugin.getLanguage().getAdmingUsageChangepass());
                        return;
                    }
                    String userName = strings[1];
                    String password = strings[2];
                    if (password.length() < plugin.getObjectSettings().getLenghtPassword()) {
                        userAuth.sendMessage(plugin.getLanguage().getPasswordMin());
                        return;
                    }
                    if (plugin.getPlayerDatabase().updatePasswordOfflinePlayerByUsername(userName, password)) {
                        userAuth.sendMessage(plugin.getLanguage().getAdminpasswordUpdated(), "{player}", userName);
                    } else {
                        userAuth.sendMessage(plugin.getLanguage().getAdminuserNotExist(), "{player}", userName);
                    }
                }
            }
        } else {
            if (strings.length < 1) {
                commandSender.sendMessage(Utils.chatColor(plugin.getLanguage().getAdminUsage()));
                return;
            }
            String subcommand = strings[0];
            if (subcommand.toLowerCase().equals("reload")) {
                if (plugin.getMojangAPI().isLoadingProxies()) {
                    commandSender.sendMessage(Utils.chatColor("&a - [LoginCub] is loading proxies."));
                    return;
                }
                plugin.reloadObjectSettings();
                commandSender.sendMessage(Utils.chatColor("&a - [LoginCub] settings reloaded!"));
                return;
            }
            if (subcommand.toLowerCase().equals("changepass")) {
                if (strings.length < 3) {
                    commandSender.sendMessage(Utils.chatColor(plugin.getLanguage().getAdmingUsageChangepass()));
                    return;
                }
                String userName = strings[1];
                String password = strings[2];
                if (password.length() < plugin.getObjectSettings().getLenghtPassword()) {
                    commandSender.sendMessage(Utils.chatColor(plugin.getLanguage().getPasswordMin()));
                    return;
                }
                if (plugin.getPlayerDatabase().updatePasswordOfflinePlayerByUsername(userName, password)) {
                    commandSender.sendMessage(Utils.replace(plugin.getLanguage().getAdminpasswordUpdated(), "{player}", userName));
                } else {
                    commandSender.sendMessage(Utils.replace(plugin.getLanguage().getAdminuserNotExist(), "{player}", userName));
                }
            }

        }
    }
}
