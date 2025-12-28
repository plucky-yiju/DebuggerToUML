package com.plucky.debugger.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.plucky.debugger.generator.PlantUMLRenderer;
import net.sourceforge.plantuml.FileFormat;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;

/**
 * 导出图表动作
 * 将生成的图表导出为文件
 */
public class ExportDiagramAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 获取当前的PlantUML代码
        String plantUMLCode = getCurrentPlantUMLCode(project);
        if (plantUMLCode == null || plantUMLCode.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                "没有可导出的图表内容",
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 选择导出格式
        String[] formats = {"PNG", "SVG", "PDF"};
        String selectedFormat = (String) JOptionPane.showInputDialog(
            null,
            "选择导出格式:",
            "导出图表",
            JOptionPane.QUESTION_MESSAGE,
            null,
            formats,
            formats[0]
        );

        if (selectedFormat == null) {
            return; // 用户取消
        }

        // 选择保存位置
        FileSaverDescriptor descriptor = new FileSaverDescriptor(
            "导出时序图",
            "选择保存位置",
            selectedFormat.toLowerCase()
        );

        FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project);
        VirtualFileWrapper fileWrapper = dialog.save(project.getBaseDir(), "sequence-diagram." + selectedFormat.toLowerCase());

        if (fileWrapper == null) {
            return; // 用户取消
        }

        File outputFile = fileWrapper.getFile();

        // 在后台线程中导出
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                FileFormat format = getFileFormat(selectedFormat);
                PlantUMLRenderer.renderToFile(plantUMLCode, outputFile, format);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(null,
                        "导出成功: " + outputFile.getAbsolutePath(),
                        "成功",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                        "导出失败: " + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * 获取当前的PlantUML代码
     */
    private String getCurrentPlantUMLCode(Project project) {
        // 从工具窗口获取代码
        com.intellij.openapi.wm.ToolWindow toolWindow =
            com.intellij.openapi.wm.ToolWindowManager.getInstance(project).getToolWindow("DebuggerToUML");

        if (toolWindow != null) {
            com.intellij.ui.content.Content content = toolWindow.getContentManager().getContent(0);
            if (content != null) {
                Object component = content.getComponent();
                if (component instanceof javax.swing.JPanel) {
                    Object toolWindowObj = ((javax.swing.JPanel) component).getClientProperty("DebuggerToUMLToolWindow");
                    if (toolWindowObj instanceof com.plucky.debugger.ui.DebuggerToUMLToolWindow) {
                        return ((com.plucky.debugger.ui.DebuggerToUMLToolWindow) toolWindowObj).getDiagramCode();
                    }
                }
            }
        }

        return null;
    }

    /**
     * 将字符串格式转换为FileFormat枚举
     */
    private FileFormat getFileFormat(String format) {
        switch (format.toUpperCase()) {
            case "PNG":
                return FileFormat.PNG;
            case "SVG":
                return FileFormat.SVG;
            case "PDF":
                return FileFormat.PDF;
            default:
                return FileFormat.PNG;
        }
    }
}
