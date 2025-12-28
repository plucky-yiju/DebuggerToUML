package com.plucky.debugger.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 调用栈信息
 * 包含完整的方法调用链
 */
public class CallStackInfo {

    private List<MethodCallInfo> methodCalls;
    private long captureTime;
    private String sessionName;

    public CallStackInfo() {
        this.methodCalls = new ArrayList<>();
        this.captureTime = System.currentTimeMillis();
    }

    public void addMethodCall(MethodCallInfo methodCall) {
        this.methodCalls.add(methodCall);
    }

    public List<MethodCallInfo> getMethodCalls() {
        return methodCalls;
    }

    public void setMethodCalls(List<MethodCallInfo> methodCalls) {
        this.methodCalls = methodCalls;
    }

    public long getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(long captureTime) {
        this.captureTime = captureTime;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public int getDepth() {
        return methodCalls.size();
    }

    public boolean isEmpty() {
        return methodCalls.isEmpty();
    }
}
