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
import com.plucky.debugger.generator.TextReportGenerator;
import com.plucky.debugger.model.CallStackInfo;
import net.sourceforge.plantuml.FileFormat;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
        String[] formats = {"PNG", "SVG", "PDF", "TXT (文本报告)"};
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

        // 处理文本报告导出
        if (selectedFormat.startsWith("TXT")) {
            exportTextReport(project, plantUMLCode);
            return;
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
     * 导出文本报告
     */
    private void exportTextReport(Project project, String plantUMLCode) {
        LOG.info("Starting text report export");

        // 选择保存位置
        FileSaverDescriptor descriptor = new FileSaverDescriptor(
            "导出文本报告",
            "选择保存位置",
            "txt"
        );

        FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project);
        VirtualFileWrapper fileWrapper = dialog.save(project.getBaseDir(), "call-stack-report.txt");

        if (fileWrapper == null) {
            LOG.info("User cancelled file selection for text report");
            return; // 用户取消
        }

        File outputFile = fileWrapper.getFile();
        LOG.info("Text report output file: " + outputFile.getAbsolutePath());

        // 显示进度对话框
        JDialog progressDialog = new JDialog();
        progressDialog.setTitle("导出中");
        progressDialog.setModal(false);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(null);
        JLabel progressLabel = new JLabel("正在生成文本报告，请稍候...", SwingConstants.CENTER);
        progressDialog.add(progressLabel);
        progressDialog.setVisible(true);

        // 在后台线程中导出
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                LOG.info("Generating text report in background thread");

                // 尝试从工具窗口获取CallStackInfo
                CallStackInfo callStackInfo = getCurrentCallStackInfo(project);

                String reportContent;
                if (callStackInfo != null && !callStackInfo.isEmpty()) {
                    // 使用TextReportGenerator生成详细报告
                    TextReportGenerator generator = new TextReportGenerator();
                    reportContent = generator.generateReport(callStackInfo);
                    LOG.info("Generated detailed text report from CallStackInfo");
                } else {
                    // 降级方案：直接使用PlantUML代码
                    reportContent = generateSimpleReport(plantUMLCode);
                    LOG.warn("CallStackInfo not available, generated simple report from PlantUML code");
                }

                // 写入文件
                try (FileWriter writer = new FileWriter(outputFile)) {
                    writer.write(reportContent);
                    LOG.info("Text report written to file successfully");
                } catch (IOException e) {
                    LOG.error("Failed to write text report", e);
                    throw e;
                }

                return null;
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    get();
                    JOptionPane.showMessageDialog(null,
                        "文本报告导出成功！\n\n文件位置:\n" + outputFile.getAbsolutePath() +
                        "\n\n提示：此报告包含详细的调用栈信息，便于AI分析代码逻辑。",
                        "成功",
                        JOptionPane.INFORMATION_MESSAGE);
                    LOG.info("Text report export successful: " + outputFile.getAbsolutePath());
                } catch (Exception ex) {
                    LOG.error("Text report export failed", ex);
                    String errorMsg = "导出失败: " + ex.getMessage() + "\n\n";
                    errorMsg += "可能的原因:\n";
                    errorMsg += "1. 文件路径无效或无写入权限\n";
                    errorMsg += "2. 磁盘空间不足\n\n";
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
     * 获取当前的CallStackInfo
     */
    private CallStackInfo getCurrentCallStackInfo(Project project) {
        try {
            // 从工具窗口获取CallStackInfo
            com.intellij.openapi.wm.ToolWindow toolWindow =
                com.intellij.openapi.wm.ToolWindowManager.getInstance(project).getToolWindow("DebuggerToUML");

            if (toolWindow != null) {
                com.intellij.ui.content.Content content = toolWindow.getContentManager().getContent(0);
                if (content != null) {
                    Object component = content.getComponent();
                    if (component instanceof javax.swing.JPanel) {
                        Object toolWindowObj = ((javax.swing.JPanel) component).getClientProperty("DebuggerToUMLToolWindow");
                        if (toolWindowObj instanceof com.plucky.debugger.ui.DebuggerToUMLToolWindow) {
                            com.plucky.debugger.ui.DebuggerToUMLToolWindow window =
                                (com.plucky.debugger.ui.DebuggerToUMLToolWindow) toolWindowObj;
                            // 尝试获取当前历史记录的CallStackInfo
                            return window.getCurrentCallStackInfo();
                        }
                    }
                }
            }
            LOG.warn("Could not retrieve CallStackInfo from tool window");
        } catch (Exception e) {
            LOG.error("Error retrieving CallStackInfo", e);
        }

        return null;
    }

    /**
     * 生成简单的文本报告（降级方案）
     */
    private String generateSimpleReport(String plantUMLCode) {
        StringBuilder report = new StringBuilder();

        report.append("=".repeat(60)).append("\n");
        report.append("调用栈分析报告（简化版）\n");
        report.append("=".repeat(60)).append("\n\n");

        report.append("【说明】\n");
        report.append("由于无法获取完整的调用栈信息，此报告仅包含PlantUML代码。\n");
        report.append("建议：重新捕获调用栈以获取更详细的报告。\n\n");

        report.append("=".repeat(60)).append("\n");
        report.append("PlantUML时序图代码\n");
        report.append("=".repeat(60)).append("\n\n");

        report.append(plantUMLCode);
        report.append("\n\n");

        report.append("提示: 可以将上述PlantUML代码复制到在线编辑器查看图形化时序图\n");
        report.append("在线编辑器: https://www.plantuml.com/plantuml/uml/\n");

        return report.toString();
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
