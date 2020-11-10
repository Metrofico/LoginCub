package me.metrofico.logincub.objects;

import com.google.common.base.Charsets;
import me.metrofico.logincub.Cryptography;
import me.metrofico.logincub.MojangAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UserAuth {
    private static final ConcurrentHashMap<UUID, UserAuth> usersAuth = new ConcurrentHashMap<>();

    public static void addUser(UserAuth auth) {
        usersAuth.putIfAbsent(auth.getUuid(), auth);
    }

    public static void updateUser(UserAuth auth) {
        usersAuth.put(auth.getUuid(), auth);
    }

    public static UserAuth getUser(UUID id) {
        return usersAuth.getOrDefault(id, null);
    }

    public static void removeUser(UUID id) {
        usersAuth.remove(id);
    }

    public static Iterator<UserAuth> getUsers() {
        return usersAuth.values().iterator();
    }

    public static Iterator<UserAuth> getUsersNotLogged() {
        return usersAuth.values().stream().filter(user -> !user.isLogged() && user.isPlayerOnline() && !user.isLoginPremium()).collect(Collectors.toList()).iterator();
    }


    private final String userName;
    private final UUID uuid;
    private final MojangAPI.Account status;
    private String objectId;
    private boolean loginPremium;
    private ProxiedPlayer player;
    private String passwordHashed;
    private boolean isLogged, wait;
    private ServerInfo targetServer;
    private long timeOnFinishOut;
    private long timeOnFinishOutPremiumEnabled;
    private long timeOnFinishJoinPremium;

    public UserAuth(String userName, UUID uuid, MojangAPI.Account status, String objectId) {
        this.userName = userName;
        this.uuid = uuid;
        this.status = status;
        this.loginPremium = false;
        wait = false;
        passwordHashed = null;
        isLogged = false;
        targetServer = null;
        timeOnFinishOut = 0;
        timeOnFinishOutPremiumEnabled = 0;
        timeOnFinishJoinPremium = 0;
        this.objectId = objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setWait(boolean wait) {
        this.wait = wait;
    }

    public boolean isWait() {
        return wait;
    }

    public void setTimeOut(int seconds) {
        LocalDateTime dateTime = LocalDateTime.now().plus(Duration.of(seconds, ChronoUnit.SECONDS));
        Date tmfn = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        timeOnFinishOut = tmfn.getTime();
    }

    public void setTimeoutPremiumEnable(int seconds) {
        if (seconds == 0) {
            timeOnFinishOutPremiumEnabled = 0;
            return;
        }
        LocalDateTime dateTime = LocalDateTime.now().plus(Duration.of(seconds, ChronoUnit.SECONDS));
        Date tmfn = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        timeOnFinishOutPremiumEnabled = tmfn.getTime();
    }

    public void setTimeoutJoinPremium(int minutes) {
        if (minutes == 0) {
            timeOnFinishJoinPremium = 0;
            return;
        }
        LocalDateTime dateTime = LocalDateTime.now().plus(Duration.of(minutes, ChronoUnit.MINUTES));
        Date tmfn = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        timeOnFinishJoinPremium = tmfn.getTime();
    }

    public boolean isTimeOnFinishJoinPremium() {
        if (timeOnFinishJoinPremium == 0) {
            return true;
        }
        Date dFinish = new Date(timeOnFinishJoinPremium);
        Date now = new Date();
        return now.after(dFinish);
    }

    public boolean timeOutPremiumActive() {
        if (timeOnFinishOutPremiumEnabled == 0) {
            return true;
        }
        Date dFinish = new Date(timeOnFinishOutPremiumEnabled);
        Date now = new Date();
        return now.after(dFinish);
    }

    public ProxiedPlayer getPlayer() {
        return ProxyServer.getInstance().getPlayer(uuid);
    }

    public boolean isPlayerOnline() {
        return getPlayer() != null && getPlayer().isConnected();
    }

    public void setPasswordHashed(String passwordHashed) {
        this.passwordHashed = passwordHashed;
    }

    public boolean timeOut() {
        if (timeOnFinishOut == 0) {
            return false;
        }
        Date dFinish = new Date(timeOnFinishOut);
        Date now = new Date();
        return now.after(dFinish);
    }

    public String getPasswordHashed() {
        return passwordHashed;
    }

    public void setTargetServer(ServerInfo targetServer) {
        this.targetServer = targetServer;
    }

    public ServerInfo getTargetServer() {
        return targetServer;
    }

    private static String md5(String password) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(password.getBytes(Charsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean passwordVerify(String password) {
        if (passwordHashed != null && passwordHashed.length() == 32) {
            String inputpassword = md5(password);
            isLogged = passwordHashed != null && passwordHashed.equals(inputpassword);
            return isLogged;
        }
        String inputpassword = Cryptography.sha512(password);
        isLogged = passwordHashed != null && passwordHashed.equals(inputpassword);
        return isLogged;
    }

    public boolean checkPassword(String password) {
        String inputpassword = Cryptography.sha512(password);
        return passwordHashed != null && passwordHashed.equals(inputpassword);
    }

    public void setLogged(boolean logged) {
        isLogged = logged;
    }

    public boolean isLogged() {
        return isLogged;
    }

    public MojangAPI.Account getStatus() {
        return status;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isLoginPremium() {
        return loginPremium;
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

    public void disconnect(String text) {
        if (getPlayer() != null) {
            BaseComponent[] texta = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text));
            getPlayer().disconnect(texta);
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setLoginPremium(boolean loginPremium) {
        if (loginPremium) {
            setLogged(true);
        }
        this.loginPremium = loginPremium;
    }

    public void redirectToTarget() {
        //getPlayer().connect(getTargetServer());
    }
}
