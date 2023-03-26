using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Utility
{
    public class LimitedPool<T> : IDisposable where T : class
    {
        readonly Func<T> _valueFactory;
        readonly Action<T> _valueDisposeAction;
        readonly TimeSpan _valueLifetime;
        readonly ConcurrentStack<LimitedPoolItem<T>> _pool;
        bool _disposed;

        public LimitedPool(Func<T> valueFactory, Action<T> valueDisposeAction, TimeSpan? valueLifetime = null)
        {
            _valueFactory = valueFactory;
            _valueDisposeAction = valueDisposeAction;
            _valueLifetime = valueLifetime ?? TimeSpan.FromHours(1);
            _pool = new ConcurrentStack<LimitedPoolItem<T>>();
        }

        public int IdleCount => _pool.Count;

        public LimitedPoolItem<T> Get()
        {
            LimitedPoolItem<T> item;
            // try to get live cached item
            while (!_disposed && _pool.TryPop(out item))
            {
                if (!item.Expired)
                    return item;
                // dispose expired item
                item.Dispose();
            }
            // since no cached items available we create a new one
            return new LimitedPoolItem<T>(_valueFactory(), disposedItem =>
            {
                if (disposedItem.Expired)
                {
                    // item has been expired, dispose it forever
                    _valueDisposeAction(disposedItem.Value);
                }
                else
                {
                    // item is still fresh enough, return it to the pool
                    if (!_disposed)
                        _pool.Push(disposedItem);
                }
            }, _valueLifetime);
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        void Dispose(bool disposing)
        {
            if (disposing)
            {
                _disposed = true;
                var items = _pool.ToArray();
                foreach (var item in items)
                    _valueDisposeAction(item.Value);
            }
        }
    }

    public class LimitedPoolItem<T> : IDisposable
    {
        readonly Action<LimitedPoolItem<T>> _disposeAction;

        readonly TimeSpan _lifetime;
        bool _expired;

        public T Value { get; }

        internal bool Expired
        {
            get
            {
                if (_expired)
                    return true;
                _expired = _stopwatch.Elapsed > _lifetime;
                return _expired;
            }
        }
        readonly Stopwatch _stopwatch;

        internal LimitedPoolItem(T value, Action<LimitedPoolItem<T>> disposeAction, TimeSpan lifetime)
        {
            _disposeAction = disposeAction;
            _lifetime = lifetime;
            Value = value;
            _stopwatch = new Stopwatch();
            _stopwatch.Start();
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        void Dispose(bool disposing)
        {
            if (disposing)
            {
                _disposeAction(this);
            }
        }
    }
}
