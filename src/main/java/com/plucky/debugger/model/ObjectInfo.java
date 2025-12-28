package com.plucky.debugger.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 对象信息
 * 表示一个对象的详细信息（包括字段）
 */
public class ObjectInfo {

    private String className;
    private Map<String, VariableInfo> fields;

    public ObjectInfo() {
        this.fields = new LinkedHashMap<>();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<String, VariableInfo> getFields() {
        return fields;
    }

    public void setFields(Map<String, VariableInfo> fields) {
        this.fields = fields;
    }

    public void addField(String name, VariableInfo field) {
        this.fields.put(name, field);
    }

    @Override
    public String toString() {
        return className + " {" + fields.size() + " fields}";
    }
}
