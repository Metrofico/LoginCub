package me.metrofico.logincub.commands;

import com.google.common.base.Charsets;
import me.metrofico.logincub.Cryptography;
import me.metrofico.logincub.Init;
import me.metrofico.logincub.MojangAPI;
import me.metrofico.logincub.Utils;
import me.metrofico.logincub.callbacks.SingleCallback;
import me.metrofico.logincub.eventos.PlayerCrackedLogged;
import me.metrofico.logincub.objects.UserAuth;
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
            UserAuth userAuth = UserAuth.getUser(player.getName());
            if (!userAuth.isLogged()) {
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
                    if (userAuth.getStatus() == MojangAPI.Account.PREMIUM) {
                        userAuth.setWait(true);
                        plugin.getPlayerDatabase().saveOnlinePlayer(userAuth.getObjectId(), userAuth.getUserName(), userAuth.getUuid(), password,
                                callSuccess(userAuth));
                    } else {
                        plugin.getPlayerDatabase().saveOfflinePlayer(userAuth.getObjectId(), userAuth.getUserName(), userAuth.getUuid(),
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

    public SingleCallback callSuccess(UserAuth userAuth) {
        userAuth.setWait(true);
        return new SingleCallback() {
            @Override
            public void onSuccess() {
                userAuth.setLogged(true);
                userAuth.sendMessage(plugin.getLanguage().getSignupSuccessful());
                ProxiedPlayer proxiedPlayer = userAuth.getPlayer();
                if (proxiedPlayer != null) {
                    PlayerCrackedLogged logged = new PlayerCrackedLogged(userAuth, proxiedPlayer, true);
                    BungeeCord.getInstance().getPluginManager().callEvent(logged);
                    proxiedPlayer.connect(plugin.getProxy().getServerInfo("lobby"));
                }
                userAuth.setWait(false);
            }

            @Override
            public void onError(Throwable throwable) {
                userAuth.kick("&cHa ocurrido un error [0x0002] contacta con un administrador");
                userAuth.sendMessage("Ha ocurrido un error mientras se registraba tu cuenta");
                userAuth.setWait(false);
            }
        };
    }

}
