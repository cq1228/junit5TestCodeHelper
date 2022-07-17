package com.cq.valuegenerator;

public interface JsonValueService<T> {
    T defaultValue();
    T randomValue();
}
