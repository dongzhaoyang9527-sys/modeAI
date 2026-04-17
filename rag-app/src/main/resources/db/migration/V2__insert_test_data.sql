-- ============================================================
-- V2__insert_test_data.sql
-- 测试/演示数据迁移脚本
-- ============================================================

-- -----------------------------------------------------------
-- 1. admin 用户角色关联
-- -----------------------------------------------------------
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
    (1, 1),   -- admin → ROLE_ADMIN
    (1, 3);   -- admin → ROLE_DOC_ADMIN

-- -----------------------------------------------------------
-- 2. 新增测试用户
--    密码 123456 的 BCrypt 加密值
-- -----------------------------------------------------------
INSERT INTO sys_user (username, password, nickname, email, enabled) VALUES
    ('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKj3mNPqG', '张三', 'zhangsan@modeai.com', TRUE),
    ('lisi',     '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKj3mNPqG', '李四', 'lisi@modeai.com', TRUE),
    ('wangwu',   '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKj3mNPqG', '王五', 'wangwu@modeai.com', TRUE);

-- -----------------------------------------------------------
-- 3. 测试用户角色关联
-- -----------------------------------------------------------
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
CROSS JOIN sys_role r
WHERE u.username IN ('zhangsan', 'lisi', 'wangwu')
  AND r.role_code IN ('ROLE_USER', 'ROLE_DOC_ADMIN')
  AND NOT (
      (u.username = 'zhangsan' AND r.role_code = 'ROLE_DOC_ADMIN')
      OR (u.username = 'wangwu' AND r.role_code = 'ROLE_DOC_ADMIN')
  );

-- -----------------------------------------------------------
-- 4. 用户部门关联
--    部门: 技术部=1, 产品部=2, 运营部=3, 人力资源部=4
-- -----------------------------------------------------------
INSERT IGNORE INTO sys_user_department (user_id, department_id) VALUES
    (1, 1),   -- admin  → 技术部
    (2, 1),   -- zhangsan → 技术部
    (3, 2),   -- lisi   → 产品部
    (4, 3);   -- wangwu → 运营部

-- -----------------------------------------------------------
-- 5. 文档数据
--    access_level: 0=公开, 1=部门, 2=机密, 3=绝密
--    status: PENDING / PROCESSING / COMPLETED / FAILED
-- -----------------------------------------------------------
INSERT INTO kb_document (title, file_name, file_path, file_size, file_type, content_hash, department_id, access_level, status, chunk_count, created_by, created_at) VALUES
    ('Spring Boot 3 新特性指南',
     'spring-boot-3-guide.pdf',
     '/data/documents/spring-boot-3-guide.pdf',
     2048000,
     'PDF',
     'a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2',
     1,    -- 技术部
     0,    -- 公开
     'COMPLETED',
     3,
     1,    -- admin
     '2024-06-15 10:30:00'),

    ('企业信息安全管理制度',
     'info-security-policy.pdf',
     '/data/documents/info-security-policy.pdf',
     1536000,
     'PDF',
     'b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3',
     4,    -- 人力资源部
     2,    -- 机密
     'COMPLETED',
     2,
     1,
     '2024-06-18 14:20:00'),

    ('2024年度产品规划',
     '2024-product-plan.pdf',
     '/data/documents/2024-product-plan.pdf',
     3072000,
     'PDF',
     'c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4',
     2,    -- 产品部
     1,    -- 部门
     'COMPLETED',
     3,
     1,
     '2024-07-01 09:00:00'),

    ('API 接口设计规范',
     'api-design-standard.pdf',
     '/data/documents/api-design-standard.pdf',
     1024000,
     'PDF',
     'd4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5',
     1,    -- 技术部
     0,    -- 公开
     'PROCESSING',
     0,
     1,
     '2024-07-10 16:45:00'),

    ('Q4 运营数据分析报告',
     'q4-operation-report.pdf',
     '/data/documents/q4-operation-report.pdf',
     4096000,
     'PDF',
     'e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6',
     3,    -- 运营部
     1,    -- 部门
     'PENDING',
     0,
     1,
     '2024-07-15 11:30:00'),

    ('新员工入职手册',
     'new-employee-handbook.pdf',
     '/data/documents/new-employee-handbook.pdf',
     2560000,
     'PDF',
     'f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1',
     4,    -- 人力资源部
     0,    -- 公开
     'COMPLETED',
     2,
     1,
     '2024-07-20 08:15:00');

