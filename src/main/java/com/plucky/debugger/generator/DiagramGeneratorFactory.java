package com.plucky.debugger.generator;

import com.plucky.debugger.config.DebuggerToUMLSettings;

/**
 * 图表生成器工厂
 * 根据配置创建相应的图表生成器
 */
public class DiagramGeneratorFactory {

    /**
     * 创建图表生成器
     *
     * @return 图表生成器实例
     */
    public static DiagramGenerator createGenerator() {
        DebuggerToUMLSettings settings = DebuggerToUMLSettings.getInstance();
        String diagramType = settings.diagramType;

        if ("graphviz".equalsIgnoreCase(diagramType)) {
            // 暂时返回PlantUML生成器，后续可以实现Graphviz生成器
            return new PlantUMLGenerator();
        } else {
            // 默认使用PlantUML
            return new PlantUMLGenerator();
        }
    }
}
