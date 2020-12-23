package me.metrofico.logincub.objects;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private static final HashMap<String, UserPremiumValidate> usersValidate = new HashMap<>();
    private static final HashMap<String, UserInLogin> usersAuthenticated = new HashMap<>(); // usuarios que no han iniciado sesion
    private static final ConcurrentHashMap<String, UserInLogin> usersNotAuthenticated = new ConcurrentHashMap<>();


    public static UserInLogin getUserAuthenticated(String playerName) {
        return usersAuthenticated.getOrDefault(playerName.toLowerCase(), null);
    }

    public static UserPremiumValidate getPremiumValidate(String playerName) {
        return usersValidate.getOrDefault(playerName.toLowerCase(), null);
    }

    public static void removePremiumValidate(String playerName) {
        usersValidate.remove(playerName.toLowerCase());
    }

    public static void setPremiumValidate(String playerName, UserPremiumValidate validate) {
        usersValidate.put(playerName.toLowerCase(), validate);
    }


    public static void setUserAuthenticated(String username, UserInLogin userInLogin) {
        usersAuthenticated.put(username.toLowerCase(), userInLogin);
    }


    public static void removeUserAuthenticated(String playerName) {
        usersAuthenticated.remove(playerName.toLowerCase());
    }

    public static void removeUserNotAuthenticated(String playerName) {
        usersNotAuthenticated.remove(playerName.toLowerCase());
    }

    public static void addUserNotAuthenticated(String playerName, UserInLogin userInLogin) {
        usersNotAuthenticated.put(playerName.toLowerCase(), userInLogin);
    }

    public static UserInLogin getUserNotAuthenticated(String player) {
        return usersNotAuthenticated.getOrDefault(player.toLowerCase(), null);
    }


    public static Collection<UserInLogin> getUsersNotLogged() {
        return usersNotAuthenticated.values();
    }
}
