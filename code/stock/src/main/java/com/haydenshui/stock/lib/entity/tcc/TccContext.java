package com.haydenshui.stock.lib.entity.tcc;

import java.util.HashMap;
import java.util.Map;

public class TccContext {
    
<<<<<<< HEAD
    private final String ctxXid;
    private final Map<String, Object> context = new HashMap<>();

    public TccContext(String xid) {
        this.ctxXid = xid;
    }

    public String getCtxXid() {
        return ctxXid;
=======
    private final String xid;
    private final Map<String, Object> context = new HashMap<>();

    public TccContext(String xid) {
        this.xid = xid;
    }

    public String getXid() {
        return xid;
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
    }

    public void put(String key, Object value) {
        context.put(key, value);
    }

    public Object get(String key) {
        return context.get(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        return clazz.cast(context.get(key));
    }

    public Map<String, Object> getContext() {
        return context;
    }
}
