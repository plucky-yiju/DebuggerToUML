package com.plucky.debugger.capture;

import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.jdi.StackFrameProxyImpl;
import com.intellij.openapi.diagnostic.Logger;
import com.plucky.debugger.config.DebuggerToUMLSettings;
import com.plucky.debugger.model.DebugLogInfo;
import com.plucky.debugger.model.ObjectInfo;
import com.plucky.debugger.model.VariableInfo;
import com.sun.jdi.*;

import java.util.*;

/**
 * 调试日志捕获器
 * 捕获断点触发时的详细调试信息
 */
public class DebugLogCapture {

    private static final Logger LOG = Logger.getInstance(DebugLogCapture.class);

    // 敏感字段名（用于脱敏）
    private static final Set<String> SENSITIVE_FIELD_NAMES = new HashSet<>(Arrays.asList(
        "password", "pwd", "secret", "token", "key", "apikey", "apiKey",
        "accessToken", "refreshToken", "privateKey", "secretKey"
    ));

    // 最大值长度
    private static final int MAX_VALUE_LENGTH = 500;

    /**
     * 捕获调试信息
     */
    public static DebugLogInfo captureDebugInfo(StackFrameProxyImpl frameProxy) {
        if (frameProxy == null) {
            return null;
        }

        DebugLogInfo info = new DebugLogInfo();
        DebuggerToUMLSettings settings = DebuggerToUMLSettings.getInstance();

        try {
            StackFrame frame = frameProxy.getStackFrame();

            // 捕获基本信息
            info.setLocation(getLocation(frameProxy));
            info.setThreadName(frameProxy.threadProxy().name());
            info.setTimestamp(System.currentTimeMillis());
            info.setCallStackDepth(frameProxy.threadProxy().frameCount());

            // 捕获方法参数
            if (settings.showMethodParameters) {
                Map<String, VariableInfo> parameters = captureParameters(frame);
                info.setParameters(parameters);
            }

            // 捕获局部变量
            Map<String, VariableInfo> localVariables = captureLocalVariables(frame);
            info.setLocalVariables(localVariables);

            // 捕获this对象
            ObjectInfo thisObject = captureThisObject(frame);
            info.setThisObject(thisObject);

            // 捕获调用栈
            List<String> callStack = captureCallStack(frameProxy);
            info.setCallStack(callStack);

        } catch (Exception e) {
            LOG.error("Failed to capture debug info", e);
        }

        return info;
    }

    /**
     * 获取位置信息
     */
    private static String getLocation(StackFrameProxyImpl frameProxy) {
        try {
            Location location = frameProxy.location();
            String className = location.declaringType().name();
            String methodName = location.method().name();
            int lineNumber = location.lineNumber();
            return className + "." + methodName + ":" + lineNumber;
        } catch (Exception e) {
            LOG.error("Failed to get location", e);
            return "Unknown";
        }
    }

    /**
     * 捕获方法参数
     */
    private static Map<String, VariableInfo> captureParameters(StackFrame frame) {
        Map<String, VariableInfo> parameters = new LinkedHashMap<>();

        try {
            List<LocalVariable> variables = frame.visibleVariables();
            for (LocalVariable var : variables) {
                // 只捕获参数
                if (var.isArgument()) {
                    Value value = frame.getValue(var);
                    VariableInfo varInfo = new VariableInfo();
                    varInfo.setName(var.name());
                    varInfo.setType(var.typeName());
                    varInfo.setValue(formatValue(value, var.name()));
                    parameters.put(var.name(), varInfo);
                }
            }
        } catch (AbsentInformationException e) {
            LOG.debug("No parameter information available");
        } catch (Exception e) {
            LOG.error("Failed to capture parameters", e);
        }

        return parameters;
    }

