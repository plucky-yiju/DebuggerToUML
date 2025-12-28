package com.plucky.debugger.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 插件设置服务
 * 用于持久化保存插件的配置信息
 */
@State(
    name = "DebuggerToUMLSettings",
    storages = @Storage("DebuggerToUMLSettings.xml")
)
public class DebuggerToUMLSettings implements PersistentStateComponent<DebuggerToUMLSettings> {

    private static final Logger LOG = Logger.getInstance(DebuggerToUMLSettings.class);

    // 配置常量
    public static final int MIN_CALL_STACK_DEPTH = 1;
    public static final int MAX_CALL_STACK_DEPTH = 100;
    public static final int DEFAULT_CALL_STACK_DEPTH = 10;

    public static final String DEFAULT_FILTERED_PACKAGES = "java.lang,java.util,sun.";
    public static final String DEFAULT_DIAGRAM_TYPE = "plantuml";

    // 最大调用栈深度，默认为10
    public int maxCallStackDepth = DEFAULT_CALL_STACK_DEPTH;

    // 是否自动捕获调用栈
    public boolean autoCapture = true;

    // 是否显示方法参数
    public boolean showMethodParameters = true;

    // 是否显示返回值
    public boolean showReturnValues = false;

    // 是否显示时间戳
    public boolean showTimestamps = false;

    // 过滤的包名列表（用逗号分隔）
    public String filteredPackages = DEFAULT_FILTERED_PACKAGES;

    // 是否过滤JDK类
    public boolean filterJdkClasses = true;

    // 图表类型：plantuml 或 graphviz
    public String diagramType = DEFAULT_DIAGRAM_TYPE;

    public static DebuggerToUMLSettings getInstance() {
        return ApplicationManager.getApplication().getService(DebuggerToUMLSettings.class);
    }

    @Nullable
    @Override
    public DebuggerToUMLSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull DebuggerToUMLSettings state) {
        XmlSerializerUtil.copyBean(state, this);
        // 加载后验证配置
        validateAndFix();
    }

    /**
     * 验证并修复配置
     * 确保所有配置值都在有效范围内
     */
    public void validateAndFix() {
        boolean changed = false;

        // 验证调用栈深度
        if (maxCallStackDepth < MIN_CALL_STACK_DEPTH) {
            LOG.warn("Invalid maxCallStackDepth: " + maxCallStackDepth + ", resetting to minimum: " + MIN_CALL_STACK_DEPTH);
            maxCallStackDepth = MIN_CALL_STACK_DEPTH;
            changed = true;
        } else if (maxCallStackDepth > MAX_CALL_STACK_DEPTH) {
            LOG.warn("Invalid maxCallStackDepth: " + maxCallStackDepth + ", resetting to maximum: " + MAX_CALL_STACK_DEPTH);
            maxCallStackDepth = MAX_CALL_STACK_DEPTH;
            changed = true;
        }

        // 验证过滤包名
        if (filteredPackages == null) {
            LOG.warn("filteredPackages is null, resetting to default");
            filteredPackages = DEFAULT_FILTERED_PACKAGES;
            changed = true;
        }

        // 验证图表类型
        if (diagramType == null || diagramType.trim().isEmpty()) {
            LOG.warn("diagramType is null or empty, resetting to default");
            diagramType = DEFAULT_DIAGRAM_TYPE;
            changed = true;
        } else if (!isValidDiagramType(diagramType)) {
            LOG.warn("Invalid diagramType: " + diagramType + ", resetting to default");
            diagramType = DEFAULT_DIAGRAM_TYPE;
            changed = true;
        }

        if (changed) {
            LOG.info("Configuration was fixed, some invalid values were reset to defaults");
        }
    }

    /**
     * 验证图表类型是否有效
     */
    private boolean isValidDiagramType(String type) {
        return "plantuml".equalsIgnoreCase(type) || "graphviz".equalsIgnoreCase(type);
    }

    /**
     * 重置所有配置到默认值
     */
    public void resetToDefaults() {
        LOG.info("Resetting all settings to defaults");
        maxCallStackDepth = DEFAULT_CALL_STACK_DEPTH;
        autoCapture = true;
        showMethodParameters = true;
        showReturnValues = false;
        showTimestamps = false;
        filteredPackages = DEFAULT_FILTERED_PACKAGES;
        filterJdkClasses = true;
        diagramType = DEFAULT_DIAGRAM_TYPE;
    }

    /**
     * 验证配置是否有效
     * @return 如果配置有效返回null，否则返回错误消息
     */
    public String validate() {
        if (maxCallStackDepth < MIN_CALL_STACK_DEPTH || maxCallStackDepth > MAX_CALL_STACK_DEPTH) {
            return "调用栈深度必须在 " + MIN_CALL_STACK_DEPTH + " 到 " + MAX_CALL_STACK_DEPTH + " 之间";
        }

        if (filteredPackages == null) {
            return "过滤包名不能为null";
        }

        if (diagramType == null || diagramType.trim().isEmpty()) {
            return "图表类型不能为空";
        }

        if (!isValidDiagramType(diagramType)) {
            return "无效的图表类型: " + diagramType;
        }

        return null; // 配置有效
    }

    /**
     * 获取配置摘要信息
     */
    public String getSummary() {
        return String.format(
            "DebuggerToUML Settings:\n" +
            "  Max Depth: %d\n" +
            "  Auto Capture: %s\n" +
            "  Show Parameters: %s\n" +
            "  Show Return Values: %s\n" +
            "  Show Timestamps: %s\n" +
            "  Filter JDK: %s\n" +
            "  Filtered Packages: %s\n" +
            "  Diagram Type: %s",
            maxCallStackDepth,
            autoCapture,
            showMethodParameters,
            showReturnValues,
            showTimestamps,
            filterJdkClasses,
            filteredPackages,
            diagramType
        );
    }
}
