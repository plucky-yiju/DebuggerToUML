package com.plucky.debugger.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;

/**
 * DebuggerToUML工具窗口
 * 显示生成的时序图
 */
public class DebuggerToUMLToolWindow {

    private final Project project;
    private JPanel mainPanel;
    private JTextArea diagramTextArea;
    private JButton captureButton;
    private JButton exportButton;
    private JButton clearButton;

    public DebuggerToUMLToolWindow(Project project) {
        this.project = project;
        initUI();
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout());

        // 创建工具栏
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        captureButton = new JButton("捕获调用栈");
        exportButton = new JButton("导出图表");
        clearButton = new JButton("清空");

        toolbarPanel.add(captureButton);
        toolbarPanel.add(exportButton);
        toolbarPanel.add(clearButton);

        // 创建文本区域显示PlantUML代码
        diagramTextArea = new JTextArea();
        diagramTextArea.setEditable(false);
        diagramTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        diagramTextArea.setText("等待捕获调用栈...\n\n" +
            "使用方法：\n" +
            "1. 在代码中设置断点\n" +
            "2. 启动调试模式\n" +
            "3. 当断点触发时，点击'捕获调用栈'按钮\n" +
            "4. 查看生成的PlantUML时序图代码\n" +
            "5. 可以将代码复制到PlantUML编辑器中查看图形");

        JBScrollPane scrollPane = new JBScrollPane(diagramTextArea);

        // 添加组件到主面板
        mainPanel.add(toolbarPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 添加按钮事件监听器
        clearButton.addActionListener(e -> clearDiagram());
    }

    public JPanel getContent() {
        return mainPanel;
    }

    /**
     * 更新显示的图表内容
     */
    public void updateDiagram(String diagramCode) {
        diagramTextArea.setText(diagramCode);
    }

    /**
     * 清空图表
     */
    private void clearDiagram() {
        diagramTextArea.setText("");
    }

    public JButton getCaptureButton() {
        return captureButton;
    }

    public JButton getExportButton() {
        return exportButton;
    }
}
