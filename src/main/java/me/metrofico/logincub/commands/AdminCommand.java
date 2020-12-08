package me.metrofico.logincub.commands;

import me.metrofico.logincub.BungeeUUID;
import me.metrofico.logincub.Init;
import me.metrofico.logincub.MojangAPI;
import me.metrofico.logincub.Utils;
import me.metrofico.logincub.objects.UserAuth;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminCommand extends Command {
    private final Init plugin;
    private final ExecutorService service;

    public AdminCommand(Init plugin, String name) {
        super(name, null);

        service = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (!player.hasPermission("logincuub.admin")) {
                return;
            }
            UserAuth userAuth = UserAuth.getUser(player.getName());
            if (userAuth.isLogged()) {
                if (strings.length < 1) {
                    userAuth.sendMessage(plugin.getLanguage().getAdminUsage());
                    return;
                }
                String subcommand = strings[0];
                if (subcommand.equalsIgnoreCase("reload")) {
                    if (plugin.getMojangAPI().isLoadingProxies()) {
                        userAuth.sendMessage("&a - [LoginCub] is loading proxies.");
                        return;
                    }
                    plugin.reloadObjectSettings();
                    userAuth.sendMessage("&a - [LoginCub] settings reloaded!");
                    return;
                }
                if (subcommand.equalsIgnoreCase("changepass")) {
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
            if (subcommand.equalsIgnoreCase("reload")) {
                if (plugin.getMojangAPI().isLoadingProxies()) {
                    commandSender.sendMessage(Utils.chatColor("&a - [LoginCub] is loading proxies."));
                    return;
                }
                plugin.reloadObjectSettings();
                commandSender.sendMessage(Utils.chatColor("&a - [LoginCub] settings reloaded!"));
                return;
            }
            if (subcommand.equalsIgnoreCase("updatenick")) {
                if (strings.length < 3) {
                    commandSender.sendMessage(Utils.chatColor("&cUtiliza /authadmin updatenick <oldNick> <newNick>"));
                    return;
                }
                String oldName = strings[1];
                String newName = strings[2];
                service.submit(() -> {
                    try {
                        Document document = plugin.getPlayerDatabase().getPlayerByUsername(oldName);
                        if (document == null) {
                            commandSender.sendMessage(Utils.chatColor("&cNo existe ese usuario"));
                            return;
                        }
                        Document Otherdocument = plugin.getPlayerDatabase().getPlayerByUsername(newName);
                        if (Otherdocument != null) {
                            if (oldName.equalsIgnoreCase(newName)) {
                                plugin.getPlayerDatabase().updatePlayerNameFromId(document.getObjectId("_id"), newName);
                                commandSender.sendMessage(Utils.chatColor("&aSe ha actualizado el nick de &e" + oldName + "&a a &b" + newName));
                                return;
                            }
                            commandSender.sendMessage(Utils.chatColor("&cEste nick ya está en uso por otra persona"));
                            return;
                        }
                        plugin.getPlayerDatabase().updatePlayerNameFromId(document.getObjectId("_id"), newName);
                        commandSender.sendMessage(Utils.chatColor("&aSe ha actualizado el nick de &e" + oldName + "&a a &b" + newName));
                    } catch (Throwable w) {
                        commandSender.sendMessage(Utils.chatColor("&cNo se pudo actualizar el nickname ha ocurrido un error"));
                        w.printStackTrace();
                    }
                });
                return;
            }
            if (subcommand.equalsIgnoreCase("premium")) {
                if (strings.length < 3) {
                    commandSender.sendMessage(Utils.chatColor("&cUtiliza /authadmin premium <nick> " +
                            "(enable/disable) para actualizar una premium a un usario"));
                    return;
                }
                String userName = strings[1];
                String parameterEnableOrDisable = strings[2];
                service.submit(() -> {
                    commandSender.sendMessage(Utils.chatColor("&aBuscando usuario..."));
                    Document document = plugin.getPlayerDatabase().getPlayerByUsername(userName);
                    if (document == null) {
                        commandSender.sendMessage(Utils.chatColor("&cNo se encontró el jugador"));
                        return;
                    }
                    String realName = document.get("playerName", null);
                    if (realName == null) {
                        return;
                    }
                    switch (parameterEnableOrDisable.toLowerCase()) {
                        case "enable": {
                            commandSender.sendMessage(Utils.chatColor("&aVerificando cuenta premium de: " + userName));
                            try {
                                UUID onlineUid = BungeeUUID.getUUIDfromString(document.get("onlineUid", null));
                                if (onlineUid == null) {
                                    MojangAPI.FetchAccount account = plugin.getMojangAPI().fetchUUID(userName);
                                    if (account.getStatus() == MojangAPI.Account.RATE_LIMIT) {
                                        commandSender.sendMessage(Utils.chatColor("&cNo podemos contactarnos con los servidores, intenta nuevamente más tarde!"));
                                        return;
                                    }
                                    if (account.getStatus() == MojangAPI.Account.ERROR) {
                                        commandSender.sendMessage(Utils.chatColor("&cNo podemos contactarnos con los servidores, intenta nuevamente más tarde!"));
                                        return;
                                    }
                                    UUID uuid = account.getUuid();
                                    //cambio de uuid dependiendo del usuario //CRACKED (No premium por defecto)
                                    if (account.getStatus() != MojangAPI.Account.PREMIUM) {
                                        commandSender.sendMessage(Utils.chatColor(plugin.getLanguage().getNopremiumPlayer()));
                                        return;
                                    }
                                    if (uuid == null) {
                                        commandSender.sendMessage(Utils.chatColor("&cNo se puede obtener la información de identificación de mojang, contacta con un administrador!"));
                                        return;
                                    }
                                    plugin.getPlayerDatabase().updateOnlineUuid(realName, uuid.toString(), true);
                                    plugin.getPlayerDatabase().updatePremiumMode(realName, true);
                                    commandSender.sendMessage(Utils.chatColor("&aHemos cambiado el modo premium &6&lACTIVO&a para: " + realName));
                                    return;
                                }
                                plugin.getPlayerDatabase().updateOnlineUuid(realName, onlineUid.toString(), false);
                                commandSender.sendMessage(Utils.chatColor("&aHemos cambiado el modo premium &6&lHABILITADO&a para: " + realName));
                            } catch (Throwable w) {
                                commandSender.sendMessage(Utils.chatColor("Error mientras se procesaba la solicitud del comando /authadmin premium"));
                                w.printStackTrace();
                            }

                            plugin.getPlayerDatabase().updatePremiumMode(userName, true);
                            break;
                        }
                        case "disable": {
                            commandSender.sendMessage(Utils.chatColor("&aCambiando el modo de inicio de sesión..."));
                            plugin.getPlayerDatabase().updatePremiumMode(realName, false);
                            commandSender.sendMessage(Utils.chatColor("&aHemos cambiado el modo premium &c&lDESABILITADO&a para: " + realName));

                            break;
                        }
                        default: {
                            commandSender.sendMessage(Utils.chatColor("&cUtiliza /authadmin premium " + userName +
                                    " (enable/disable) para actualizar una premium a un usario"));
                        }
                    }
                });
            }
            if (subcommand.equalsIgnoreCase("changepass")) {
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
