package com.plucky.debugger.capture;

import com.intellij.openapi.diagnostic.Logger;
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

    private static final Logger LOG = Logger.getInstance(CallStackCapture.class);

    /**
     * 捕获当前调试会话的调用栈（同步方法）
     *
     * @param session 调试会话
     * @return 调用栈信息
     */
    public static CallStackInfo captureCallStack(XDebugSession session) {
        if (session == null) {
            LOG.warn("Cannot capture call stack: session is null");
            return null;
        }

        XSuspendContext suspendContext = session.getSuspendContext();
        if (suspendContext == null) {
            LOG.warn("Cannot capture call stack: suspend context is null");
            return null;
        }

        XExecutionStack activeStack = suspendContext.getActiveExecutionStack();
        if (activeStack == null) {
            LOG.warn("Cannot capture call stack: active stack is null");
            return null;
        }

        CallStackInfo callStackInfo = new CallStackInfo();
        callStackInfo.setSessionName(session.getSessionName());
        DebuggerToUMLSettings settings = DebuggerToUMLSettings.getInstance();

        // 使用CountDownLatch等待异步操作完成
        final List<XStackFrame> frames = new ArrayList<>();
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

        activeStack.computeStackFrames(0, new XExecutionStack.XStackFrameContainer() {
            @Override
            public void addStackFrames(@org.jetbrains.annotations.NotNull List<? extends XStackFrame> stackFrames, boolean last) {
                frames.addAll(stackFrames);
                if (last) {
                    latch.countDown();
                }
            }

            @Override
            public void errorOccurred(@org.jetbrains.annotations.NotNull String errorMessage) {
                LOG.error("Error capturing stack frames: " + errorMessage);
                latch.countDown();
            }
        });

        // 等待异步操作完成，最多等待5秒
        try {
            boolean completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
            if (!completed) {
                LOG.warn("Timeout waiting for stack frames");
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while waiting for stack frames", e);
            return null;
        }

        LOG.info("Captured " + frames.size() + " stack frames");

        // 处理栈帧，转换为方法调用信息
        int depth = 0;
        for (XStackFrame frame : frames) {
            if (depth >= settings.maxCallStackDepth) {
                LOG.debug("Reached max call stack depth: " + settings.maxCallStackDepth);
                break;
            }

            MethodCallInfo methodInfo = extractMethodInfo(frame, depth);
            if (methodInfo != null && shouldIncludeMethod(methodInfo, settings)) {
                callStackInfo.addMethodCall(methodInfo);
                depth++;
            }
        }

        LOG.info("Processed " + depth + " methods after filtering");
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

        MethodCallInfo info = new MethodCallInfo();
        info.setDepth(depth);
        info.setTimestamp(System.currentTimeMillis());

        // 尝试使用Java调试器API获取详细信息
        if (frame instanceof com.intellij.debugger.engine.JavaStackFrame) {
            com.intellij.debugger.engine.JavaStackFrame javaFrame = (com.intellij.debugger.engine.JavaStackFrame) frame;
            try {
                com.intellij.debugger.jdi.StackFrameProxyImpl frameProxy = javaFrame.getStackFrameProxy();
                if (frameProxy != null) {
                    com.sun.jdi.Location location = frameProxy.location();
                    if (location != null) {
                        // 获取类名
                        String className = location.declaringType().name();
                        // 获取方法名
                        String methodName = location.method().name();
                        // 获取行号
                        int lineNumber = location.lineNumber();

                        info.setClassName(className);
                        info.setMethodName(methodName);
                        info.setLineNumber(lineNumber);

                        LOG.debug("Extracted: " + className + "." + methodName + ":" + lineNumber);
                        return info;
                    }
                }
            } catch (Exception e) {
                LOG.warn("Failed to extract info from JavaStackFrame: " + e.getMessage());
            }
        }

        // 回退方案：尝试从XStackFrame获取源位置信息
        com.intellij.xdebugger.XSourcePosition sourcePosition = frame.getSourcePosition();
        if (sourcePosition != null) {
            // 从源位置获取文件信息
            String fileName = sourcePosition.getFile().getName();
            int lineNumber = sourcePosition.getLine();

            // 从文件名推断类名（去掉.java扩展名）
            String className = fileName.replace(".java", "").replace(".kt", "");

            info.setClassName(className);
            info.setMethodName("method"); // 无法获取方法名时使用占位符
            info.setLineNumber(lineNumber);

            LOG.debug("Extracted from source position: " + className + ":line " + lineNumber);
        } else {
            // 最后的回退：使用toString解析
            String frameText = frame.toString();
            parseFrameText(frameText, info);
            LOG.debug("Extracted from toString: " + frameText);
        }

        return info;
    }

    /**
     * 解析栈帧文本，提取类名和方法名
     * 支持多种格式：
     * - ClassName.methodName(FileName.java:lineNumber)
     * - package.ClassName.methodName
     * - methodName
     */
    private static void parseFrameText(String frameText, MethodCallInfo info) {
        if (frameText == null || frameText.isEmpty()) {
            info.setClassName("Unknown");
            info.setMethodName("Unknown");
            return;
        }

        // 移除文件名和行号部分 (FileName.java:123)
        int parenIndex = frameText.indexOf('(');
        String methodPart = parenIndex > 0 ? frameText.substring(0, parenIndex) : frameText;

        // 查找最后一个点，分离类名和方法名
        int lastDot = methodPart.lastIndexOf('.');
        if (lastDot > 0 && lastDot < methodPart.length() - 1) {
            String className = methodPart.substring(0, lastDot);
            String methodName = methodPart.substring(lastDot + 1);

            info.setClassName(className);
            info.setMethodName(methodName);
        } else {
            // 没有点，可能只是方法名
            info.setClassName("Unknown");
            info.setMethodName(methodPart);
        }
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
