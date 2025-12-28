package com.plucky.debugger.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.plucky.debugger.model.CallStackInfo;

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
        updateDiagram(project, diagramCode, null);
    }

    /**
     * 更新工具窗口显示的图表（带CallStackInfo）
     *
     * @param project 项目
     * @param diagramCode 图表代码
     * @param callStackInfo 调用栈信息
     */
    public static void updateDiagram(Project project, String diagramCode, CallStackInfo callStackInfo) {
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
                    findAndUpdateToolWindow((javax.swing.JPanel) component, diagramCode, callStackInfo);
                }
            }

            // 激活工具窗口
            toolWindow.activate(null);
        }
    }

    /**
     * 查找并更新DebuggerToUMLToolWindow
     */
    private static void findAndUpdateToolWindow(javax.swing.JPanel panel, String diagramCode, CallStackInfo callStackInfo) {
        // 尝试从panel的client property中获取DebuggerToUMLToolWindow实例
        Object toolWindowObj = panel.getClientProperty("DebuggerToUMLToolWindow");
        if (toolWindowObj instanceof DebuggerToUMLToolWindow) {
            ((DebuggerToUMLToolWindow) toolWindowObj).updateDiagram(diagramCode, callStackInfo);
        }
    }
}
