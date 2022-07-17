package com.cq.valuegenerator.impl;

import com.cq.valuegenerator.AbstractJsonValueService;

/**
 * @author 有尘
 * @date 2021/9/29
 */
public class FloatGenerator extends AbstractJsonValueService<Float> {

    @Override
    public Float defaultValue() {
        return Float.valueOf(1.0f);
    }

    @Override
    public Float randomValue() {
        return Float.valueOf(valueContext.getFaker().number().randomNumber(3, false));
    }
}
