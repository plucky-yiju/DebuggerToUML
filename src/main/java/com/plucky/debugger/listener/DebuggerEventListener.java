package com.plucky.debugger.listener;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManagerListener;
import com.plucky.debugger.capture.CallStackCapture;
import com.plucky.debugger.config.DebuggerToUMLSettings;
import com.plucky.debugger.generator.DiagramGenerator;
import com.plucky.debugger.generator.DiagramGeneratorFactory;
import com.plucky.debugger.model.CallStackInfo;
import com.plucky.debugger.ui.DebuggerToUMLToolWindowManager;
import org.jetbrains.annotations.NotNull;

/**
 * 调试事件监听器
 * 监听调试会话的启动、停止和断点触发事件
 */
public class DebuggerEventListener implements XDebuggerManagerListener {

    private static final Logger LOG = Logger.getInstance(DebuggerEventListener.class);

    private final Project project;

    public DebuggerEventListener(Project project) {
        this.project = project;
        LOG.info("DebuggerEventListener created for project: " + project.getName());
    }

    @Override
    public void currentSessionChanged(XDebugSession previousSession, XDebugSession currentSession) {
        LOG.info("currentSessionChanged called - previous: " +
            (previousSession != null ? previousSession.getSessionName() : "null") +
            ", current: " +
            (currentSession != null ? currentSession.getSessionName() : "null"));

        if (currentSession != null) {
            LOG.info("Adding session listener to: " + currentSession.getSessionName());

            // 添加会话监听器，监听暂停事件
            currentSession.addSessionListener(new com.intellij.xdebugger.XDebugSessionListener() {
                @Override
                public void sessionPaused() {
                    LOG.info("sessionPaused event triggered!");
                    // 断点触发，会话暂停
                    onSessionPaused(currentSession);
                }

                @Override
                public void sessionResumed() {
                    LOG.info("Session resumed");
                }

                @Override
                public void sessionStopped() {
                    LOG.info("Session stopped");
                }

                @Override
                public void stackFrameChanged() {
                    LOG.debug("Stack frame changed");
                }

                @Override
                public void beforeSessionResume() {
                    LOG.debug("Before session resume");
                }
            });

            LOG.info("Session listener added successfully");
        }
    }

    /**
     * 会话暂停时的处理（断点触发）
     */
    private void onSessionPaused(XDebugSession session) {
        DebuggerToUMLSettings settings = DebuggerToUMLSettings.getInstance();

        // 自动日志输出
        if (settings.enableAutoLogging) {
            try {
                // 尝试获取当前栈帧
                // 注意：由于XDebugProcess不能直接转换为DebugProcessImpl
                // 我们使用XDebugSession的API来获取栈帧信息
                com.intellij.xdebugger.frame.XSuspendContext suspendContext = session.getSuspendContext();
                if (suspendContext != null) {
                    com.intellij.xdebugger.frame.XExecutionStack activeStack = suspendContext.getActiveExecutionStack();
                    if (activeStack != null) {
                        // 获取顶层栈帧
                        com.intellij.xdebugger.frame.XStackFrame topFrame = activeStack.getTopFrame();
                        if (topFrame != null) {
                            // 尝试从XStackFrame获取StackFrameProxyImpl
                            // 这需要通过反射或其他方式，但为了兼容性，我们暂时跳过
                            // 直接使用XStackFrame的信息
                            LOG.info("Debug log capture: XStackFrame available but StackFrameProxyImpl conversion not implemented yet");
                            // TODO: 实现XStackFrame到StackFrameProxyImpl的转换
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Failed to capture and output debug log", e);
            }
        }

        // 如果启用了自动捕获，则捕获调用栈
        if (settings.autoCapture) {
            LOG.info("Auto-capturing call stack for session: " + session.getSessionName());

            // 在后台线程中执行捕获操作
            com.intellij.openapi.application.ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    CallStackInfo callStackInfo = CallStackCapture.captureCallStack(session);

                    if (callStackInfo != null && !callStackInfo.isEmpty()) {
                        LOG.info("Call stack captured successfully. Methods: " + callStackInfo.getDepth());

                        // 生成图表
                        DiagramGenerator generator = DiagramGeneratorFactory.createGenerator();
                        String diagramCode = generator.generateDiagram(callStackInfo);

                        // 更新工具窗口显示，传递CallStackInfo
                        CallStackInfo finalCallStackInfo = callStackInfo;
                        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                            DebuggerToUMLToolWindowManager.updateDiagram(project, diagramCode, finalCallStackInfo);
                        });
                    } else {
                        LOG.warn("Failed to capture call stack or stack is empty");
                    }
                } catch (Exception e) {
                    LOG.error("Error capturing call stack", e);
                }
            });
        } else {
            LOG.debug("Auto-capture is disabled");
        }
    }
}
