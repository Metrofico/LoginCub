package me.metrofico.logincub.callbacks;

public interface SingleCallback {
    void onSuccess();

    default void onError(Throwable throwable) {
        System.out.println("[ERROR] (" + throwable.getMessage() + ")");
    }
}
