package com.cq.valuegenerator.impl;

import com.cq.valuegenerator.AbstractJsonValueService;

/**
 * @author 有尘
 * @date 2021/9/29
 */
public class LongGenerator extends AbstractJsonValueService<Long> {

    @Override
    public Long defaultValue() {
        return 1L;
    }

    @Override
    public Long randomValue() {
        return Long.valueOf(valueContext.getFaker().number().randomNumber());
    }
}
