package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.CharTree;
import com.wizzardo.tools.misc.Pair;
import com.wizzardo.tools.reflection.FieldReflection;

/**
 * @author: wizzardo
 * Date: 3/21/14
 */
class JsonUtils {

    private static long[] fractionalShift;

    static {
        fractionalShift = new long[10];
        fractionalShift[0] = 1;
        for (int i = 1; i < fractionalShift.length; i++) {
            fractionalShift[i] = fractionalShift[i - 1] * 10;
        }
    }

    static int trimRight(char[] s, int from, int to) {
        while ((from < to) && (s[to - 1] <= ' ')) {
            to--;
        }
        return to;
    }

    static boolean isNull(char[] s, int from, int length) {
        return length == 4 && s[from] == 'n' && s[from + 1] == 'u' && s[from + 2] == 'l' && s[from + 3] == 'l';
    }

    static boolean isTrue(char[] s, int from, int length) {
        return length == 4 && s[from] == 't' && s[from + 1] == 'r' && s[from + 2] == 'u' && s[from + 3] == 'e';
    }

    static boolean isFalse(char[] s, int from, int length) {
        return length == 5 && s[from] == 'f' && s[from + 1] == 'a' && s[from + 2] == 'l' && s[from + 3] == 's' && s[from + 4] == 'e';
    }

    static int skipSpaces(char[] s, int from, int to) {
        while (from < to && s[from] <= ' ') {
            from++;
        }
        return from;
    }

    static int parseNumber(JsonBinder binder, char[] s, int from, int to) {
        int i = from;

        boolean minus = s[from] == '-';
        if (minus)
            i++;

        if (i == to)
            throw new IllegalStateException("no number found, only '-'");

        boolean floatValue = false;
        long l = 0;
        char ch = 0;
        while (i < to) {
            ch = s[i];
            if (ch != '0')
                break;
            i++;
        }
        from = i;
        while (i < to) {
            ch = s[i];
            if (ch >= '0' && ch <= '9') {
                l = l * 10 + (ch - '0');
            } else if (ch == '.') {
                floatValue = true;
                break;
            } else
                break;
            i++;
        }

        double d = 0;
        if (floatValue) {
            long number = l;
            int numberLength = number == 0 ? 0 : i - from;
            i++;
            int fractionalPartStart = i;
            int fractionalPartStop = -1;

            if (numberLength == 0)
                while (i < to) {
                    ch = s[i];
                    if (ch != '0')
                        break;
                    i++;
                }

            outer:
            while (i < to) {
                ch = s[i];
                if (ch >= '0' && ch <= '9') {
                    if (numberLength < 19) {
                        number = number * 10 + (ch - '0');
                        numberLength++;
                    }
                    if (numberLength == 19) {
                        fractionalPartStop = i + 1;
                        while (i < to) {
                            ch = s[i];
                            if (ch < '0' || ch > '9') {
                                break outer;
                            }
                            i++;
                        }
                    }
                } else {
                    fractionalPartStop = i;
                    break;
                }
                i++;
            }
            if (fractionalPartStop == -1)
                fractionalPartStop = i;

//            if (ch != '"' && ch != '\'' && ch != '}' && ch != ']' && ch != ',')
//                throw new NumberFormatException("can't parse '" + new String(s, from, i - from + 1) + "' as number");


            if (minus)
                d = -createDouble(number, fractionalPartStop - fractionalPartStart);
            else
                d = createDouble(number, fractionalPartStop - fractionalPartStart);
        }

        if (minus)
            l = -l;

        if (ch == 'e' || ch == 'E') {
            if (!floatValue) {
                d = l;
                floatValue = true;
            }

            i++;
            int degree = 0;
            minus = s[i] == '-';
            if (minus)
                i++;

            while (i < to) {
                ch = s[i];
                if (ch >= '0' && ch <= '9')
                    degree = degree * 10 + (ch - '0');
                else
                    break;
                i++;
            }

            d = (minus) ? d / Math.pow(10, degree) : d * Math.pow(10, degree);
        }

        if (binder == null)
            return i;

        JsonFieldSetter setter = binder.getFieldSetter();
        if (setter != null) {
            setNumber(setter, binder.getObject(), l, d, floatValue);
            return i;
        }

        if (!floatValue)
            binder.add(l);
        else
            binder.add(d);

        return i;
    }

    private static double createDouble(long number, int fractionalPartLength) {
        double d = number;
        while (fractionalPartLength >= 10) {
            d /= 10000000000d;
            fractionalPartLength -= 10;
        }
        if (fractionalPartLength != 0) {
            d /= fractionalShift[fractionalPartLength];
        }
        return d;
    }

    private static void setNumber(FieldReflection setter, Object object, long l, double d, boolean floatValue) {
        switch (setter.getType()) {
            case INTEGER: {
                setter.setInteger(object, (int) l);
                break;
            }
            case LONG: {
                setter.setLong(object, l);
                break;
            }
            case BYTE: {
                setter.setByte(object, (byte) l);
                break;
            }
            case SHORT: {
                setter.setShort(object, (short) l);
                break;
            }
            case CHAR: {
                setter.setChar(object, (char) l);
                break;
            }
            case FLOAT: {
                if (floatValue)
                    setter.setFloat(object, (float) d);
                else
                    setter.setFloat(object, (float) l);
                break;
            }
            case DOUBLE: {
                if (floatValue)
                    setter.setDouble(object, d);
                else
                    setter.setDouble(object, (double) l);
                break;
            }
            case BOOLEAN: {
                setter.setBoolean(object, l != 0);
                break;
            }
            case OBJECT: {
                try {
                    if (floatValue)
                        setter.setObject(object, String.valueOf(d));
                    else
                        setter.setObject(object, String.valueOf(l));
                } catch (Exception e) {
                    throw new IllegalStateException("Can not set number " + (floatValue ? d : l) + " to " + setter);
                }
                break;
            }
        }
    }

