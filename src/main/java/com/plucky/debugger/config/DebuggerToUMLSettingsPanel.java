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
        maxDepthSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        mainPanel.add(maxDepthSpinner, gbc);

        row++;

        // 自动捕获
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        autoCaptureCheckBox = new JCheckBox("自动捕获调用栈（断点触发时）");
        mainPanel.add(autoCaptureCheckBox, gbc);

        row++;

        // 显示方法参数
        gbc.gridy = row;
        showParametersCheckBox = new JCheckBox("显示方法参数");
        mainPanel.add(showParametersCheckBox, gbc);

        row++;

        // 显示返回值
        gbc.gridy = row;
        showReturnValuesCheckBox = new JCheckBox("显示返回值");
        mainPanel.add(showReturnValuesCheckBox, gbc);

        row++;

        // 显示时间戳
        gbc.gridy = row;
        showTimestampsCheckBox = new JCheckBox("显示时间戳");
        mainPanel.add(showTimestampsCheckBox, gbc);

        row++;

        // 过滤JDK类
        gbc.gridy = row;
        filterJdkCheckBox = new JCheckBox("过滤JDK类");
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
        mainPanel.add(diagramTypeComboBox, gbc);

        // 添加空白填充
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
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

    public void apply() {
        settings.maxCallStackDepth = (Integer) maxDepthSpinner.getValue();
        settings.autoCapture = autoCaptureCheckBox.isSelected();
        settings.showMethodParameters = showParametersCheckBox.isSelected();
        settings.showReturnValues = showReturnValuesCheckBox.isSelected();
        settings.showTimestamps = showTimestampsCheckBox.isSelected();
        settings.filterJdkClasses = filterJdkCheckBox.isSelected();
        settings.filteredPackages = filteredPackagesField.getText();
        settings.diagramType = (String) diagramTypeComboBox.getSelectedItem();
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
    }
}
