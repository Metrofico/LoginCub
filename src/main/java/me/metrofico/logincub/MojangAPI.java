package me.metrofico.logincub;


import json.Json;
import json.JsonArray;
import json.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


//Created by Metrofico

public class MojangAPI {
    public enum Account {
        UNPAIDED, UNMIGRATED, PREMIUM, CRACKED, ERROR, RATE_LIMIT
    }

    public enum RequestAccountApi {
        MOJANG, MINETOOLS
    }

    public static class ClientProxify {
        OkHttpClient client;
        long lastDate;

        ClientProxify(OkHttpClient client) {
            this.client = client;
        }

        public long lastDate() {
            return lastDate;
        }

        public void setRefresh() {
            LocalDateTime dateTime = LocalDateTime.now().plus(Duration.of(6, ChronoUnit.MINUTES));
            Date tmfn = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
            lastDate = tmfn.getTime();
        }

        public void reset() {
            lastDate = 0;
        }

        public boolean available() {
            if (lastDate != 0) {
                Date dateFinished = new Date(lastDate);
                Date now = new Date();
                return now.after(dateFinished);
            }
            return true;
        }
    }

    public static class Proxify {
        String host;
        int port;
        String type;
        long lastDate;

        public Proxify(String host, int port, String type) {
            this.host = host;
            this.port = port;
            this.type = type;
            lastDate = 0;
        }

        public Proxy.Type getType() {
            switch (type) {
                case "http":
                    return Proxy.Type.HTTP;
                case "socks":
                    return Proxy.Type.SOCKS;
                case "direct":
                    return Proxy.Type.DIRECT;
            }
            return Proxy.Type.HTTP;
        }


    }

    public static class FetchAccount {
        private String uuid;
        private Account status;
        private UUID _UUID;

        public FetchAccount(String uuid, Account status) {
            this.uuid = uuid;
            this.status = status;
            if (uuid != null) {
                this._UUID = UUID.fromString(uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32));
            }
        }

        public Account getStatus() {
            return status;
        }

        public String getStringUuid() {
            return uuid;
        }

