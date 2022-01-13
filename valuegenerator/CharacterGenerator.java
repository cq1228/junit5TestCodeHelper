package com.cq.valuegenerator.impl;

import com.cq.valuegenerator.AbstractJsonValueService;

/**
 * @author 有尘
 * @date 2021/9/29
 */
public class CharacterGenerator extends AbstractJsonValueService<Character> {

    @Override
    public Character defaultValue() {
        return 'a';
    }

    @Override
    public Character randomValue() {
        return valueContext.getFaker().lorem().character();
    }
}
