package me.metrofico.logincub.commands;

import me.metrofico.logincub.Init;
import me.metrofico.logincub.eventos.PlayerCrackedLogged;
import me.metrofico.logincub.objects.UserAuth;
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
            UserAuth userAuth = UserAuth.getUser(player.getName());
            if (!userAuth.isLogged()) {
                if (userAuth.getPasswordHashed() != null) {
                    if (!userAuth.isLoginPremium()) {
                        if (strings.length < 1) {
                            userAuth.sendMessage(plugin.getLanguage().getLoginUsage());
                            return;
                        }
                        String password = strings[0];
                        if (userAuth.passwordVerify(password)) {
                            userAuth.sendMessage(plugin.getLanguage().getLoginSuccessfulPassword());
                            PlayerCrackedLogged logged = new PlayerCrackedLogged(userAuth,
                                    player, false);
                            BungeeCord.getInstance().getPluginManager().callEvent(logged);
                            player.connect(plugin.getProxy().getServerInfo("lobby"));
                        } else {
                            userAuth.sendMessage(plugin.getLanguage().getPasswordFailed());
                        }
                    }
                }
            }
        }
    }
}
