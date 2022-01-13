package com.cq.valuegenerator.impl;

import com.cq.valuegenerator.AbstractJsonValueService;

/**
 * @author 有尘
 * @date 2021/9/29
 */
public class BooleanGenerator extends AbstractJsonValueService<Boolean> {

    @Override
    public Boolean defaultValue() {
        return true;
    }

    @Override
    public Boolean randomValue() {
        return false;
    }
}
