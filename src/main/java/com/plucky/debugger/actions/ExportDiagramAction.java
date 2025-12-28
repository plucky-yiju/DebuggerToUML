package com.plucky.debugger.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 导出图表动作
 * 将生成的图表导出为文件
 */
public class ExportDiagramAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // TODO: 实现导出功能
        JOptionPane.showMessageDialog(null,
            "导出功能正在开发中...",
            "提示",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
