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
    }

    @Override
    public void currentSessionChanged(XDebugSession previousSession, XDebugSession currentSession) {
        if (currentSession != null) {
            LOG.info("Current session changed to: " + currentSession.getSessionName());

            // 添加会话监听器，监听暂停事件
            currentSession.addSessionListener(new com.intellij.xdebugger.XDebugSessionListener() {
                @Override
                public void sessionPaused() {
                    // 断点触发，会话暂停
                    onSessionPaused(currentSession);
                }

                @Override
                public void sessionResumed() {
                    LOG.debug("Session resumed");
                }

                @Override
                public void sessionStopped() {
                    LOG.debug("Session stopped");
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
        }
    }

    /**
     * 会话暂停时的处理（断点触发）
     */
    private void onSessionPaused(XDebugSession session) {
        DebuggerToUMLSettings settings = DebuggerToUMLSettings.getInstance();

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

                        // 更新工具窗口显示
                        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                            DebuggerToUMLToolWindowManager.updateDiagram(project, diagramCode);
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
