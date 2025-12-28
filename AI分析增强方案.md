# AI代码分析增强方案

## 问题分析

### 当前插件输出的信息
1. ✅ 类名和方法名
2. ✅ 调用顺序和层级
3. ✅ 行号
4. ⚠️ 参数信息（可能不完整）
5. ⚠️ 返回值（可能不完整）
6. ❌ 变量值
7. ❌ 方法源码
8. ❌ 执行路径信息
9. ❌ 异常信息

### AI分析代码逻辑需要的关键信息

#### 1. 调用链信息（已有）✅
- 方法调用顺序
- 调用层级关系
- 类和方法名称

#### 2. 数据流信息（部分缺失）⚠️
- **参数实际值** - 不只是类型，还要有实际值
- **返回值** - 方法返回的实际数据
- **变量值** - 关键变量在执行过程中的值
- **对象状态** - 对象字段的值

#### 3. 控制流信息（缺失）❌
- **条件分支** - if/else走了哪个分支
- **循环次数** - 循环执行了多少次
- **异常信息** - 是否抛出异常，异常类型

#### 4. 代码上下文（缺失）❌
- **方法源码** - 方法的实际代码
- **注释信息** - 方法的文档注释
- **方法签名** - 完整的方法签名（包括修饰符、返回类型）

#### 5. 性能信息（部分有）⚠️
- **执行时间** - 方法执行耗时
- **调用次数** - 方法被调用的次数

## 增强方案

### 方案1: 增强文本报告导出（推荐）

#### 目��
导出一个详细的文本报告，包含所有调试信息，便于AI分析。

#### 导出格式示例
```
=== 调用栈分析报告 ===
捕获时间: 2025-12-29 10:30:45
断点位置: com.example.UserService.login:45
总方法数: 15
总执行时间: 234ms

=== 调用链详情 ===

[1] com.example.controller.UserController.login(String, String)
  位置: UserController.java:23
  参数:
    - username: "admin"
    - password: "******"
  调用时间: 10:30:45.123
  执行耗时: 234ms

  [2] com.example.service.UserService.authenticate(String, String)
    位置: UserService.java:45
    参数:
      - username: "admin"
      - password: "******"
    调用时间: 10:30:45.125
    执行耗时: 220ms

    [3] com.example.dao.UserDao.findByUsername(String)
      位置: UserDao.java:67
      参数:
        - username: "admin"
      返回值: User{id=1, username="admin", role="ADMIN"}
      调用时间: 10:30:45.130
      执行耗时: 50ms

    [3] com.example.util.PasswordEncoder.matches(String, String)
      位置: PasswordEncoder.java:34
      参数:
        - rawPassword: "******"
        - encodedPassword: "$2a$10$..."
      返回值: true
      调用时间: 10:30:45.185
      执行耗时: 150ms

  返回值: User{id=1, username="admin", role="ADMIN"}

返回值: LoginResponse{success=true, token="eyJhbGc..."}

=== 类路径列表 ===
1. com.example.controller.UserController
2. com.example.service.UserService
3. com.example.dao.UserDao
4. com.example.util.PasswordEncoder

=== 方法调用统计 ===
- 总方法数: 15
- 最深层级: 4
- 最慢方法: PasswordEncoder.matches (150ms)
- 调用最多: StringUtils.isEmpty (5次)

=== PlantUML代码 ===
@startuml
...
@enduml
```

#### 实现要点
1. **层级缩进** - 用缩进表示调用层级
2. **完整信息** - 包含参数值、返回值、时间
3. **统计信息** - 提供调用统计
4. **PlantUML代码** - 附带原始PlantUML代码

### 方案2: JSON格式导出

#### 目标
导出结构化的JSON数据，便于程序化处理和AI解析。

