package org.jmock.internal.perf;

import java.util.HashMap;
import java.util.Map;

public class Param {
    private Map<String, Object> map = new HashMap<>();

    public void addParameter(String k, Object v) {
        map.put(k, v);
    }

    public Object getParameter(String k) {
        return map.get(k);
    }
}