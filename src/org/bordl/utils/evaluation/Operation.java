/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils.evaluation;

import org.bordl.utils.Range;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Moxa
 */
class Operation {

    private Expression leftPart;
    private Expression rightPart;
    private Operator operator;
    private int start, end;

    public Operation(Expression leftPart, Expression rightPart, Operator operator) {
        this.leftPart = leftPart;
        this.rightPart = rightPart;
        this.operator = operator;
    }

    public Operation(Expression leftPart, Operator operator, int start, int end) {
        this.leftPart = leftPart;
        this.operator = operator;
        this.start = start;
        this.end = end;
    }

    @Override
    public Operation clone() {
        if (rightPart == null) {
            return new Operation(leftPart.clone(), null, operator);
        } else {
            return new Operation(leftPart.clone(), rightPart.clone(), operator);
        }
    }

    @Override
    public String toString() {
        return (leftPart == null ? "" : leftPart + " ") + getOperator().text + (rightPart == null ? "" : " " + rightPart);
    }

    public Expression getLeftPart() {
        return leftPart;
    }

    public void setLeftPart(Expression leftPart) {
        this.leftPart = leftPart;
    }

    public Expression getRightPart() {
        return rightPart;
    }

    public void setRightPart(Expression rightPart) {
        this.rightPart = rightPart;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Expression leftPart() {
        return leftPart;
    }

    public void leftPart(Expression leftPart) {
        this.leftPart = leftPart;
    }

    public Expression rightPart() {
        return rightPart;
    }

    public void rightPart(Expression rightPart) {
        this.rightPart = rightPart;
    }

    public Operator operator() {
        return operator;
    }

    public void operator(Operator operator) {
        this.operator = operator;
    }

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int end() {
        return end;
    }

    public int start() {
        return start;
    }

    public void end(int end) {
        this.end = end;
    }

    public void start(int start) {
        this.start = start;
    }

    public boolean isFull() {
        switch (operator.requirement) {
            case ANY:
                return leftPart != null || rightPart != null;
            case RIGHR:
                return rightPart != null;
            case LEFT:
                return leftPart != null;
            case BOTH:
                return leftPart != null && rightPart != null;
        }
        return false;
    }

    public Object evaluate(Map<String, Object> model) throws Exception {
        //System.out.println("execute: " + this);
        Object ob1 = null;
        Object ob2 = null;
        if (leftPart != null
                && operator != Operator.EQUAL
                && operator != Operator.PLUS2
                && operator != Operator.MINUS2
                && operator != Operator.PLUS_EQUAL
                && operator != Operator.MINUS_EQUAL
                && operator != Operator.DIVIDE_EQUAL
                && operator != Operator.MULTIPLY_EQUAL
                ) {
            ob1 = leftPart.get(model);
        }
        if (rightPart != null
                && operator != Operator.TERNARY
                && operator != Operator.AND2
                && operator != Operator.OR2
                && operator != Operator.EQUAL
                && operator != Operator.PLUS2
                && operator != Operator.MINUS2
                && operator != Operator.PLUS_EQUAL
                && operator != Operator.MINUS_EQUAL
                && operator != Operator.DIVIDE_EQUAL
                && operator != Operator.MULTIPLY_EQUAL
                ) {
            ob2 = rightPart.get(model);
        }
        //System.out.println(model);
        //System.out.println(ob1 + "\t" + operator + "\t" + ob2);
        switch (operator) {
            case PLUS: {
                return plus(ob1, ob2);
            }
            case MINUS: {
                return minus(ob1, ob2);
            }
            case MULTIPLY: {
                return multiply(ob1, ob2);
            }
            case DIVIDE: {
                return divide(ob1, ob2);
            }
            case PLUS_EQUAL:
            case MINUS_EQUAL:
            case MULTIPLY_EQUAL:
            case DIVIDE_EQUAL:
            case EQUAL:
            case PLUS2:
            case MINUS2: {
                return set(leftPart, rightPart, model, operator);
            }
            case NOT: {
                return !(Boolean) ob2;
            }
            case GREATE: {
                return gt(ob1, ob2);
            }
            case LOWER: {
                return lt(ob1, ob2);
            }
            case GREATE_EQUAL: {
                return gte(ob1, ob2);
            }
            case LOWER_EQUAL: {
                return lte(ob1, ob2);
            }
            case EQUAL2: {
                return e(ob1, ob2);
            }
            case NOT_EQUAL: {
                return ne(ob1, ob2);
            }
            case TERNARY: {
                //System.out.println("left: " + leftPart);
                //System.out.println("right: " + rightPart);
                if ((Boolean) ob1) {
                    return rightPart.operation.leftPart.get(model);
                } else {
                    return rightPart.operation.rightPart.get(model);
                }
            }
            case OR2: {
                if ((Boolean) ob1) {
                    return true;
                } else {
                    return rightPart().get(model);
                }
            }
            case OR: {
                if ((Boolean) ob1) {
//                    rightPart().get(model); already done
                    return true;
                } else {
                    return ob2;
                }
            }
            case AND2: {
                if (!(Boolean) ob1) {
                    return false;
                } else {
                    return rightPart().get(model);
                }
            }
            case AND: {
                if (!(Boolean) ob1) {
//                    rightPart().get(model); already done
                    return false;
                } else {
                    return ob2;
                }
            }
            case APPEND: {
                return append(ob1, ob2);
            }
            case GET: {
                return get(ob1, ob2);
            }
            case RANGE: {
                return createRange(ob1, ob2);
            }
        }

        throw new UnsupportedOperationException("Not yet implemented:" + this.operator);
    }

    private static Range createRange(Object ob1, Object ob2) {
        if (ob1 == null || ob2 == null) {
            throw new NullPointerException("can not append to null");
        }
        if (ob1 instanceof Number && ob2 instanceof Number) {
            return new Range(((Number) ob1).intValue(), ((Number) ob2).intValue());
        }
        return null;
    }

    private static Object get(Object ob1, Object ob2) {
        if (ob1 == null) {
            throw new NullPointerException("can not append to null");
        }
        if (ob1 instanceof Map) {
            return ((Map) ob1).get(ob2);
        }
        if (ob1 instanceof List && ob2 instanceof Number) {
            List l = (List) ob1;
            int i = ((Number) ob2).intValue();
            if (i >= l.size())
                return null;
            else
                return (l).get(i);
        }
        if (ob1.getClass().getName().startsWith("[") && ob2 instanceof Number) {
            return Array.get(ob1, ((Number) ob2).intValue());
        }
        return null;
    }

    private static Object append(Object ob1, Object ob2) {
        if (ob1 == null) {
            throw new NullPointerException("can not append to null");
        }
        if (ob1 instanceof Collection) {
            ((Collection) ob1).add(ob2);
            return ob1;
        }
        if (ob1 instanceof StringBuilder) {
            ((StringBuilder) ob1).append(ob2);
            return ob1;
        }
        return null;
    }

    private static Object set(Expression leftPart, Expression rightPart, Map<String, Object> model) throws Exception {
        return set(leftPart, rightPart, model, null);
    }

    private static Object set(Expression leftPart, Expression rightPart, Map<String, Object> model, Operator operator) throws Exception {
        //left part not yet executed
        Object ob1 = null;
        Object ob2 = null;
        if (leftPart != null) {
            if (rightPart != null) {
                ob2 = rightPart.get(model);
            }
            if (leftPart.function != null) {
                ob1 = leftPart.function.getThatObject().get(model);
            }
            if (leftPart.function != null
                    && leftPart.function.getFieldName() != null
                    && ob1 instanceof Map) {

                if (operator != null) {
                    String key = leftPart.function.getFieldName();
                    Map m = (Map) ob1;
                    return mapSetAndReturn(key, m, m.get(key), ob2, operator);
                }

                ((Map) ob1).put(leftPart.function.getFieldName(), ob2);
                return ob2;
            }
            if (leftPart.function != null
                    && leftPart.function.prepareField(ob1) != null) {

                if (operator != null) {
                    Field key = leftPart.function.getField();
                    return fieldSetAndReturn(ob1, key, key.get(ob1), ob2, operator);
                }

                leftPart.function.getField().set(ob1, ob2);
                return ob2;
            }
            if (leftPart.operation != null
                    && leftPart.operation.operator() == Operator.GET) {
                ob1 = leftPart.operation.leftPart().get(model);
                if (ob1 instanceof Map) {

                    if (operator != null) {
                        Object key = leftPart.operation.rightPart().get(model);
                        Map m = (Map) ob1;
                        return mapSetAndReturn(key, m, m.get(key), ob2, operator);
                    }

                    ((Map) ob1).put(leftPart.operation.rightPart().get(model), ob2);
                    return ob2;
                }
                int index = ((Number) leftPart.operation.rightPart().get(model)).intValue();
                if (ob1 instanceof List) {
                    List l = (List) ob1;
                    while (index >= l.size()) {
                        l.add(null);
                    }
                    l.set(index, ob2);
                    return ob2;
                }
                if (ob1.getClass().getName().startsWith("[")) {
                    Array.set(ob1, index, ob2);
                    return ob2;
                }
            }
        }
        if (operator != null) {
            if (rightPart != null && rightPart.function != null) {
                Object thatObject = rightPart.function.getThatObject().get(model);
                if (rightPart.function.getFieldName() != null && thatObject instanceof Map) {
                    String key = rightPart.function.getFieldName();
                    Map m = (Map) thatObject;
                    return mapSetAndReturn(key, m, ob1, m.get(key), operator);
                }

                if (rightPart.function.prepareField(thatObject) != null) {
                    Field key = rightPart.function.getField();
                    return fieldSetAndReturn(thatObject, key, null, key.get(thatObject), operator);
                }
            } else if (leftPart != null) {
                ob1 = leftPart.get(model);
            } else if (rightPart != null) {
                ob2 = rightPart.get(model);
            }
            return mapSetAndReturn(leftPart != null ? leftPart.exp() : rightPart.exp(), model, ob1, ob2, operator);
        }
        return null;
    }

    private static Object mapSetAndReturn(Object key, Map model, Object left, Object right, Operator operator) {
        switch (operator) {
            case PLUS2: {
                //pre-increment
                if (right != null) {
                    Object ob = increment(right);
                    model.put(key, ob);
                    return ob;
                }
                //post-increment
                if (left != null) {
                    Object r = increment(left);
                    model.put(key, r);
                    return left;
                }
            }
            case MINUS2: {
                //pre-decrement
                if (right != null) {
                    Object ob = decrement(right);
                    model.put(key, ob);
                    return ob;
                }
                //post-decrement
                if (left != null) {
                    Object r = decrement(left);
                    model.put(key, r);
                    return left;
                }
            }
            case PLUS_EQUAL: {
                Object r = plus(left, right);
                model.put(key, r);
                return r;
            }
            case MINUS_EQUAL: {
                Object r = minus(left, right);
                model.put(key, r);
                return r;
            }
            case MULTIPLY_EQUAL: {
                Object r = multiply(left, right);
                model.put(key, r);
                return r;
            }
            case DIVIDE_EQUAL: {
                Object r = divide(left, right);
                model.put(key, r);
                return r;
            }
            case EQUAL: {
                model.put(key, right);
                return right;
            }
        }
        throw new UnsupportedOperationException("Not yet implemented:" + operator);
    }

    private static Object fieldSetAndReturn(Object thatObject, Field field, Object left, Object right, Operator operator) throws IllegalAccessException {
        switch (operator) {
            case PLUS2: {
                //pre-increment
                if (right != null) {
                    Object ob = increment(right);
                    field.set(thatObject, ob);
                    return ob;
                }
                //post-increment
                if (left != null) {
                    Object r = increment(left);
                    field.set(thatObject, r);
                    return left;
                }
            }
            case MINUS2: {
                //pre-decrement
                if (right != null) {
                    Object ob = decrement(right);
                    field.set(thatObject, ob);
                    return ob;
                }
                //post-decrement
                if (left != null) {
                    Object r = decrement(left);
                    field.set(thatObject, r);
                    return left;
                }
            }
            case PLUS_EQUAL: {
                Object r = plus(left, right);
                field.set(thatObject, r);
                return r;
            }
            case MINUS_EQUAL: {
                Object r = minus(left, right);
                field.set(thatObject, r);
                return r;
            }
            case MULTIPLY_EQUAL: {
                Object r = multiply(left, right);
                field.set(thatObject, r);
                return r;
            }
            case DIVIDE_EQUAL: {
                Object r = divide(left, right);
                field.set(thatObject, r);
                return r;
            }
            case EQUAL: {
                field.set(thatObject, right);
                return right;
            }
        }
        throw new UnsupportedOperationException("Not yet implemented:" + operator);
    }

    private static Object plus(Object ob1, Object ob2) {
        if (ob1 instanceof Number && ob2 instanceof Number) {
            if (ob1 instanceof Double || ob2 instanceof Double) {
                return ((Number) ob1).doubleValue() + ((Number) ob2).doubleValue();
            }
            if (ob1 instanceof Float || ob2 instanceof Float) {
                return ((Number) ob1).floatValue() + ((Number) ob2).floatValue();
            }
            if (ob1 instanceof Long || ob2 instanceof Long) {
                return ((Number) ob1).longValue() + ((Number) ob2).longValue();
            }
            if (ob1 instanceof Integer || ob2 instanceof Integer) {
                return ((Number) ob1).intValue() + ((Number) ob2).intValue();
            }
            if (ob1 instanceof Short || ob2 instanceof Short) {
                return ((Number) ob1).shortValue() + ((Number) ob2).shortValue();
            }
            if (ob1 instanceof Byte || ob2 instanceof Byte) {
                return ((Number) ob1).byteValue() + ((Number) ob2).byteValue();
            }
            return ((Number) ob1).doubleValue() + ((Number) ob2).doubleValue();
        } else {
            if (ob1 instanceof Collection) {
                if (ob2 instanceof Collection) {
                    ((Collection) ob1).addAll((Collection) ob2);
                    return ob1;
                } else {
                    ((Collection) ob1).add(ob2);
                    return ob1;
                }
            }
            return String.valueOf(ob1) + String.valueOf(ob2);
        }
    }

    private static Object minus(Object ob1, Object ob2) {
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return (ob1 != null ? ((Number) ob1).doubleValue() : 0) - ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return (ob1 != null ? ((Number) ob1).floatValue() : 0) - ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return (ob1 != null ? ((Number) ob1).longValue() : 0) - ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return (ob1 != null ? ((Number) ob1).intValue() : 0) - ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return (ob1 != null ? ((Number) ob1).shortValue() : 0) - ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return (ob1 != null ? ((Number) ob1).byteValue() : 0) - ((Number) ob2).byteValue();
        }
        return (ob1 != null ? ((Number) ob1).doubleValue() : 0) - ((Number) ob2).doubleValue();
    }

