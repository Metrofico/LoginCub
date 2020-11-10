package me.metrofico.logincub.configs;


import com.google.common.base.Charsets;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import me.metrofico.logincub.ConsoleUtil;
import me.metrofico.logincub.Init;

import java.io.*;
import java.util.Objects;

public class CustomConfig {
    private Configuration config;
    private File file;
    private Init main;
    private ConfigurationProvider provider;

    public CustomConfig(Init main, String resourceName) {
        provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        main.getDataFolder().mkdirs();
        this.file = new File(main.getDataFolder(), resourceName + ".yml");
        if (!file.exists()) {
            if (main.getClass().getClassLoader().getResourceAsStream(resourceName + ".yml") == null) {
                try {
                    if (file.createNewFile()) {
                        ConsoleUtil.prefixConsole("&c  - Creating file " + resourceName + "");
                    } else {
                        ConsoleUtil.prefixConsole("&c  - Error to Create file " + resourceName + "");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    ConsoleUtil.prefixConsole("&c  - Error to Create file " + resourceName + "");
                }
                try {
                    config = provider.load(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    ConsoleUtil.prefixConsole("&c  - Error to Read file " + resourceName + "");
                }
                ConsoleUtil.prefixConsole("&a  - Loading file " + resourceName);
            } else {
                // file.mkdirs();

                InputStreamReader g = new InputStreamReader(
                        Objects.requireNonNull(main.getClass().getClassLoader().getResourceAsStream(resourceName + ".yml")), Charsets.UTF_8);
                config = provider.load(g);
                try {
                    provider.save(config, file);
                    ConsoleUtil.prefixConsole("&c  - Loading file " + resourceName + "");
                } catch (IOException e) {
                    e.printStackTrace();
                    ConsoleUtil.prefixConsole("&c  - Error to Loading file " + resourceName + "");
                }
            }

        } else {
            try {
                config = provider.load(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
                ConsoleUtil.prefixConsole("&a  - Loading file " + resourceName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ConsoleUtil.prefixConsole("&c  - Error to Read file " + resourceName + "");
            }

        }
    }

    public void save() {

        try {
            provider.save(config, file);
        } catch (Exception exception) {
            // empty catch block
        }


    }

    public void sDefault(String path, Object value) {
        if (this.config.get(path) == null) {
            this.config.set(path, value);
            //this.save();
        }
    }

    public void reload() {
        try {
            config = provider.load(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
            ConsoleUtil.prefixConsole("&a  - Reloading file " + file.getName().replace(".yml", ""));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ConsoleUtil.prefixConsole("&c  - Error to Read file " + file.getName().replace(".yml", "") + "");
        }
    }

    public void set(String path, Object value) {

        this.config.set(path, value);
        //this.save();

    }

    public Configuration getConfig() {
        return this.config;
    }

    public File getFile() {
        return this.file;
    }

    public boolean isSet(String path) {
        return getConfig() != null && getConfig().get(path) != null;
    }
}

