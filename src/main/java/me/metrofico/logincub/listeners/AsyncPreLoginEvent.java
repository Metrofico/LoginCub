package me.metrofico.logincub.listeners;

import com.google.common.base.Charsets;
import me.metrofico.logincub.BungeeUUID;
import me.metrofico.logincub.Init;
import me.metrofico.logincub.MojangAPI;
import me.metrofico.logincub.eventos.PlayerAuthenticationEvent;
import me.metrofico.logincub.objects.UserInLogin;
import me.metrofico.logincub.objects.UserManager;
import me.metrofico.logincub.objects.UserPremiumValidate;
import me.metrofico.logincub.objects.UserUUID;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.UUID;

public class AsyncPreLoginEvent implements Runnable {
    private final PreLoginEvent event;
    private final Init plugin;

    public AsyncPreLoginEvent(PreLoginEvent event, Init plugin) {
        this.event = event;
        this.plugin = plugin;
    }

    private void dispatchCancelUser(String razon) {
        try {
            event.setCancelled(true);
            if (razon == null) {
                razon = "";
            }
            event.setCancelReason(new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', razon)).create());
            event.completeIntent(plugin);
            return;
        } catch (Throwable w) {
            w.printStackTrace();
        }
        event.setCancelled(true);
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
                boolean updateInGame = playerByUsername.getBoolean("updateInGame", false);
                String savedPlayerName = (String) playerByUsername.getOrDefault("playerName", null);
                if (savedPlayerName == null) {
                    dispatchCancelUser("&cTu usuario está registrada erroneamente donde no se ha encontrado tu usuario registrado," +
                            " contacta con un administrador");
                    return;
                }
                String password = playerByUsername.containsKey("password") ? playerByUsername.getString("password") : null;
                MojangAPI.Account status;
                String uuid;
                UUID onlineUuid = BungeeUUID.getUUIDfromString(playerByUsername.getString("onlineUid"));
                UUID offlineUuid = BungeeUUID.getUUIDfromString(playerByUsername.getString("offlineUid"));
                if (playerByUsername.containsKey("onlineUid")) {
                    if (updateInGame) {
                        uuid = playerByUsername.getString("offlineUid");
                    } else {
                        uuid = playerByUsername.getString("onlineUid");
                    }
                    status = MojangAPI.Account.PREMIUM;
                } else {
                    uuid = playerByUsername.getString("offlineUid");
                    status = MojangAPI.Account.CRACKED;
                }
                if (uuid == null) {
                    dispatchCancelUser("0x0006: No se a asignado una UUID, contacta con un Administrador");
                    return;
                }
                UUID uidParser = BungeeUUID.getUUIDfromString(uuid);

                // en caso de que esté en memoria al usar /premium login enable
                UserInLogin userInLogin = UserManager.getUserAuthenticated(username);
                if (userInLogin == null) {
                    UserUUID userUid = new UserUUID(uidParser, offlineUuid, onlineUuid);
                    userInLogin = new UserInLogin(username, userUid, status, _id);
                }
                /*
                    Verificar si el usuario está usando el (/premium login enable) (con un timeOut)
                 */
                UserPremiumValidate validatePremium = UserManager.getPremiumValidate(username);
                if (validatePremium != null) {
                    if (!validatePremium.isFinishedPremiumValidate()) {
                        loginPremium = true;
                    } else {
                        UserManager.removePremiumValidate(username);
                    }
                }
                if (!loginPremium) {
                    //* El usuario no es premium estableciendole el tiempo de finalización
                    userInLogin.setTimeOnFinishOut(plugin.getObjectSettings().getTimeOutAuth());
                }
                user_pending.setOnlineMode(loginPremium);
                //Usuario con login premium activado/desactivado
                user_pending.setUniqueId(uidParser);
                userInLogin.setEnableLoginPremium(loginPremium);
                userInLogin.setLogged(false);
                userInLogin.setPasswordHashed(password);
                asyncAuthentication(userInLogin, user_pending, false);
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
            UUID onlineUuid = uuid;
            UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
            //cambio de uuid dependiendo del usuario //CRACKED (No premium por defecto)
            if (account.getStatus() != MojangAPI.Account.PREMIUM) {
                uuid = offlineUuid;
            }
            //Verificar si el usuario esta en la base de datos
            if (uuid == null) {
                dispatchCancelUser("0x0006: No se pudo encontrar tu UUID en los servidores de Mojang, contacta con un Administrador");
                return;
            }
            Document player = plugin.getPlayerDatabase().getPlayer(uuid);
            if (player != null) {
                String uid = player.getObjectId("_id").toHexString();
                boolean loginPremium = player.getBoolean("loginPremium", false);
                boolean updateInGame = player.getBoolean("updateInGame", false);
                if (updateInGame) {
                    uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
                }
                String password = player.containsKey("password") ? player.getString("password") : null;
                //POSIBLE cambio de username
                String usernameFromDatabase = player.getString("playerName");
                if (!usernameFromDatabase.equalsIgnoreCase(username)) {
                    plugin.getPlayerDatabase().updatePlayerNameFromUUID(uuid, username);
                }
                //Usuario con login premium activado/desactivado
                UserInLogin userInLogin = UserManager.getUserNotAuthenticated(user_pending.getName());
                if (userInLogin == null) {
                    UserUUID userUUID = new UserUUID(uuid, uuid, onlineUuid);
                    userInLogin = new UserInLogin(username, userUUID, account.getStatus(), uid);
                }
                if (!loginPremium) {
                    userInLogin.setTimeOnFinishOut(plugin.getObjectSettings().getTimeOutAuth());
                }
                user_pending.setOnlineMode(loginPremium);
                user_pending.setUniqueId(uuid);
                userInLogin.setEnableLoginPremium(loginPremium);
                userInLogin.setPasswordHashed(password);
                userInLogin.setLogged(false);
                asyncAuthentication(userInLogin, user_pending, false);
                return;
            }
            //no existe
            user_pending.setOnlineMode(false);
            user_pending.setUniqueId(uuid);
            UserUUID userUuid = new UserUUID(uuid, offlineUuid, onlineUuid);
            UserInLogin userInLogin = new UserInLogin(username, userUuid, account.getStatus(), new ObjectId().toHexString());
            userInLogin.setTimeOnFinishOut(plugin.getObjectSettings().getTimeOutAuth());
            asyncAuthentication(userInLogin, user_pending, true);
        } catch (Throwable w) {
            dispatchCancelUser("Error mientras se cargaban tus datos contacta con un Administrador de MineCub");
            //event.completeIntent(plugin);
            w.printStackTrace();
        }
    }

    public void asyncAuthentication(UserInLogin userInLogin, PendingConnection user_pending, boolean isNewUser) {
        PlayerAuthenticationEvent pAuthentication = new PlayerAuthenticationEvent(userInLogin, user_pending, isNewUser
                , new Callback<PlayerAuthenticationEvent>() {
            @Override
            public void done(PlayerAuthenticationEvent doneEvent, Throwable throwable) {
                UserManager.addUserNotAuthenticated(user_pending.getName(), doneEvent.getUserInLogin());
                String kickReason = null;
                if (doneEvent.getKickReason() != null) {
                    kickReason = doneEvent.getKickReason();
                }
                if (kickReason != null) {
                    dispatchCancelUser(kickReason);
                    return;
                }
                event.completeIntent(plugin);
            }
        });
        plugin.getProxy().getPluginManager().callEvent(pAuthentication);
    }
}
