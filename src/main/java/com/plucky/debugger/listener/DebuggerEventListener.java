package com.plucky.debugger.listener;

import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManagerListener;
import com.plucky.debugger.capture.CallStackCapture;
import com.plucky.debugger.config.DebuggerToUMLSettings;
import org.jetbrains.annotations.NotNull;

/**
 * 调试事件监听器
 * 监听调试会话的启动、停止和断点触发事件
 */
public class DebuggerEventListener implements XDebuggerManagerListener {

    @Override
    public void processStarted(@NotNull XDebugSession session) {
        // 调试会话启动时的处理
        System.out.println("Debug session started: " + session.getSessionName());
    }

    @Override
    public void processStopped(@NotNull XDebugSession session) {
        // 调试会话停止时的处理
        System.out.println("Debug session stopped: " + session.getSessionName());
    }

    @Override
    public void currentSessionChanged(XDebugSession previousSession, XDebugSession currentSession) {
        // 当前调试会话改变时的处理
        if (currentSession != null) {
            DebuggerToUMLSettings settings = DebuggerToUMLSettings.getInstance();

            // 如果启用了自动捕获，则捕获调用栈
            if (settings.autoCapture) {
                CallStackCapture.captureCallStack(currentSession);
            }
        }
    }
}
