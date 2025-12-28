package com.plucky.debugger.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.plucky.debugger.config.DebuggerToUMLSettings;
import com.plucky.debugger.generator.PlantUMLRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * DebuggerToUML工具窗口
 * 显示生成的时序图
 */
public class DebuggerToUMLToolWindow {

    private static final Logger LOG = Logger.getInstance(DebuggerToUMLToolWindow.class);

    private final Project project;
    private JPanel mainPanel;
    private JTextArea diagramTextArea;
    private JTextArea classListArea;  // 新增：类路径列表
    private JLabel imageLabel;
    private JButton captureButton;
    private JButton exportButton;
    private JButton clearButton;
    private JButton renderButton;
    private JCheckBox enableCheckBox;  // 新增：启用/禁用开关
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

        // 启用/禁用开关
        enableCheckBox = new JCheckBox("启用自动捕获", DebuggerToUMLSettings.getInstance().autoCapture);
        enableCheckBox.setToolTipText("启用后，断点触发时会自动捕获调用栈");
        enableCheckBox.addActionListener(e -> {
            boolean enabled = enableCheckBox.isSelected();
            DebuggerToUMLSettings.getInstance().autoCapture = enabled;
            LOG.info("Auto capture " + (enabled ? "enabled" : "disabled"));
        });
        toolbarPanel.add(enableCheckBox);

        // 分隔符
        toolbarPanel.add(new JSeparator(SwingConstants.VERTICAL));

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

        // 类路径列表视图
        classListArea = new JTextArea();
        classListArea.setEditable(false);
        classListArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        classListArea.setText("等待捕获调用栈...\n\n提示：这里会显示所有涉及的类路径，方便你识别需要过滤的类。");
        JBScrollPane classListScrollPane = new JBScrollPane(classListArea);
        tabbedPane.addTab("类路径列表", classListScrollPane);

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

            // 提取并显示类路径列表
            updateClassList(diagramCode);

            // 自动切换到代码视图
            tabbedPane.setSelectedIndex(0);

            // 显示通知
            showNotification("调用栈已捕获，共 " + countMethods(diagramCode) + " 个方法");
        }
    }

    /**
     * 从PlantUML代码中提取类路径列表并更新显示
     */
    private void updateClassList(String plantUMLCode) {
        if (plantUMLCode == null || plantUMLCode.isEmpty()) {
            return;
        }

        StringBuilder classList = new StringBuilder();
        classList.append("=== 调用栈涉及的类路径列表 ===\n\n");
        classList.append("提示：复制需要过滤的类路径到Settings中的过滤配置\n");
        classList.append("Settings -> Tools -> DebuggerToUML Settings -> 过滤的类名/包名\n\n");
        classList.append("---\n\n");

        java.util.Set<String> uniqueClasses = new java.util.LinkedHashSet<>();
        String[] lines = plantUMLCode.split("\n");

        // 从participant行提取类名
        for (String line : lines) {
            if (line.trim().startsWith("participant")) {
                // 格式: participant "ClassName" as ClassName_0
                int firstQuote = line.indexOf('"');
                int secondQuote = line.indexOf('"', firstQuote + 1);
                if (firstQuote >= 0 && secondQuote > firstQuote) {
                    String className = line.substring(firstQuote + 1, secondQuote).trim();
                    // 移除可能的前缀（如"JavaFrame "）
                    if (className.contains(" ")) {
                        className = className.substring(className.lastIndexOf(' ') + 1);
                    }
                    uniqueClasses.add(className);
                }
            }
        }

        // 按字母顺序排序并显示
        java.util.List<String> sortedClasses = new java.util.ArrayList<>(uniqueClasses);
        java.util.Collections.sort(sortedClasses);

        int index = 1;
        for (String className : sortedClasses) {
            classList.append(String.format("%2d. %s\n", index++, className));
        }

        classList.append("\n---\n");
        classList.append(String.format("共 %d 个不同的类\n", uniqueClasses.size()));

        classListArea.setText(classList.toString());
        LOG.info("Updated class list with " + uniqueClasses.size() + " classes");
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

        LOG.info("Starting diagram rendering");
        LOG.debug("PlantUML code to render:\n" + plantUMLCode);

        // 显示进度提示
        imageLabel.setText("正在渲染图表，请稍候...");
        imageLabel.setIcon(null);

        // 在后台线程中渲染
        SwingWorker<BufferedImage, Void> worker = new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                LOG.info("Rendering in background thread");
                return PlantUMLRenderer.renderToImage(plantUMLCode);
            }

            @Override
            protected void done() {
                try {
                    BufferedImage image = get();
                    if (image != null) {
                        LOG.info("Rendering completed successfully");
                        imageLabel.setText(null);
                        imageLabel.setIcon(new ImageIcon(image));
                        // 切换到图片视图
                        tabbedPane.setSelectedIndex(1);
                    } else {
                        LOG.error("Rendered image is null");
                        imageLabel.setText("渲染失败：图片为空");
                    }
                } catch (Exception e) {
                    LOG.error("Failed to render diagram", e);
                    imageLabel.setText("渲染失败");
                    String errorMsg = "渲染失败: " + e.getMessage() + "\n\n";
                    errorMsg += "可能的原因：\n";
                    errorMsg += "1. PlantUML代码格式错误\n";
                    errorMsg += "2. 内存不足\n";
                    errorMsg += "3. 图表过于复杂\n\n";
                    errorMsg += "建议：\n";
                    errorMsg += "- 检查PlantUML代码语法\n";
                    errorMsg += "- 减少调用栈深度\n";
                    errorMsg += "- 查看IDEA日志获取详细错误信息\n\n";
                    errorMsg += "详细错误：\n" + getStackTrace(e);

                    JOptionPane.showMessageDialog(mainPanel,
                        errorMsg,
                        "渲染错误",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * 获取异常堆栈信息
     */
    private String getStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        // 只返回前500个字符
        return stackTrace.length() > 500 ? stackTrace.substring(0, 500) + "..." : stackTrace;
    }

    /**
     * 清空图表
     */
    private void clearDiagram() {
        diagramTextArea.setText("");
        imageLabel.setIcon(null);
        classListArea.setText("等待捕获调用栈...\n\n提示：这里会显示所有涉及的类路径，方便你识别需要过滤的类。");
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
