package com.plucky.debugger.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;

/**
 * 工具窗口管理器
 * 用于更新工具窗口的内容
 */
public class DebuggerToUMLToolWindowManager {

    /**
     * 更新工具窗口显示的图表
     *
     * @param project 项目
     * @param diagramCode 图表代码
     */
    public static void updateDiagram(Project project, String diagramCode) {
        if (project == null || diagramCode == null) {
            return;
        }

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow("DebuggerToUML");

        if (toolWindow != null) {
            Content content = toolWindow.getContentManager().getContent(0);
            if (content != null) {
                Object component = content.getComponent();
                if (component instanceof javax.swing.JPanel) {
                    // 查找DebuggerToUMLToolWindow实例
                    findAndUpdateToolWindow((javax.swing.JPanel) component, diagramCode);
                }
            }

            // 激活工具窗口
            toolWindow.activate(null);
        }
    }

    /**
     * 查找并更新DebuggerToUMLToolWindow
     */
    private static void findAndUpdateToolWindow(javax.swing.JPanel panel, String diagramCode) {
        // 尝试从panel的client property中获取DebuggerToUMLToolWindow实例
        Object toolWindowObj = panel.getClientProperty("DebuggerToUMLToolWindow");
        if (toolWindowObj instanceof DebuggerToUMLToolWindow) {
            ((DebuggerToUMLToolWindow) toolWindowObj).updateDiagram(diagramCode);
        }
    }
}
