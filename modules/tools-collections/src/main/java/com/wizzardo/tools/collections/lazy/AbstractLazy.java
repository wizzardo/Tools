package com.wizzardo.tools.collections.lazy;

import java.util.*;

/**
 * Created by wizzardo on 15.11.15.
 */
public abstract class AbstractLazy<A, B> extends Command<A, B> {
    public static final Supplier SUPPLIER_HASH_MAP = new Supplier<Map>() {
        @Override
        public Map supply() {
            return new HashMap();
        }
    };

    public static <K, V> Supplier<Map<K, V>> hashMapSupplier() {
        return SUPPLIER_HASH_MAP;
    }

    public abstract AbstractLazy<B, B> filter(Filter<? super B> filter);

    public abstract AbstractLazy<B, B> each(Consumer<? super B> consumer);

    public abstract AbstractLazy<B, B> each(ConsumerWithInt<? super B> consumer);

    public abstract <T> AbstractLazy<B, T> merge(Mapper<? super B, ? extends Lazy<T, T>> mapper);

    public <K> LazyGrouping<K, B, B, LazyGroup<K, B>> groupBy(final Mapper<? super B, K> toKey) {
        return groupBy(toKey, AbstractLazy.<K, LazyGroup<K, B>>hashMapSupplier());
    }

    public <K> LazyGrouping<K, B, B, LazyGroup<K, B>> groupBy(final Mapper<? super B, K> toKey, final Supplier<Map<K, LazyGroup<K, B>>> groupMapSupplier) {
        return this.then(new LazyGrouping<K, B, B, LazyGroup<K, B>>(groupMapSupplier.supply()) {

            @Override
            protected void process(B b) {
                K key = toKey.map(b);
                LazyGroup<K, B> group = groups.get(key);
                if (group == null) {
                    groups.put(key, group = new LazyGroup<K, B>(key));
                    processToChild(group);
                }
                group.process(b);
            }

            @Override
            protected void onEnd() {
                for (LazyGroup<K, B> group : groups.values()) {
                    group.onEnd();
                }
                super.onEnd();
            }
        });
    }

    public <C> C collect(C collector, BiConsumer<? super C, ? super B> accumulator) {
        return then(new LazyCollectWithAccumulator<C, B>(collector, accumulator)).startAndGet();
    }

    public void execute() {
        then(new Command.FinishCommand<B, B>()).start();
    }

    public int count() {
        LazyCount<B> count = then(new LazyCount<B>());
        count.start();
        return count.getCount();
    }

    public B first() {
        return first(null);
    }

    public B first(B def) {
        return then(new LazyFirst<B>(def)).startAndGet();
    }

    public B last() {
        return last(null);
    }

    public B last(B def) {
        return then(new LazyLast<B>(def)).startAndGet();
    }

    public B min(Comparator<? super B> comparator) {
        return min(null, comparator);
    }

    public B min(B def, Comparator<? super B> comparator) {
        return then(new LazyMinWithComparator<B>(def, comparator)).startAndGet();
    }

    public B min() {
        return min((B) null);
    }

    public B min(B def) {
        return then(new LazyMin<B>(def)).startAndGet();
    }

    public B max(Comparator<? super B> comparator) {
        return max(null, comparator);
    }

    public B max(B def, Comparator<? super B> comparator) {
        return then(new LazyMaxWithComparator<B>(def, comparator)).startAndGet();
    }

    public B max() {
        return max((B) null);
    }

    public B max(B def) {
        return then(new LazyMax<B>(def)).startAndGet();
    }

    public List<B> toList() {
        return collect(new ArrayList<B>());
    }

    public <C extends Collection<B>> C collect(C collection) {
        return then(new LazyCollect<B, C>(collection)).startAndGet();
    }

    public List<B> toSortedList(Comparator<? super B> comparator) {
        return toSortedList(new ArrayList<B>(), comparator);
    }

    public List<B> toSortedList() {
        return toSortedList(new ArrayList<B>());
    }

    public List<B> toSortedList(List<B> list, Comparator<? super B> comparator) {
        Collections.sort(collect(list), comparator);
        return list;
    }

    public List<B> toSortedList(List<B> list) {
        Collections.sort((List<Comparable>) collect(list));
        return list;
    }

    public String join(String separator) {
        return join(separator, new StringBuilder());
    }

    public String join(String separator, StringBuilder sb) {
        return then(new LazyJoin<B>(sb, separator)).startAndGet();
    }

    public <K, V> Map<K, V> toMap(Mapper<B, K> toKey, Mapper<LazyGroup<K, B>, V> toValue) {
        return toMap(AbstractLazy.<K, LazyGroup<K, B>>hashMapSupplier(), toKey, toValue);
    }

    public <K> Map<K, List<B>> toMap(Supplier<Map<K, LazyGroup<K, B>>> groupMapSupplier, Mapper<B, K> toKey) {
        return toMap(groupMapSupplier, toKey, new LazyGroupToListMapper<K, B>());
    }

    public <K, V> Map<K, V> toMap(Supplier<Map<K, LazyGroup<K, B>>> groupMapSupplier, Mapper<B, K> toKey, Mapper<LazyGroup<K, B>, V> toValue) {
        return groupBy(toKey, groupMapSupplier).toMap(toValue);
    }
}
