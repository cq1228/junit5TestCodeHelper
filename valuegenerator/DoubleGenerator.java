package com.cq.valuegenerator.impl;

import com.cq.valuegenerator.AbstractJsonValueService;

/**
 * @author 有尘
 * @date 2021/9/29
 */
public class DoubleGenerator extends AbstractJsonValueService<Double> {

    @Override
    public Double defaultValue() {
        return Double.valueOf(1.0f);
    }

    @Override
    public Double randomValue() {
        return Double.valueOf(valueContext.getFaker().number().randomNumber(3, false));
    }
}
