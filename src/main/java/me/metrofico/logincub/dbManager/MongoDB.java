package me.metrofico.logincub.dbManager;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import me.metrofico.logincub.ConsoleUtil;
import org.bson.Document;

public class MongoDB {
    private final MongoClient client;
    private final MongoDatabase database;
    private final String prefix_collection;

    public MongoDB(String databaseName, String prefix_collection, String host, int port, String username, String password) throws Exception {
        this.prefix_collection = prefix_collection;
        ConsoleUtil.Console("Inicializando Mongodb");
        MongoCredential mongoCredential = MongoCredential.createCredential(username, databaseName,
                password.toCharArray());
        client = new MongoClient(new ServerAddress(host, port), mongoCredential,
                new MongoClientOptions.Builder().connectTimeout(360000)
                        .serverSelectionTimeout(500)
                        .connectionsPerHost(30)
                        .socketTimeout(360000)
                        .maxWaitTime(120000).writeConcern(WriteConcern.JOURNALED)
                        .heartbeatSocketTimeout(1000)
                        .retryReads(true)
                        .retryWrites(true)
                        .cursorFinalizerEnabled(false).build());
        database = client.getDatabase(databaseName);
        checkConnection();

    }

    public MongoDB(String databaseName, String prefix_collection, String host, int port) throws Exception {
        this.prefix_collection = prefix_collection;
        ConsoleUtil.Console("Inicializando Mongodb");
        client = new MongoClient(new ServerAddress(host, port),
                new MongoClientOptions.Builder().connectTimeout(360000)
                        .serverSelectionTimeout(500)
                        .connectionsPerHost(30)
                        .socketTimeout(360000)
                        .maxWaitTime(120000).writeConcern(WriteConcern.JOURNALED)
                        .heartbeatSocketTimeout(1000)
                        .retryReads(true)
                        .retryWrites(true)
                        .cursorFinalizerEnabled(false).build());
        database = client.getDatabase(databaseName);
        checkConnection();


    }

    public void checkConnection() throws Exception {
        database.runCommand(new Document("serverStatus", 1));
        ConsoleUtil.Console("Conexion establecida");
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public String getPrefixCollections() {
        return this.prefix_collection;
    }
}
