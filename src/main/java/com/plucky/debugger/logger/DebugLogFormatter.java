package com.plucky.debugger.logger;

import com.plucky.debugger.model.DebugLogInfo;
import com.plucky.debugger.model.ObjectInfo;
import com.plucky.debugger.model.VariableInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 调试日志格式化器
 * 将调试信息格式化为易读的日志文本
 */
public class DebugLogFormatter {

    private static final String SEPARATOR = "=".repeat(60);

    /**
     * 格式化调试信息为日志文本
     */
    public static String format(DebugLogInfo info) {
        if (info == null) {
            return "";
        }

        StringBuilder log = new StringBuilder();

        // 头部
        log.append(SEPARATOR).append("\n");
        log.append("[断点触发] ").append(formatTimestamp(info.getTimestamp())).append("\n");
        log.append(SEPARATOR).append("\n");
        log.append("位置: ").append(info.getLocation()).append("\n");
        log.append("线程: ").append(info.getThreadName()).append("\n");
        log.append("调用栈深度: ").append(info.getCallStackDepth()).append("\n");
        log.append("\n");

        // 方法参数
        if (!info.getParameters().isEmpty()) {
            log.append("【方法参数】\n");
            for (VariableInfo var : info.getParameters().values()) {
                log.append("  ").append(var.getName());
                log.append(" (").append(var.getType()).append(")");
                log.append(" = ").append(var.getValue()).append("\n");
            }
            log.append("\n");
        }

        // 局部变量
        if (!info.getLocalVariables().isEmpty()) {
            log.append("【局部变量】\n");
            for (VariableInfo var : info.getLocalVariables().values()) {
                log.append("  ").append(var.getName());
                log.append(" (").append(var.getType()).append(")");
                log.append(" = ").append(var.getValue()).append("\n");
            }
            log.append("\n");
        }

        // this对象
        if (info.getThisObject() != null) {
            ObjectInfo obj = info.getThisObject();
            log.append("【this对象】\n");
            log.append("  类: ").append(obj.getClassName()).append("\n");

            if (!obj.getFields().isEmpty()) {
                log.append("  字段:\n");
                for (VariableInfo field : obj.getFields().values()) {
                    log.append("    ").append(field.getName());
                    log.append(" (").append(field.getType()).append(")");
                    log.append(" = ").append(field.getValue()).append("\n");
                }
            }
            log.append("\n");
        }

        // 调用栈
        if (!info.getCallStack().isEmpty()) {
            log.append("【调用栈】\n");
            for (int i = 0; i < info.getCallStack().size(); i++) {
                String frame = info.getCallStack().get(i);
                log.append("  [").append(i + 1).append("] ").append(frame);
                if (i == 0) {
                    log.append("  ← 当前位置");
                }
                log.append("\n");
            }
            log.append("\n");
        }

        log.append(SEPARATOR).append("\n\n");

        return log.toString();
    }

    /**
     * 格式化时间戳
     */
    private static String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(timestamp));
    }
}
