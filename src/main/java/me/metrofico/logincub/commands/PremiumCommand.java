package me.metrofico.logincub.commands;

import me.metrofico.logincub.Init;
import me.metrofico.logincub.MojangAPI;
import me.metrofico.logincub.Utils;
import me.metrofico.logincub.objects.UserInLogin;
import me.metrofico.logincub.objects.UserManager;
import me.metrofico.logincub.objects.UserPremiumValidate;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PremiumCommand extends Command {
    private final Init plugin;
    private final ExecutorService service;

    public PremiumCommand(Init plugin, String name) {
        super(name, null);
        service = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            UserInLogin userAuth = UserManager.getUserAuthenticated(player.getName());
            if (userAuth != null && userAuth.isLogged()) {
                if (userAuth.getStatus() != MojangAPI.Account.PREMIUM) {
                    if (userAuth.isDownloadingPremium()) {
                        commandSender.sendMessage(Utils.chatColor("&cEstamos verificando tu cuenta espera un momento..."));
                        return;
                    }
                    commandSender.sendMessage(Utils.chatColor("&aVálidando tu cuenta por favor espera..."));
                    userAuth.setDownloadingPremium(true);
                    service.submit(() -> {
                        try {
                            MojangAPI.FetchAccount account = plugin.getMojangAPI().fetchUUID(player.getName());
                            userAuth.setDownloadingPremium(false);
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
                                userAuth.sendMessage(plugin.getLanguage().getNopremiumPlayer());
                                return;
                            }
                            if (uuid == null) {
                                commandSender.sendMessage(Utils.chatColor("&cNo se puede obtener la información de identificación de mojang, contacta con un administrador!"));
                                return;
                            }
                            userAuth.setStatus(MojangAPI.Account.PREMIUM);
                            plugin.getPlayerDatabase().updateOnlineUuid(player.getName(), uuid.toString(), true);
                            player.disconnect(Utils.chatColor("&6Hemos válidado la información de tu cuenta premium \n&6Ingresa a la network y escribe &b/premium login enable \n&6Para solicitar activar la cuenta premium"));
                        } catch (Throwable w) {
                            userAuth.setDownloadingPremium(false);
                            commandSender.sendMessage(Utils.chatColor("&cNo se pudo válidar tu cuenta premium por un error desconocido"));
                            w.printStackTrace();
                        }
                    });
                    return;
                }
                if (strings.length < 2) {
                    userAuth.sendMessage(plugin.getLanguage().getPremiumUsage());
                    return;
                }
                String parametro = strings[0];
                if ("login".equalsIgnoreCase(parametro)) {
                    String enableDisable = strings[1];
                    switch (enableDisable.toLowerCase()) {
                        case "enable": {
                            if (userAuth.isEnableLoginPremium()) {
                                userAuth.sendMessage("&eYa tienes habilitado el modo &aPREMIUM");
                                return;
                            }
                            userAuth.sendMessage(plugin.getLanguage().getAlertEnablePremium(), "{time}", "" + plugin.getObjectSettings().getTimeOutConfirmPremium());
                            UserPremiumValidate validate = UserManager.getPremiumValidate(player.getName());
                            if (validate == null) {
                                validate = new UserPremiumValidate(0, 0);
                            }
                            if (validate.isFinishedPremiumAnswer()) {
                                validate.setTimeoutPremiumEnable(plugin.getObjectSettings().getTimeOutConfirmPremium());
                            }
                            UserManager.setPremiumValidate(player.getName(), validate);
                            break;
                        }
                        case "disable": {
                            if (!userAuth.isEnableLoginPremium()) {
                                userAuth.sendMessage("&cYa tienes desabilitado el modo &bPREMIUM");
                                return;
                            }
                            userAuth.sendMessage(plugin.getLanguage().getAlertDisablePremium(), "{time}", "" + plugin.getObjectSettings().getTimeOutConfirmPremium());
                            UserPremiumValidate validate = UserManager.getPremiumValidate(player.getName());
                            if (validate == null) {
                                validate = new UserPremiumValidate(0, 0);
                            }
                            if (validate.isFinishedPremiumAnswer()) {
                                validate.setTimeoutPremiumEnable(plugin.getObjectSettings().getTimeOutConfirmPremium());
                            }
                            UserManager.setPremiumValidate(player.getName(), validate);
                            break;
                        }
                        case "yes": {
                            UserPremiumValidate validate = UserManager.getPremiumValidate(player.getName());
                            if (validate == null) {
                                validate = new UserPremiumValidate(0, 0);
                            }
                            if (validate.isFinishedPremiumAnswer()) {
                                userAuth.sendMessage("&eNo tienes pendiente ningun comando de &aENABLE o &cDISABLE");
                                return;
                            }
                            if (userAuth.isEnableLoginPremium()) {
                                UUID uuid = userAuth.getUuid();
                                userAuth.sendMessage(plugin.getLanguage().getYesDisablePremium());
                                plugin.getPlayerDatabase().updatePremiumMode(player.getName(), uuid, false);
                                return;
                            }
                            userAuth.sendMessage(plugin.getLanguage().getYesEnablePremium(), "{time}", "" + plugin.getObjectSettings().getTimeOutJoinPremium());
                            validate.setTimeoutJoinPremium(plugin.getObjectSettings().getTimeOutJoinPremium());
                            UserManager.setPremiumValidate(player.getName(), validate);
                            break;
                        }
                    }
                }
            }
        }
    }
}
