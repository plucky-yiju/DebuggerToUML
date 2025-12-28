package com.plucky.debugger.generator;

import com.plucky.debugger.config.DebuggerToUMLSettings;
import com.plucky.debugger.model.CallStackInfo;
import com.plucky.debugger.model.MethodCallInfo;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 文本报告生成器
 * 生成详细的文本格式调用栈报告，便于AI分析和人类阅读
 */
public class TextReportGenerator {

    private static final String INDENT = "  ";
    private static final String SEPARATOR = "=".repeat(60);

    /**
     * 生成详细的文本报告
     */
    public String generateReport(CallStackInfo callStackInfo) {
        if (callStackInfo == null || callStackInfo.isEmpty()) {
            return "调用栈为空，无法生成报告。";
        }

        StringBuilder report = new StringBuilder();
        DebuggerToUMLSettings settings = DebuggerToUMLSettings.getInstance();

        // 1. 报告头部
        appendHeader(report, callStackInfo);

        // 2. 调用链详情
        appendCallChainDetails(report, callStackInfo, settings);

        // 3. 类路径列表
        appendClassList(report, callStackInfo);

        // 4. 方法调用统计
        appendStatistics(report, callStackInfo);

        // 5. PlantUML代码
        appendPlantUMLCode(report, callStackInfo);

        return report.toString();
    }

    /**
     * 添加报告头部
     */
    private void appendHeader(StringBuilder report, CallStackInfo callStackInfo) {
        report.append(SEPARATOR).append("\n");
        report.append("调用栈分析报告\n");
        report.append(SEPARATOR).append("\n\n");

        // 基本信息
        report.append("【基本信息】\n");
        report.append("捕获时间: ").append(getCurrentTime()).append("\n");

        List<MethodCallInfo> methods = callStackInfo.getMethodCalls();
        if (!methods.isEmpty()) {
            MethodCallInfo first = methods.get(0);
            report.append("起始位置: ").append(first.getClassName())
                  .append(".").append(first.getMethodName());
            if (first.getLineNumber() > 0) {
                report.append(":").append(first.getLineNumber());
            }
            report.append("\n");
        }

        report.append("总方法数: ").append(methods.size()).append("\n");
        report.append("最大深度: ").append(calculateMaxDepth(methods)).append("\n");
        report.append("\n");
    }

    /**
     * 添加调用链详情
     */
    private void appendCallChainDetails(StringBuilder report, CallStackInfo callStackInfo,
                                       DebuggerToUMLSettings settings) {
        report.append(SEPARATOR).append("\n");
        report.append("调用链详情\n");
        report.append(SEPARATOR).append("\n\n");

        List<MethodCallInfo> methods = callStackInfo.getMethodCalls();

        for (int i = 0; i < methods.size(); i++) {
            MethodCallInfo method = methods.get(i);
            int depth = method.getDepth();

            // 缩进表示层级
            String indent = INDENT.repeat(depth);

            // 方法序号和名称
            report.append(indent).append("[").append(i + 1).append("] ");
            report.append(method.getClassName()).append(".");
            report.append(method.getMethodName());

            // 参数
            if (settings.showMethodParameters && method.getParameters() != null) {
                report.append("(").append(method.getParameters()).append(")");
            } else {
                report.append("()");
            }
            report.append("\n");

            // 位置信息
            if (method.getLineNumber() > 0) {
                report.append(indent).append(INDENT);
                report.append("位置: ");
                report.append(getSimpleClassName(method.getClassName()));
                report.append(".java:").append(method.getLineNumber());
                report.append("\n");
            }

            // 参数详情
            if (settings.showMethodParameters && method.getParameters() != null
                && !method.getParameters().isEmpty()) {
                report.append(indent).append(INDENT);
                report.append("参数: ").append(method.getParameters());
                report.append("\n");
            }

            // 返回值
            if (settings.showReturnValues && method.getReturnValue() != null) {
                report.append(indent).append(INDENT);
                report.append("返回值: ").append(method.getReturnValue());
                report.append("\n");
            }

            // 时间戳
            if (settings.showTimestamps && method.getTimestamp() > 0) {
                report.append(indent).append(INDENT);
                report.append("时间: ").append(formatTimestamp(method.getTimestamp()));
                report.append("\n");
            }

            report.append("\n");
        }
    }

