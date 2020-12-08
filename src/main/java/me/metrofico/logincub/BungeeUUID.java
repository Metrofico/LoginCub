package me.metrofico.logincub;

import com.google.common.base.Charsets;

import java.util.UUID;

public class BungeeUUID {
    public static UUID getUUIDfromString(String uuid) {
        try {
            if (uuid.contains("-")) {
                return UUID.fromString(uuid);
            }
            return UUID.fromString(uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32));
        } catch (Throwable w) {

        }
        return null;
    }

    public static UUID generateUUIDOffline(String username) {
        try {
            return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
        } catch (Throwable w) {
        }
        return null;
    }
}
