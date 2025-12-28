package com.plucky.debugger.generator;

import com.plucky.debugger.config.DebuggerToUMLSettings;
import com.plucky.debugger.model.CallStackInfo;
import com.plucky.debugger.model.MethodCallInfo;

import java.util.List;

/**
 * PlantUML时序图生成器
 * 将调用栈信息转换为PlantUML格式的时序图
 */
public class PlantUMLGenerator implements DiagramGenerator {

    @Override
    public String generateDiagram(CallStackInfo callStackInfo) {
        if (callStackInfo == null || callStackInfo.isEmpty()) {
            return "";
        }

        StringBuilder uml = new StringBuilder();
        DebuggerToUMLSettings settings = DebuggerToUMLSettings.getInstance();

        // PlantUML头部
        uml.append("@startuml\n");
        uml.append("title 调用栈时序图\n\n");

        // 设置样式
        uml.append("skinparam sequenceArrowThickness 2\n");
        uml.append("skinparam roundcorner 20\n");
        uml.append("skinparam maxmessagesize 60\n");
        uml.append("skinparam sequenceParticipant underline\n\n");

        List<MethodCallInfo> methodCalls = callStackInfo.getMethodCalls();

        // 声明参与者（类）
        for (int i = 0; i < methodCalls.size(); i++) {
            MethodCallInfo method = methodCalls.get(i);
            String participantName = getParticipantName(method, i);
            uml.append("participant \"").append(method.getClassName()).append("\" as ").append(participantName).append("\n");
        }
        uml.append("\n");

        // 生成调用序列
        for (int i = 0; i < methodCalls.size() - 1; i++) {
            MethodCallInfo caller = methodCalls.get(i);
            MethodCallInfo callee = methodCalls.get(i + 1);

            String callerName = getParticipantName(caller, i);
            String calleeName = getParticipantName(callee, i + 1);

            // 生成调用箭头
            uml.append(callerName).append(" -> ").append(calleeName).append(" : ");
            uml.append(callee.getMethodName());

            // 如果启用了显示参数
            if (settings.showMethodParameters && callee.getParameters() != null) {
                uml.append("(").append(callee.getParameters()).append(")");
            } else {
                uml.append("()");
            }

            uml.append("\n");

            // 如果启用了显示时间戳
            if (settings.showTimestamps) {
                uml.append("note right: ").append(callee.getTimestamp()).append("\n");
            }

            // 激活生命线
            uml.append("activate ").append(calleeName).append("\n");
        }

        // 生成返回序列
        for (int i = methodCalls.size() - 1; i > 0; i--) {
            MethodCallInfo caller = methodCalls.get(i - 1);
            MethodCallInfo callee = methodCalls.get(i);

            String callerName = getParticipantName(caller, i - 1);
            String calleeName = getParticipantName(callee, i);

            // 如果启用了显示返回值
            if (settings.showReturnValues && callee.getReturnValue() != null) {
                uml.append(calleeName).append(" --> ").append(callerName);
                uml.append(" : ").append(callee.getReturnValue()).append("\n");
            } else {
                uml.append(calleeName).append(" --> ").append(callerName).append("\n");
            }

            // 停用生命线
            uml.append("deactivate ").append(calleeName).append("\n");
        }

        // PlantUML尾部
        uml.append("\n@enduml");

        return uml.toString();
    }

    /**
     * 获取参与者名称（用于PlantUML中的别名）
     */
    private String getParticipantName(MethodCallInfo method, int index) {
        // 使用简化的类名作为参与者名称
        String className = method.getClassName();
        int lastDot = className.lastIndexOf('.');
        String simpleName = lastDot > 0 ? className.substring(lastDot + 1) : className;
        return simpleName + "_" + index;
    }

    @Override
    public String getFormat() {
        return "plantuml";
    }
}
