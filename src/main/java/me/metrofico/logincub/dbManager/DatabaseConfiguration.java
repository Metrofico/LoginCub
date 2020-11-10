package me.metrofico.logincub.dbManager;

public class DatabaseConfiguration {
    String host;
    String username;
    String database;
    String password;
    String prefixCollections;
    int port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getPrefixCollection() {
        return prefixCollections;
    }

    public void setPrefixCollection(String collection) {
        this.prefixCollections = collection;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
