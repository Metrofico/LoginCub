package me.metrofico.logincub.objects;

import me.metrofico.logincub.Cryptography;
import me.metrofico.logincub.MojangAPI;
import me.metrofico.logincub.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.UUID;

public class UserInLogin {
    private final String userName;
    private final UserUUID uuid;
    private final String objectId;
    private String passwordHashed;
    private boolean isEnableLoginPremium;
    private long timeOnFinishOut;
    private boolean isLogged;
    private MojangAPI.Account status;
    private ServerInfo targetServer;
    private boolean isWait;
    private boolean isDownloadingPremium = false;
    private boolean sendedLobby = false;


    public UserInLogin(String userName, UserUUID uuid, MojangAPI.Account status, String objectId) {
        this.status = status;
        this.userName = userName;
        this.uuid = uuid;
        this.isWait = false;
        this.sendedLobby = false;
        this.objectId = objectId;
        this.passwordHashed = null;
        isLogged = false;
        isEnableLoginPremium = false;
    }

    public boolean isSendedLobby() {
        return sendedLobby;
    }

    public void setSendedLobby(boolean sendedLobby) {
        this.sendedLobby = sendedLobby;
    }

    public boolean isWait() {
        return isWait;
    }

    public void setWait(boolean wait) {
        isWait = wait;
    }

    public boolean isDownloadingPremium() {
        return isDownloadingPremium;
    }

    public void setDownloadingPremium(boolean downloadingPremium) {
        isDownloadingPremium = downloadingPremium;
    }

    public String getObjectIdHex() {
        return objectId;
    }

    public ObjectId getObjectId() {
        if (objectId != null) {
            return new ObjectId(objectId);
        }
        return null;
    }

    public boolean isEnableLoginPremium() {
        return isEnableLoginPremium;
    }

    public void setEnableLoginPremium(boolean enableLoginPremium) {
        isEnableLoginPremium = enableLoginPremium;
    }

    public boolean isLogged() {
        return isLogged;
    }

    public void setLogged(boolean logged) {
        isLogged = logged;
    }

    public void setTimeOnFinishOut(long timeFinishOnSeconds) {
        this.timeOnFinishOut = System.currentTimeMillis() + (1000 * timeFinishOnSeconds);
    }

    public boolean timeOutAuthentication() {
        if (timeOnFinishOut == 0) {
            return false;
        }
        Date dFinish = new Date(timeOnFinishOut);
        Date now = new Date();
        return now.after(dFinish);
    }

    public String getUserName() {
        return userName;
    }

    public String getPasswordHashed() {
        return passwordHashed;
    }

    public void setPasswordHashed(String passwordHashed) {
        this.passwordHashed = passwordHashed;
    }

    public void kick(String s) {
        ProxiedPlayer player = getPlayer();
        if (player != null) {
            player.disconnect(new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', s)).create());
        }
    }

    public UUID getUuid() {
        return uuid.getUuidInUse();
    }

    public ProxiedPlayer getPlayer() {
        return ProxyServer.getInstance().getPlayer(userName);
    }

    public boolean checkPassword(String password) {
        if (passwordHashed != null && passwordHashed.length() == 32) {
            String inputpassword = Utils.md5(password);
            return passwordHashed != null && passwordHashed.equals(inputpassword);
        }
        String inputpassword = Cryptography.sha512(password);
        return passwordHashed != null && passwordHashed.equals(inputpassword);
    }

    public MojangAPI.Account getStatus() {
        return status;
    }

    public void setStatus(MojangAPI.Account status) {
        this.status = status;
    }

    public void sendMessage(String[] text) {
        if (text == null) {
            return;
        }
        if (getPlayer() != null) {
            for (String lines : text) {
                BaseComponent[] texta = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lines));
                getPlayer().sendMessage(texta);
            }
        }
    }

    public void sendMessage(String[] text, String replace, String to) {
        if (text == null) {
            return;
        }
        if (getPlayer() != null) {
            for (String lines : text) {
                BaseComponent[] texta = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lines.replace(replace, to)));
                getPlayer().sendMessage(texta);
            }
        }
    }

    public void sendMessage(String text) {
        if (text == null) {
            return;
        }
        if (text.equalsIgnoreCase("none")) {
            return;
        }
        if (getPlayer() != null) {
            BaseComponent[] texta = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text));
            getPlayer().sendMessage(texta);
        }
    }

    public ServerInfo getTargetServer() {
        return targetServer;
    }

    public void setTargetServer(ServerInfo targetServer) {
        this.targetServer = targetServer;
    }

    public void sendMessage(String text, String replace, Object obj) {
        if (text == null) {
            return;
        }
        if (text.equalsIgnoreCase("none")) {
            return;
        }
        if (getPlayer() != null) {
            BaseComponent[] texta = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text.replace(replace, obj
                    .toString())));
            getPlayer().sendMessage(texta);
        }
    }

    public boolean passwordVerify(String password) {
        if (passwordHashed != null && passwordHashed.length() == 32) {
            String inputpassword = Utils.md5(password);
            isLogged = passwordHashed != null && passwordHashed.equals(inputpassword);
            return isLogged;
        }
        String inputpassword = Cryptography.sha512(password);
        isLogged = passwordHashed != null && passwordHashed.equals(inputpassword);
        return isLogged;
    }

    public boolean isPlayerInServer() {
        return getPlayer() != null && getPlayer().isConnected();
    }

    public void disconnect(String text) {
        if (getPlayer() != null) {
            BaseComponent[] texta = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text));
            getPlayer().disconnect(texta);
        }
    }

}
