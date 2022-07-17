package com.cq.valuegenerator;

import com.cq.valuegenerator.impl.ValueContext;

public abstract class AbstractJsonValueService<T> implements JsonValueService {
    protected ValueContext valueContext;

    public ValueContext getValueContext() {
        return valueContext;
    }

    public void setValueContext(ValueContext valueContext) {
        this.valueContext = valueContext;
    }
}
