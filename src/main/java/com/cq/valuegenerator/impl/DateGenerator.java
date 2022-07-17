package com.cq.valuegenerator.impl;

import com.cq.valuegenerator.AbstractJsonValueService;

import java.util.Date;
/**
 * @author 有尘
 * @date 2021/9/29
 */
public class DateGenerator extends AbstractJsonValueService<Date> {

    @Override
    public Date defaultValue() {
        return new Date();
    }

    @Override
    public Date randomValue() {
        return valueContext.getFaker().date().birthday();
    }
}
