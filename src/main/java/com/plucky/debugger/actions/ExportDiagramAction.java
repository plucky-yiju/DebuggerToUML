package com.plucky.debugger.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
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

    private static final Logger LOG = Logger.getInstance(ExportDiagramAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            LOG.warn("Project is null");
            return;
        }

        LOG.info("Export diagram action triggered");

        // 获取当前的PlantUML代码
        String plantUMLCode = getCurrentPlantUMLCode(project);
        if (plantUMLCode == null || plantUMLCode.trim().isEmpty() || plantUMLCode.contains("欢迎使用")) {
            LOG.warn("No diagram code available for export");
            JOptionPane.showMessageDialog(null,
                "没有可导出的图表内容\n\n请先捕获调用栈",
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        LOG.info("PlantUML code length: " + plantUMLCode.length());

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
            LOG.info("User cancelled format selection");
            return; // 用户取消
        }

        LOG.info("Selected format: " + selectedFormat);

        // 选择保存位置
        FileSaverDescriptor descriptor = new FileSaverDescriptor(
            "导出时序图",
            "选择保存位置",
            selectedFormat.toLowerCase()
        );

        FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project);
        VirtualFileWrapper fileWrapper = dialog.save(project.getBaseDir(), "sequence-diagram." + selectedFormat.toLowerCase());

        if (fileWrapper == null) {
            LOG.info("User cancelled file selection");
            return; // 用户取消
        }

        File outputFile = fileWrapper.getFile();
        LOG.info("Output file: " + outputFile.getAbsolutePath());

        // 显示进度对话框
        JDialog progressDialog = new JDialog();
        progressDialog.setTitle("导出中");
        progressDialog.setModal(false);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(null);
        JLabel progressLabel = new JLabel("正在导出图表，请稍候...", SwingConstants.CENTER);
        progressDialog.add(progressLabel);
        progressDialog.setVisible(true);

        // 在后台线程中导出
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                LOG.info("Starting export in background thread");
                FileFormat format = getFileFormat(selectedFormat);
                PlantUMLRenderer.renderToFile(plantUMLCode, outputFile, format);
                LOG.info("Export completed successfully");
                return null;
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    get();
                    JOptionPane.showMessageDialog(null,
                        "导出成功!\n\n文件位置:\n" + outputFile.getAbsolutePath(),
                        "成功",
                        JOptionPane.INFORMATION_MESSAGE);
                    LOG.info("Export successful: " + outputFile.getAbsolutePath());
                } catch (Exception ex) {
                    LOG.error("Export failed", ex);
                    String errorMsg = "导出失败: " + ex.getMessage() + "\n\n";
                    errorMsg += "可能的原因:\n";
                    errorMsg += "1. 文件路径无效或无写入权限\n";
                    errorMsg += "2. PlantUML代码格式错误\n";
                    errorMsg += "3. 磁盘空间不足\n\n";
                    errorMsg += "请检查IDEA日志获取详细错误信息";

                    JOptionPane.showMessageDialog(null,
                        errorMsg,
                        "导出错误",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * 获取当前的PlantUML代码
     */
    private String getCurrentPlantUMLCode(Project project) {
        try {
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
                            String code = ((com.plucky.debugger.ui.DebuggerToUMLToolWindow) toolWindowObj).getDiagramCode();
                            LOG.info("Retrieved diagram code from tool window");
                            return code;
                        }
                    }
                }
            }
            LOG.warn("Could not retrieve diagram code from tool window");
        } catch (Exception e) {
            LOG.error("Error retrieving diagram code", e);
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

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabled(project != null);
    }
}
