package com.wizzardo.tools.evaluation;

import java.util.Map;

/**
 * Created by wizzardo on 29/08/16.
 */
public class ClosureLookup extends Expression {

    protected final String functionName;
    protected final Map<String, UserFunction> functions;
    protected final Object[] args;
    protected final ClassExpression parent;

    public ClosureLookup(String functionName, Map<String, UserFunction> functions, int argsCount, ClassExpression parent) {
        this.functionName = functionName;
        this.functions = functions;
        this.parent = parent;
        args = new Object[argsCount];
    }

    @Override
    public void setVariable(Variable v) {
    }

    @Override
    public Expression clone() {
        return this;
    }

    @Override
    public Object get(Map<String, Object> model) {
        return get(model, args);
    }

    public Object get(Map<String, Object> model, Object[] args) {
        if (model == null)
            return lookupInFunctions();

        Object localFunction = model.get(functionName);
        if (localFunction instanceof Expression) {
            return localFunction;
        }

        Object delegate = model.get("delegate");
        if (delegate instanceof ClassExpression) {
            ClosureHolder method = ((ClassExpression) delegate).findMethod(functionName, args);
            if (method != null)
                return method.get(model);
        }
        if (parent != null) {
            ClosureHolder method = parent.findMethod(functionName, args);
            if (method != null)
                return method.get(model);
        }

        localFunction = lookupInFunctions();
        if (localFunction instanceof Expression) {
            return localFunction;
        }

        return delegate;
    }

    private Object lookupInFunctions() {
        UserFunction userFunction = functions.get(functionName);
        if (userFunction == null)
            return null;

        UserFunction function = userFunction.clone();
        function.setUserFunctions(functions);
        return function;
    }

    @Override
    public String toString() {
        return functionName;
    }
}
