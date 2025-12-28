package com.plucky.debugger.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebugSession;
import com.plucky.debugger.capture.CallStackCapture;
import com.plucky.debugger.generator.DiagramGenerator;
import com.plucky.debugger.generator.DiagramGeneratorFactory;
import com.plucky.debugger.model.CallStackInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 捕获调用栈动作
 * 手动触发调用栈捕获
 */
public class CaptureCallStackAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 获取当前调试会话
        XDebuggerManager debuggerManager = XDebuggerManager.getInstance(project);
        XDebugSession currentSession = debuggerManager.getCurrentSession();

        if (currentSession == null) {
            JOptionPane.showMessageDialog(null,
                "没有活动的调试会话。请先启动调试。",
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 捕获调用栈
        CallStackInfo callStackInfo = CallStackCapture.captureCallStack(currentSession);

        if (callStackInfo == null || callStackInfo.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                "无法捕获调用栈信息。",
                "错误",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 生成图表
        DiagramGenerator generator = DiagramGeneratorFactory.createGenerator();
        String diagramCode = generator.generateDiagram(callStackInfo);

        // 显示结果（这里简单地弹出对话框，实际应该更新工具窗口）
        JTextArea textArea = new JTextArea(diagramCode);
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(600, 400));

        JOptionPane.showMessageDialog(null,
            scrollPane,
            "PlantUML时序图代码",
            JOptionPane.INFORMATION_MESSAGE);
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
