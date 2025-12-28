package com.plucky.debugger.model;

/**
 * 方法调用信息
 * 表示调用栈中的一个方法调用
 */
public class MethodCallInfo {

    private String className;
    private String methodName;
    private String parameters;
    private String returnValue;
    private int depth;
    private long timestamp;
    private int lineNumber;

    public MethodCallInfo() {
    }

    public MethodCallInfo(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * 获取完整的方法签名
     */
    public String getFullSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(className).append(".").append(methodName);
        if (parameters != null && !parameters.isEmpty()) {
            sb.append("(").append(parameters).append(")");
        } else {
            sb.append("()");
        }
        if (lineNumber > 0) {
            sb.append(":").append(lineNumber);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getFullSignature();
    }
}
