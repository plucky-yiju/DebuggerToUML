package com.plucky.debugger.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 调试日志信息
 * 表示断点触发时的完整调试信息
 */
public class DebugLogInfo {

    private String location;                          // 断点位置
    private String threadName;                        // 线程名称
    private long timestamp;                           // 时间戳
    private int callStackDepth;                       // 调用栈深度
    private Map<String, VariableInfo> parameters;     // 方法参数
    private Map<String, VariableInfo> localVariables; // 局部变量
    private ObjectInfo thisObject;                    // this对象
    private List<String> callStack;                   // 调用栈

    public DebugLogInfo() {
        this.parameters = new LinkedHashMap<>();
        this.localVariables = new LinkedHashMap<>();
        this.callStack = new ArrayList<>();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getCallStackDepth() {
        return callStackDepth;
    }

    public void setCallStackDepth(int callStackDepth) {
        this.callStackDepth = callStackDepth;
    }

    public Map<String, VariableInfo> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, VariableInfo> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(String name, VariableInfo parameter) {
        this.parameters.put(name, parameter);
    }

    public Map<String, VariableInfo> getLocalVariables() {
        return localVariables;
    }

    public void setLocalVariables(Map<String, VariableInfo> localVariables) {
        this.localVariables = localVariables;
    }

    public void addLocalVariable(String name, VariableInfo variable) {
        this.localVariables.put(name, variable);
    }

    public ObjectInfo getThisObject() {
        return thisObject;
    }

    public void setThisObject(ObjectInfo thisObject) {
        this.thisObject = thisObject;
    }

    public List<String> getCallStack() {
        return callStack;
    }

    public void setCallStack(List<String> callStack) {
        this.callStack = callStack;
    }

    public void addCallStackFrame(String frame) {
        this.callStack.add(frame);
    }
}