    private static Object gt(Object ob1, Object ob2) {
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() > ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() > ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() > ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() > ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() > ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() > ((Number) ob2).byteValue();
        }
        return ((Number) ob1).doubleValue() > ((Number) ob2).doubleValue();
    }

    private static Object lt(Object ob1, Object ob2) {
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() < ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() < ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() < ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() < ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() < ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() < ((Number) ob2).byteValue();
        }
        return ((Number) ob1).doubleValue() < ((Number) ob2).doubleValue();
    }

    private static Object gte(Object ob1, Object ob2) {
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() >= ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() >= ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() >= ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() >= ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() >= ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() >= ((Number) ob2).byteValue();
        }
        return ((Number) ob1).doubleValue() >= ((Number) ob2).doubleValue();
    }

    private static Object lte(Object ob1, Object ob2) {
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() <= ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() <= ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() <= ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() <= ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() <= ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() <= ((Number) ob2).byteValue();
        }
        return ((Number) ob1).doubleValue() <= ((Number) ob2).doubleValue();
    }

    private static Object e(Object ob1, Object ob2) {
        if (ob1 == null || ob2 == null) {
            return ob1 == ob2;
        }
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() == ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() == ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() == ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() == ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() == ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() == ((Number) ob2).byteValue();
        }
        return ob1 == ob2;
    }

    private static Object ne(Object ob1, Object ob2) {
        if (ob1 == null || ob2 == null) {
            return ob1 != ob2;
        }
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() != ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() != ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() != ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() != ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() != ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() != ((Number) ob2).byteValue();
        }
        return ob1 != ob2;
    }

    private static Object multiply(Object ob1, Object ob2) {
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() * ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() * ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() * ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() * ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() * ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() * ((Number) ob2).byteValue();
        }
        return ((Number) ob1).doubleValue() * ((Number) ob2).doubleValue();
    }

    private static Object divide(Object ob1, Object ob2) {
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() / ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() / ((Number) ob2).floatValue();
        }
        if (EvalUtils.defaultEvaluatingStrategy == EvalUtils.EvaluatingStrategy.DOUBLE) {
            return ((Number) ob1).doubleValue() / ((Number) ob2).doubleValue();
        }
        if (EvalUtils.defaultEvaluatingStrategy == EvalUtils.EvaluatingStrategy.FLOAT) {
            return ((Number) ob1).floatValue() / ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() / ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() / ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() / ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() / ((Number) ob2).byteValue();
        }
        return ((Number) ob1).doubleValue() / ((Number) ob2).doubleValue();
    }

    private static Object increment(Object ob1) {
        if (ob1 instanceof Double) {
            return ((Number) ob1).doubleValue() + 1;
        }
        if (ob1 instanceof Float) {
            return ((Number) ob1).floatValue() + 1;
        }
        if (ob1 instanceof Long) {
            return ((Number) ob1).longValue() + 1;
        }
        if (ob1 instanceof Short) {
            return ((Number) ob1).shortValue() + 1;
        }
        if (ob1 instanceof Integer) {
            return ((Number) ob1).intValue() + 1;
        }
        if (ob1 instanceof Byte) {
            return ((Number) ob1).byteValue() + 1;
        }
        return ((Number) ob1).doubleValue() + 1;
    }

    private static Object decrement(Object ob1) {
        if (ob1 instanceof Double) {
            return ((Number) ob1).doubleValue() - 1;
        }
        if (ob1 instanceof Float) {
            return ((Number) ob1).floatValue() - 1;
        }
        if (ob1 instanceof Long) {
            return ((Number) ob1).longValue() - 1;
        }
        if (ob1 instanceof Integer) {
            return ((Number) ob1).intValue() - 1;
        }
        if (ob1 instanceof Short) {
            return ((Number) ob1).shortValue() - 1;
        }
        if (ob1 instanceof Byte) {
            return ((Number) ob1).byteValue() - 1;
        }
        return ((Number) ob1).doubleValue() - 1;
    }
}