#### 导出格式示例
```json
{
  "captureTime": "2025-12-29T10:30:45",
  "breakpointLocation": "com.example.UserService.login:45",
  "totalMethods": 15,
  "totalDuration": 234,
  "callStack": [
    {
      "index": 1,
      "className": "com.example.controller.UserController",
      "methodName": "login",
      "fullSignature": "public LoginResponse login(String username, String password)",
      "location": "UserController.java:23",
      "depth": 0,
      "parameters": [
        {"name": "username", "type": "String", "value": "admin"},
        {"name": "password", "type": "String", "value": "******"}
      ],
      "returnValue": {
        "type": "LoginResponse",
        "value": "LoginResponse{success=true, token=\"eyJhbGc...\"}"
      },
      "timestamp": 1735441845123,
      "duration": 234,
      "children": [
        {
          "index": 2,
          "className": "com.example.service.UserService",
          "methodName": "authenticate",
          ...
        }
      ]
    }
  ],
  "classes": [
    "com.example.controller.UserController",
    "com.example.service.UserService",
    ...
  ],
  "statistics": {
    "totalMethods": 15,
    "maxDepth": 4,
    "slowestMethod": {
      "method": "PasswordEncoder.matches",
      "duration": 150
    }
  },
  "plantUML": "@startuml\n..."
}
```

### 方案3: Markdown格式导出

#### 目标
导出易读的Markdown文档，便于人类阅读和AI分析。

#### 导出格式示例
```markdown
# 调用栈分析报告

## 基本信息
- **捕获时间**: 2025-12-29 10:30:45
- **断点位置**: com.example.UserService.login:45
- **总方法数**: 15
- **总执行时间**: 234ms

## 调用链详情

### 1. UserController.login
- **完整路径**: com.example.controller.UserController.login
- **位置**: UserController.java:23
- **参数**:
  - username: "admin"
  - password: "******"
- **返回值**: LoginResponse{success=true, token="eyJhbGc..."}
- **执行耗时**: 234ms

#### 1.1 UserService.authenticate
- **完整路径**: com.example.service.UserService.authenticate
- **位置**: UserService.java:45
- **参数**:
  - username: "admin"
  - password: "******"
- **返回值**: User{id=1, username="admin", role="ADMIN"}
- **执行耗时**: 220ms

##### 1.1.1 UserDao.findByUsername
- **完整路径**: com.example.dao.UserDao.findByUsername
- **位置**: UserDao.java:67
- **参数**:
  - username: "admin"
- **返回值**: User{id=1, username="admin", role="ADMIN"}
- **执行耗时**: 50ms

...

## 类路径列表
1. com.example.controller.UserController
2. com.example.service.UserService
3. com.example.dao.UserDao
4. com.example.util.PasswordEncoder

## 方法调用统计
- **总方法数**: 15
- **最深层级**: 4
- **最慢方法**: PasswordEncoder.matches (150ms)
- **调用最多**: StringUtils.isEmpty (5次)

## PlantUML时序图
\`\`\`plantuml
@startuml
...
@enduml
\`\`\`
```

## 需要增强的调试信息捕获

### 1. 参数实际值捕获
**当前**: 只捕获参数类型
**需要**: 捕获参数的实际值

**实现方法**:
```java
// 在CallStackCapture中增强
StackFrame frame = ...;
List<LocalVariable> variables = frame.visibleVariables();
for (LocalVariable var : variables) {
    Value value = frame.getValue(var);
    String valueStr = value.toString();
    // 保存参数值
}
```

### 2. 返回值捕获
**当前**: 部分支持
**需要**: 完整捕获方法返回值

**实现方法**:
```java
// 监听方法退出事件
MethodExitEvent exitEvent = ...;
Value returnValue = exitEvent.returnValue();
String returnValueStr = returnValue != null ? returnValue.toString() : "void";
```

### 3. 局部变量捕获
**当前**: 不支持
**需要**: 捕获关键局部变量的值

**实现方法**:
```java
// 捕获所有可见的局部变量
List<LocalVariable> localVars = frame.visibleVariables();
Map<String, String> variables = new HashMap<>();
for (LocalVariable var : localVars) {
    Value value = frame.getValue(var);
    variables.put(var.name(), value.toString());
}
```

### 4. 对象字段值捕获
**当前**: 不支持
**需要**: 捕获对象的字段值

**实现方法**:
```java
// 捕获this对象的字段
ObjectReference thisObject = frame.thisObject();
if (thisObject != null) {
    Map<Field, Value> fields = thisObject.getValues(thisObject.referenceType().allFields());
    // 保存字段值
}
```

### 5. 异常信息捕获
**当前**: 不支持
**需要**: 捕获抛出的异常

**实现方法**:
```java
// 监听异常事件
ExceptionEvent exceptionEvent = ...;
ObjectReference exception = exceptionEvent.exception();
String exceptionType = exception.referenceType().name();
String exceptionMessage = ...;
```

### 6. 执行时间捕获
**当前**: 只有时间戳
**需要**: 方法执行耗时

