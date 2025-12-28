# 测试项目说明

## 目的
这个测试项目用于验证DebuggerToUML插件的功能。

## 测试步骤

### 1. 准备测试环境
1. 在IntelliJ IDEA中打开plucky-debuggerToUML项目
2. 等待Gradle同步完成
3. 运行 `./gradlew runIde` 启动插件沙箱环境

### 2. 在沙箱IDEA中测试
1. 在沙箱IDEA中打开test-project目录
2. 找到 `DemoService.java` 文件
3. 在 `main` 方法的第一行设置断点（第68行）
4. 以Debug模式运行 `DemoService.main()`

### 3. 验证插件功能
当断点触发时，插件应该：
- ✅ 自动捕获调用栈
- ✅ 在右侧显示"DebuggerToUML"工具窗口
- ✅ 显示生成的PlantUML代码
- ✅ 可以点击"渲染图表"按钮查看时序图
- ✅ 可以导出为PNG/SVG/PDF格式

### 4. 预期的调用链
```
main()
  → registerUser()
    → validateUserInfo()
    → userExists()
    → createUser()
    → save()
    → sendWelcomeEmail()
      → sendEmail()
```

## 测试场景

### 场景1：基本功能测试
- 设置断点在main方法
- 验证自动捕获
- 验证PlantUML代码生成
- 验证图表渲染

### 场景2：配置测试
1. 打开 Settings -> Tools -> DebuggerToUML Settings
2. 修改最大调用栈深度为5
3. 重新调试，验证只捕获5层
4. 启用/禁用JDK类过滤
5. 添加自定义过滤包名

### 场景3：导出测试
1. 捕获调用栈后
2. 点击"导出图表"按钮
3. 选择PNG格式
4. 验证文件生成
5. 重复测试SVG和PDF格式

### 场景4：离线测试
1. 断开网络连接
2. 重启IDEA
3. 执行所有测试场景
4. 验证所有功能正常

## 常见问题

### Q: 工具窗口没有显示
A: 检查 View -> Tool Windows -> DebuggerToUML

### Q: 没有自动捕获调用栈
A: 检查 Settings 中是否启用了"自动捕获"

### Q: 渲染失败
A: 查看IDEA的日志文件，检查错误信息

### Q: 导出失败
A: 确保有写入权限，检查磁盘空间

## 测试清单

- [ ] 基本功能：自动捕获调用栈
- [ ] 基本功能：PlantUML代码生成
- [ ] 基本功能：图表渲染
- [ ] 基本功能：图表导出（PNG）
- [ ] 基本功能：图表导出（SVG）
- [ ] 基本功能：图表导出（PDF）
- [ ] 配置：修改调用栈深度
- [ ] 配置：JDK类过滤
- [ ] 配置：自定义包过滤
- [ ] 配置：关闭自动捕获
- [ ] 离线：断网后所有功能正常
- [ ] 性能：大调用栈（深度>20）
- [ ] 错误处理：无调试会话时的提示
- [ ] 错误处理：空调用栈的处理