        public UUID getUuid() {
            return this._UUID;
        }
    }

    //~ MojanAPI only support 600 request per 10 minutes
    //~ is neccesary use proxy for request


    // @OkHttp3
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client;
    private int start = 1;
    private List<Proxify> listProxy;
    private final HashMap<String, ClientProxify> cleanProxy;
    private boolean changing_to_proxy = false;
    private boolean loadingProxies;
    private RequestAccountApi requestAccountApi;
    private long finishRateLimit = 0;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> future;

    MojangAPI(List<Proxify> listProxy) {
        this.listProxy = listProxy;
        requestAccountApi = RequestAccountApi.MOJANG;
        cleanProxy = new HashMap<>();
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.readTimeout(2, TimeUnit.SECONDS);
        httpClient.writeTimeout(2, TimeUnit.SECONDS);
        client = httpClient.build();

        executorService = Executors.newScheduledThreadPool(1);
        loadingProxies = true;

        //cleanProxy();
    }

    public void runAvailableAPI(Runnable runnable, int minutes) {
        if (future == null) {
            future = executorService.schedule(runnable, minutes, TimeUnit.MINUTES);
        }
    }

    public RequestAccountApi getRequestAccountApi() {
        return requestAccountApi;
    }

    public void setListProxy(List<Proxify> proxifies) {
        loadingProxies = true;
        this.listProxy = proxifies;
        cleanProxy();
    }

    public void cleanProxy() {
        new Thread(() -> {
            ConsoleUtil.prefixConsole("Verificando proxies disponibles para mojangapi");
            for (Proxify proxify : listProxy) {
                try {
                    Proxy proxy = new Proxy(proxify.getType(),
                            new InetSocketAddress(proxify.host, proxify.port));
                    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
                    httpClient.proxy(proxy);
                    httpClient.connectTimeout(3, TimeUnit.SECONDS);
                    httpClient.readTimeout(3, TimeUnit.SECONDS);
                    httpClient.writeTimeout(3, TimeUnit.SECONDS);
                    OkHttpClient client = httpClient.build();
                    FetchAccount c = fetchUUIDValidate("test", client);
                    if (c.getStatus() == Account.ERROR || c.getStatus() == Account.RATE_LIMIT) {
                        System.out.println("Proxy invalido: " + proxify.host);
                        continue;
                    }
                    cleanProxy.put(proxify.host, new ClientProxify(client));
                    System.out.println("Proxy disponible: " + proxify.host);
                } catch (Exception e) {
                    System.out.println("Proxy invalido: " + proxify.host);
                }
            }
            ConsoleUtil.prefixConsole("Proxies disponibles totales: " + cleanProxy.size());
            loadingProxies = false;
        }).start();

    }


    private String post(String url, String json, OkHttpClient client) throws IOException {
        RequestBody body = RequestBody.Companion.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody rbody = response.body();
        return rbody != null ? rbody.string() : null;
    }


    private String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.Companion.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody rbody = response.body();
        return rbody != null ? rbody.string() : null;
    }

    private String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody rbody = response.body();
        return rbody != null ? rbody.string().trim() : null;
    }

    public boolean isLoadingProxies() {
        return loadingProxies;
    }

    public void changeProxyRequest() {
        if (changing_to_proxy) {
            boolean found = false;
            if (isFinishRateLimit()) {
                start = 1;
                changing_to_proxy = false;
                resetClientLocalNetwork();
                return;
            }
            for (ClientProxify proxi : cleanProxy.values()) {
                if (proxi.available()) {
                    client = proxi.client;
                    found = true;
                    System.out.println("Proxy inicializado con hostname: " + "(proxy)");
                    break;
                }
            }
            if (!found) {
                resetClientLocalNetwork();
            }
            start = 1;
            changing_to_proxy = false;
            System.out.println("Proxy finalizado (conectado)");
        }
    }

    public void resetClientLocalNetwork() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.readTimeout(2, TimeUnit.SECONDS);
        httpClient.writeTimeout(2, TimeUnit.SECONDS);
        client = httpClient.build();
        System.out.println("Proxy inicializado con hostname: " + "(publica ip - local)");
    }


    private String jsonFetchUUID(String username) {
        return "[ \"" + username + "\" ]";
    }
