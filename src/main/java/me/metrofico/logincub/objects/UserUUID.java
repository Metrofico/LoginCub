package me.metrofico.logincub.objects;

import java.util.UUID;

public class UserUUID {
    private final UUID uuidInUse;
    private final UUID offline;
    private final UUID online;

    public UserUUID(UUID uuidInUse, UUID offline, UUID online) {
        this.uuidInUse = uuidInUse;
        this.offline = offline;
        this.online = online;
    }

    public UUID getUuidInUse() {
        return uuidInUse;
    }

    public UUID getOffline() {
        return offline;
    }

    public UUID getOnline() {
        return online;
    }
}
