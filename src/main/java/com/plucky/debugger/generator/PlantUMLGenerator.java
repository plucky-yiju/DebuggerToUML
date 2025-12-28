package com.plucky.debugger.generator;

import com.plucky.debugger.config.DebuggerToUMLSettings;
import com.plucky.debugger.model.CallStackInfo;
import com.plucky.debugger.model.MethodCallInfo;

import java.util.*;

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
        uml.append("!pragma layout smetana\n");  // 确保离线渲染
        uml.append("title 调用栈时序图\n\n");

        // 设置样式
        uml.append("skinparam sequenceArrowThickness 2\n");
        uml.append("skinparam roundcorner 20\n");
        uml.append("skinparam maxmessagesize 60\n");
        uml.append("skinparam sequenceParticipant underline\n\n");

        List<MethodCallInfo> methodCalls = callStackInfo.getMethodCalls();

        // 收集所有唯一的类（参与者）
        Map<String, String> participants = new LinkedHashMap<>();
        for (MethodCallInfo method : methodCalls) {
            String className = method.getClassName();
            if (!participants.containsKey(className)) {
                String simpleName = getSimpleClassName(className);
                participants.put(className, simpleName);
            }
        }

        // 声明参与者（类）
        for (Map.Entry<String, String> entry : participants.entrySet()) {
            String fullName = entry.getKey();
            String simpleName = entry.getValue();
            uml.append("participant \"").append(simpleName).append("\" as ").append(getParticipantAlias(fullName)).append("\n");
        }
        uml.append("\n");

        // 检测循环调用
        List<LoopInfo> loops = detectLoops(methodCalls);

        // 生成调用序列
        int currentLoopIndex = 0;
        for (int i = 0; i < methodCalls.size() - 1; i++) {
            MethodCallInfo caller = methodCalls.get(i);
            MethodCallInfo callee = methodCalls.get(i + 1);

            // 检查是否是循环的开始
            if (currentLoopIndex < loops.size() && loops.get(currentLoopIndex).startIndex == i) {
                LoopInfo loop = loops.get(currentLoopIndex);
                uml.append("loop ").append(loop.count).append(" 次循环调用\n");
            }

            String callerAlias = getParticipantAlias(caller.getClassName());
            String calleeAlias = getParticipantAlias(callee.getClassName());

            // 生成调用箭头
            uml.append(callerAlias).append(" -> ").append(calleeAlias).append(" : ");
            uml.append(callee.getMethodName());

            // 如果启用了显示参数
            if (settings.showMethodParameters && callee.getParameters() != null) {
                uml.append("(").append(callee.getParameters()).append(")");
            } else {
                uml.append("()");
            }

            // 添加行号信息
            if (callee.getLineNumber() > 0) {
                uml.append(":").append(callee.getLineNumber());
            }

            uml.append("\n");

            // 如果启用了显示时间戳
            if (settings.showTimestamps) {
                uml.append("note right: ").append(callee.getTimestamp()).append("\n");
            }

            // 激活生命线
            uml.append("activate ").append(calleeAlias).append("\n");

            // 检查是否是循环的结束
            if (currentLoopIndex < loops.size() && loops.get(currentLoopIndex).endIndex == i) {
                uml.append("end\n");
                currentLoopIndex++;
            }
        }

        // 生成返回序列
        for (int i = methodCalls.size() - 1; i > 0; i--) {
            MethodCallInfo caller = methodCalls.get(i - 1);
            MethodCallInfo callee = methodCalls.get(i);

            String callerAlias = getParticipantAlias(caller.getClassName());
            String calleeAlias = getParticipantAlias(callee.getClassName());

            // 如果启用了显示返回值
            if (settings.showReturnValues && callee.getReturnValue() != null) {
                uml.append(calleeAlias).append(" --> ").append(callerAlias);
                uml.append(" : ").append(callee.getReturnValue()).append("\n");
            } else {
                uml.append(calleeAlias).append(" --> ").append(callerAlias).append("\n");
            }

            // 停用生命线
            uml.append("deactivate ").append(calleeAlias).append("\n");
        }

        // PlantUML尾部
        uml.append("\n@enduml");

        return uml.toString();
    }

    /**
     * 检测循环调用
     * 识别相同方法的连续重复调用
     */
    private List<LoopInfo> detectLoops(List<MethodCallInfo> methodCalls) {
        List<LoopInfo> loops = new ArrayList<>();

        for (int i = 0; i < methodCalls.size() - 2; i++) {
            MethodCallInfo current = methodCalls.get(i);
            MethodCallInfo next = methodCalls.get(i + 1);

            // 检查是否是相同的方法调用
            if (isSameMethod(current, next)) {
                int count = 1;
                int endIndex = i;

                // 统计连续相同调用的次数
                for (int j = i + 1; j < methodCalls.size() - 1; j++) {
                    if (isSameMethod(methodCalls.get(j), methodCalls.get(j + 1))) {
                        count++;
                        endIndex = j;
                    } else {
                        break;
                    }
                }

                // 如果有至少2次重复，标记为循环
                if (count >= 2) {
                    loops.add(new LoopInfo(i, endIndex, count));
                    i = endIndex; // 跳过已处理的循环
                }
            }
        }

        return loops;
    }

    /**
     * 判断两个方法调用是否相同
     */
    private boolean isSameMethod(MethodCallInfo m1, MethodCallInfo m2) {
        return m1.getClassName().equals(m2.getClassName()) &&
               m1.getMethodName().equals(m2.getMethodName());
    }

    /**
     * 获取简化的类名
     */
    private String getSimpleClassName(String fullClassName) {
        if (fullClassName == null) {
            return "Unknown";
        }
        int lastDot = fullClassName.lastIndexOf('.');
        return lastDot > 0 ? fullClassName.substring(lastDot + 1) : fullClassName;
    }

    /**
     * 获取参与者别名（用于PlantUML中的引用）
     */
    private String getParticipantAlias(String className) {
        // 使用类名的哈希值作为别名，确保唯一性
        return "C" + Math.abs(className.hashCode());
    }

    @Override
    public String getFormat() {
        return "plantuml";
    }

    /**
     * 循环信息
     */
    private static class LoopInfo {
        int startIndex;
        int endIndex;
        int count;

        LoopInfo(int startIndex, int endIndex, int count) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.count = count;
        }
    }
}