-- -----------------------------------------------------------
-- 6. 文档分块 (为已完成的文档插入分块)
--    文档 id 依赖自增, 使用子查询按 title 定位
-- -----------------------------------------------------------

-- Spring Boot 3 新特性指南 (3 个分块)
INSERT INTO kb_document_chunk (document_id, chunk_index, content, token_count)
SELECT d.id, 1, 'Spring Boot 3 是 Spring Boot 框架的重大升级版本，最低要求 Java 17。它引入了对 Jakarta EE 9+ 的全面支持，将 javax.* 命名空间迁移至 jakarta.*。此外，Spring Boot 3 还集成了 GraalVM 原生镜像支持，显著提升了应用的启动速度和内存效率。', 128
FROM kb_document d WHERE d.title = 'Spring Boot 3 新特性指南';

INSERT INTO kb_document_chunk (document_id, chunk_index, content, token_count)
SELECT d.id, 2, '在配置方面，Spring Boot 3 引入了新的配置属性绑定机制，支持更灵活的类型转换和属性映射。同时，对 Micrometer 的深度集成使得可观测性（Observability）成为一等公民，内置了对 Prometheus、OpenTelemetry 等监控系统的支持。', 115
FROM kb_document d WHERE d.title = 'Spring Boot 3 新特性指南';

INSERT INTO kb_document_chunk (document_id, chunk_index, content, token_count)
SELECT d.id, 3, 'Spring Boot 3 还改进了安全配置，简化了 Spring Security 的默认设置。新的 Auto-configuration 报告功能可以帮助开发者更好地理解自动配置的原理。此外，对虚拟线程（Virtual Threads）的实验性支持为高并发场景提供了新的解决方案。', 120
FROM kb_document d WHERE d.title = 'Spring Boot 3 新特性指南';

-- 企业信息安全管理制度 (2 个分块)
INSERT INTO kb_document_chunk (document_id, chunk_index, content, token_count)
SELECT d.id, 1, '第一章 总则。为加强公司信息安全管理，保护公司商业秘密和客户数据安全，根据《中华人民共和国网络安全法》《数据安全法》等相关法律法规，结合公司实际情况，特制定本制度。本制度适用于公司全体员工、外包人员及临时访问人员。', 108
FROM kb_document d WHERE d.title = '企业信息安全管理制度';

INSERT INTO kb_document_chunk (document_id, chunk_index, content, token_count)
SELECT d.id, 2, '第二章 信息分级与访问控制。公司信息资产分为公开、内部、机密、绝密四个等级。机密及以上级别信息实行最小权限原则，必须经过审批流程方可访问。所有访问行为均需记录日志，定期进行安全审计。违反信息安全规定的行为将依据公司纪律处分条例进行处理。', 126
FROM kb_document d WHERE d.title = '企业信息安全管理制度';

-- 2024年度产品规划 (3 个分块)
INSERT INTO kb_document_chunk (document_id, chunk_index, content, token_count)
SELECT d.id, 1, '一、战略目标。2024年产品线将围绕"智能化、平台化、国际化"三大战略方向展开。核心目标是实现年度营收增长 40%，用户规模突破 500 万，并在东南亚市场建立本地化运营团队。', 95
FROM kb_document d WHERE d.title = '2024年度产品规划';

