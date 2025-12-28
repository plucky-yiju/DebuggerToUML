package com.plucky.debugger.model;

import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 捕获历史记录
 * 保存单次调试会话的完整数据
 */
public class CaptureHistory {

    private final String timestamp;
    private final String plantUMLCode;
    private final String classListText;
    private BufferedImage renderedImage;
    private final int methodCount;
    private final CallStackInfo callStackInfo;  // 新增：保存原始调用栈信息

    public CaptureHistory(String plantUMLCode, String classListText, int methodCount) {
        this(plantUMLCode, classListText, methodCount, null);
    }

    public CaptureHistory(String plantUMLCode, String classListText, int methodCount, CallStackInfo callStackInfo) {
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.plantUMLCode = plantUMLCode;
        this.classListText = classListText;
        this.methodCount = methodCount;
        this.callStackInfo = callStackInfo;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPlantUMLCode() {
        return plantUMLCode;
    }

    public String getClassListText() {
        return classListText;
    }

    public BufferedImage getRenderedImage() {
        return renderedImage;
    }

    public void setRenderedImage(BufferedImage renderedImage) {
        this.renderedImage = renderedImage;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public CallStackInfo getCallStackInfo() {
        return callStackInfo;
    }

    /**
     * 获取显示标签
     */
    public String getDisplayLabel() {
        return String.format("[%s] %d个方法", timestamp, methodCount);
    }

    @Override
    public String toString() {
        return getDisplayLabel();
    }
}
