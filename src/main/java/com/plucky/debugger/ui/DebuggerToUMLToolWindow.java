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
        renderButton = new JButton("渲染图表");
        exportButton = new JButton("导出图表");
        clearButton = new JButton("清空");

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
        diagramTextArea.setText("等待捕获调用栈...\n\n" +
            "使用方法：\n" +
            "1. 在代码中设置断点\n" +
            "2. 启动调试模式\n" +
            "3. 当断点触发时，插件会自动捕获调用栈\n" +
            "4. 点击'渲染图表'按钮查看图形化时序图\n" +
            "5. 可以将代码复制到PlantUML编辑器中查看");

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

    public JPanel getContent() {
        return mainPanel;
    }

    /**
     * 更新显示的图表内容
     */
    public void updateDiagram(String diagramCode) {
        diagramTextArea.setText(diagramCode);
        // 自动切换到代码视图
        tabbedPane.setSelectedIndex(0);
    }

    /**
     * 渲染图表
     */
    private void renderDiagram() {
        String plantUMLCode = diagramTextArea.getText();
        if (plantUMLCode == null || plantUMLCode.trim().isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel,
                "没有PlantUML代码可以渲染",
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

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
                        imageLabel.setIcon(new ImageIcon(image));
                        // 切换到图片视图
                        tabbedPane.setSelectedIndex(1);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mainPanel,
                        "渲染失败: " + e.getMessage(),
                        "错误",
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
