package com.plucky.debugger.logger;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.plucky.debugger.model.DebugLogInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 调试日志输出器
 * 将调试信息输出到IDEA日志和独立文件
 */
public class DebugLogWriter {

    private static final Logger LOG = Logger.getInstance(DebugLogWriter.class);
    private static final String LOG_DIR = "debugger-to-uml-logs";

    /**
     * 输出调试日志（同步）
     */
    public static void writeLog(DebugLogInfo info) {
        if (info == null) {
            return;
        }

        String logContent = DebugLogFormatter.format(info);

        // 输出到IDEA日志
        LOG.info(logContent);

        // 输出到独立文件
        writeToFile(logContent);
    }

    /**
     * 异步输出调试日志
     */
    public static void writeLogAsync(DebugLogInfo info) {
        if (info == null) {
            return;
        }

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            writeLog(info);
        });
    }

    /**
     * 写入文件
     */
    private static void writeToFile(String content) {
        try {
            // 创建日志目录
            File logDir = new File(LOG_DIR);
            if (!logDir.exists()) {
                boolean created = logDir.mkdirs();
                if (!created) {
                    LOG.warn("Failed to create log directory: " + LOG_DIR);
                    return;
                }
            }

            // 生成日志文件名（按日期）
            String fileName = "debug-log-" +
                new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".txt";
            File logFile = new File(logDir, fileName);

            // 追加写入
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(content);
                writer.flush();
                LOG.debug("Debug log written to file: " + logFile.getAbsolutePath());
            }
        } catch (IOException e) {
            LOG.error("Failed to write debug log to file", e);
        }
    }

    /**
     * 获取日志目录路径
     */
    public static String getLogDirectory() {
        return new File(LOG_DIR).getAbsolutePath();
    }
}
