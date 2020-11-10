package me.metrofico.logincub.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import me.metrofico.logincub.Init;
import me.metrofico.logincub.MojangAPI;
import me.metrofico.logincub.objects.UserAuth;

public class PremiumCommand extends Command {
    private Init plugin;

    public PremiumCommand(Init plugin, String name) {
        super(name, null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            UserAuth userAuth = UserAuth.getUser(player.getUniqueId());
            if (userAuth.isLogged()) {
                if (userAuth.getStatus() != MojangAPI.Account.PREMIUM) {
                    userAuth.sendMessage(plugin.getLanguage().getNopremiumPlayer());
                    return;
                }
                if (strings.length < 2) {
                    userAuth.sendMessage(plugin.getLanguage().getPremiumUsage());
                    return;
                }
                String parametro = strings[0];
                if ("login".equals(parametro.toLowerCase())) {
                    String enableDisable = strings[1];
                    switch (enableDisable.toLowerCase()) {
                        case "enable": {
                            if (userAuth.isLoginPremium()) {
                                return;
                            }
                            userAuth.sendMessage(plugin.getLanguage().getAlertEnablePremium(), "{time}", "" + plugin.getObjectSettings().getTimeOutConfirmPremium());
                            if (userAuth.timeOutPremiumActive()) {
                                userAuth.setTimeoutPremiumEnable(plugin.getObjectSettings().getTimeOutConfirmPremium());
                            }
                            break;
                        }
                        case "disable": {
                            if (!userAuth.isLoginPremium()) {
                                return;
                            }
                            userAuth.sendMessage(plugin.getLanguage().getAlertDisablePremium(), "{time}", "" + plugin.getObjectSettings().getTimeOutConfirmPremium());
                            if (userAuth.timeOutPremiumActive()) {
                                userAuth.setTimeoutPremiumEnable(plugin.getObjectSettings().getTimeOutConfirmPremium());
                            }
                            break;
                        }
                        case "yes": {
                            if (userAuth.timeOutPremiumActive()) {
                                return;
                            }
                            if (userAuth.isLoginPremium()) {
                                userAuth.sendMessage(plugin.getLanguage().getYesDisablePremium());
                                plugin.getPlayerDatabase().updatePremiumMode(userAuth.getUuid(), false);
                                return;
                            }
                            userAuth.sendMessage(plugin.getLanguage().getYesEnablePremium(), "{time}", "" + plugin.getObjectSettings().getTimeOutJoinPremium());
                            userAuth.setTimeoutJoinPremium(plugin.getObjectSettings().getTimeOutJoinPremium());

                            break;
                        }
                    }
                }
            }
        }
    }
}
