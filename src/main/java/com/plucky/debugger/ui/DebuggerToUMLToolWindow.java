package com.plucky.debugger.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.plucky.debugger.generator.PlantUMLRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * DebuggerToUML工具窗口
 * 显示生成的时序图
 */
public class DebuggerToUMLToolWindow {

    private final Project project;
    private JPanel mainPanel;
    private JTextArea diagramTextArea;
    private JLabel imageLabel;
    private JButton captureButton;
    private JButton exportButton;
    private JButton clearButton;
    private JButton renderButton;
    private JBTabbedPane tabbedPane;

    public DebuggerToUMLToolWindow(Project project) {
        this.project = project;
        initUI();

        // 将自己存储到panel的client property中，方便后续访问
        mainPanel.putClientProperty("DebuggerToUMLToolWindow", this);
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout());

        // 创建工具栏
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        captureButton = new JButton("捕获调用栈");
        captureButton.setToolTipText("手动捕获当前调试会话的调用栈");

        renderButton = new JButton("渲染图表");
        renderButton.setToolTipText("将PlantUML代码渲染为时序图");

        exportButton = new JButton("导出图表");
        exportButton.setToolTipText("导出时序图为PNG/SVG/PDF文件");

        clearButton = new JButton("清空");
        clearButton.setToolTipText("清空当前显示的内容");

        toolbarPanel.add(captureButton);
        toolbarPanel.add(renderButton);
        toolbarPanel.add(exportButton);
        toolbarPanel.add(clearButton);

        // 创建选项卡面板
        tabbedPane = new JBTabbedPane();

        // 代码视图
        diagramTextArea = new JTextArea();
        diagramTextArea.setEditable(false);
        diagramTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        diagramTextArea.setText(getWelcomeMessage());

        JBScrollPane codeScrollPane = new JBScrollPane(diagramTextArea);
        tabbedPane.addTab("PlantUML代码", codeScrollPane);

        // 图片视图
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        JBScrollPane imageScrollPane = new JBScrollPane(imageLabel);
        tabbedPane.addTab("时序图", imageScrollPane);

        // 添加组件到主面板
        mainPanel.add(toolbarPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // 添加按钮事件监听器
        clearButton.addActionListener(e -> clearDiagram());
        renderButton.addActionListener(e -> renderDiagram());
    }

    /**
     * 获取欢迎消息
     */
    private String getWelcomeMessage() {
        return "欢迎使用 DebuggerToUML 插件！\n\n" +
            "=== 快速开始 ===\n" +
            "1. 在代码中设置断点\n" +
            "2. 启动调试模式（Debug）\n" +
            "3. 当断点触发时，插件会自动捕获调用栈\n" +
            "4. 点击'渲染图表'按钮查看图形化时序图\n" +
            "5. 点击'导出图表'按钮保存为文件\n\n" +
            "=== 配置选项 ===\n" +
            "Settings -> Tools -> DebuggerToUML Settings\n" +
            "- 调整最大调用栈深度\n" +
            "- 配置类/方法过滤规则\n" +
            "- 启用/禁用自动捕获\n\n" +
            "=== 提示 ===\n" +
            "- 可以将PlantUML代码复制到在线编辑器查看\n" +
            "- 支持导出PNG、SVG、PDF格式\n" +
            "- 完全离线可用，无需网络连接\n\n" +
            "等待捕获调用栈...";
    }

    public JPanel getContent() {
        return mainPanel;
    }

    /**
     * 更新显示的图表内容
     */
    public void updateDiagram(String diagramCode) {
        if (diagramCode != null && !diagramCode.isEmpty()) {
            diagramTextArea.setText(diagramCode);
            // 自动切换到代码视图
            tabbedPane.setSelectedIndex(0);

            // 显示通知
            showNotification("调用栈已捕获，共 " + countMethods(diagramCode) + " 个方法");
        }
    }

    /**
     * 统计方法数量
     */
    private int countMethods(String plantUMLCode) {
        if (plantUMLCode == null) return 0;
        int count = 0;
        String[] lines = plantUMLCode.split("\n");
        for (String line : lines) {
            if (line.contains("participant")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 显示通知消息
     */
    private void showNotification(String message) {
        // 可以使用IDEA的通知系统
        com.intellij.notification.Notifications.Bus.notify(
            new com.intellij.notification.Notification(
                "DebuggerToUML",
                "DebuggerToUML",
                message,
                com.intellij.notification.NotificationType.INFORMATION
            ),
            project
        );
    }

    /**
     * 渲染图表
     */
    private void renderDiagram() {
        String plantUMLCode = diagramTextArea.getText();
        if (plantUMLCode == null || plantUMLCode.trim().isEmpty() || plantUMLCode.contains("欢迎使用")) {
            JOptionPane.showMessageDialog(mainPanel,
                "没有PlantUML代码可以渲染\n\n请先捕获调用栈或手动输入PlantUML代码",
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 显示进度提示
        imageLabel.setText("正在渲染图表，请稍候...");
        imageLabel.setIcon(null);

        // 在后台线程中渲染
        SwingWorker<BufferedImage, Void> worker = new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                return PlantUMLRenderer.renderToImage(plantUMLCode);
            }

            @Override
            protected void done() {
                try {
                    BufferedImage image = get();
                    if (image != null) {
                        imageLabel.setText(null);
                        imageLabel.setIcon(new ImageIcon(image));
                        // 切换到图片视图
                        tabbedPane.setSelectedIndex(1);
                    }
                } catch (Exception e) {
                    imageLabel.setText("渲染失败");
                    String errorMsg = "渲染失败: " + e.getMessage() + "\n\n";
                    errorMsg += "可能的原因：\n";
                    errorMsg += "1. PlantUML代码格式错误\n";
                    errorMsg += "2. 内存不足\n";
                    errorMsg += "3. 图表过于复杂\n\n";
                    errorMsg += "建议：\n";
                    errorMsg += "- 检查PlantUML代码语法\n";
                    errorMsg += "- 减少调用栈深度\n";
                    errorMsg += "- 查看IDEA日志获取详细错误信息";

                    JOptionPane.showMessageDialog(mainPanel,
                        errorMsg,
                        "渲染错误",
                        JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    /**
     * 清空图表
     */
    private void clearDiagram() {
        diagramTextArea.setText("");
        imageLabel.setIcon(null);
    }

    public JButton getCaptureButton() {
        return captureButton;
    }

    public JButton getExportButton() {
        return exportButton;
    }

    public String getDiagramCode() {
        return diagramTextArea.getText();
    }
}
