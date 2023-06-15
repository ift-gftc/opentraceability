package opentraceability.utility;

import java.util.concurrent.TimeUnit;

public class LimitedPoolItem<T> {
    public T value;
    private Action<LimitedPoolItem<T>> disposeAction;
    private long lifetime = TimeUnit.HOURS.toMillis(1);
    private long creationTime = System.currentTimeMillis();

    public LimitedPoolItem(T value, Action<LimitedPoolItem<T>> disposeAction, long lifetime) {
        this.value = value;
        this.disposeAction = disposeAction;
        this.lifetime = lifetime;
    }

    public LimitedPoolItem(T value, Action<LimitedPoolItem<T>> disposeAction) {
        this.value = value;
        this.disposeAction = disposeAction;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - creationTime > lifetime;
    }

    public void dispose() {
        disposeAction.invoke(this);
    }
}