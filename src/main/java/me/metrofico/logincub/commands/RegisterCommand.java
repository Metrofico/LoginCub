package me.metrofico.logincub.commands;

import com.google.common.base.Charsets;
import me.metrofico.logincub.Cryptography;
import me.metrofico.logincub.Init;
import me.metrofico.logincub.MojangAPI;
import me.metrofico.logincub.Utils;
import me.metrofico.logincub.callbacks.SingleCallback;
import me.metrofico.logincub.eventos.PlayerCrackedLogged;
import me.metrofico.logincub.objects.UserInLogin;
import me.metrofico.logincub.objects.UserManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.UUID;

public class RegisterCommand extends Command {
    private final Init plugin;

    public RegisterCommand(Init plugin, String name) {
        super(name, null);
        this.plugin = plugin;
    }


    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            UserInLogin userAuth = UserManager.getUserNotAuthenticated(player.getName());
            if (userAuth != null && !userAuth.isLogged()) {
                if (userAuth.getPasswordHashed() == null) {
                    if (strings.length < 2) {
                        userAuth.sendMessage(plugin.getLanguage().getRegisterUsage());
                        return;
                    }
                    String password = strings[0];
                    String password_repeat = strings[1];
                    if (password.length() < plugin.getObjectSettings().getLenghtPassword()) {
                        userAuth.sendMessage(plugin.getLanguage().getPasswordMin());
                        return;
                    }
                    if (!password.equals(password_repeat)) {
                        userAuth.sendMessage(plugin.getLanguage().getPasswordNoMatch());
                        return;
                    }
                    if (userAuth.isWait()) {
                        userAuth.sendMessage("&cEspera a que se procese la solicitud anterior");
                        return;
                    }
                    userAuth.setPasswordHashed(Cryptography.sha512(password));
                    UUID uuid = userAuth.getUuid();
                    if (userAuth.getStatus() == MojangAPI.Account.PREMIUM) {
                        userAuth.setWait(true);
                        plugin.getPlayerDatabase().saveOnlinePlayer(userAuth.getObjectIdHex(),
                                userAuth.getUserName(), uuid, password,
                                callSuccess(userAuth));
                    } else {
                        plugin.getPlayerDatabase().saveOfflinePlayer(userAuth.getObjectIdHex(),
                                userAuth.getUserName(), uuid,
                                password, callSuccess(userAuth));
                    }
                }
            }
        } else {
            if (strings.length < 2) {
                commandSender.sendMessage(Utils.chatColor("&fUso correcto: &c/register <nombre> <clave>"));
                return;
            }
            String username = strings[0];
            String password = strings[1];
            if (password.length() < plugin.getObjectSettings().getLenghtPassword()) {
                commandSender.sendMessage(Utils.chatColor(plugin.getLanguage().getPasswordMin()));
                return;
            }
            Document alreadyExist = plugin.getPlayerDatabase().getPlayerByUsername(username);
            if (alreadyExist != null) {
                commandSender.sendMessage(Utils.chatColor("&cEste nombre de usuario ya está registrado!"));
                return;
            }
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
            plugin.getPlayerDatabase().saveOfflinePlayer(new ObjectId().toHexString(), username, uuid, password, null);
            commandSender.sendMessage(Utils.chatColor("&aHas creado el usuario correctamente!"));
        }
    }

    public SingleCallback callSuccess(UserInLogin userInLogin) {
        userInLogin.setWait(true);
        return new SingleCallback() {
            @Override
            public void onSuccess() {
                userInLogin.setLogged(true);
                userInLogin.sendMessage(plugin.getLanguage().getSignupSuccessful());
                ProxiedPlayer proxiedPlayer = userInLogin.getPlayer();
                if (proxiedPlayer != null) {
                    UserManager.setUserAuthenticated(proxiedPlayer.getName(), userInLogin);
                    UserManager.removeUserNotAuthenticated(proxiedPlayer.getName());
                    PlayerCrackedLogged logged = new PlayerCrackedLogged(userInLogin, proxiedPlayer, true,
                            (playerCrackedLogged, throwable) -> proxiedPlayer.connect(plugin.getProxy().getServerInfo("lobby")));
                    BungeeCord.getInstance().getPluginManager().callEvent(logged);
                }
                userInLogin.setWait(false);
            }

            @Override
            public void onError(Throwable throwable) {
                userInLogin.kick("&cHa ocurrido un error [0x0002] contacta con un administrador");
                userInLogin.sendMessage("Ha ocurrido un error mientras se registraba tu cuenta");
                userInLogin.setWait(false);
            }
        };
    }

}
