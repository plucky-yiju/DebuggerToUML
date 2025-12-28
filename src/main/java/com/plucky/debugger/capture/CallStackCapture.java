package com.plucky.debugger.capture;

import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.plucky.debugger.model.CallStackInfo;
import com.plucky.debugger.model.MethodCallInfo;
import com.plucky.debugger.config.DebuggerToUMLSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * 调用栈捕获器
 * 负责从调试会话中捕获调用栈信息
 */
public class CallStackCapture {

    /**
     * 捕获当前调试会话的调用栈
     *
     * @param session 调试会话
     * @return 调用栈信息
     */
    public static CallStackInfo captureCallStack(XDebugSession session) {
        if (session == null) {
            return null;
        }

        XSuspendContext suspendContext = session.getSuspendContext();
        if (suspendContext == null) {
            return null;
        }

        XExecutionStack activeStack = suspendContext.getActiveExecutionStack();
        if (activeStack == null) {
            return null;
        }

        CallStackInfo callStackInfo = new CallStackInfo();
        DebuggerToUMLSettings settings = DebuggerToUMLSettings.getInstance();

        // 获取栈帧列表
        List<XStackFrame> frames = new ArrayList<>();
        activeStack.computeStackFrames(0, new XExecutionStack.XStackFrameContainer() {
            @Override
            public void addStackFrames(@org.jetbrains.annotations.NotNull List<? extends XStackFrame> stackFrames, boolean last) {
                frames.addAll(stackFrames);
            }

            @Override
            public void errorOccurred(@org.jetbrains.annotations.NotNull String errorMessage) {
                System.err.println("Error capturing stack frames: " + errorMessage);
            }
        });

        // 处理栈帧，转换为方法调用信息
        int depth = 0;
        for (XStackFrame frame : frames) {
            if (depth >= settings.maxCallStackDepth) {
                break;
            }

            MethodCallInfo methodInfo = extractMethodInfo(frame, depth);
            if (methodInfo != null && shouldIncludeMethod(methodInfo, settings)) {
                callStackInfo.addMethodCall(methodInfo);
                depth++;
            }
        }

        return callStackInfo;
    }

    /**
     * 从栈帧中提取方法调用信息
     *
     * @param frame 栈帧
     * @param depth 调用深度
     * @return 方法调用信息
     */
    private static MethodCallInfo extractMethodInfo(XStackFrame frame, int depth) {
        if (frame == null) {
            return null;
        }

        // 获取方法的完整描述
        String frameText = frame.toString();

        // 解析类名和方法名
        // 格式通常为: ClassName.methodName(FileName.java:lineNumber)
        MethodCallInfo info = new MethodCallInfo();
        info.setDepth(depth);
        info.setTimestamp(System.currentTimeMillis());

        // 简单解析（实际实现需要更复杂的解析逻辑）
        if (frameText.contains(".")) {
            int lastDot = frameText.lastIndexOf('.');
            int openParen = frameText.indexOf('(', lastDot);

            if (lastDot > 0 && openParen > lastDot) {
                String className = frameText.substring(0, lastDot);
                String methodName = frameText.substring(lastDot + 1, openParen);

                info.setClassName(className);
                info.setMethodName(methodName);
            } else {
                info.setClassName("Unknown");
                info.setMethodName(frameText);
            }
        } else {
            info.setClassName("Unknown");
            info.setMethodName(frameText);
        }

        return info;
    }

    /**
     * 判断是否应该包含该方法
     * 根据配置的过滤规则进行判断
     *
     * @param methodInfo 方法信息
     * @param settings 配置
     * @return 是否包含
     */
    private static boolean shouldIncludeMethod(MethodCallInfo methodInfo, DebuggerToUMLSettings settings) {
        String className = methodInfo.getClassName();

        // 如果启用了JDK类过滤
        if (settings.filterJdkClasses) {
            if (className.startsWith("java.") ||
                className.startsWith("javax.") ||
                className.startsWith("sun.") ||
                className.startsWith("jdk.")) {
                return false;
            }
        }

        // 检查自定义过滤包名
        if (settings.filteredPackages != null && !settings.filteredPackages.isEmpty()) {
            String[] packages = settings.filteredPackages.split(",");
            for (String pkg : packages) {
                if (className.startsWith(pkg.trim())) {
                    return false;
                }
            }
        }

        return true;
    }
}
