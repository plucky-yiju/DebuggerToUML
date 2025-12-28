package com.plucky.debugger.config;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 插件配置界面
 * 提供用户可配置的选项
 */
public class DebuggerToUMLConfigurable implements Configurable {

    private DebuggerToUMLSettingsPanel settingsPanel;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "DebuggerToUML";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsPanel = new DebuggerToUMLSettingsPanel();
        return settingsPanel.getPanel();
    }

    @Override
    public boolean isModified() {
        return settingsPanel != null && settingsPanel.isModified();
    }

    @Override
    public void apply() {
        if (settingsPanel != null) {
            settingsPanel.apply();
        }
    }

    @Override
    public void reset() {
        if (settingsPanel != null) {
            settingsPanel.reset();
        }
    }

    @Override
    public void disposeUIResources() {
        settingsPanel = null;
    }
}
