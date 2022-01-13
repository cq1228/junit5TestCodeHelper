package com.cq.valuegenerator.impl;

import com.cq.valuegenerator.AbstractJsonValueService;

/**
 * @author 有尘
 * @date 2021/9/29
 */
public class ByteGenerator extends AbstractJsonValueService<Byte> {

    @Override
    public Byte defaultValue() {
        return 1;
    }

    @Override
    public Byte randomValue() {
        return 0;
    }
}
