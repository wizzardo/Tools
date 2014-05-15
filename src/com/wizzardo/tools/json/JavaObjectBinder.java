package com.wizzardo.tools.json;

import java.util.Map;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
class JavaObjectBinder implements ObjectBinder {
    protected Object object;
    protected Class clazz;
    protected GenericInfo genericInfo;
    protected Map<String, FieldInfo> fields;

    public JavaObjectBinder(GenericInfo genericInfo) {
        this.clazz = genericInfo.clazz;
        this.genericInfo = genericInfo;
        object = Binder.createInstance(clazz);
        fields = Binder.getFields(clazz);
    }

    @Override
    public void put(String key, Object value) {
        put(key, new JsonItem(value));
    }

    @Override
    public void put(String key, JsonItem value) {
        Binder.setValue(object, fields.get(key), value);
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public ObjectBinder getObjectBinder(String key) {
        FieldInfo info = fields.get(key);
        if (info == null)
            return null;

        if (Map.class.isAssignableFrom(info.field.getType()))
            return new JavaMapBinder(info.genericInfo);

        return new JavaObjectBinder(info.genericInfo);
    }

    @Override
    public ArrayBinder getArrayBinder(String key) {
        FieldInfo info = fields.get(key);
        if (genericInfo != null) {
            GenericInfo type = genericInfo.getGenericType(info.field);
            if (type != null)
                return new JavaArrayBinder(type);
        }

        return new JavaArrayBinder(info.genericInfo);
    }
}