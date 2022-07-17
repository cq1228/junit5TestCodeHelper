package com.cq.valuegenerator.impl;

import com.cq.valuegenerator.AbstractJsonValueService;

/**
 * @author 有尘
 * @date 2021/9/29
 */
public class IntegerGenerator extends AbstractJsonValueService<Integer> {

    @Override
    public Integer defaultValue() {
        return 1;
    }

    @Override
    public Integer randomValue() {
        return valueContext.getFaker().number().randomDigit();
    }
}
