package com.example.demo;

/**
 * 示例服务类 - 用于测试DebuggerToUML插件
 *
 * 使用方法：
 * 1. 在main方法的第一行设置断点
 * 2. 以Debug模式运行
 * 3. 当断点触发时，插件会自动捕获调用栈
 * 4. 在右侧的"DebuggerToUML"工具窗口查看生成的时序图
 */
public class DemoService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public DemoService() {
        this.userRepository = new UserRepository();
        this.emailService = new EmailService();
    }

    /**
     * 用户注册流程
     * 这个方法会调用多个其他方法，形成调用链
     */
    public void registerUser(String username, String email) {
        System.out.println("开始注册用户: " + username);

        // 验证用户信息
        if (!validateUserInfo(username, email)) {
            throw new IllegalArgumentException("用户信息无效");
        }

        // 检查用户是否已存在
        if (userRepository.userExists(username)) {
            throw new IllegalStateException("用户已存在");
        }

        // 创建用户
        User user = createUser(username, email);

        // 保存用户
        userRepository.save(user);

        // 发送欢迎邮件
        sendWelcomeEmail(user);

        System.out.println("用户注册成功: " + username);
    }

    /**
     * 验证用户信息
     */
    private boolean validateUserInfo(String username, String email) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (email == null || !email.contains("@")) {
            return false;
        }
        return true;
    }

    /**
     * 创建用户对象
     */
    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setCreatedAt(System.currentTimeMillis());
        return user;
    }

    /**
     * 发送欢迎邮件
     */
    private void sendWelcomeEmail(User user) {
        String subject = "欢迎注册";
        String content = "欢迎您，" + user.getUsername() + "！";
        emailService.sendEmail(user.getEmail(), subject, content);
    }

    /**
     * 主方法 - 在这里设置断点进行测试
     */
    public static void main(String[] args) {
        // 在这一行设置断点！
        DemoService service = new DemoService();

        try {
            // 测试用户注册
            service.registerUser("testuser", "test@example.com");
        } catch (Exception e) {
            System.err.println("注册失败: " + e.getMessage());
        }
    }
}

/**
 * 用户实体类
 */
class User {
    private String username;
    private String email;
    private long createdAt;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}

/**
 * 用户仓储类
 */
class UserRepository {

    public boolean userExists(String username) {
        // 模拟数据库查询
        System.out.println("检查用户是否存在: " + username);
        return false;
    }

    public void save(User user) {
        // 模拟保存到数据库
        System.out.println("保存用户到数据库: " + user.getUsername());
    }
}

/**
 * 邮件服务类
 */
class EmailService {

    public void sendEmail(String to, String subject, String content) {
        // 模拟发送邮件
        System.out.println("发送邮件到: " + to);
        System.out.println("主题: " + subject);
        System.out.println("内容: " + content);
    }
}