    static int parseValue(JsonBinder binder, char[] s, int from, int to, char end) {
        char ch = s[from];

        char quote = 0;
        if (ch == '"' || ch == '\'') {
            quote = ch;
            from++;
        }

        int i = from;
        boolean escape = false;
        boolean needDecoding = false;
        if (quote == 0) {
            for (; i < to; i++) {
                ch = s[i];
                if (ch <= ' ' || ch == ',' || ch == end)
                    break;

                if (ch == '\\')
                    needDecoding = true;
            }
        } else {
            for (; i < to; i++) {
                if (escape) {
                    escape = false;
                } else {
                    ch = s[i];
                    if (ch == quote)
                        break;

                    if (ch == '\\')
                        escape = needDecoding = true;
                }
            }
        }

        int k = i;
        if (quote == 0)
            k = trimRight(s, from, k);
        else
            i++;

        if (binder == null)
            return i;

        JsonFieldSetter setter = binder.getFieldSetter();
        String value;
        int l = k - from;
        if (!needDecoding) {
            if (quote == 0) {
                if (isNull(s, from, l)) {
                    setNull(setter, binder);
                    return i;
                }
                if (isTrue(s, from, l)) {
                    setBoolean(setter, binder, true);
                    return i;
                }
                if (isFalse(s, from, l)) {
                    setBoolean(setter, binder, false);
                    return i;
                }
            }

            value = new String(s, from, l);
        } else
            value = JsonTools.unescape(s, from, k);

        setString(setter, binder, value);

        return i;
    }

    private static void setString(JsonFieldSetter setter, JsonBinder binder, String value) {
        if (setter != null)
            try {
                setter.setString(binder.getObject(), value);
            } catch (Exception e) {
                throw new IllegalStateException("Can not set '" + value + "' (" + value.getClass() + ") to " + setter);
            }
        else
            binder.add(value);
    }

    private static void setBoolean(JsonFieldSetter setter, JsonBinder binder, boolean value) {
        if (setter != null)
            setter.setBoolean(binder.getObject(), value);
        else
            binder.add(value);
    }

    private static void setNull(JsonFieldSetter setter, JsonBinder binder) {
        if (setter != null)
            setter.setObject(binder.getObject(), null);
        else
            binder.add((Object) null);
    }

    static int parseKey(JsonBinder binder, char[] s, int from, int to) {
        int i = from;
        char ch = s[i];

        char quote = 0;
        if (ch == '\'' || ch == '"') {
            quote = ch;
            i++;
            from++;
        }

        CharTree.CharTreeNode<Pair<String, JsonFieldInfo>> node = binder == null ? null : binder.getFieldsTree();

        int rightBorder;
        boolean escape = false;
        boolean needDecoding = false;
        if (quote == 0) {
            if (node != null)
                for (; i < to; i++) {
                    ch = s[i];
                    if (ch <= ' ' || ch == ':' || ch == '}') break;

                    if (ch == '\\')
                        needDecoding = true;

                    node = node.next(ch);
                    if (node == null)
                        break; // continue in next loop
                }
            for (; i < to; i++) {
                ch = s[i];
                if (ch <= ' ' || ch == ':' || ch == '}') break;

                if (ch == '\\')
                    needDecoding = true;
            }
            rightBorder = trimRight(s, from, i);
        } else {
            if (node != null)
                for (; i < to; i++) {
                    if (escape) {
                        escape = false;
                    } else {
                        ch = s[i];
                        if (ch == quote)
                            break;

                        if (ch == '\\')
                            escape = needDecoding = true;

                        node = node.next(ch);

                        if (node == null)
                            break; // continue in next loop
                    }
                }

            if (s[i] != quote)
                for (; i < to; i++) {
                    if (escape) {
                        escape = false;
                    } else {
                        ch = s[i];
                        if (ch == quote)
                            break;

                        if (ch == '\\')
                            escape = needDecoding = true;
                    }
                }

            rightBorder = i;
            i++;
        }

        i = skipSpaces(s, i, to);

        if (rightBorder == from)
            throw new IllegalStateException("key not found");

        if (s[i] != ':')
            throw new IllegalStateException("here must be key-value separator ':', but found: " + s[i]);


        if (binder == null)
            return i + 1;

        String value;
        if (!needDecoding) {
            if (node != null) {
                Pair<String, JsonFieldInfo> v = node.getValue();
                if (v != null) {
                    binder.setTemporaryKey(v);
                    return i + 1;
                }
            }

            value = new String(s, from, rightBorder - from);
        } else
            value = JsonTools.unescape(s, from, rightBorder);

        binder.setTemporaryKey(value);

        return i + 1;
    }
}
