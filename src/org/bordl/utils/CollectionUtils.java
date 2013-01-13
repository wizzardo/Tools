package org.bordl.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author: moxa
 * Date: 12/17/12
 */
public class CollectionUtils {

    public static <T> void each(Iterable<T> c, Closure<Void, T> closure) {
        for (T t : c) {
            closure.execute(t);
        }
    }

    public static <T> void eachWithIndex(Iterable<T> c, Closure2<Void, Integer, T> closure) {
        int i = 0;
        for (T t : c) {
            closure.execute(i, t);
            i++;
        }
    }

    public static <T, R> List<R> collect(Iterable<T> c, Closure<R, T> closure) {
        List<R> l = new ArrayList<R>();
        for (T t : c) {
            l.add(closure.execute(t));
        }
        return l;
    }

    public static <T> List<T> grep(Iterable<T> c, Object expression) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (t.equals(expression))
                l.add(t);
        }
        return l;
    }

    public static <T> List<T> grep(Iterable<T> c, Closure<Boolean, T> closure) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (closure.execute(t))
                l.add(t);
        }
        return l;
    }

    public static <T> List<T> grep(Iterable<T> c, Range r) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (r.contains(t))
                l.add(t);
        }
        return l;
    }

    public static <T> List<T> grep(Iterable<T> c, Collection r) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (r.contains(t))
                l.add(t);
        }
        return l;
    }

    public static <T> List<T> grep(Iterable<T> c, Pattern p) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (t.toString().matches(p.pattern()))
                l.add(t);
        }
        return l;
    }

    public static <T> List<T> grep(Iterable<T> c, Class clazz) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (clazz.isAssignableFrom(t.getClass()))
                l.add(t);
        }
        return l;
    }

    public static <T> List<T> findAll(Iterable<T> c, Closure<Boolean, T> closure) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (closure.execute(t))
                l.add(t);
        }
        return l;
    }

    public static <T> T find(Iterable<T> c, Closure<Boolean, T> closure) {
        for (T t : c) {
            if (closure.execute(t))
                return t;
        }
        return null;
    }

    public static <T> boolean every(Iterable<T> c, Closure<Boolean, T> closure) {
        boolean b = true;
        for (T t : c) {
            b &= closure.execute(t);
            if (!b) {
                return false;
            }
        }
        return b;
    }

    public static <T> boolean any(Iterable<T> c, Closure<Boolean, T> closure) {
        for (T t : c) {
            if (closure.execute(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T> String join(Iterable<T> c, String separator) {
        StringBuilder sb = new StringBuilder();
        for (T t : c) {
            if (sb.length() > 0) {
                sb.append(separator);
            }
            sb.append(t);
        }
        return sb.toString();
    }

    public static abstract class Closure<R, T> {
        public abstract R execute(T it);
    }

    public static abstract class Closure2<R, T1, T2> {
        public abstract R execute(T1 it, T2 it2);
    }

    public static abstract class Closure3<R, T1, T2, T3> {
        public abstract R execute(T1 it, T2 it2, T3 it3);
    }

    public static abstract class Closure4<R, T1, T2, T3, T4> {
        public abstract R execute(T1 it, T2 it2, T3 it3, T4 it4);
    }

    public static abstract class Closure5<R, T1, T2, T3, T4, T5> {
        public abstract R execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5);
    }

    public static abstract class Closure6<R, T1, T2, T3, T4, T5, T6> {
        public abstract R execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5, T6 it6);
    }

    public static abstract class Closure7<R, T1, T2, T3, T4, T5, T6, T7> {
        public abstract R execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5, T6 it6, T7 it7);
    }

    public static abstract class Closure8<R, T1, T2, T3, T4, T5, T6, T7, T8, T9> {
        public abstract R execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5, T6 it6, T7 it7, T8 it8);
    }

    public static abstract class Closure9<R, T1, T2, T3, T4, T5, T6, T7, T8, T9> {
        public abstract R execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5, T6 it6, T7 it7, T8 it8, T9 it9);
    }

    public static abstract class Closure10<R, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> {
        public abstract R execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5, T6 it6, T7 it7, T8 it8, T9 it9, T10 it10);
    }

}
