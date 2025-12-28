package com.plucky.debugger.generator;

import com.plucky.debugger.model.CallStackInfo;

/**
 * 图表生成器接口
 * 定义生成时序图的通用接口
 */
public interface DiagramGenerator {

    /**
     * 生成图表
     *
     * @param callStackInfo 调用栈信息
     * @return 图表的文本表示（如PlantUML代码）
     */
    String generateDiagram(CallStackInfo callStackInfo);

    /**
     * 获取图表格式
     *
     * @return 格式名称（如 "plantuml", "graphviz"）
     */
    String getFormat();
}
