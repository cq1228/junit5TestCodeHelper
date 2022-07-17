package com.cq.valuegenerator.impl;

import com.cq.valuegenerator.AbstractJsonValueService;

/**
 * @author 有尘
 * @date 2021/9/29
 */
public class StringGenerator extends AbstractJsonValueService<String> {

    @Override
    public String defaultValue() {
        return "test";
    }

    @Override
    public String randomValue() {
        return valueContext.getFaker().name().username();
    }
}