    /**
     * 捕获局部变量
     */
    private static Map<String, VariableInfo> captureLocalVariables(StackFrame frame) {
        Map<String, VariableInfo> variables = new LinkedHashMap<>();

        try {
            List<LocalVariable> localVars = frame.visibleVariables();
            for (LocalVariable var : localVars) {
                // 只捕获局部变量（不包括参数）
                if (!var.isArgument()) {
                    Value value = frame.getValue(var);
                    VariableInfo varInfo = new VariableInfo();
                    varInfo.setName(var.name());
                    varInfo.setType(var.typeName());
                    varInfo.setValue(formatValue(value, var.name()));
                    variables.put(var.name(), varInfo);
                }
            }
        } catch (AbsentInformationException e) {
            LOG.debug("No local variable information available");
        } catch (Exception e) {
            LOG.error("Failed to capture local variables", e);
        }

        return variables;
    }

    /**
     * 捕获this对象
     */
    private static ObjectInfo captureThisObject(StackFrame frame) {
        try {
            ObjectReference thisObject = frame.thisObject();
            if (thisObject != null) {
                ObjectInfo objInfo = new ObjectInfo();
                objInfo.setClassName(thisObject.referenceType().name());

                // 捕获对象字段（限制数量，避免过多）
                Map<String, VariableInfo> fields = new LinkedHashMap<>();
                List<Field> allFields = thisObject.referenceType().allFields();

                // 只捕获前20个字段
                int fieldCount = 0;
                for (Field field : allFields) {
                    if (fieldCount >= 20) {
                        break;
                    }

                    try {
                        Value value = thisObject.getValue(field);
                        VariableInfo fieldInfo = new VariableInfo();
                        fieldInfo.setName(field.name());
                        fieldInfo.setType(field.typeName());
                        fieldInfo.setValue(formatValue(value, field.name()));
                        fields.put(field.name(), fieldInfo);
                        fieldCount++;
                    } catch (Exception e) {
                        LOG.debug("Failed to get field value: " + field.name(), e);
                    }
                }

                objInfo.setFields(fields);
                return objInfo;
            }
        } catch (Exception e) {
            LOG.error("Failed to capture this object", e);
        }

        return null;
    }

    /**
     * 捕获调用栈
     */
    private static List<String> captureCallStack(StackFrameProxyImpl frameProxy) {
        List<String> callStack = new ArrayList<>();

        try {
            List<StackFrameProxyImpl> frames = frameProxy.threadProxy().frames();

            // 限制调用栈深度
            int maxDepth = Math.min(frames.size(), 10);

            for (int i = 0; i < maxDepth; i++) {
                StackFrameProxyImpl frame = frames.get(i);
                Location location = frame.location();
                String className = location.declaringType().name();
                String methodName = location.method().name();
                int lineNumber = location.lineNumber();

                String frameStr = className + "." + methodName + ":" + lineNumber;
                callStack.add(frameStr);
            }
        } catch (Exception e) {
            LOG.error("Failed to capture call stack", e);
        }

        return callStack;
    }

    /**
     * 格式化值（包括脱敏处理）
     */
    private static String formatValue(Value value, String fieldName) {
        if (value == null) {
            return "null";
        }

        try {
            // 脱敏处理
            if (isSensitiveField(fieldName)) {
                return "******";
            }

            String valueStr;

            // 特殊类型处理
            if (value instanceof StringReference) {
                valueStr = "\"" + ((StringReference) value).value() + "\"";
            } else if (value instanceof ObjectReference) {
                ObjectReference objRef = (ObjectReference) value;
                // 对于对象，只显示类型和哈希码
                valueStr = objRef.referenceType().name() + "@" + objRef.uniqueID();
            } else {
                valueStr = value.toString();
            }

            // 长度限制
            if (valueStr.length() > MAX_VALUE_LENGTH) {
                return valueStr.substring(0, MAX_VALUE_LENGTH) + "...";
            }

            return valueStr;
        } catch (Exception e) {
            LOG.debug("Failed to format value", e);
            return "<error>";
        }
    }

    /**
     * 判断是否是敏感字段
     */
    private static boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }

        String lowerName = fieldName.toLowerCase();
        for (String sensitive : SENSITIVE_FIELD_NAMES) {
            if (lowerName.contains(sensitive)) {
                return true;
            }
        }
        return false;
    }
}
