package me.metrofico.logincub.listeners;

import com.google.common.base.Charsets;
import me.metrofico.logincub.BungeeUUID;
import me.metrofico.logincub.Init;
import me.metrofico.logincub.MojangAPI;
import me.metrofico.logincub.eventos.PlayerAuthentication;
import me.metrofico.logincub.objects.UserAuth;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import org.bson.Document;

import java.util.UUID;

public class AsyncPreLoginEvent implements Runnable {
    private final PreLoginEvent event;
    private final Init plugin;

    public AsyncPreLoginEvent(PreLoginEvent event, Init plugin) {
        this.event = event;
        this.plugin = plugin;
    }

    private void dispatchCancelUser(String razon) {
        event.setCancelled(true);
        if (razon == null) {
            razon = "";
        }
        event.setCancelReason(new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', razon)).create());
        event.completeIntent(plugin);
    }

    @Override
    public void run() {
        try {
            PendingConnection user_pending = event.getConnection();
            String username = user_pending.getName();
            Document playerByUsername = plugin.getPlayerDatabase().getPlayerByUsername(username);
            //Reutilizar username para consultar solo datos sin fetch a mojang
            if (playerByUsername != null) {
                String _id = playerByUsername.getObjectId("_id").toHexString();
                boolean loginPremium = playerByUsername.getBoolean("loginPremium", false);
                String password = playerByUsername.containsKey("password") ? playerByUsername.getString("password") : null;
                MojangAPI.Account status;
                String uuid;
                if (playerByUsername.containsKey("onlineUid")) {
                    uuid = playerByUsername.getString("onlineUid");
                    status = MojangAPI.Account.PREMIUM;
                } else {
                    uuid = playerByUsername.getString("offlineUid");
                    status = MojangAPI.Account.CRACKED;
                }
                UUID uidParser = BungeeUUID.getUUIDfromString(uuid);
                //Usuario con login premium activado/desactivado
                user_pending.setUniqueId(uidParser);
                UserAuth userAuth = UserAuth.getUser(uidParser);
                if (userAuth == null) {
                    userAuth = new UserAuth(username, uidParser, status, _id);
                }
                if (!userAuth.isTimeOnFinishJoinPremium()) {
                    loginPremium = true;
                }
                if (!loginPremium) {
                    userAuth.setTimeOut(plugin.getObjectSettings().getTimeOutAuth());
                }
                user_pending.setOnlineMode(loginPremium);
                userAuth.setLogged(false);
                userAuth.setLoginPremium(loginPremium);
                userAuth.setPasswordHashed(password);
                UserAuth.updateUser(userAuth);
                PlayerAuthentication playerAuthentication = new PlayerAuthentication(userAuth,
                        user_pending, false); // se ejecuta sincrono
                BungeeCord.getInstance().getPluginManager().callEvent(playerAuthentication);
                String kickReason = playerAuthentication.getKickReason();
                if (kickReason != null) {
                    dispatchCancelUser(kickReason);
                    return;
                }
                event.completeIntent(plugin);
                return;
            }
        /*
          NO SIEMPRE SE CAMBIAN DE NOMBRE 500 JUGADORES AL MISMO TIEMPO POR LO QUE ESTO
          SOLO SE EJECUTARÁ CUANDO:
         - NO EXISTE EL USUARIO
         - VERIFICAR SI MOJANG CONTIENE LA UUID utilizando el NOMBRE
         */
            MojangAPI.FetchAccount account = plugin.getMojangAPI().fetchUUID(username);
            if (account.getStatus() == MojangAPI.Account.RATE_LIMIT) {
                dispatchCancelUser(plugin.getLanguage().getRateLimit());
                return;
            }
            if (account.getStatus() == MojangAPI.Account.ERROR) {
                dispatchCancelUser(plugin.getLanguage().getErrorLogin());
                return;
            }
            UUID uuid = account.getUuid();
            //cambio de uuid dependiendo del usuario //CRACKED (No premium por defecto)
            if (account.getStatus() != MojangAPI.Account.PREMIUM) {
                uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
            }
            //Verificar si el usuario esta en la base de datos
            if (uuid == null) {
                dispatchCancelUser("Error Inesperado, contacta con un Administrador de Minecub");
                return;
            }
            Document player = plugin.getPlayerDatabase().getPlayer(uuid);
            if (player != null) {
                String uid = player.getObjectId("_id").toString();
                boolean loginPremium = player.getBoolean("loginPremium", false);
                String password = player.containsKey("password") ? player.getString("password") : null;
                //POSIBLE cambio de username
                String usernameFromDatabase = player.getString("playerName");
                if (!usernameFromDatabase.equals(username)) {
                    plugin.getPlayerDatabase().updatePlayerNameFromUUID(uuid, username);
                }
                //Usuario con login premium activado/desactivado
                user_pending.setUniqueId(uuid);
                UserAuth userAuth = UserAuth.getUser(uuid);
                if (userAuth == null) {
                    userAuth = new UserAuth(username, uuid, account.getStatus(), uid);
                }
                if (userAuth.isTimeOnFinishJoinPremium()) {
                    loginPremium = true;
                }
                if (!loginPremium) {
                    userAuth.setTimeOut(plugin.getObjectSettings().getTimeOutAuth());
                }
                user_pending.setOnlineMode(loginPremium);
                userAuth.setLogged(false);
                userAuth.setLoginPremium(loginPremium);
                userAuth.setPasswordHashed(password);
                UserAuth.updateUser(userAuth);
                PlayerAuthentication playerAuthentication = new PlayerAuthentication(userAuth, user_pending, false); // se ejecuta sincrono
                BungeeCord.getInstance().getPluginManager().callEvent(playerAuthentication);
                String kickReason = playerAuthentication.getKickReason();
                if (kickReason != null) {
                    dispatchCancelUser(kickReason);
                    return;
                }
                event.completeIntent(plugin);
                return;
            }
            //no existe
            user_pending.setUniqueId(uuid);
            user_pending.setOnlineMode(false);
            UserAuth user = new UserAuth(username, uuid, account.getStatus(), null);
            user.setTimeOut(plugin.getObjectSettings().getTimeOutAuth());
            UserAuth.updateUser(user);
            PlayerAuthentication playerAuthentication = new PlayerAuthentication(user, user_pending, true); // se ejecuta sincrono
            BungeeCord.getInstance().getPluginManager().callEvent(playerAuthentication);
            String kickReason = playerAuthentication.getKickReason();
            if (kickReason != null) {
                dispatchCancelUser(kickReason);
                return;
            }
            event.completeIntent(plugin);
        } catch (Throwable w) {
            dispatchCancelUser("Error mientras se cargaban tus datos contacta con un Administrador de MineCub");
            //event.completeIntent(plugin);
            w.printStackTrace();
        }
    }
}
