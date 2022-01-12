package com.cq.common;

import java.util.HashSet;
import java.util.Set;

/**
 * @author 有尘
 * @date 2021/9/30
 */
public class MutiValuesWithClass {
    private Object object;
    private Set<String> names;

    public MutiValuesWithClass(Object object) {
        this.object = object;
        this.names = new HashSet<>();
    }

    public void addNames(String name) {
        names.add(name);
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Set<String> getNames() {
        return names;
    }

    public void setNames(Set<String> names) {
        this.names = names;
    }
}