INSERT INTO kb_document_chunk (document_id, chunk_index, content, token_count)
SELECT d.id, 2, '二、产品路线图。Q1 完成核心平台 V2.0 升级，引入 AI 辅助决策模块；Q2 上线移动端 3.0 版本，优化用户体验；Q3 启动国际化版本开发，支持多语言和多币种；Q4 推出开放平台 API，建立开发者生态。', 112
FROM kb_document d WHERE d.title = '2024年度产品规划';

INSERT INTO kb_document_chunk (document_id, chunk_index, content, token_count)
SELECT d.id, 3, '三、资源投入与风险管控。全年研发预算 2000 万元，其中 AI 能力建设占比 35%。主要风险包括：技术人才竞争加剧、海外市场政策不确定性、数据合规要求变化等。应对措施包括加强雇主品牌建设、提前进行合规评估、建立灵活的资源调配机制。', 118
FROM kb_document d WHERE d.title = '2024年度产品规划';

-- 新员工入职手册 (2 个分块)
INSERT INTO kb_document_chunk (document_id, chunk_index, content, token_count)
SELECT d.id, 1, '欢迎加入公司！本手册将帮助您快速了解公司文化、规章制度和工作流程。公司实行弹性工作制，核心工作时间为 10:00-16:00，上下班时间可灵活调整。入职第一周需完成 IT 系统开通、部门介绍和导师对接。', 108
FROM kb_document d WHERE d.title = '新员工入职手册';

INSERT INTO kb_document_chunk (document_id, chunk_index, content, token_count)
SELECT d.id, 2, '员工福利包括：五险一金、补充商业保险、年度体检、带薪年假（入职即享 10 天）、节日福利、团队建设活动等。公司提供免费午餐和下午茶，设有健身房和休息区。晋升通道分为专业序列和管理序列，每年进行两次绩效评估。', 116
FROM kb_document d WHERE d.title = '新员工入职手册';

-- -----------------------------------------------------------
-- 7. 文档权限 (ACL)
-- -----------------------------------------------------------

-- "企业信息安全管理制度" (机密) → 技术部可读, 产品部可读
INSERT INTO kb_document_permission (document_id, role_id, permission_type)
SELECT d.id, r.id, 'READ'
FROM kb_document d
CROSS JOIN sys_role r
WHERE d.title = '企业信息安全管理制度'
  AND r.role_code IN ('ROLE_USER')
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

INSERT INTO kb_document_permission (document_id, department_id, permission_type)
SELECT d.id, dept.id, 'READ'
FROM kb_document d
CROSS JOIN sys_department dept
WHERE d.title = '企业信息安全管理制度'
  AND dept.code IN ('TECH', 'PRODUCT');

-- "2024年度产品规划" (部门) → lisi 有 ADMIN 权限
INSERT INTO kb_document_permission (document_id, user_id, permission_type)
SELECT d.id, u.id, 'ADMIN'
FROM kb_document d
CROSS JOIN sys_user u
WHERE d.title = '2024年度产品规划'
  AND u.username = 'lisi';

-- -----------------------------------------------------------
-- 8. 对话历史 (admin 的 3 条对话)
-- -----------------------------------------------------------
INSERT INTO kb_chat_history (user_id, title, message_count, created_at) VALUES
    (1, 'Spring Boot 3 配置问题咨询', 4, '2024-07-22 09:30:00'),
    (1, '信息安全制度相关查询', 2, '2024-07-23 14:15:00'),
    (1, '产品规划文档摘要', 4, '2024-07-24 10:00:00');

-- -----------------------------------------------------------
-- 9. 对话消息
--    使用子查询按 title 定位对话历史
-- -----------------------------------------------------------

-- 对话 1: Spring Boot 3 配置问题咨询 (4 条消息)
INSERT INTO kb_chat_message (history_id, role, content, token_count, created_at)
SELECT h.id, 'USER', 'Spring Boot 3 中如何配置自定义的 Jackson ObjectMapper？', 32, '2024-07-22 09:30:00'
FROM kb_chat_history h WHERE h.title = 'Spring Boot 3 配置问题咨询';

