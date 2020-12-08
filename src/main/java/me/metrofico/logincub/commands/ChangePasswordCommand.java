package me.metrofico.logincub.commands;

import me.metrofico.logincub.Cryptography;
import me.metrofico.logincub.Init;
import me.metrofico.logincub.objects.UserAuth;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ChangePasswordCommand extends Command {
    private final Init plugin;

    public ChangePasswordCommand(Init plugin, String name) {
        super(name, null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            UserAuth userAuth = UserAuth.getUser(player.getName());
            if (userAuth.isLogged()) {
                if (userAuth.getPasswordHashed() != null) {
                    if (!userAuth.isLoginPremium()) {
                        if (strings.length < 2) {
                            userAuth.sendMessage(plugin.getLanguage().getPlayerChangepasswordUsage());
                            return;
                        }
                        String password = strings[0];
                        String passwordNew = strings[1];
                        if (userAuth.checkPassword(password)) {
                            if (userAuth.checkPassword(passwordNew)) {
                                userAuth.sendMessage(plugin.getLanguage().getSamePassword());
                            } else {
                                if (passwordNew.length() < plugin.getObjectSettings().getLenghtPassword()) {
                                    userAuth.sendMessage(plugin.getLanguage().getPasswordMin());
                                    return;
                                }
                                userAuth.sendMessage(plugin.getLanguage().getChangepasswordSuccessful());
                                userAuth.setPasswordHashed(Cryptography.sha512(passwordNew));
                                plugin.getPlayerDatabase().updatePasswordByUUID(userAuth.getUuid(), passwordNew);
                            }
                        } else {
                            userAuth.sendMessage(plugin.getLanguage().getChangepasswordPasswordError());
                        }
                    }
                }
            }
        }
    }
}
