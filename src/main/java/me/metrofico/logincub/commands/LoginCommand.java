package me.metrofico.logincub.commands;

import me.metrofico.logincub.Init;
import me.metrofico.logincub.eventos.PlayerCrackedLogged;
import me.metrofico.logincub.objects.UserInLogin;
import me.metrofico.logincub.objects.UserManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class LoginCommand extends Command {
    private final Init plugin;

    public LoginCommand(Init plugin, String name) {
        super(name, null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            UserInLogin userInLogin = UserManager.getUserNotAuthenticated(player.getName());
            if (userInLogin != null && !userInLogin.isLogged()) {
                if (userInLogin.getPasswordHashed() != null) {
                    if (!userInLogin.isEnableLoginPremium()) {
                        if (strings.length < 1) {
                            userInLogin.sendMessage(plugin.getLanguage().getLoginUsage());
                            return;
                        }
                        String password = strings[0];
                        if (userInLogin.passwordVerify(password)) {
                            userInLogin.sendMessage(plugin.getLanguage().getLoginSuccessfulPassword());
                            UserManager.setUserAuthenticated(player.getName(), userInLogin);
                            UserManager.removeUserNotAuthenticated(player.getName());
                            PlayerCrackedLogged logged =
                                    new PlayerCrackedLogged(userInLogin, player, false,
                                            (playerCrackedLogged, throwable) -> player.connect(plugin.getProxy().getServerInfo("lobby")));
                            BungeeCord.getInstance().getPluginManager().callEvent(logged);
                        } else {
                            userInLogin.sendMessage(plugin.getLanguage().getPasswordFailed());
                        }
                    }
                }
            }
        }
    }
}
