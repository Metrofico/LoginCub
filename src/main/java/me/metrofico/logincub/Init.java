package me.metrofico.logincub;

import me.metrofico.logincub.commands.*;
import me.metrofico.logincub.configs.CustomConfig;
import me.metrofico.logincub.controllers.AuthCheck;
import me.metrofico.logincub.dbManager.DatabaseConfiguration;
import me.metrofico.logincub.dbManager.MongoDB;
import me.metrofico.logincub.dbManager.PlayerDatabase;
import me.metrofico.logincub.objects.Language;
import me.metrofico.logincub.objects.Settings;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Init extends Plugin {

    private MojangAPI mapi;
    private PlayerDatabase playerDatabase;
    private Events events;
    private List<String> commandsAllowed;
    private CustomConfig settings, lang;
    private Settings ObjectSettings;
    private Language language;
    private boolean development;
    ScheduledExecutorService scheduledExecutorService;

    public static void main(String[] args) {

    }

    @Override
    public void onEnable() {
        scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() - 1);
        development = false;
        commandsAllowed = new ArrayList<>();
        settings = new CustomConfig(this, "settings");
        lang = new CustomConfig(this, "language");
        defaultSettingsValues();
        loadLanguage();
        ConsoleUtil.prefixConsole("Inicializando Autenticador de sesiones " + "v1.1BETA");
        registerCommands();
        MongoDB mongoDb = loadMongoDatabase();
        if (mongoDb == null) {
            return;
        }
        playerDatabase = new PlayerDatabase(mongoDb);
        mapi = new MojangAPI(ObjectSettings.getProxies());
        //mapi.startRequest(3000);
        events = new Events(this);
        ConsoleUtil.prefixConsole("Autenticador de sesiones inicializado");
        AuthCheck check = new AuthCheck(this);
        scheduledExecutorService.scheduleAtFixedRate(check, 0, 4, TimeUnit.SECONDS);
    }

    public MongoDB loadMongoDatabase() {
        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();
        loadDatabaseConfiguration(databaseConfiguration);
        try {
            MongoDB database;
            if (!databaseConfiguration.getPassword().isEmpty()) {
                database = new MongoDB(databaseConfiguration.getDatabase(), databaseConfiguration.getPrefixCollection(), databaseConfiguration.getHost(),
                        databaseConfiguration.getPort(), databaseConfiguration.getUsername(),
                        databaseConfiguration.getPassword());
            } else {
                database = new MongoDB(databaseConfiguration.getDatabase(), databaseConfiguration.getPrefixCollection(), databaseConfiguration.getHost(),
                        databaseConfiguration.getPort());
            }
            return database;
        } catch (Exception e) {
            ConsoleUtil.Console("&4No se ha podido conectar a la base de datos asegurate de configurar bien los datos de accesos: ["
                    + e.getMessage() + "]");
            BungeeCord.getInstance().stop();
        }
        return null;
    }

    public void loadDatabaseConfiguration(DatabaseConfiguration databaseConfiguration) {
        Configuration file = settings.getConfig();
        String host = file.getString("mongodb.host", "localhost");
        String database = file.getString("mongodb.databaseName", "test");
        String prefix = file.getString("mongodb.prefixCollections", "db_prefix_");
        String username = file.getString("mongodb.username", "");
        String password = file.getString("mongodb.password", "");
        int port = file.getInt("mongodb.port", 27017);
        databaseConfiguration.setHost(host.trim());
        databaseConfiguration.setDatabase(database.trim());
        databaseConfiguration.setUsername(username.trim());
        databaseConfiguration.setPrefixCollection(prefix.trim());
        databaseConfiguration.setPassword(password.trim());
        databaseConfiguration.setPort(port);
    }

    public boolean isDevelopment() {
        return development;
    }

    public void loadLanguage() {
        if (lang != null) {
            language = new Language();
            String rateLimit = lang.getConfig().getString("MojangAPI.kickPlayer.rateLimit", "&cRate Limit from Mojang, re-try");
            String errorUnexpected = lang.getConfig().getString("MojangAPI.kickPlayer.Unexpected", "&cError Unexpected, re-try login");
            String premiumVerified = lang.getConfig().getString("OnLogin.PremiumVerified", "&cTHE PREMIUM SESSION OF YOUR ACCOUNT HAS BEEN VERIFIED");
            String LoginSuccessful = lang.getConfig().getString("OnLogin.LoginSuccessful", "&6Login Successful");
            //  SignupSuccessful: '&aSign up successfull'
            String signupsuccesful = lang.getConfig().getString("OnLogin.SignupSuccessful", "&aSign up successfull");
            String passwordFailed = lang.getConfig().getString("OnLogin.passwordFail", "Login failed, password incorrect");
            String LoginSuccessfulPassword = lang.getConfig().getString("OnLogin.LoginPasswordSuccessful", "&aLogin Successful");
            String loginWelcome = lang.getConfig().getString("OnLogin.loginWelcome", "&cPlease authenticate to continue");
            String registerWelcome = lang.getConfig().getString("OnLogin.registerWelcome", "&cPlease sign up to continue");
            String timeOut = lang.getConfig().getString("OnLogin.timeOut", "&eAuthentication timeout expired");
            String loginController = lang.getConfig().getString("Commands.login", "&aSign in with /login [password]");
            String registerController = lang.getConfig().getString("Commands.register", "&cSign up with /register [password] [repeat-password]");
            String loginusage = lang.getConfig().getString("Commands.loginUsage", "&cArguments: &f/login [password]");
            String registerusage = lang.getConfig().getString("Commands.registerUsage", "&cArguments: &e/register [password] [repeat-password]");
            String passwordNoMatch = lang.getConfig().getString("Commands.passwordNoMatch", "&cLas contraseñas no conciden, intenta nuevamente");
            String noPremiumUser = lang.getConfig().getString("Commands.noPremiumUser", "&cIn order to use this command you need a premium account from &eminecraft.net");
            String[] alertEnablePremium = lang.getConfig().getString("Premium.alertEnable", "").split("\\n");
            String[] alertDisablePremium = lang.getConfig().getString("Premium.alertDisable", "").split("\\n");
            String[] yesEnablePremium = lang.getConfig().getString("Premium.yesEnablePremium", "").split("\\n");
            String[] yesDisablePremium = lang.getConfig().getString("Premium.yesDisablePremium", "").split("\\n");
            String adminUsage = lang.getConfig().getString("Commands.adminUsage", "&cArguments: &e/authadmin &areload&f|&achangepass");
            String adminUsageChangepass = lang.getConfig().getString("Commands.adminUsageChangepass", "&cArguments: /authadmin changepass [usuario] [clave]");
            String changepasswordUsage = lang.getConfig().getString("Commands.playerChangepasswordUsage", "&cArguments: /changepassword [old-pass] [new-pass]");
            String adminpasswordUpdated = lang.getConfig().getString("Commands.adminpasswordUpdated", "&aPassword updated for user: {player}");
            String adminUserNoExist = lang.getConfig().getString("Commands.adminUserNoExist", "&cThe user {player} doesnt exist in database");
            String changepasswordSamePassword = lang.getConfig().getString("Commands.changepasswordSamePassword", "&cYour passwors is equals to old, please type a new password");
            String ChangepasswordSuccessful = lang.getConfig().getString("Commands.ChangepasswordSuccessful", "&eYour password is successful updated");
            String premiumUsage = lang.getConfig().getString("Commands.premiumUsage", "&cArguments: &e/premium login (enable/disable)");
            String ChangepasswordPasswordError = lang.getConfig().getString("Commands.ChangepasswordPasswordError", "&cYour password is incorrect, please retry");
            String passwordMinChar = lang.getConfig().getString("Password.minChar", "&cThe password must be greater than or equal to 5 characters");
            language.setPasswordMin(passwordMinChar);
            language.setChangepasswordSuccessful(ChangepasswordSuccessful);
            language.setChangepasswordPasswordError(ChangepasswordPasswordError);
            language.setSamePassword(changepasswordSamePassword);
            language.setAdmingUsageChangepass(adminUsageChangepass);
            language.setAdminpasswordUpdated(adminpasswordUpdated);
            language.setPremiumUsage(premiumUsage);
            language.setAdminuserNotExist(adminUserNoExist);
            language.setRateLimit(rateLimit);
            language.setAdminUsage(adminUsage);
            language.setErrorLogin(errorUnexpected);
            language.setPlayerChangepasswordUsage(changepasswordUsage);
            language.setPremiumVerified(premiumVerified);
            language.setLogingSuccessful(LoginSuccessful);
            language.setLoginWelcome(loginWelcome);
            language.setRegisterWelcome(registerWelcome);
            language.setTimeOut(timeOut);
            language.setLoginController(loginController);
            language.setLoginSuccessfulPassword(LoginSuccessfulPassword);
            language.setYesEnablePremium(yesEnablePremium);
            language.setYesDisablePremium(yesDisablePremium);
            language.setRegisterController(registerController);
            language.setPasswordFailed(passwordFailed);
            language.setRegisterUsage(registerusage);
            language.setLoginUsage(loginusage);
            language.setSignupSuccessful(signupsuccesful);
            language.setPasswordNoMatch(passwordNoMatch);
            language.setNopremiumPlayer(noPremiumUser);
            language.setAlertEnablePremium(alertEnablePremium);
            language.setAlertDisablePremium(alertDisablePremium);
        }
    }

    public Language getLanguage() {
        return language;
    }

    private boolean isANumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (Exception ignored) {

        }
        return false;
    }

    public CustomConfig getLang() {
        return lang;
    }

    private void defaultSettingsValues() {
        if (settings != null) {
            String[] proxiesSettings = settings.getConfig().getString("MojangAPI.proxies", "").split("\\n");
            String[] commandsAllowedSettings = settings.getConfig().getString("CommandsAllowedNoAuthenticate", "").split("\\n");
            this.commandsAllowed = Arrays.asList(commandsAllowedSettings);
            List<String> proxies = Arrays.asList(proxiesSettings);
            int timeOut = settings.getConfig().getInt("Authentication.timeOut", 20);
            int timeOutConfirmPremium = settings.getConfig().getInt("UserPremium.timeOutConfirmInSeconds", 20);
            int timeOutJoinPremium = settings.getConfig().getInt("UserPremium.timeOutJoinPremiumInMinutes", 5);
            String serverName = settings.getConfig().getString("Authentication.serverNameAuth", "prelobby");
            int passwordLenght = settings.getConfig().getInt("PasswordSettings.minChar", 5);
            List<MojangAPI.Proxify> proxifies = new ArrayList<>();
            proxies.forEach(line -> {
                String[] split = line.split(":");
                if (split.length == 3) {
                    String host = split[0];
                    String port = split[1];
                    if (isANumber(port)) {
                        int port_ = Integer.parseInt(port);
                        String type = split[2];
                        proxifies.add(new MojangAPI.Proxify(host, port_, type));
                    }
                }
            });
            ObjectSettings = new Settings(proxifies, timeOut, serverName, timeOutConfirmPremium, timeOutJoinPremium);
            ObjectSettings.setLenghtPassword(passwordLenght);
        }
    }


    public Settings getObjectSettings() {
        return ObjectSettings;
    }

    public void reloadObjectSettings() {
        settings.reload();
        defaultSettingsValues();
        // mapi.setListProxy(ObjectSettings.getProxies());

    }

    public List<String> getCommandsAllowed() {
        return commandsAllowed;
    }

    public boolean matchCommandAllowed(String command) {
        return commandsAllowed.stream().filter(sc -> sc.trim().toLowerCase().equals(command.toLowerCase())).count() >= 1;
    }

    private void registerCommands() {
        getProxy().getPluginManager().registerCommand(this, new RegisterCommand(this, "register"));
        getProxy().getPluginManager().registerCommand(this, new LoginCommand(this, "login"));
        getProxy().getPluginManager().registerCommand(this, new AdminCommand(this, "authadmin"));
        getProxy().getPluginManager().registerCommand(this, new PremiumCommand(this, "premium"));
        getProxy().getPluginManager().registerCommand(this, new ChangePasswordCommand(this, "changepassword"));

    }

    @Override
    public void onDisable() {

    }

    public Events getEvents() {
        return events;
    }

    public PlayerDatabase getPlayerDatabase() {
        return playerDatabase;
    }

    public MojangAPI getMojangAPI() {
        return mapi;
    }
}