    /**
     * 添加类路径列表
     */
    private void appendClassList(StringBuilder report, CallStackInfo callStackInfo) {
        report.append(SEPARATOR).append("\n");
        report.append("类路径列表\n");
        report.append(SEPARATOR).append("\n\n");

        Set<String> uniqueClasses = new LinkedHashSet<>();
        for (MethodCallInfo method : callStackInfo.getMethodCalls()) {
            uniqueClasses.add(method.getClassName());
        }

        List<String> sortedClasses = new ArrayList<>(uniqueClasses);
        Collections.sort(sortedClasses);

        int index = 1;
        for (String className : sortedClasses) {
            report.append(String.format("%2d. %s\n", index++, className));
        }

        report.append("\n");
        report.append("共 ").append(uniqueClasses.size()).append(" 个不同的类\n");
        report.append("\n");
    }

    /**
     * 添加方法调用统计
     */
    private void appendStatistics(StringBuilder report, CallStackInfo callStackInfo) {
        report.append(SEPARATOR).append("\n");
        report.append("方法调用统计\n");
        report.append(SEPARATOR).append("\n\n");

        List<MethodCallInfo> methods = callStackInfo.getMethodCalls();

        // 总方法数
        report.append("总方法数: ").append(methods.size()).append("\n");

        // 最大深度
        int maxDepth = calculateMaxDepth(methods);
        report.append("最大深度: ").append(maxDepth).append("\n");

        // 方法调用频率统计
        Map<String, Integer> methodCount = new HashMap<>();
        for (MethodCallInfo method : methods) {
            String key = method.getClassName() + "." + method.getMethodName();
            methodCount.put(key, methodCount.getOrDefault(key, 0) + 1);
        }

        // 找出调用最多的方法
        String mostCalledMethod = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : methodCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCalledMethod = entry.getKey();
            }
        }

        if (mostCalledMethod != null && maxCount > 1) {
            report.append("调用最多: ").append(mostCalledMethod);
            report.append(" (").append(maxCount).append("次)\n");
        }

        // 不同类的数量
        Set<String> uniqueClasses = new HashSet<>();
        for (MethodCallInfo method : methods) {
            uniqueClasses.add(method.getClassName());
        }
        report.append("涉及类数: ").append(uniqueClasses.size()).append("\n");

        report.append("\n");
    }

    /**
     * 添加PlantUML代码
     */
    private void appendPlantUMLCode(StringBuilder report, CallStackInfo callStackInfo) {
        report.append(SEPARATOR).append("\n");
        report.append("PlantUML时序图代码\n");
        report.append(SEPARATOR).append("\n\n");

        // 使用PlantUMLGenerator生成代码
        PlantUMLGenerator generator = new PlantUMLGenerator();
        String plantUMLCode = generator.generateDiagram(callStackInfo);

        report.append(plantUMLCode);
        report.append("\n\n");

        report.append("提示: 可以将上述PlantUML代码复制到在线编辑器查看图形化时序图\n");
        report.append("在线编辑器: https://www.plantuml.com/plantuml/uml/\n");
        report.append("\n");
    }

    /**
     * 计算最大深度
     */
    private int calculateMaxDepth(List<MethodCallInfo> methods) {
        int maxDepth = 0;
        for (MethodCallInfo method : methods) {
            if (method.getDepth() > maxDepth) {
                maxDepth = method.getDepth();
            }
        }
        return maxDepth;
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
     * 获取当前时间
     */
    private String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * 格式化时间戳
     */
    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(timestamp));
    }
}
