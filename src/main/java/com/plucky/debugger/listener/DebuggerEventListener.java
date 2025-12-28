package com.plucky.debugger.listener;

import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.SuspendContextImpl;
import com.intellij.debugger.jdi.StackFrameProxyImpl;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManagerListener;
import com.plucky.debugger.capture.CallStackCapture;
import com.plucky.debugger.capture.DebugLogCapture;
import com.plucky.debugger.config.DebuggerToUMLSettings;
import com.plucky.debugger.generator.DiagramGenerator;
import com.plucky.debugger.generator.DiagramGeneratorFactory;
import com.plucky.debugger.logger.DebugLogWriter;
import com.plucky.debugger.model.CallStackInfo;
import com.plucky.debugger.model.DebugLogInfo;
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
                // 获取当前栈帧
                DebugProcessImpl debugProcess = (DebugProcessImpl) session.getDebugProcess();
                if (debugProcess != null) {
                    SuspendContextImpl suspendContext = debugProcess.getSuspendManager().getPausedContext();
                    if (suspendContext != null && suspendContext.getFrameProxy() != null) {
                        StackFrameProxyImpl frameProxy = suspendContext.getFrameProxy();

                        // 捕获调试信息
                        DebugLogInfo logInfo = DebugLogCapture.captureDebugInfo(frameProxy);

                        // 异步输出日志
                        if (logInfo != null) {
                            DebugLogWriter.writeLogAsync(logInfo);
                            LOG.info("Debug log captured and queued for output");
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
