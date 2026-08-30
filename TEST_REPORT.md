# 设置系 Kuikly 页面测试报告

> 设备：571337b7（3EP0219314002291 未连接，经用户确认改用此设备）
> 方法：TestHub 进入 → adb tap（真实分辨率截图+image-reader定位坐标，uiautomator dump 对 Kuikly 自渲染引擎无效已验证）→ logcat -s HelixKuikly 断言 Render/Click/Error → 返回键验证页面栈 → 发现问题即修复即 commit

## 环境修复（阶段0）

| Bug | 页面 | 修复 | Commit |
|---|---|---|---|
| B1 | SettingAboutPage | 「开源许可」跳转白屏（目标页不存在）→ 改跳 WebViewPage 携带 LICENSE URL | 69def5c4 |
| B2 | SettingAboutPage | 「检查更新」跳转报错（未注册页面）→ 移除该项 | 69def5c4 |
| B3 | SettingModelPage | 6 模型槽位跳转白屏（ModelPickerPage不存在）→ 改 toast 占位 | d89f6153 |
| B6 | SettingPage | 「分享」空 TODO 无反应 → 接 bridge().shareText() | fba71270 |

## 小组A：入口链路（18 项，18/18 通过）

### SettingPage 设置主页 [P0✅ P1✅ P2✅]
- Render 断言：`[SettingPage#body...] [NavBar] [设置]` 正确
- 14 个列表项全部测试：偏好设置/助手/扩展/默认模型/供应商/搜索服务/TTS服务/MCP/Web服务器/数据备份/聊天存储/关于/分享/捐赠
- 返回栈：验证多次 backOrFinish 正确弹栈回到来源页
- **发现并修复 2 个真实Bug**（见下）

### SettingPreferencesPage 偏好设置页 [P0✅ P1✅ P2✅]
- Render 断言：`[SettingPreferencesPage#body...] [NavBar] [偏好设置]` 正确
- 4 项全部测试：通知→SettingPreferencesNotificationPage / 通用→SettingPreferencesGeneralPage / 界面→SettingPreferencesUIPage / 网络→SettingPreferencesNetworkPage
- 返回栈：栈深度正确递增递减（2→1→0）

## 测试中发现并修复的严重 Bug

### 🔴 [全局] 返回键直接退出App（Predictive Back未注册）
- **现象**：任意 Kuikly 页面按返回键（无论栈深度）直接退到系统桌面，而非按预期弹栈
- **根因**：AndroidManifest 开启 `enableOnBackInvokedCallback="true"`（targetSdk 35），Android 13+ 走 Predictive Back API 分发返回事件，不再触发 `dispatchKeyEvent`。`KuiklyHostActivity` 从未注册 `OnBackInvokedCallback`，系统走默认行为直接 finish()
- **影响范围**：全部 46 个 Kuikly 页面
- **修复**：`onCreate` 注册回调 / `onDestroy` 注销，API<33 保留原 dispatchKeyEvent 兼容旧设备
- **Commit**：880ead71

### 🔴 [全局] 分享按钮点击无反应（异常被静默吞掉）
- **现象**：点击「分享」有 Click 日志、桥接 Module-Call 日志正常到达原生侧，但系统分享面板完全不弹出
- **根因**：`HelixRenderModule.METHOD_SHARE` 的 `startActivity` 缺少 `FLAG_ACTIVITY_NEW_TASK`，抛出的异常被 `runCatching{}` 静默吞掉，无任何日志
- **修复**：加 FLAG_ACTIVITY_NEW_TASK + onFailure 日志与 toast 兜底
- **验证**：adb截图确认系统 ShareSheet 正常弹出（含微信/QQ/短信等应用图标）
- **Commit**：（分享修复 commit）

## 技术方法论发现

- **uiautomator dump 对 Kuikly 页面完全失效**：Kuikly 自渲染引擎，dump 出的 68 个节点全是无 text/无 clickable 属性的通用 FrameLayout，无法定位具体元素。改用真实分辨率截图 + image-reader 多模态定位坐标
- **坐标估算存在 ~150-300px 方差**：单次估算不可靠，对不确定坐标采用**扫描法**（在预估范围内多点尝试+日志验证真实边界）更可靠
- **状态漂移风险**：adb 命令链用 `&&` 时 grep 无匹配返回码1会中断链条，导致部分命令未执行但脚本继续、造成状态漂移（返回键位置计算错误）；改用 `;` 分隔 + 每步截图/日志确认当前状态后再进行下一步操作

## 待处理（P3，不在本轮修复范围）

详见子代理已完成的 5 份差异分析报告（设置系合计 224 测试点），核心共性问题：
1. 所有 Kuikly 页面开关状态为内存 observable，无 DataStore 持久化
2. 供应商/MCP/TTS/搜索服务等列表为硬编码演示数据，无真实数据绑定
3. 复杂交互缺失：拖拽排序、左滑删除、ModalBottomSheet编辑器、二维码扫描、连通性测试等
4. SettingProviderDetailPage 全部28个交互点均为 toast 占位

以上记录为「Kuikly 迁移二期」需求，本轮聚焦 P0/P1/P2（页面可达+交互响应+基础对齐）。

## 下一步

继续小组B（开关组4页：Notification/General/UI/Network，17个SwitchRow二次回弹验证）。
