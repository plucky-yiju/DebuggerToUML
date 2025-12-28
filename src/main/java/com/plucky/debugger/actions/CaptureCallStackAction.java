package com.plucky.debugger.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebugSession;
import com.plucky.debugger.capture.CallStackCapture;
import com.plucky.debugger.generator.DiagramGenerator;
import com.plucky.debugger.generator.DiagramGeneratorFactory;
import com.plucky.debugger.model.CallStackInfo;
import com.plucky.debugger.ui.DebuggerToUMLToolWindowManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 捕获调用栈动作
 * 手动触发调用栈捕获
 */
public class CaptureCallStackAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(CaptureCallStackAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            LOG.warn("Project is null");
            return;
        }

        // 获取当前调试会话
        XDebuggerManager debuggerManager = XDebuggerManager.getInstance(project);
        XDebugSession currentSession = debuggerManager.getCurrentSession();

        if (currentSession == null) {
            LOG.warn("No active debug session");
            JOptionPane.showMessageDialog(null,
                "没有活动的调试会话。请先启动调试。",
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        LOG.info("Manual capture triggered for session: " + currentSession.getSessionName());

        // 在后台线程中执行捕获操作
        com.intellij.openapi.application.ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                // 捕获调用栈
                CallStackInfo callStackInfo = CallStackCapture.captureCallStack(currentSession);

                if (callStackInfo == null || callStackInfo.isEmpty()) {
                    LOG.warn("Failed to capture call stack or stack is empty");
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null,
                            "无法捕获调用栈信息。请确保程序已暂停在断点处。",
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    });
                    return;
                }

                LOG.info("Call stack captured successfully. Methods: " + callStackInfo.getDepth());

                // 生成图表
                DiagramGenerator generator = DiagramGeneratorFactory.createGenerator();
                String diagramCode = generator.generateDiagram(callStackInfo);

                // 更新工具窗口显示
                com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                    DebuggerToUMLToolWindowManager.updateDiagram(project, diagramCode);
                    LOG.info("Tool window updated with diagram");
                });

            } catch (Exception ex) {
                LOG.error("Error capturing call stack", ex);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                        "捕获调用栈时发生错误: " + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            e.getPresentation().setEnabled(false);
            return;
        }

        // 检查是否有活动的调试会话
        XDebuggerManager debuggerManager = XDebuggerManager.getInstance(project);
        XDebugSession currentSession = debuggerManager.getCurrentSession();
        e.getPresentation().setEnabled(currentSession != null);
    }
}