INSERT INTO kb_chat_message (history_id, role, content, token_count, sources, created_at)
SELECT h.id, 'ASSISTANT', '在 Spring Boot 3 中，你可以通过以下方式自定义 Jackson ObjectMapper：

1. **通过 application.yml 配置**：
```yaml
spring:
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
    serialization:
      indent-output: true
```

2. **通过 @Bean 注入自定义配置**：
```java
@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
```

3. **通过 Jackson2ObjectMapperBuilderCustomizer**（推荐方式）：
```java
@Component
public class CustomJacksonCustomizer implements Jackson2ObjectMapperBuilderCustomizer {
    @Override
    public void customize(Jackson2ObjectMapperBuilder builder) {
        builder.indentOutput(true)
               .dateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }
}
```

推荐使用第 3 种方式，因为它不会影响 Spring Boot 的自动配置。', 256, '[{"documentId":1,"title":"Spring Boot 3 新特性指南","chunkIndex":2,"score":0.92}]', '2024-07-22 09:30:15'
FROM kb_chat_history h WHERE h.title = 'Spring Boot 3 配置问题咨询';

INSERT INTO kb_chat_message (history_id, role, content, token_count, created_at)
SELECT h.id, 'USER', '那 Spring Boot 3 对 GraalVM 原生镜像的支持情况如何？', 28, '2024-07-22 09:31:00'
FROM kb_chat_history h WHERE h.title = 'Spring Boot 3 配置问题咨询';

INSERT INTO kb_chat_message (history_id, role, content, token_count, sources, created_at)
SELECT h.id, 'ASSISTANT', 'Spring Boot 3 对 GraalVM 原生镜像提供了全面的支持：

1. **AOT（Ahead-Of-Time）编译**：Spring Boot 3 引入了 AOT 处理引擎，可以在构建阶段进行 Bean 注册、配置属性绑定等优化。

2. **使用方式**：通过 Spring Boot Maven/Gradle 插件即可构建原生镜像：
```bash
./mvnw spring-boot:build-image -Pnative
```

3. **注意事项**：
   - 反射调用需要通过 `@RegisterReflectionForBinding` 显式声明
   - 动态代理有限制，需使用 `RuntimeHintsRegistrar`
   - 部分第三方库可能不兼容

4. **性能提升**：启动时间可从数十秒缩短至毫秒级，内存占用降低 50%-80%。

建议在项目中先进行充分的兼容性测试后再考虑生产环境使用。', 230, '[{"documentId":1,"title":"Spring Boot 3 新特性指南","chunkIndex":3,"score":0.89}]', '2024-07-22 09:31:20'
FROM kb_chat_history h WHERE h.title = 'Spring Boot 3 配置问题咨询';

-- 对话 2: 信息安全制度相关查询 (2 条消息)
INSERT INTO kb_chat_message (history_id, role, content, token_count, created_at)
SELECT h.id, 'USER', '公司的信息安全等级是如何划分的？', 18, '2024-07-23 14:15:00'
FROM kb_chat_history h WHERE h.title = '信息安全制度相关查询';

INSERT INTO kb_chat_message (history_id, role, content, token_count, sources, created_at)
SELECT h.id, 'ASSISTANT', '根据《企业信息安全管理制度》，公司信息资产分为四个等级：

1. **公开**：可对外公开的信息，如公司介绍、产品宣传资料
2. **内部**：仅限公司内部使用的信息，如内部通知、会议纪要
3. **机密**：包含商业秘密的信息，如技术方案、财务数据、客户名单
4. **绝密**：最高级别信息，如核心算法、战略规划、并购方案

机密及以上级别信息实行最小权限原则，必须经过审批流程方可访问。所有访问行为均需记录日志，定期进行安全审计。', 198, '[{"documentId":2,"title":"企业信息安全管理制度","chunkIndex":2,"score":0.95}]', '2024-07-23 14:15:20'
FROM kb_chat_history h WHERE h.title = '信息安全制度相关查询';

