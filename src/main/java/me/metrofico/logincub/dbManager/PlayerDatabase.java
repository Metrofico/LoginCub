package me.metrofico.logincub.dbManager;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationStrength;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import me.metrofico.logincub.Cryptography;
import me.metrofico.logincub.callbacks.SingleCallback;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PlayerDatabase {
    private final MongoDB mongoDB;
    private MongoCollection<Document> users_collections;
    private final MongoDatabase database;
    private final ScheduledExecutorService service;
    private final Collation collationInsensitiveCase;

    public PlayerDatabase(MongoDB mongoDB) {
        this.mongoDB = mongoDB;
        collationInsensitiveCase = Collation.builder().locale("en").
                caseLevel(false)
                .collationStrength(CollationStrength.SECONDARY).build();
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
        Bson query = Filters.eq("playerName", username);
        return users_collections.find(query).collation(collationInsensitiveCase).first();
    }

    public Document getPlayerByUsernameOrUuidOnline(String username, UUID uuid) {
        Document query = new Document(
                "$or", Arrays.asList(
                new Document("onlineUid", uuid), new Document().append("playerName", username)));
        return users_collections.find(query).collation(collationInsensitiveCase).first();
    }

    public Document getPlayerByUsernameOrUuidOffline(String username, UUID uuid) {
        Document query = new Document(
                "$or", Arrays.asList(new Document().append("playerName", username),
                new Document("offlineUid", uuid)));
        return users_collections.find(query).collation(collationInsensitiveCase).first();
    }


    public boolean updateOnlineUuid(String playerName, String uuid, boolean updateInGame) {
        Bson query = new Document().append("playerName", playerName);
        Document update = new Document("onlineUid", uuid);
        if (updateInGame) {
            update.append("updateInGame", true);
        }
        UpdateOptions options = new UpdateOptions();
        options.collation(collationInsensitiveCase);
        UpdateResult result = users_collections.updateOne(query,
                new Document("$set", update), options);
        return result.getMatchedCount() != 0;
    }

    public boolean updatePlayerNameFromId(ObjectId id, String newUsername) {
        UpdateResult result = users_collections.updateOne(new Document("_id", id),
                new Document("$set", new Document("playerName", newUsername)));
        return result.getMatchedCount() != 0;
    }

    public boolean updatePlayerNameFromUUID(UUID uuid, String newUsername) {
        String suuid = uuid.toString();
        Document query = new Document(
                "$or", Arrays.asList(new Document("offlineUid", suuid),
                new Document("onlineUid", suuid)));
        UpdateResult result = users_collections.updateOne(query,
                new Document("$set", new Document("playerName", newUsername)));
        return result.getMatchedCount() != 0;
    }

    public boolean updatePremiumMode(String player, boolean active) {
        UpdateOptions options = new UpdateOptions();
        options.collation(collationInsensitiveCase);
        UpdateResult result = users_collections.updateOne(new Document("playerName", player),
                new Document("$set", new Document("loginPremium", active)), options);
        return result.getMatchedCount() != 0;
    }

    public boolean updatePremiumMode(String player, UUID uuid, boolean active) {
        String suuid = uuid.toString();
        Document query = new Document("$or", Arrays.asList(
                new Document("playerName", player),
                new Document("offlineUid", suuid),
                new Document("onlineUid", suuid)));
        UpdateResult result = users_collections.updateOne(query,
                new Document("$set", new Document("loginPremium", active)));
        return result.getMatchedCount() != 0;
    }

    public boolean updatePasswordOfflinePlayerByUsername(String username, String password) {
        UpdateOptions options = new UpdateOptions().upsert(true);
        options.collation(collationInsensitiveCase);
        Bson query = new Document().append("playerName", username);
        UpdateResult result = users_collections.updateOne(query,
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

    public void saveOfflinePlayer(String objectId, String playerName, UUID uuid, String password, SingleCallback result) {
        service.submit(() -> {
            try {
                Document document = getPlayerByUsernameOrUuidOffline(playerName, uuid);
                if (document != null) {
                    return;
                }
                Document obj = new Document();
                obj.put("_id", new ObjectId(objectId));
                obj.put("playerName", playerName);
                obj.put("created", new Date().getTime());
                obj.put("password", Cryptography.sha512(password));
                obj.put("offlineUid", uuid.toString());
                users_collections.insertOne(obj);
                result.onSuccess();
            } catch (Throwable throwable) {
                result.onError(throwable);
            }
        });
    }

    public void saveOnlinePlayer(String objectId, String playerName, UUID uuid, String password, SingleCallback result) {
        service.submit(() -> {
            try {
                Document document = getPlayerByUsernameOrUuidOnline(playerName, uuid);
                if (document != null) {
                    return;
                }
                Document obj = new Document();
                obj.put("_id", new ObjectId(objectId));
                obj.put("playerName", playerName);
                obj.put("created", new Date().getTime());
                obj.put("password", Cryptography.sha512(password));
                obj.put("onlineUid", uuid.toString());
                users_collections.insertOne(obj);
                result.onSuccess();
            } catch (Throwable throwable) {
                result.onError(throwable);
            }
        });
    }

}
