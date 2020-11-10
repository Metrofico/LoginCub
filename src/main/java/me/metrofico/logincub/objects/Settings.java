package me.metrofico.logincub.objects;

import me.metrofico.logincub.MojangAPI;

import java.util.List;

public class Settings {
    private List<MojangAPI.Proxify> proxies;
    private int timeOutAuth, timeOutConfirmPremium, timeOutJoinPremium;
    private String serverNameAuth;
    private int lenghtPassword;

    public Settings(List<MojangAPI.Proxify> proxies, int timeOutAuth, String serverNameAuth, int timeOutConfirmPremium, int timeOutJoinPremium) {
        this.proxies = proxies;
        this.timeOutAuth = timeOutAuth;
        this.serverNameAuth = serverNameAuth;
        this.timeOutConfirmPremium = timeOutConfirmPremium;
        this.timeOutJoinPremium = timeOutJoinPremium;
    }

    public void setLenghtPassword(int lenghtPassword) {
        this.lenghtPassword = lenghtPassword;
    }

    public int getLenghtPassword() {
        return lenghtPassword;
    }

    public int getTimeOutConfirmPremium() {
        return timeOutConfirmPremium;
    }

    public int getTimeOutJoinPremium() {
        return timeOutJoinPremium;
    }

    public List<MojangAPI.Proxify> getProxies() {
        return proxies;
    }

    public int getTimeOutAuth() {
        return timeOutAuth;
    }

    public String getServerNameAuth() {
        return serverNameAuth;
    }
}
