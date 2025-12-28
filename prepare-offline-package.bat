@echo off
REM 离线部署包准备脚本 (Windows版本)
REM 在有网络的环境中运行此脚本

echo ==========================================
echo   Plucky DebuggerToUML 离线部署包准备
echo ==========================================
echo.

REM 检查是否在项目根目录
if not exist "build.gradle.kts" (
    echo ❌ 错误: 请在项目根目录运行此脚本
    exit /b 1
)

echo 步骤 1/5: 清理旧的构建...
call gradlew.bat clean

echo.
echo 步骤 2/5: 下载依赖并构建插件...
echo （这可能需要几分钟，会下载约660MB的依赖）
call gradlew.bat buildPlugin --refresh-dependencies

if not exist "build\distributions\plucky-debuggerToUML-1.0.0.zip" (
    echo ❌ 构建失败！
    exit /b 1
)

echo.
echo 步骤 3/5: 创建离线部署包目录...
set OFFLINE_DIR=offline-deployment-package
if exist "%OFFLINE_DIR%" rmdir /s /q "%OFFLINE_DIR%"
mkdir "%OFFLINE_DIR%"

echo.
echo 步骤 4/5: 复制必要文件...

REM 复制插件文件
echo   - 复制插件文件...
copy "build\distributions\*.zip" "%OFFLINE_DIR%\"

REM 复制Gradle wrapper
echo   - 复制Gradle wrapper...
mkdir "%OFFLINE_DIR%\gradle\wrapper"
copy "gradle\wrapper\gradle-wrapper.jar" "%OFFLINE_DIR%\gradle\wrapper\" 2>nul
copy "gradle\wrapper\gradle-wrapper.properties" "%OFFLINE_DIR%\gradle\wrapper\"

REM 创建安装说明
echo   - 创建安装说明...
(
echo ========================================
echo   Plucky DebuggerToUML 离线安装指南
echo ========================================
echo.
echo ## 安装步骤
echo.
echo 1. 打开 IntelliJ IDEA
echo.
echo 2. 进入插件设置
echo    Settings ^(或 Preferences^) -^> Plugins
echo.
echo 3. 安装插件
echo    点击齿轮图标 -^> Install Plugin from Disk...
echo    选择: plucky-debuggerToUML-1.0.0.zip
echo.
echo 4. 重启 IDEA
echo.
echo 5. 验证安装
echo    - 查看右侧是否有 "DebuggerToUML" 工具窗口
echo    - Settings -^> Tools -^> DebuggerToUML Settings
echo.
echo ## 使用方法
echo.
echo 1. 在代码中设置断点
echo 2. 以 Debug 模式运行程序
echo 3. 当断点触发时，插件会自动捕获调用栈
echo 4. 在右侧 "DebuggerToUML" 工具窗口查看时序图
echo 5. 点击 "渲染图表" 查看图形化时序图
echo 6. 点击 "导出图表" 保存为 PNG/SVG/PDF
echo.
echo ## 重要说明
echo.
echo ✅ 此插件完全离线可用
echo ✅ 不需要网络连接
echo ✅ 不需要外部 Graphviz
echo ✅ 所有功能都可以在离线环境中使用
echo.
echo ## 系统要求
echo.
echo - IntelliJ IDEA 2023.1 或更高版本
echo - JDK 17 或更高版本
echo.
echo ========================================
) > "%OFFLINE_DIR%\安装说明.txt"

echo.
echo ==========================================
echo   ✅ 离线部署包准备完成！
echo ==========================================
echo.
echo 生成的目录: %OFFLINE_DIR%\
echo.
echo 包含文件:
dir /b "%OFFLINE_DIR%"
echo.
echo 下一步:
echo   1. 将 %OFFLINE_DIR% 目录复制到离线环境
echo   2. 按照 安装说明.txt 安装插件
echo.
echo 注意:
echo   - 插件本身完全离线可用
echo   - 不需要网络连接
echo   - 所有依赖已打包在插件中
echo.
pause
