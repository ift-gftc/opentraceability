package opentraceability.utility;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class LimitedPool<T> {
    private ConcurrentLinkedDeque<LimitedPoolItem<T>> pool = new ConcurrentLinkedDeque<>();
    private boolean disposed = false;
    private long valueLifetime;
    private ValueFactory<T> valueFactory;
    private ValueDisposeAction<T> valueDisposeAction;

    public LimitedPool(ValueFactory<T> valueFactory, ValueDisposeAction<T> valueDisposeAction, long valueLifetime) {
        this.valueFactory = valueFactory;
        this.valueDisposeAction = valueDisposeAction;
        this.valueLifetime = valueLifetime;
    }

    public LimitedPool(ValueFactory<T> valueFactory, ValueDisposeAction<T> valueDisposeAction) {
        this(valueFactory, valueDisposeAction, TimeUnit.HOURS.toMillis(1));
    }

    public int getIdleCount() {
        return pool.size();
    }

    public LimitedPoolItem<T> get() {
        LimitedPoolItem<T> item = null;
        while (!disposed) {
            item = pool.pollLast();
            if (item == null || item.isExpired()) {
                if (item != null) {
                    item.dispose();
                }
            } else {
                return item;
            }
        }
        return new LimitedPoolItem<>(valueFactory.create(), (disposedItem) -> {
            if (disposedItem.isExpired()) {
                valueDisposeAction.dispose(disposedItem.getValue());
            } else {
                if (!disposed) {
                    pool.addLast(disposedItem);
                }
            }
        }, valueLifetime);
    }

    public void dispose() {
        disposed = true;
        LimitedPoolItem[] items = pool.toArray(new LimitedPoolItem[0]);
        for (LimitedPoolItem<T> item : items) {
            valueDisposeAction.dispose(item.getValue());
        }
    }

    public interface ValueFactory<T> {
        T create();
    }

    public interface ValueDisposeAction<T> {
        void dispose(T value);
    }
}