/*
    void startRequest(int max) {
        Thread fetching = new Thread(() -> {
            synchronized (this) {
                while (start < max) {
                    if (changing_to_proxy) {
                        try {
                            System.out.println("En espera del cambio de proxy");
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    long msStart = new Date().getTime();
                    System.out.println("Solicitud  #" + start);
                    FetchAccount a = testFetchUUID("Hasco", requestAccountApi);
                    long msfinish = new Date().getTime();
                    String timeFinished = msfinish - msStart + "ms";
                    System.out.println("Solicitud respuesta: " + a.getStatus() + " - " + (a.getUuid() != null ? a.getUuid() : "undefined") + " [" + timeFinished + "]");
                    if (a.getStatus() == Account.RATE_LIMIT) {
                        System.out.println("Se encontro limite de racha, solicitudes realizadas: " + start);
                        System.out.println("Inicializando proxy");
                        changeProxyRequest();
                        System.out.println("notificando hilo");
                        notify();
                    }
                    start++;
                }
            }
        });
        fetching.start();
    }*/

    public FetchAccount fetchUUIDValidate(String username, OkHttpClient client) {
        // Fetch - UUID
        String url_fetchUUID = "https://api.mojang.com/profiles/minecraft";
        String fetching = null;
        try {
            fetching = post(url_fetchUUID, jsonFetchUUID(username), client);
        } catch (IOException e) {
            changingProxyFetch();
            return new FetchAccount(null, Account.RATE_LIMIT);
        }
        if (fetching != null) {
            if (fetching.contains("Request Blocked") || fetching.contains("The request could not be satisfied")) {
                changingProxyFetch();
                return new FetchAccount(null, Account.RATE_LIMIT);
            }
        }
        JsonArray value;
        try {
            value = Json.parse(fetching).asArray();
        } catch (Exception e) {
            return new FetchAccount(null, Account.CRACKED);
        }
        if (value == null || value.isEmpty() || value.isNull()) {
            return new FetchAccount(null, Account.CRACKED);
        }
        JsonObject obj = value.get(0).asObject();
        String uuid = obj.get("id").asString();
        if (obj.get("legacy") != null) {
            boolean unmigrated = obj.get("legacy").asBoolean();
            if (unmigrated) {
                return new FetchAccount(null, Account.UNMIGRATED);
            }
        }
        if (obj.get("demo") != null) {
            boolean paid = obj.get("demo").asBoolean();
            if (paid) {
                return new FetchAccount(null, Account.UNPAIDED);
            }
        }
        return new FetchAccount(uuid, Account.PREMIUM);

    }

    public void setFinishRateLimit() {
        LocalDateTime dateTime = LocalDateTime.now().plus(Duration.of(6, ChronoUnit.MINUTES));
        Date tmfn = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        finishRateLimit = tmfn.getTime();
    }

    public boolean isFinishRateLimit() {
        if (finishRateLimit != 0) {
            Date dateFinished = new Date(finishRateLimit);
            Date now = new Date();
            return now.after(dateFinished);
        }
        return false;
    }

    public void changingProxyFetch() {
        if (finishRateLimit == 0) {
            setFinishRateLimit();
        }
        Proxy proxy = client.proxy();
        if (proxy != null) {
            String address = proxy.address().toString();
            String host = address.substring(address.indexOf("/") + 1, address.indexOf(":"));
            ClientProxify h = cleanProxy.get(host);
            h.setRefresh();
            cleanProxy.put(host, h);
        }
        System.out.println("Se encontro limite de racha");
        System.out.println("Inicializando proxy");
        changing_to_proxy = true;
        changeProxyRequest();
    }

    public void turneOtherBackendApi() {
        requestAccountApi = RequestAccountApi.MINETOOLS;
        if (future == null) {
            runAvailableAPI(() -> {
                try {
                    requestAccountApi = RequestAccountApi.MOJANG;
                    future = null;
                } catch (Throwable t) {
                    t.printStackTrace();
                    requestAccountApi = RequestAccountApi.MOJANG;
                }
            }, 7);
        }
    }

    public FetchAccount fetchUUID(String username) {
        // Fetch - UUID
        String url_fetchUUID = requestAccountApi == RequestAccountApi.MOJANG ? "https://api.mojang.com/profiles/minecraft" :
                "https://api.minetools.eu/uuid/";
        String fetching;
        try {
            if (requestAccountApi == RequestAccountApi.MOJANG) {
                fetching = post(url_fetchUUID, jsonFetchUUID(username));
            } else {
                fetching = get(url_fetchUUID + username.trim());
            }
        } catch (IOException e) {
            turneOtherBackendApi();
            return new FetchAccount(null, Account.RATE_LIMIT);
        }
        if (fetching != null) {
            if (fetching.contains("Request Blocked") || fetching.contains("The request could not be satisfied")) {
                turneOtherBackendApi();
                return new FetchAccount(null, Account.RATE_LIMIT);
            }
        }
        JsonArray mojanValue = null;
        JsonObject minetoolsValue = null;
        try {
            if (requestAccountApi == RequestAccountApi.MOJANG) {
                mojanValue = Json.parse(fetching).asArray();
            } else {
                minetoolsValue = Json.parse(fetching).asObject();
            }
        } catch (Exception e) {
            return new FetchAccount(null, Account.CRACKED);
        }
        if (requestAccountApi == RequestAccountApi.MINETOOLS) {
            if (minetoolsValue == null || minetoolsValue.isEmpty() || minetoolsValue.isNull()) {
                return new FetchAccount(null, Account.CRACKED);
            }
            String uuid = minetoolsValue.get("id").asString();
            String status = minetoolsValue.get("status").asString();
            if (status == null || !status.trim().equalsIgnoreCase("OK") ||
                    (uuid == null || uuid.trim().isEmpty() || uuid.trim().equalsIgnoreCase("null"))) {
                return new FetchAccount(null, Account.CRACKED);
            }
            return new FetchAccount(uuid, Account.PREMIUM);
        }
        if (mojanValue == null || mojanValue.isEmpty() || mojanValue.isNull()) {
            return new FetchAccount(null, Account.CRACKED);
        }
        JsonObject obj = mojanValue.get(0).asObject();
        String uuid = obj.get("id").asString();
        if (obj.get("legacy") != null) {
            boolean unmigrated = obj.get("legacy").asBoolean();
            if (unmigrated) {
                return new FetchAccount(null, Account.UNMIGRATED);
            }
        }
        if (obj.get("demo") != null) {
            boolean paid = obj.get("demo").asBoolean();
            if (paid) {
                return new FetchAccount(null, Account.UNPAIDED);
            }
        }
        return new FetchAccount(uuid, Account.PREMIUM);
    }

    /*public FetchAccount testFetchUUID(String username, RequestAccountApi api) {
        if (changing_to_proxy) {
            return new FetchAccount(null, Account.RATE_LIMIT);
        }
        // Fetch - UUID
        String url_fetchUUID = api == RequestAccountApi.MOJANG ? "https://api.mojang.com/profiles/minecraft" :
                "https://api.minetools.eu/uuid/";
        String fetching;
        try {
            if (api == RequestAccountApi.MOJANG) {
                fetching = post(url_fetchUUID, jsonFetchUUID(username));
            } else {
                fetching = get(url_fetchUUID + username.trim());
            }
        } catch (IOException e) {
            requestAccountApi = api == RequestAccountApi.MINETOOLS ? RequestAccountApi.MOJANG : RequestAccountApi.MINETOOLS;
            return new FetchAccount(null, Account.RATE_LIMIT);
        }
        if (fetching != null) {
            if (fetching.contains("Request Blocked") || fetching.contains("The request could not be satisfied")) {
                requestAccountApi = api == RequestAccountApi.MINETOOLS ? RequestAccountApi.MOJANG : RequestAccountApi.MINETOOLS;
                return new FetchAccount(null, Account.RATE_LIMIT);
            }
        }
        JsonArray mojanValue = null;
        JsonObject minetoolsValue = null;
        try {
            if (api == RequestAccountApi.MOJANG) {
                mojanValue = Json.parse(fetching).asArray();
            } else {
                minetoolsValue = Json.parse(fetching).asObject();
            }
        } catch (Exception e) {
            return new FetchAccount(null, Account.CRACKED);
        }
        if (api == RequestAccountApi.MINETOOLS) {
            if (minetoolsValue == null || minetoolsValue.isEmpty() || minetoolsValue.isNull()) {
                return new FetchAccount(null, Account.CRACKED);
            }
            String uuid = minetoolsValue.get("id").asString();
            String status = minetoolsValue.get("status").asString();
            if (status == null || !status.trim().equalsIgnoreCase("OK") ||
                    (uuid == null || uuid.trim().isEmpty() || uuid.trim().equalsIgnoreCase("null"))) {
                return new FetchAccount(null, Account.CRACKED);
            }
            return new FetchAccount(uuid, Account.PREMIUM);
        }
        if (mojanValue == null || mojanValue.isEmpty() || mojanValue.isNull()) {
            return new FetchAccount(null, Account.CRACKED);
        }
        JsonObject obj = mojanValue.get(0).asObject();
        String uuid = obj.get("id").asString();
        if (obj.get("legacy") != null) {
            boolean unmigrated = obj.get("legacy").asBoolean();
            if (unmigrated) {
                return new FetchAccount(null, Account.UNMIGRATED);
            }
        }
        if (obj.get("demo") != null) {
            boolean paid = obj.get("demo").asBoolean();
            if (paid) {
                return new FetchAccount(null, Account.UNPAIDED);
            }
        }
        return new FetchAccount(uuid, Account.PREMIUM);
    }*/

}
