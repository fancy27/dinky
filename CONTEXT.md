# Dinky 私有化登录

本仓库私有化部署时，在 nginx 后增加网关身份换票。本文件只记录领域用语，不含实现细节。

## Language

**Webwaytoken**:
nginx 注入到请求上的网关 JWT，载荷含 username、name、email。它不是 Dinky 登录态。
_Avoid_: Dinky token, Sa-Token, 会话 token

**ztway 免登**:
登录页上的一次换票：用 Webwaytoken 换取 Dinky 会话。打开登录页不会自动完成。
_Avoid_: SSO 静默登录, 免登录页, 每请求验 JWT

**Dinky 会话**:
换票成功后由 Sa-Token 建立的登录态。之后接口只认这套会话，不再读取 Webwaytoken。
_Avoid_: Webwaytoken, JWT 登录态

**ZTWAY 用户**:
ztway 免登时自动创建的用户类型。已有 LOCAL / LDAP 账号可以免登，但不会被改成这种类型。
_Avoid_: LDAP 用户, 本地用户