**实现方法**:
```java
// 记录方法进入和退出时间
long enterTime = System.currentTimeMillis();
// ... 方法执行
long exitTime = System.currentTimeMillis();
long duration = exitTime - enterTime;
```

## 推荐实现优先级

### 第一优先级（立即实现）
1. **增强文本报告导出** - 最容易实现，最有价值
2. **参数实际值捕获** - 对AI分析最关键
3. **返回值完整捕获** - 理解数据流必需

### 第二优先级（短期实现）
4. **Markdown格式导出** - 易读性好
5. **执行时间统计** - 性能分析有用
6. **局部变量捕获** - 理解逻辑有帮助

### 第三优先级（长期实现）
7. **JSON格式导出** - 程序化处理
8. **对象字段值捕获** - 深度分析
9. **异常信息捕获** - 错误分析

## AI分析时需要的信息清单

### 必需信息（当前已有）✅
- [x] 完整的调用链
- [x] 类名和方法名
- [x] 调用顺序
- [x] 调用层级

### 重要信息（部分缺失）⚠️
- [ ] 参数实际值（当前只有类型）
- [ ] 返回值（当前不完整）
- [ ] 方法执行耗时
- [ ] 行号信息

### 有用信息（当前缺失）❌
- [ ] 局部变量值
- [ ] 对象字段值
- [ ] 异常信息
- [ ] 条件分支信息
- [ ] 循环执行信息

### 可选信息（增强分析）
- [ ] 方法源码片段
- [ ] 方法注释
- [ ] 完整方法签名
- [ ] 调用统计信息

## 使用场景示例

### 场景1: 分析业务逻辑流程
**用户提供**:
- 增强文本报告（包含完整调用链和参数值）
- 关键类的类路径列表

**AI可以分析**:
- 业务流程的执行顺序
- 数据在各层之间的传递
- 哪些服务/DAO被调用
- 参数如何转换和传递

### 场景2: 排查Bug
**用户提供**:
- 增强文本报告（包含参数值和返回值）
- 异常发生的位置

**AI可以分析**:
- 哪个方法返回了异常值
- 参数传递是否正确
- 数据转换是否有问题
- 调用顺序是否符合预期

### 场景3: 性能分析
**用户提供**:
- 增强文本报告（包含执行时间）
- 性能统计信息

**AI可以分析**:
- 哪些方法执行慢
- 是否有不必要的重复调用
- 调用层级是否过深
- 优化建议

### 场景4: 理解遗留代码
**用户提供**:
- 增强文本报告（完整调用链）
- 类路径列表

**AI可以分析**:
- 代码的整体架构
- 各层之间的职责划分
- 依赖关系
- 设计模式的使用

## 实现建议

### 新增导出选项
在导出对话框中增加格式选择：
- PNG（图片）
- SVG（矢量图）
- PDF（文档）
- **TXT（文本报告）** ← 新增
- **MD（Markdown报告）** ← 新增
- **JSON（结构化数据）** ← 新增

### 新增数据模型
```java
public class EnhancedMethodCallInfo extends MethodCallInfo {
    private List<ParameterInfo> parameterValues;  // 参数实际值
    private String returnValueDetail;              // 详细返回值
    private Map<String, String> localVariables;    // 局部变量
    private long duration;                         // 执行耗时
    private ExceptionInfo exception;               // 异常信息
}

public class ParameterInfo {
    private String name;
    private String type;
    private String value;
}

public class ExceptionInfo {
    private String type;
    private String message;
    private String stackTrace;
}
```

### 新增导出器
```java
public class TextReportExporter {
    public String export(CallStackInfo callStack);
}

public class MarkdownReportExporter {
    public String export(CallStackInfo callStack);
}

public class JsonReportExporter {
    public String export(CallStackInfo callStack);
}
```

## 总结

为了让AI更好地分析代码逻辑，插件需要：

1. **增强信息捕获**
   - 参数实际值
   - 返回值详情
   - 执行时间
   - 局部变量（可选）

2. **新增导出格式**
   - 文本报告（推荐）
   - Markdown报告
   - JSON数据

3. **提供更多上下文**
   - 调用统计
   - 性能分析
   - 类路径列表

这样用户就可以在离线环境中：
1. 使用插件捕获调试信息
2. 导出详细的文本报告
3. 将报告拿出来给AI分析
4. AI基于报告理解代码逻辑和业务流程
