package com.plucky.debugger.config;

import javax.swing.*;
import java.awt.*;

/**
 * 设置面板UI
 * 提供用户配置界面
 */
public class DebuggerToUMLSettingsPanel {

    private JPanel mainPanel;
    private JSpinner maxDepthSpinner;
    private JCheckBox autoCaptureCheckBox;
    private JCheckBox showParametersCheckBox;
    private JCheckBox showReturnValuesCheckBox;
    private JCheckBox showTimestampsCheckBox;
    private JCheckBox filterJdkCheckBox;
    private JTextField filteredPackagesField;
    private JComboBox<String> diagramTypeComboBox;
    private JButton resetButton;
    private JLabel validationLabel;

    private final DebuggerToUMLSettings settings;

    public DebuggerToUMLSettingsPanel() {
        this.settings = DebuggerToUMLSettings.getInstance();
        createUI();
        reset();
    }

    private void createUI() {
        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // 最大调用栈深度
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("最大调用栈深度:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        maxDepthSpinner = new JSpinner(new SpinnerNumberModel(
            DebuggerToUMLSettings.DEFAULT_CALL_STACK_DEPTH,
            DebuggerToUMLSettings.MIN_CALL_STACK_DEPTH,
            DebuggerToUMLSettings.MAX_CALL_STACK_DEPTH,
            1
        ));
        maxDepthSpinner.setToolTipText(String.format(
            "调用栈深度范围: %d - %d",
            DebuggerToUMLSettings.MIN_CALL_STACK_DEPTH,
            DebuggerToUMLSettings.MAX_CALL_STACK_DEPTH
        ));
        mainPanel.add(maxDepthSpinner, gbc);

        row++;

        // 自动捕获
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        autoCaptureCheckBox = new JCheckBox("自动捕获调用栈（断点触发时）");
        autoCaptureCheckBox.setToolTipText("启用后，断点触发时会自动捕获调用栈");
        mainPanel.add(autoCaptureCheckBox, gbc);

        row++;

        // 显示方法参数
        gbc.gridy = row;
        showParametersCheckBox = new JCheckBox("显示方法参数");
        showParametersCheckBox.setToolTipText("在时序图中显示方法参数");
        mainPanel.add(showParametersCheckBox, gbc);

        row++;

        // 显示返回值
        gbc.gridy = row;
        showReturnValuesCheckBox = new JCheckBox("显示返回值");
        showReturnValuesCheckBox.setToolTipText("在时序图中显示方法返回值");
        mainPanel.add(showReturnValuesCheckBox, gbc);

        row++;

        // 显示时间戳
        gbc.gridy = row;
        showTimestampsCheckBox = new JCheckBox("显示时间戳");
        showTimestampsCheckBox.setToolTipText("在时序图中显示方法调用时间戳");
        mainPanel.add(showTimestampsCheckBox, gbc);

        row++;

        // 过滤JDK类
        gbc.gridy = row;
        filterJdkCheckBox = new JCheckBox("过滤JDK类");
        filterJdkCheckBox.setToolTipText("自动过滤Java标准库的类（java.*, javax.*, sun.*, jdk.*）");
        mainPanel.add(filterJdkCheckBox, gbc);

        row++;

