# NoiProject（Noi 信息管理系统）

> 基于 Spring Boot 3 + MyBatis-Plus + Swing 的 TCP C/S 架构信息管理系统（带桌面 UI）

## 项目简介

本仓库是一个**带桌面端 UI 的 C/S 架构 Java 项目**，演示了如何把传统的 TCP 通信封装到 Spring Boot 工程里，并用 Swing 提供可视化操作界面。

整套系统分四个模块：

| 模块        | 作用                                                          |
| ----------- | ------------------------------------------------------------- |
| `noi-server`| TCP **服务端**，内置 Swing ServerUI/ServerView                |
| `noi-client`| TCP **客户端**，同时启动 3 个 ClientUI 窗口模拟多客户端      |
| `noi-service`| 业务服务层（DAO / Entity / Service / 公共响应 JsonResult）   |
| `test_tcp`  | 独立 Java 模块：纯 Socket + Swing 的 TCP Demo（教学用）        |

适合作为：
- Spring Boot + Swing 桌面混合架构的参考
- MyBatis-Plus 多模块拆分（pojo / dao / service / server）的练习
- TCP 自定义协议 + JsonResult 通信的入门示例

## 技术栈

| 类别        | 技术 / 版本                                |
| ----------- | ------------------------------------------ |
| 基础框架    | Spring Boot **3.4.8**                      |
| 持久层      | MyBatis-Plus **3.5.12**（Spring Boot 3 适配）|
| 数据库      | MySQL 8                                    |
| 分页        | PageHelper Spring Boot Starter 2.1.1        |
| 鉴权        | jjwt 0.12.5                                |
| 工具        | Lombok / commons-lang3 3.18                |
| 序列化      | **Gson 2.10**（客户端用）                  |
| 桌面 UI     | **Swing**（JFrame / JTextArea / JButton）  |
| 通信        | **TCP Socket**（自实现协议）               |
| 构建工具    | Maven 3.8+                                 |
| JDK         | **17**（test_tcp 模块使用 24）             |

## 项目结构

```
NoiProject/
├── pom.xml                     # 父 POM（Spring Boot 3.4.8）
├── identifier.sqlite           # 内置 SQLite 数据（标识符）
├── noi-server/                 # TCP 服务端
│   ├── pom.xml
│   └── src/main/java/com/tjetc/
│       ├── NoiServerApplication.java   # 启动 + 打开 ServerUI
│       ├── controller/InformationController.java
│       ├── ui/ServerUI.java            # Swing 主窗口
│       └── ui/ServerView.java          # Swing 视图
├── noi-service/                # 业务层（pojo / dao / service）
│   ├── pom.xml
│   └── src/main/java/com/tjetc/
│       ├── common/JsonResult.java
│       ├── dao/InformationMapper.java
│       ├── entity/Information.java
│       └── service/ + impl/
├── noi-client/                 # TCP 客户端（启动 3 个 ClientUI）
│   ├── pom.xml
│   └── src/main/java/com/tjetc/
│       ├── NoiClientApplication.java    # 默认启动 3 个客户端窗口
│       ├── config/
│       └── ui/ClientUI.java
└── test_tcp/                   # 独立 TCP Demo（不依赖 Spring）
    ├── pom.xml
    └── src/main/java/
        ├── ClientUI.java
        ├── ServerUI.java
        └── test.java
```

## 核心功能

- ✅ Spring Boot 3 多模块工程
- ✅ MyBatis-Plus 实体 / DAO / Service 经典三层
- ✅ TCP 服务端 + Swing UI 实时显示客户端连接 / 消息
- ✅ TCP 客户端 + Swing UI，支持自定义 IP / 端口
- ✅ 默认一次性启动 **3 个客户端窗口**，便于多端联调
- ✅ JsonResult 统一响应体
- ✅ Gson 进行 JSON 序列化（跨模块通信）
- ✅ 独立 `test_tcp` 模块：纯 Socket / Swing / IO 教学示例

## 环境要求

- JDK **17**（服务端 / 客户端）
- JDK 24（test_tcp，可选）
- Maven 3.8+
- MySQL 8.0+
- Swing 桌面环境（Windows / macOS / Linux + X11）

## 快速开始

### 1. 克隆代码
```bash
git clone https://github.com/asgudao/NoiProject.git
cd NoiProject
```

### 2. 初始化数据库
执行 `weixin.sql`（如有）或手动创建 `noi` 数据库后建表（`information` 表对应 `Information` 实体）。

修改 `noi-server/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/noi?serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 3. 编译打包
```bash
mvn clean install
```

### 4. 启动服务端
```bash
mvn -pl noi-server spring-boot:run
# 启动后自动打开 ServerUI 窗口
```

### 5. 启动客户端
```bash
mvn -pl noi-client spring-boot:run
# 默认启动 3 个 ClientUI 窗口，连接 127.0.0.1:8888
```

### 6. 单独运行 TCP Demo（可选）
```bash
cd test_tcp
# 先启动 ServerUI，再启动 ClientUI
java -cp target/classes ServerUI
java -cp target/classes ClientUI
```

## 协议说明

服务端与客户端之间使用 **JSON + 换行符** 的文本协议：

```json
{ "type": "MSG", "from": "client-1", "content": "hello" }
{ "type": "BROADCAST", "content": "system notification" }
```

所有消息通过 `JsonResult` 包装：
```json
{ "code": 200, "msg": "ok", "data": { ... } }
```

## 后续可扩展方向

- [ ] 引入 Netty 替代原生 Socket，提升高并发能力
- [ ] 引入 Protobuf 替换 JSON
- [ ] 拆分为微服务（Spring Cloud / Dubbo）
- [ ] 引入 WebSocket 实现浏览器 ↔ 桌面端双向通信
- [ ] GUI 改用 JavaFX 或前端 Web 化（Electron / Tauri）

## License

未指定 License。建议以个人 / 教学项目为主。
