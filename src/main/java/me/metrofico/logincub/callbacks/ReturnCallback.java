package me.metrofico.logincub.callbacks;

public interface ReturnCallback<T> {
    void onSuccess(T object);

    default void onError(Throwable throwable) {
        System.out.println("[ERROR] (" + throwable.getMessage() + ")");
    }
}
