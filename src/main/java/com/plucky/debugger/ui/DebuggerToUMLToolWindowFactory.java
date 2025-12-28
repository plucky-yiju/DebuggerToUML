package com.plucky.debugger.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.plucky.debugger.listener.DebuggerListenerManager;
import org.jetbrains.annotations.NotNull;

/**
 * 工具窗口工厂
 * 创建DebuggerToUML工具窗口
 */
public class DebuggerToUMLToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 初始化调试监听器管理器（确保监听器被注册）
        project.getService(DebuggerListenerManager.class);

        DebuggerToUMLToolWindow toolWindowContent = new DebuggerToUMLToolWindow(project);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(toolWindowContent.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
