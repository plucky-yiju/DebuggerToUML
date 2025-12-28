package com.plucky.debugger.generator;

import com.intellij.openapi.diagnostic.Logger;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * PlantUML图表渲染器
 * 将PlantUML代码渲染成图片
 * 完全离线可用，不需要外部Graphviz
 */
public class PlantUMLRenderer {

    private static final Logger LOG = Logger.getInstance(PlantUMLRenderer.class);

    /**
     * 将PlantUML代码渲染成BufferedImage
     *
     * @param plantUMLCode PlantUML代码
     * @return 渲染后的图片
     * @throws IOException 渲染失败
     */
    public static BufferedImage renderToImage(String plantUMLCode) throws IOException {
        if (plantUMLCode == null || plantUMLCode.isEmpty()) {
            throw new IllegalArgumentException("PlantUML code cannot be null or empty");
        }

        LOG.info("Starting to render PlantUML diagram");
        LOG.debug("PlantUML code length: " + plantUMLCode.length());

        try {
            // 使用SourceStringReader渲染PlantUML
            // 注意：使用!pragma layout smetana 确保使用内置的Smetana布局引擎，完全离线
            String codeWithPragma = injectSmetanaPragma(plantUMLCode);
            LOG.debug("Code with pragma: " + codeWithPragma);

            SourceStringReader reader = new SourceStringReader(codeWithPragma);

            // 渲染为PNG格式
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            reader.outputImage(baos, new FileFormatOption(FileFormat.PNG));

            // 将字节数组转换为BufferedImage
            byte[] imageBytes = baos.toByteArray();
            LOG.info("Rendered image size: " + imageBytes.length + " bytes");

            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bais);

            if (image == null) {
                throw new IOException("Failed to read rendered image");
            }

            LOG.info("Successfully rendered diagram: " + image.getWidth() + "x" + image.getHeight());
            return image;
        } catch (Exception e) {
            LOG.error("Failed to render PlantUML diagram", e);
            throw new IOException("Failed to render PlantUML diagram: " + e.getMessage(), e);
        }
    }

    /**
     * 将PlantUML代码渲染并保存为文件
     *
     * @param plantUMLCode PlantUML代码
     * @param outputFile 输出文件
     * @param format 文件格式（PNG, SVG, PDF等）
     * @throws IOException 渲染或保存失败
     */
    public static void renderToFile(String plantUMLCode, File outputFile, FileFormat format) throws IOException {
        if (plantUMLCode == null || plantUMLCode.isEmpty()) {
            throw new IllegalArgumentException("PlantUML code cannot be null or empty");
        }

        if (outputFile == null) {
            throw new IllegalArgumentException("Output file cannot be null");
        }

        // 注入Smetana pragma确保离线可用
        String codeWithPragma = injectSmetanaPragma(plantUMLCode);

        SourceStringReader reader = new SourceStringReader(codeWithPragma);

        // 渲染并保存到文件
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            reader.outputImage(fos, new FileFormatOption(format));
        }
    }

    /**
     * 注入Smetana pragma到PlantUML代码中
     * Smetana是PlantUML内置的纯Java布局引擎，不需要Graphviz
     *
     * @param plantUMLCode 原始PlantUML代码
     * @return 注入pragma后的代码
     */
    private static String injectSmetanaPragma(String plantUMLCode) {
        // 如果代码中已经包含@startuml，在其后插入pragma
        if (plantUMLCode.contains("@startuml")) {
            return plantUMLCode.replace("@startuml", "@startuml\n!pragma layout smetana");
        }
        // 否则在开头添加
        return "!pragma layout smetana\n" + plantUMLCode;
    }

    /**
     * 检查PlantUML是否可用（测试方法）
     *
     * @return true如果PlantUML可以正常工作
     */
    public static boolean isPlantUMLAvailable() {
        try {
            String testCode = "@startuml\nAlice -> Bob: test\n@enduml";
            renderToImage(testCode);
            return true;
        } catch (Exception e) {
            System.err.println("PlantUML is not available: " + e.getMessage());
            return false;
        }
    }
}
