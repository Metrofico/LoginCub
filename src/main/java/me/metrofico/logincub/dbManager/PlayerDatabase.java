package me.metrofico.logincub.dbManager;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import me.metrofico.logincub.Cryptography;
import me.metrofico.logincub.callbacks.ReturnCallback;
import org.bson.Document;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PlayerDatabase {
    private MongoDB mongoDB;
    private MongoCollection<Document> users_collections;
    private MongoDatabase database;
    private ScheduledExecutorService service;

    public PlayerDatabase(MongoDB mongoDB) {
        this.mongoDB = mongoDB;

        service = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        database = mongoDB.getDatabase();
        if (database != null) {
            users_collections = database.getCollection(mongoDB.getPrefixCollections() + "accounts");
        }
    }


    public boolean getPlayer(String uuid) {
        Document query = new Document(
                "$or", Arrays.asList(new Document("offlineUid", uuid), new Document("onlineUid", uuid)));
        return users_collections.find(query).first() != null;
    }

    public Document getPlayer(UUID uuid) {
        String suuid = uuid.toString();
        Document query = new Document(
                "$or", Arrays.asList(new Document("offlineUid", suuid), new Document("onlineUid", suuid)));
        return users_collections.find(query).first();
    }

    public Document getPlayerByUsername(String username) {
        Document query = new Document("playerName", username);
        return users_collections.find(query).first();
    }

    public Document getPlayerByUsernameOrUuidOnline(String username, UUID uuid) {
        Document query = new Document(
                "$or", Arrays.asList(new Document("playerName", username), new Document("onlineUid", uuid)));
        return users_collections.find(query).first();
    }

    public Document getPlayerByUsernameOrUuidOffline(String username, UUID uuid) {
        Document query = new Document(
                "$or", Arrays.asList(new Document("playerName", username), new Document("offlineUid", uuid)));
        return users_collections.find(query).first();
    }

    public boolean updateOnlineUuid(String playerName, String uuid) {
        UpdateResult result = users_collections.updateOne(new Document("playerName", playerName),
                new Document("$set", new Document("onlineUid", uuid)));
        return result.getMatchedCount() != 0;
    }

    public boolean updatePlayerNameFromUUID(UUID uuid, String newUsername) {
        String suuid = uuid.toString();
        Document query = new Document(
                "$or", Arrays.asList(new Document("offlineUid", suuid), new Document("onlineUid", suuid)));
        UpdateResult result = users_collections.updateOne(query,
                new Document("$set", new Document("playerName", newUsername)));
        return result.getMatchedCount() != 0;
    }

    public boolean updatePremiumMode(String uuid, boolean active) {
        UpdateResult result = users_collections.updateOne(new Document("onlineUid", uuid),
                new Document("$set", new Document("loginPremium", active)));
        return result.getMatchedCount() != 0;
    }

    public boolean updatePremiumMode(UUID uuid, boolean active) {
        String suuid = uuid.toString();
        UpdateResult result = users_collections.updateOne(new Document("onlineUid", suuid),
                new Document("$set", new Document("loginPremium", active)));
        return result.getMatchedCount() != 0;
    }

    public boolean updatePasswordOfflinePlayerByUsername(String username, String password) {
        UpdateOptions options = new UpdateOptions().upsert(true);
        UpdateResult result = users_collections.updateOne(new Document("playerName", username),
                new Document("$set", new Document("password", Cryptography.sha512(password))), options);
        return result.getMatchedCount() != 0;
    }

    public boolean updatePasswordByUUID(UUID uuid, String password) {
        String suuid = uuid.toString();
        Document query = new Document(
                "$or", Arrays.asList(new Document("offlineUid", suuid), new Document("onlineUid", suuid)));
        UpdateResult result = users_collections.updateOne(query,
                new Document("$set", new Document("password", Cryptography.sha512(password))));
        return result.getMatchedCount() != 0;
    }

    public void saveOfflinePlayer(String playerName, UUID uuid, String password, ReturnCallback<String> result) {
        service.submit(() -> {
            try {
                Document document = getPlayerByUsernameOrUuidOffline(playerName, uuid);
                if (document != null) {
                    return;
                }
                Document obj = new Document();
                obj.put("playerName", playerName);
                obj.put("created", new Date().getTime());
                obj.put("password", Cryptography.sha512(password));
                obj.put("offlineUid", uuid.toString());
                users_collections.insertOne(obj);
                String id = obj.getObjectId("_id").toString();
                result.onSuccess(id);
            } catch (Throwable throwable) {
                result.onError(throwable);
            }
        });
    }

    public void saveOnlinePlayer(String playerName, UUID uuid, String password, ReturnCallback<String> result) {
        service.submit(() -> {
            try {
                Document document = getPlayerByUsernameOrUuidOnline(playerName, uuid);
                if (document != null) {
                    return;
                }
                Document obj = new Document();
                obj.put("playerName", playerName);
                obj.put("created", new Date().getTime());
                obj.put("password", Cryptography.sha512(password));
                obj.put("onlineUid", uuid.toString());
                users_collections.insertOne(obj);
                String id = obj.getObjectId("_id").toString();
                result.onSuccess(id);
            } catch (Throwable throwable) {
                result.onError(throwable);
            }
        });
    }

}
