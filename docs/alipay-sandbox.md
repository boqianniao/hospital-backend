# 支付宝沙箱接入

本机配置已从支付宝沙箱控制台的「网页/移动应用 → 系统默认密钥 → 公钥模式」读取。
网关为 `https://openapi-sandbox.dl.alipaydev.com/gateway.do`，签名为 RSA2，应用私钥为 JAVA / PKCS8 格式。

## 本机启动

在 `hospital-backend` 目录执行：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=alipay-local
```

IntelliJ 的工作目录设为 `hospital-backend`，Active profiles 填 `alipay-local`。
本机凭证保存在 `config/application-alipay-local.yml`，文件权限 600，已被 Git 忽略，且不在 Maven resources 中，不会打进 JAR。
默认不加载这个 profile 时支付宝和模拟支付均关闭；若开启支付宝而缺少必要配置，启动直接失败。

其他机器使用环境变量配置：

| 变量 | 说明 |
| --- | --- |
| `ALIPAY_ENABLED` | `true` 开启支付宝 |
| `ALIPAY_GATEWAY` | 默认沙箱网关 |
| `ALIPAY_APP_ID` | 沙箱 APPID |
| `ALIPAY_SELLER_ID` | 沙箱应用绑定的商家 PID |
| `ALIPAY_APP_PRIVATE_KEY` | 应用私钥，JAVA / PKCS8 |
| `ALIPAY_PUBLIC_KEY` | 支付宝公钥，不是应用公钥 |
| `ALIPAY_NOTIFY_URL` | 公网可达的后端 `/api/pay/alipay/notify`，本机默认为空 |
| `ALIPAY_RETURN_URL` | 浏览器返回地址，默认 `http://localhost:8080/api/pay/alipay/return` |
| `ALIPAY_MOCK_ENABLED` | 默认 `false`；仅在支付宝关闭且该值为 `true` 时允许模拟支付 |

本机 profile 中的应用信息是实际本机配置；切换应用时更新本地文件，或者不启用此 profile 并使用上述环境变量。

## 接口与流程

1. 使用系统已有的挂号/咨询接口创建订单；金额以数据库订单为准。
2. 登录后调用 `POST /api/pay/create`，请求头带 `token`，请求体如下（业务类型 1 挂号、2 咨询）：

   ```json
   {"orderNo":"实际订单号","businessType":1,"payMethod":1}
   ```

3. 响应的 `data.mock` 为 `false`，`data.payForm` 为支付宝签名表单。前端应将表单展示在支付窗口并提交其中的 form 跳转支付宝；仅用 `innerHTML` 插入时不会自动执行 script，需要主动提交表单。当前仅支持支付宝，传微信方式会拒绝。
4. 使用沙箱买家账号完成支付，账号信息可在控制台「沙箱账号」查看。
5. 支付后浏览器回到后端同步返回页：先验证签名和 APPID，再调用支付宝查单确认金额和交易状态。页面只展示结果提示，不回显回调参数。
6. 原订单页可带登录 token 调用 `POST /api/pay/query`，请求体同下单。`data=true` 表示已确认该订单支付成功；`false` 表示未确认支付或订单已取消，支付宝调用异常则返回支付错误。确认后重新读取订单详情。

若配置公网通知地址，支付宝会向 `POST /api/pay/alipay/notify` 推送表单通知。后端检查 RSA2 签名、APPID、商家 PID、商户订单号、交易号及金额，仅在成功状态下更新订单与流水。响应是纯文本 `success` 或 `failure`；失败让支付宝继续重试。通知和同步返回路径无需登录，其余支付接口要求登录并检查订单归属。

`localhost` 无法接收支付宝服务器的异步通知。没有公网通知地址时，需要依赖浏览器返回或主动查单；用户未返回且未查单的订单不会自动完成支付状态同步。部署联调时应配置真实公网通知地址。

## 幂等、退款与取消

- 回调、主动查单、取消和完成订单使用订单行锁协调；同一交易的重复通知不重复写状态或发送通知。
- 成功通知必须匹配已有本地支付流水，不允许凭通知新建流水，也不能通过改金额或改订单号标记支付成功。
- 下单失败不降级到模拟支付；沙箱启用时模拟接口不可用。
- 退款使用固定的 `REFUND_订单号` 请求号进行全额退款，只有支付宝业务响应成功才标记已退款。异常会让本地事务回滚，之后可用同一请求号重试。
- 取消后迟到的成功通知会尝试全额退款，退款失败返回 `failure` 等待重试，不重新打开订单。
- 支付宝 RPC 与本地数据库不构成分布式事务。若支付宝已退款但本地提交失败，需要再次执行原退款操作或重放通知，以相同请求号完成本地状态更新。

## 验证

```bash
mvn test
# 显式使用本机沙箱凭证，只查一个随机不存在的交易，不写业务库、不实际扣款
mvn -Dalipay.sandbox.smoke=true -Dtest=AlipaySandboxSmokeTest test
```

测试覆盖生成签名表单、RSA2 验签及篡改拒绝、错误商家/金额/状态、重复通知、越权查单与模拟支付、下单失败、退款业务失败和取消后的迟到通知。
沙箱连通测试会生成 `target/alipay-sandbox-form.html` 供调试，但对应随机订单不在业务库中，不应付款。

## 当前联调边界

仓库内 `reservation-pay.html` 和 `consult-pay.html` 的支付按钮仍直接跳转静态成功页，尚未调用后端支付接口。此次实现范围是后端；前端需要按上述接口流程接线，不能把静态成功页当作已支付证明。
完整的「业务下单 → 沙箱买家付款 → 通知/查单 → 数据库已支付 → 退款」仍需端到端联调确认。

参考：[支付宝沙箱控制台](https://open.alipay.com/develop/sandbox/app)、[支付宝 EasySDK API 文档](https://github.com/alipay/alipay-easysdk/blob/master/APIDoc.md)。
