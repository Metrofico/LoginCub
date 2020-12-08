package me.metrofico.logincub.objects;

public class Language {
    private String rateLimit;
    private String errorLogin;
    private String logingSuccessful, loginSuccessfulPassword;
    private String premiumVerified;
    private String loginWelcome;
    private String registerWelcome;
    private String timeOut;
    private String loginController;
    private String registerController;
    private String loginUsage, registerUsage;
    private String passwordFailed, passwordNoMatch, signupSuccessful;
    private String premiumUsage;
    private String nopremiumPlayer;
    private String[] alertEnablePremium;
    private String[] alertDisablePremium;
    private String[] yesEnablePremium, yesDisablePremium;
    private String adminUsage, admingUsageChangepass;
    private String samePassword, playerChangepasswordUsage;
    private String adminpasswordUpdated, adminuserNotExist;
    private String ChangepasswordSuccessful, ChangepasswordPasswordError;
    private String passwordMin;

    public Language() {
        rateLimit = "§cRate Limit from Mojang, re-try";
        errorLogin = "§cError inesperado, intenta reloguear";
    }

    public void setChangepasswordPasswordError(String changepasswordPasswordError) {
        ChangepasswordPasswordError = changepasswordPasswordError;
    }

    public String getChangepasswordPasswordError() {
        return ChangepasswordPasswordError;
    }

    public void setPasswordMin(String passwordMin) {
        this.passwordMin = passwordMin;
    }

    public void setAdminuserNotExist(String adminuserNotExist) {
        this.adminuserNotExist = adminuserNotExist;
    }

    public void setChangepasswordSuccessful(String changepasswordSuccessful) {
        ChangepasswordSuccessful = changepasswordSuccessful;
    }

    public String getChangepasswordSuccessful() {
        return ChangepasswordSuccessful;
    }

    public String getAdminuserNotExist() {
        return adminuserNotExist;
    }

    public void setAdminpasswordUpdated(String adminpasswordUpdated) {
        this.adminpasswordUpdated = adminpasswordUpdated;
    }

    public String getAdminpasswordUpdated() {
        return adminpasswordUpdated;
    }

    public void setPlayerChangepasswordUsage(String playerChangepasswordUsage) {
        this.playerChangepasswordUsage = playerChangepasswordUsage;
    }

    public String getPlayerChangepasswordUsage() {
        return playerChangepasswordUsage;
    }

    public void setSamePassword(String samePassword) {
        this.samePassword = samePassword;
    }

    public String getSamePassword() {
        return samePassword;
    }

    public void setAdmingUsageChangepass(String admingUsageChangepass) {
        this.admingUsageChangepass = admingUsageChangepass;
    }

    public String getAdmingUsageChangepass() {
        return admingUsageChangepass;
    }

    public void setAdminUsage(String adminUsage) {
        this.adminUsage = adminUsage;
    }

    public String getAdminUsage() {
        return adminUsage;
    }

    public void setYesDisablePremium(String[] yesDisablePremium) {
        this.yesDisablePremium = yesDisablePremium;
    }

    public void setYesEnablePremium(String[] yesEnablePremium) {
        this.yesEnablePremium = yesEnablePremium;
    }

    public String[] getYesDisablePremium() {
        return yesDisablePremium;
    }

    public String[] getYesEnablePremium() {
        return yesEnablePremium;
    }

    public void setAlertDisablePremium(String[] alertDisablePremium) {
        this.alertDisablePremium = alertDisablePremium;
    }

    public String[] getAlertDisablePremium() {
        return alertDisablePremium;
    }

    public void setAlertEnablePremium(String[] alertEnablePremium) {
        this.alertEnablePremium = alertEnablePremium;
    }

    public String[] getAlertEnablePremium() {
        return alertEnablePremium;
    }

    public void setNopremiumPlayer(String nopremiumPlayer) {
        this.nopremiumPlayer = nopremiumPlayer;
    }

    public String getNopremiumPlayer() {
        return nopremiumPlayer;
    }

    public void setPremiumUsage(String premiumUsage) {
        this.premiumUsage = premiumUsage;
    }

    public String getPremiumUsage() {
        return premiumUsage;
    }

    public void setPasswordNoMatch(String passwordNoMatch) {
        this.passwordNoMatch = passwordNoMatch;
    }

    public void setSignupSuccessful(String signupSuccessful) {
        this.signupSuccessful = signupSuccessful;
    }

    public String getSignupSuccessful() {
        return signupSuccessful;
    }

    public String getPasswordNoMatch() {
        return passwordNoMatch;
    }

    public void setPasswordFailed(String passwordFailed) {
        this.passwordFailed = passwordFailed;
    }

    public String getPasswordFailed() {
        return passwordFailed;
    }

    public void setLoginSuccessfulPassword(String loginSuccessfulPassword) {
        this.loginSuccessfulPassword = loginSuccessfulPassword;
    }

    public String getLoginSuccessfulPassword() {
        return loginSuccessfulPassword;
    }

    public void setLoginWelcome(String loginWelcome) {
        this.loginWelcome = loginWelcome;
    }

    public void setLoginUsage(String loginUsage) {
        this.loginUsage = loginUsage;
    }

    public void setRegisterUsage(String registerUsage) {
        this.registerUsage = registerUsage;
    }

    public String getLoginUsage() {
        return loginUsage;
    }

    public String getRegisterUsage() {
        return registerUsage;
    }

    public void setRegisterWelcome(String registerWelcome) {
        this.registerWelcome = registerWelcome;
    }

    public void setLoginController(String loginController) {
        this.loginController = loginController;
    }

    public void setRegisterController(String registerController) {
        this.registerController = registerController;
    }

    public String getLoginController() {
        return loginController;
    }

    public String getRegisterController() {
        return registerController;
    }

    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }

    public String getTimeOut() {
        return timeOut;
    }

    public String getLoginWelcome() {
        return loginWelcome;
    }

    public String getRegisterWelcome() {
        return registerWelcome;
    }

    public void setRateLimit(String rateLimit) {
        this.rateLimit = rateLimit;
    }

    public void setPremiumVerified(String premiumVerified) {
        this.premiumVerified = premiumVerified;
    }

    public void setLogingSuccessful(String logingSuccessful) {
        this.logingSuccessful = logingSuccessful;
    }

    public String getLogingSuccessful() {
        return logingSuccessful;
    }

    public String getPremiumVerified() {
        return premiumVerified;
    }

    public String getRateLimit() {
        return rateLimit;
    }

    public void setErrorLogin(String errorLogin) {
        this.errorLogin = errorLogin;
    }

    public String getErrorLogin() {
        return errorLogin;
    }

    public String getPasswordMin() {
        return passwordMin;
    }
}
