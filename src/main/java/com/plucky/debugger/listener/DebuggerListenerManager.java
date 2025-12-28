package com.plucky.debugger.listener;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebuggerManager;

/**
 * 调试监听器管理器
 * 作为项目级别的服务，负责注册和管理调试事件监听器
 */
public class DebuggerListenerManager implements Disposable {

    private static final Logger LOG = Logger.getInstance(DebuggerListenerManager.class);

    private final Project project;
    private DebuggerEventListener listener;

    public DebuggerListenerManager(Project project) {
        this.project = project;
        init();
    }

    private void init() {
        // 创建监听器
        listener = new DebuggerEventListener(project);

        // 注册监听器到XDebuggerManager
        project.getMessageBus().connect(this).subscribe(XDebuggerManager.TOPIC, listener);

        LOG.info("DebuggerListenerManager initialized for project: " + project.getName());
    }

    @Override
    public void dispose() {
        // 清理资源
        LOG.info("DebuggerListenerManager disposed for project: " + project.getName());
    }
}
