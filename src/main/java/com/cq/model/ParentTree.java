package com.cq.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

/**
 * 为了解决属性循环的问题，比如Tree结构，不会生成过深的结构
 * @author 有尘
 * @date 2021/11/24
 */
@Getter
@Setter
public class ParentTree {
    private ParentTree parent;
    private Map<String,ParentTree> sonList;
    private String value;

    public ParentTree getParent() {
        return parent;
    }

    public void setParent(ParentTree parent) {
        this.parent = parent;
    }

    public Map<String, ParentTree> getSonList() {
        return sonList;
    }

    public void setSonList(Map<String, ParentTree> sonList) {
        this.sonList = sonList;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        ParentTree that = (ParentTree)o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public ParentTree(String value) {
        this.value = value;
        this.sonList=new HashMap<>();
    }
}