        // 过滤的包名
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("过滤的包名（逗号分隔）:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        filteredPackagesField = new JTextField();
        filteredPackagesField.setToolTipText("输入要过滤的包名前缀，用逗号分隔。例如: com.example.util,org.springframework");
        mainPanel.add(filteredPackagesField, gbc);

        row++;

        // 图表类型
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("图表类型:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        diagramTypeComboBox = new JComboBox<>(new String[]{"plantuml", "graphviz"});
        diagramTypeComboBox.setToolTipText("选择图表生成类型（目前只支持plantuml）");
        mainPanel.add(diagramTypeComboBox, gbc);

        row++;

        // 验证信息标签
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        validationLabel = new JLabel(" ");
        validationLabel.setForeground(Color.RED);
        mainPanel.add(validationLabel, gbc);

        row++;

        // 重置按钮
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        resetButton = new JButton("重置到默认值");
        resetButton.setToolTipText("将所有配置重置为默认值");
        resetButton.addActionListener(e -> resetToDefaults());
        mainPanel.add(resetButton, gbc);

        // 添加空白填充
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JPanel(), gbc);
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public boolean isModified() {
        return (Integer) maxDepthSpinner.getValue() != settings.maxCallStackDepth
            || autoCaptureCheckBox.isSelected() != settings.autoCapture
            || showParametersCheckBox.isSelected() != settings.showMethodParameters
            || showReturnValuesCheckBox.isSelected() != settings.showReturnValues
            || showTimestampsCheckBox.isSelected() != settings.showTimestamps
            || filterJdkCheckBox.isSelected() != settings.filterJdkClasses
            || !filteredPackagesField.getText().equals(settings.filteredPackages)
            || !diagramTypeComboBox.getSelectedItem().equals(settings.diagramType);
    }

    /**
     * 验证配置
     * @return 如果配置有效返回true
     */
    public boolean validate() {
        // 清除之前的验证消息
        validationLabel.setText(" ");

        // 验证调用栈深度
        int depth = (Integer) maxDepthSpinner.getValue();
        if (depth < DebuggerToUMLSettings.MIN_CALL_STACK_DEPTH ||
            depth > DebuggerToUMLSettings.MAX_CALL_STACK_DEPTH) {
            validationLabel.setText(String.format(
                "调用栈深度必须在 %d 到 %d 之间",
                DebuggerToUMLSettings.MIN_CALL_STACK_DEPTH,
                DebuggerToUMLSettings.MAX_CALL_STACK_DEPTH
            ));
            return false;
        }

        // 验证过滤包名（允许为空）
        String packages = filteredPackagesField.getText();
        if (packages == null) {
            filteredPackagesField.setText("");
        }

        // 验证图表类型
        String diagramType = (String) diagramTypeComboBox.getSelectedItem();
        if (diagramType == null || diagramType.trim().isEmpty()) {
            validationLabel.setText("图表类型不能为空");
            return false;
        }

        return true;
    }

    public void apply() {
        if (!validate()) {
            throw new IllegalStateException("配置验证失败");
        }

        settings.maxCallStackDepth = (Integer) maxDepthSpinner.getValue();
        settings.autoCapture = autoCaptureCheckBox.isSelected();
        settings.showMethodParameters = showParametersCheckBox.isSelected();
        settings.showReturnValues = showReturnValuesCheckBox.isSelected();
        settings.showTimestamps = showTimestampsCheckBox.isSelected();
        settings.filterJdkClasses = filterJdkCheckBox.isSelected();
        settings.filteredPackages = filteredPackagesField.getText();
        settings.diagramType = (String) diagramTypeComboBox.getSelectedItem();

        // 应用后验证并修复配置
        settings.validateAndFix();

        // 清除验证消息
        validationLabel.setText(" ");
    }

    public void reset() {
        maxDepthSpinner.setValue(settings.maxCallStackDepth);
        autoCaptureCheckBox.setSelected(settings.autoCapture);
        showParametersCheckBox.setSelected(settings.showMethodParameters);
        showReturnValuesCheckBox.setSelected(settings.showReturnValues);
        showTimestampsCheckBox.setSelected(settings.showTimestamps);
        filterJdkCheckBox.setSelected(settings.filterJdkClasses);
        filteredPackagesField.setText(settings.filteredPackages);
        diagramTypeComboBox.setSelectedItem(settings.diagramType);
        validationLabel.setText(" ");
    }

    /**
     * 重置到默认值
     */
    private void resetToDefaults() {
        int result = JOptionPane.showConfirmDialog(
            mainPanel,
            "确定要将所有配置重置为默认值吗？",
            "确认重置",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            maxDepthSpinner.setValue(DebuggerToUMLSettings.DEFAULT_CALL_STACK_DEPTH);
            autoCaptureCheckBox.setSelected(true);
            showParametersCheckBox.setSelected(true);
            showReturnValuesCheckBox.setSelected(false);
            showTimestampsCheckBox.setSelected(false);
            filterJdkCheckBox.setSelected(true);
            filteredPackagesField.setText(DebuggerToUMLSettings.DEFAULT_FILTERED_PACKAGES);
            diagramTypeComboBox.setSelectedItem(DebuggerToUMLSettings.DEFAULT_DIAGRAM_TYPE);
            validationLabel.setText(" ");
        }
    }
}
