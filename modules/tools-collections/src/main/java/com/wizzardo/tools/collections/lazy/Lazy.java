package com.wizzardo.tools.collections.lazy;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by wizzardo on 08.11.15.
 */
public class Lazy<A, B> extends AbstractLazy<A, B> {

    public static <T> Lazy<T, T> of(final Iterable<T> iterable) {
        return new StartLazy<T>() {
            @Override
            protected void process() {
                for (T t : iterable) {
                    if (stop)
                        break;
                    processToChild(t);
                }
            }
        };
    }

    public static <T> Lazy<T, T> of(final Iterator<T> iterator) {
        return new StartLazy<T>() {
            @Override
            protected void process() {
                Iterator<T> i = iterator;
                Command<T, ?> child = this.child;
                while (!stop && i.hasNext()) {
                    child.process(i.next());
                }
            }
        };
    }

    public static <K, V> Lazy<Map.Entry<K, V>, Map.Entry<K, V>> of(Map<K, V> map) {
        return of(map.entrySet());
    }

    public static <T> Lazy<T, T> of(final T... array) {
        return new StartLazy<T>() {
            @Override
            protected void process() {
                Command<T, ?> child = this.child;
                for (T t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Lazy<Integer, Integer> of(final int[] array) {
        return new StartLazy<Integer>() {
            @Override
            protected void process() {
                Command<Integer, ?> child = this.child;
                for (int t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Lazy<Long, Long> of(final long[] array) {
        return new StartLazy<Long>() {
            @Override
            protected void process() {
                Command<Long, ?> child = this.child;
                for (long t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Lazy<Double, Double> of(final double[] array) {
        return new StartLazy<Double>() {
            @Override
            protected void process() {
                Command<Double, ?> child = this.child;
                for (double t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Lazy<Float, Float> of(final float[] array) {
        return new StartLazy<Float>() {
            @Override
            protected void process() {
                Command<Float, ?> child = this.child;
                for (float t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Lazy<Byte, Byte> of(final byte[] array) {
        return new StartLazy<Byte>() {
            @Override
            protected void process() {
                Command<Byte, ?> child = this.child;
                for (byte t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Lazy<Boolean, Boolean> of(final boolean[] array) {
        return new StartLazy<Boolean>() {
            @Override
            protected void process() {
                Command<Boolean, ?> child = this.child;
                for (boolean t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Lazy<Short, Short> of(final short[] array) {
        return new StartLazy<Short>() {
            @Override
            protected void process() {
                Command<Short, ?> child = this.child;
                for (short t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Lazy<Character, Character> of(final char[] array) {
        return new StartLazy<Character>() {
            @Override
            protected void process() {
                Command<Character, ?> child = this.child;
                for (char t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    private static abstract class StartLazy<T> extends Lazy<T, T> {
        boolean stop = false;

        @Override
        protected void start() {
            if (child == null)
                return;

            process();

            child.onEnd();
        }

        protected abstract void process();

        @Override
        protected void stop() {
            stop = true;
        }
    }

    public B reduce(Reducer<B> reducer) {
        return reduce(null, reducer);
    }

    public B reduce(B def, Reducer<B> reducer) {
        LazyReduce<B> reduce = then(new LazyReduce<B>(def, reducer));
        reduce.start();
        return reduce.get();
    }

    public <T> Lazy<B, T> merge(Mapper<? super B, ? extends Lazy<T, T>> mapper) {
        return then(new LazyMapMerge<B, T>(mapper));
    }

    public Lazy<B, B> filter(Filter<? super B> filter) {
        return then(new LazyFilter<B>(filter));
    }

    public Lazy<B, B> each(Consumer<? super B> consumer) {
        return then(new LazyEach<B>(consumer));
    }

    public Lazy<B, B> each(ConsumerWithInt<? super B> consumer) {
        return then(new LazyEachWithIndex<B>(consumer));
    }

    public <T> Lazy<B, T> map(Mapper<? super B, T> mapper) {
        return then(new LazyMap<B, T>(mapper));
    }

    public <K> Map<K, List<B>> toMap(Mapper<B, K> toKey) {
        return toMap(Lazy.<K, LazyGroup<K, B>>hashMapSupplier(), toKey, new LazyGroupToListMapper<K, B>());
    }
}
