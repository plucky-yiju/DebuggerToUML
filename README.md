# Plucky DebuggerToUML

一个强大的IntelliJ IDEA插件，可以在调试过程中自动生成调用链的时序图和思维导图。

## 功能特性

- **自动捕获调用栈**：在调试过程中自动捕获方法调用链
- **时序图生成**：使用PlantUML生成清晰的时序图
- **灵活配置**：支持调用栈深度限制、类/方法过滤等配置
- **多种导出格式**：支持导出为PNG、SVG、PDF格式
- **Java源码和Class文件支持**：可以解析Java源码和编译后的class文件

## 安装

### 从源码构建

1. 克隆仓库：
```bash
git clone <repository-url>
cd plucky-debuggerToUML
```

2. 构建插件：
```bash
./gradlew buildPlugin
```

3. 安装插件：
   - 打开IntelliJ IDEA
   - 进入 `Settings` -> `Plugins` -> `Install Plugin from Disk`
   - 选择 `build/distributions/plucky-debuggerToUML-1.0.0.zip`

## 使用方法

1. **设置断点**：在你想要分析的代码位置设置断点
2. **启动调试**：使用Debug模式运行程序
3. **捕获调用栈**：
   - 方式1：当断点触发时，插件会自动捕获调用栈（如果启用了自动捕获）
   - 方式2：手动点击工具栏的"捕获调用栈"按钮
4. **查看时序图**：在右侧的"DebuggerToUML"工具窗口中查看生成的PlantUML代码
5. **导出图表**：点击"导出图表"按钮将图表保存为文件

## 配置选项

进入 `Settings` -> `Tools` -> `DebuggerToUML Settings` 进行配置：

- **最大调用栈深度**：限制捕获的调用栈层级（默认：10）
- **自动捕获**：断点触发时自动捕获调用栈（默认：启用）
- **显示方法参数**：在时序图中显示方法参数（默认：启用）
- **显示返回值**：在时序图中显示返回值（默认：禁用）
- **显示时间戳**：在时序图中显示时间戳（默认：禁用）
- **过滤JDK类**：过滤Java标准库的类（默认：启用）
- **过滤的包名**：自定义要过滤的包名列表

## 技术栈

- Java 17+
- IntelliJ Platform SDK
- Gradle 8.x
- PlantUML
- ASM（用于class文件解析）

## 开发

### 环境要求

- JDK 17或更高版本
- IntelliJ IDEA 2023.1或更高版本
- Gradle 8.x

### 开发命令

```bash
# 构建插件
./gradlew buildPlugin

# 在沙箱环境中运行插件
./gradlew runIde

# 运行测试
./gradlew test

# 清理构建
./gradlew clean
```

## 项目结构

```
plucky-debuggerToUML/
├── src/
│   ├── main/
│   │   ├── java/com/plucky/debugger/
│   │   │   ├── actions/         # 用户动作
│   │   │   ├── capture/         # 调用栈捕获
│   │   │   ├── config/          # 配置管理
│   │   │   ├── generator/       # 图表生成器
│   │   │   ├── listener/        # 调试监听器
│   │   │   ├── model/           # 数据模型
│   │   │   ├── parser/          # Class文件解析
│   │   │   └── ui/              # 用户��面
│   │   └── resources/
│   │       └── META-INF/
│   │           └── plugin.xml   # 插件配置
│   └── test/
├── build.gradle.kts             # Gradle构建脚本
├── 项目需求文档.md               # 需求文档
├── 项目构建文档.md               # 构建文档
└── README.md                    # 本文件
```

## 贡献

欢迎提交Issue和Pull Request！

## 许可证

MIT License

## 联系方式

- Email: support@plucky.com
- Website: https://www.plucky.com