-- 对话 3: 产品规划文档摘要 (4 条消息)
INSERT INTO kb_chat_message (history_id, role, content, token_count, created_at)
SELECT h.id, 'USER', '请帮我总结一下 2024 年度产品规划的核心内容', 22, '2024-07-24 10:00:00'
FROM kb_chat_history h WHERE h.title = '产品规划文档摘要';

INSERT INTO kb_chat_message (history_id, role, content, token_count, sources, created_at)
SELECT h.id, 'ASSISTANT', '根据《2024年度产品规划》文档，核心内容如下：

**战略目标**：
- 围绕"智能化、平台化、国际化"三大战略方向
- 年度营收增长目标 40%
- 用户规模突破 500 万
- 在东南亚市场建立本地化运营团队

**产品路线图**：
- Q1：核心平台 V2.0 升级，引入 AI 辅助决策模块
- Q2：移动端 3.0 版本上线，优化用户体验
- Q3：启动国际化版本开发，支持多语言和多币种
- Q4：推出开放平台 API，建立开发者生态', 210, '[{"documentId":3,"title":"2024年度产品规划","chunkIndex":1,"score":0.93},{"documentId":3,"title":"2024年度产品规划","chunkIndex":2,"score":0.91}]', '2024-07-24 10:00:25'
FROM kb_chat_history h WHERE h.title = '产品规划文档摘要';

INSERT INTO kb_chat_message (history_id, role, content, token_count, created_at)
SELECT h.id, 'USER', '研发预算分配和主要风险有哪些？', 16, '2024-07-24 10:01:00'
FROM kb_chat_history h WHERE h.title = '产品规划文档摘要';

INSERT INTO kb_chat_message (history_id, role, content, token_count, sources, created_at)
SELECT h.id, 'ASSISTANT', '**研发预算**：全年 2000 万元，其中 AI 能力建设占比 35%（约 700 万元）。

**主要风险及应对措施**：

| 风险 | 应对措施 |
|------|---------|
| 技术人才竞争加剧 | 加强雇主品牌建设 |
| 海外市场政策不确定性 | 提前进行合规评估 |
| 数据合规要求变化 | 建立灵活的资源调配机制 |

建议定期回顾风险清单，及时调整应对策略。', 175, '[{"documentId":3,"title":"2024年度产品规划","chunkIndex":3,"score":0.94}]', '2024-07-24 10:01:30'
FROM kb_chat_history h WHERE h.title = '产品规划文档摘要';

-- -----------------------------------------------------------
-- 10. Token 消耗记录 (admin 的消耗记录)
-- -----------------------------------------------------------
INSERT INTO kb_token_usage (user_id, model, prompt_tokens, completion_tokens, total_tokens, request_type, created_at) VALUES
    (1, 'qwen-plus',    128, 256, 384,  'CHAT',      '2024-07-22 09:30:15'),
    (1, 'qwen-plus',    156, 230, 386,  'CHAT',      '2024-07-22 09:31:20'),
    (1, 'qwen-plus',    98,  198, 296,  'CHAT',      '2024-07-23 14:15:20'),
    (1, 'qwen-plus',    112, 210, 322,  'CHAT',      '2024-07-24 10:00:25'),
    (1, 'qwen-plus',    86,  175, 261,  'CHAT',      '2024-07-24 10:01:30'),
    (1, 'text-embedding-v2', 512, 0, 512, 'EMBEDDING', '2024-06-15 10:35:00'),
    (1, 'text-embedding-v2', 384, 0, 384, 'EMBEDDING', '2024-06-18 14:25:00'),
    (1, 'text-embedding-v2', 448, 0, 448, 'EMBEDDING', '2024-07-01 09:10:00'),
    (1, 'text-embedding-v2', 320, 0, 320, 'EMBEDDING', '2024-07-20 08:20:00');
