package com.plucky.debugger.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
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

    // 最大调用栈深度，默认为10
    public int maxCallStackDepth = 10;

    // 是否自动捕获调用栈
    public boolean autoCapture = true;

    // 是否显示方法参数
    public boolean showMethodParameters = true;

    // 是否显示返回值
    public boolean showReturnValues = false;

    // 是否显示时间戳
    public boolean showTimestamps = false;

    // 过滤的包名列表（用逗号分隔）
    public String filteredPackages = "java.lang,java.util,sun.";

    // 是否过滤JDK类
    public boolean filterJdkClasses = true;

    // 图表类型：plantuml 或 graphviz
    public String diagramType = "plantuml";

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
    }
}
