package com.wizzardo.tools.collections.lazy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by wizzardo on 08.11.15.
 */
abstract class Command<A, B> {
    protected Command<?, A> parent;
    protected Command<B, ?> child;

    Command(Command<?, A> parent) {
        this.parent = parent;
        parent.child = this;
    }

    Command() {
    }

    protected void process(A a) {
    }

    protected void start() {
        parent.start();
    }

    protected void end() {
        if (child != null)
            child.end();
    }

    protected void stop() {
        if (parent != null)
            parent.stop();
    }

    public B get() {
        return null;
    }

    static class FilterCommand<T> extends Command<T, T> {
        private Filter<T> filter;

        public FilterCommand(Command<?, T> parent, Filter<T> filter) {
            super(parent);
            this.filter = filter;
        }

        @Override
        protected void process(T t) {
            if (filter.allow(t))
                child.process(t);
        }
    }

    static class EachCommand<T> extends Command<T, T> {
        private Consumer<T> consumer;

        public EachCommand(Command<?, T> parent, Consumer<T> consumer) {
            super(parent);
            this.consumer = consumer;
        }

        @Override
        protected void process(T t) {
            consumer.consume(t);
            if (child != null)
                child.process(t);
        }
    }

    static class ReduceCommand<T> extends Command<T, T> {
        private final Reducer<T> reducer;
        private T prev;

        public ReduceCommand(Command<?, T> command, Reducer<T> reducer) {
            super(command);
            this.reducer = reducer;
        }

        @Override
        protected void process(T t) {
            if (prev == null)
                prev = t;
            else
                prev = reducer.reduce(prev, t);
        }

        @Override
        protected void end() {
            child.process(prev);
            super.end();
        }
    }

    static class CountCommand<A> extends Command<A, Integer> {
        private int count = 0;

        CountCommand(Command<?, A> parent) {
            super(parent);
        }

        @Override
        protected void process(A a) {
            count++;
        }

        @Override
        public Integer get() {
            return count;
        }

        public int getCount() {
            return count;
        }
    }

    static class CollectListCommand<A> extends Command<A, List<A>> {
        private List<A> list;

        CollectListCommand(Command<?, A> parent) {
            super(parent);
            list = new ArrayList<A>();
        }

        CollectListCommand(Command<?, A> parent, int initialSize) {
            super(parent);
            list = new ArrayList<A>(initialSize);
        }

        @Override
        protected void process(A a) {
            list.add(a);
        }

        @Override
        public List<A> get() {
            return list;
        }
    }

    static class FirstCommand<A> extends Command<A, A> {
        private A first;

        FirstCommand(Command<?, A> parent) {
            super(parent);
        }

        @Override
        protected void process(A a) {
            if (first == null) {
                first = a;
                parent.stop();
            }
        }

        @Override
        public A get() {
            return first;
        }
    }

    static class LastCommand<A> extends Command<A, A> {
        private A last;

        LastCommand(Command<?, A> parent) {
            super(parent);
        }

        @Override
        protected void process(A a) {
            last = a;
        }

        @Override
        public A get() {
            return last;
        }
    }

    static class MinCommand<A> extends Command<A, A> {
        private A min;

        MinCommand(Command<?, A> parent) {
            super(parent);
        }

        @Override
        protected void process(A a) {
            if (min == null || ((Comparable<A>) min).compareTo(a) > 0)
                min = a;
        }

        @Override
        public A get() {
            return min;
        }
    }

    static class MaxCommand<A> extends Command<A, A> {
        private A max;

        MaxCommand(Command<?, A> parent) {
            super(parent);
        }

        @Override
        protected void process(A a) {
            if (max == null || ((Comparable<A>) max).compareTo(a) < 0)
                max = a;
        }

        @Override
        public A get() {
            return max;
        }
    }

    static class MinWithComparatorCommand<A> extends Command<A, A> {
        private A min;
        private Comparator<A> comparator;

        MinWithComparatorCommand(Command<?, A> parent, Comparator<A> comparator) {
            super(parent);
            this.comparator = comparator;
        }

        @Override
        protected void process(A a) {
            if (min == null || comparator.compare(min, a) > 0)
                min = a;
        }

        @Override
        public A get() {
            return min;
        }
    }

    static class MaxWithComparatorCommand<A> extends Command<A, A> {
        private A max;
        private Comparator<A> comparator;

        MaxWithComparatorCommand(Command<?, A> parent, Comparator<A> comparator) {
            super(parent);
            this.comparator = comparator;
        }

        @Override
        protected void process(A a) {
            if (max == null || comparator.compare(max, a) < 0)
                max = a;
        }

        @Override
        public A get() {
            return max;
        }
    }

}
