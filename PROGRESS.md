# 后端任务完成情况与待办

> 更新时间：2026-09-17　对照《后端任务.docx》

## 一、总体结论

《后端任务》列出的 **9 项功能模块已全部代码完成**，对应 8 个本地提交（尚未 push）。
剩余项分为：环境/凭证配置（需业务方提供）、少量工程收尾、交付动作。

## 二、模块完成对照表

| # | 任务项 | 状态 | 提交 | 备注 |
|---|---|---|---|---|
| 1 | 用户认证（Redis+验证码+Token） | ✅ 完成 | `fc69644` | 验证码已接入阿里云短信（见 待办 A4） |
| 2 | ES 索引库创建 + 医院搜索 | ✅ 完成 | `e8ec135` | 已连 ES 8.19，未建索引时自动降级 DB |
| 3 | 医生/疾病/文章搜索 | ✅ 完成 | `e8ec135` | 同上，ES 降级 DB |
| 4 | 医院/医生详情 + Redis 缓存 | ✅ 完成 | `3ba5263` | cache-aside 读缓存 |
| 5 | 预约挂号 + Redis 号源扣减 | ✅ 完成 | `30ae06c` | `StockService` Lua 脚本预扣防超卖 |
| 6 | 电话咨询流程 | ✅ 完成 | `30ae06c` | |
| 7 | 关注 / 评价 / 消息 / 反馈 | ✅ 完成 | `4695751` | 本次补齐 4 个 controller + FeedbackService |
| 8 | 搜索历史、热门搜索 | ✅ 完成 | `e8ec135` | 纯 Redis 实现，已实机验证 |
| 9 | 支付宝沙箱支付 | ✅ 完成 | `a5cb0f4` | 引入 alipay-easysdk，凭证留空时降级支付桩 |

## 三、已实机验证（mvn spring-boot:run）

- ✅ 应用 1.7s 正常启动；ES 宕机不影响启动
- ✅ ES 探活失败 → 自动降级数据库（日志确认）
- ✅ Redis 热门搜索端到端可用（`GET /api/search/hot` 返回数据）
- ✅ 鉴权/白名单正确：公开搜索免登录；`/api/search/history`、`/api/pay/*` 无 token 返回 `30001`
- ✅ `AlipayService` 初始化走「未启用 → 支付桩」分支
- ⚠️ 业务读库接口因 DB 授权问题未跑通（见 待办 A1），非代码缺陷

## 四、待办事项

### A. 环境 / 凭证（阻塞真实运行，需业务方提供）

- [x] **A1 ✅ 数据库已就绪**：`hospital_db` 建库+建表+数据完成（脚本
  `hospital-backend/src/main/resources/db/hospital_full.sql`），应用账号已授权，读库接口实测正常返回数据。
- [x] **A2 ✅ Elasticsearch 已接入并建索引**：`103.236.92.40:41324`（HTTP，elastic，ES 8.19.21），
  已写入 `application.yml`。已调 `POST /api/search/reindex` 全量建索引并实测：
  搜索走 ES（无降级告警），索引文档数 hospital=26 / doctor=22 / disease=99 / article=12。
  数据变更后可再次调 reindex 刷新。
- [ ] **A3 🟠 支付宝沙箱凭证**：填写 `hospital.alipay` 的 `app-id / app-private-key /
  alipay-public-key` 并置 `enabled: true`，即从支付桩切到真实沙箱。
- [x] **A4 ✅ 短信验证码已接入**：阿里云号码认证服务 dypnsapi `SendSmsVerifyCode`
  （`SmsServiceImpl`，依赖 `com.aliyun:dypnsapi20170525`）。`hospital.sms.enabled=true` 且
  AccessKey/签名/模板齐全时走真实通道，否则回退日志桩。已实测客户端初始化成功。
  ⚠️ 签名 `速通互联验证码` / 模板 `100001` 取自参考文件，需确认为阿里云控制台**审核通过**的值，
  否则真实发送会失败（可用环境变量 `SMS_SIGN_NAME` / `SMS_TEMPLATE_CODE` 覆盖）。
  真实发送为计费的对外操作，尚未触发实测（需指定手机号）。
- [x] **A5 ✅ 对象存储 OSS 已接入**：阿里云 OSS（bucket `sk-itcase`，endpoint
  `oss-cn-beijing.aliyuncs.com`，依赖 `com.aliyun.oss:aliyun-sdk-oss`）。
  `OssService`（enabled 且凭证齐全才初始化）+ 上传接口 `POST /api/files/upload`（需登录，参数 file、可选 dir）。
  已实测上传返回 URL 且公网 GET 200：`https://sk-itcase.oss-cn-beijing.aliyuncs.com/test/202609/...txt`。
  注：测试对象 `test/202609/e5bf3e693e374612b30b37c159c2222e.txt` 可在 OSS 控制台删除。

### B. 代码收尾（开发范畴）

- [ ] **B1 🟠 订单超时自动取消 + 号源回补**：已开启 `@EnableScheduling` 且配置了
  `hospital.order.timeout-minutes: 15`，但**没有任何 `@Scheduled` 任务**——
  超时未支付订单不会自动关单、号源不回补。唯一明显的功能缺口，建议优先补。
- [ ] **B2 🟡 测试**：`src/test` 为空，无任何单元/集成测试。
- [ ] **B3 🟡 主流程联调回归**：DB 授权打通后，需对认证/搜索/挂号/咨询/支付/用户中心做一遍端到端回归。

### C. 交付动作（需授权）

- [ ] **C1** 本地 8 个提交尚未 push，也未建 PR。可按需 push / 开 PR / 合并提交。

## 五、本会话新增提交

```
a5cb0f4 feat(pay): 接入支付宝沙箱(EasySDK)，凭证留空时降级支付桩
e8ec135 feat(search): ES 全文搜索(降级DB) + 搜索历史 + 热门搜索
4695751 feat(usercenter): 关注/评价/消息/反馈
```
