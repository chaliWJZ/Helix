# Kuikly 页面自动化测试计划（Compose 对比法）

> 设备：3EP0219314002291（adb -s 3EP0219314002291）
> 方法：TestHub 进入 Kuikly 页面 → adb 截图 + logcat（HelixKuikly tag）→ Compose 对比入口进入同页 → adb 截图 → 双版本对比 + 点击交互日志验证

## 一、测试分层

| 层级 | 内容 | 现在测 |
|---|---|---|
| P0 页面可达 | 46 页全部能打开、不白屏不崩溃、UI 骨架正确 | ✅ |
| P1 交互可点 | 可见可点元素：点击有响应（日志/跳转/状态变化） | ✅ |
| P2 Compose 对齐 | 交互行为/Toast/状态反馈与 Compose 版一致 | ✅（截图对比） |
| P3 业务逻辑 | 真实数据绑定/持久化/网络（后续迭代接入） | ❌ 本轮不测 |

## 二、已知必现 Bug（测试前先修）

| # | 页面 | 问题 |
|---|---|---|
| B1 | SettingAboutPage | 「开源许可」跳 SettingAboutLicensePage——**文件不存在，必白屏** |
| B2 | SettingAboutPage | 「检查更新」跳 UpdateCheckPage——**未注册，必报错** |
| B3 | SettingModelPage | 6 个模型槽位跳 ModelPickerPage——**页面不存在，必报错** |
| B4 | ImgGenPage | generating=true 后不重置，按钮永久显示「生成中...」 |
| B5 | ChatPage | 抽屉面板只显示静态文字「会话抽屉」，与 ChatDrawerPage 断裂 |
| B6 | SettingPage | 「分享」onClick 为空 TODO，点击无反应 |

## 三、测试批次（执行顺序）

### 批次 1：设置系（19 页，重点）
| 页面 | 核心测试点（P1/P2） |
|---|---|
| SettingPage 设置主页 | 6 组列表项跳转；返回键；「去配置」警告卡缺失记录 |
| SettingPreferencesPage 偏好设置 | 4 子项跳转 + 返回栈 |
| SettingPreferencesNotificationPage 通知 | 3 个 SwitchRow 点击→UI 开关状态变化（响应式已修）+ 日志 |
| SettingPreferencesGeneralPage 通用 | 7 个 SwitchRow 同上 |
| SettingPreferencesUIPage 界面 | 6 个 SwitchRow 同上 |
| SettingPreferencesNetworkPage 网络 | 「使用代理」开关 + 静态项展示 |
| SettingModelPage 模型设置 | 双 Tab 切换（模型/提示词）+ 6 槽位点击（先修 B3） |
| SettingModelPromptPage 提示词 | 5 项点击→editingIndex 变化（已修 mapIndexed） |
| SettingProviderPage 供应商 | 6 项点击→详情页跳转 + 「添加」按钮 |
| SettingProviderDetailPage 供应商详情 | 基础配置 3 项 + 模型列表 4 项点击→toast 反馈 |
| SettingSearchPage 搜索服务 | 5 卡片点击→详情跳转 + 「添加」按钮 |
| SettingSearchDetailPage 搜索详情 | 4 卡片点击→toast 反馈 |
| SettingSpeechPage 语音 | 4 卡片：系统TTS toast / 其余跳配置页 |
| TTSProviderConfigurePage TTS 配置 | 5 卡片点击→toast 反馈 |
| SettingMcpPage MCP | 4 卡片展示 + 「＋」按钮 toast |
| SettingWebPage Web 服务器 | 「启用」开关响应式 + 静态项展示 |
| SettingAboutPage 关于 | 3 项跳转（先修 B1/B2）+ 版本展示 |
| SettingDonatePage 捐赠 | 「爱发电」跳 WebView；微信/支付宝 TODO 记录 |
| SettingFilesPage 文件管理 | 「清空」按钮→列表清空+空状态切换 |

### 批次 2：列表系（9 页）
AssistantPage、AssistantDetailPage（6 Tab 切换）、ExtensionsPage、PromptPage、HistoryPage、FavoritePage、LogPage、StatsPage、SkillsPage 等：列表项点击跳转、Tab 切换、空状态。

### 批次 3：功能系（8 页）
TranslatorPage（翻译按钮→占位结果）、ImgGenPage（生成按钮，先修 B4）、SearchPage（搜索按钮）、ShareHandlerPage（发送按钮）、BackupPage、DebugPage、WebViewPage、MarkdownDemoPage（渲染效果截图对比）。

### 批次 4：工作区系 + 聊天系（6 页）
WorkspacePage、WorkspaceDetailPage、ChatPage（发送消息→占位回复；抽屉开关，先修 B5）、ChatDrawerPage（会话项、菜单）、HistoryPage 复测。

## 四、adb 自动化流程（每页面）

```
1. adb shell am start 进入 TestHub
2. logcat -c 清空
3. input tap (x,y) 点击目标页面入口
4. sleep 2 → screencap 截图 Kuikly 版 → pull
5. logcat -d -s HelixKuikly 抓 🖼 Render / 👆 Click / ⚠ Error
6. 遍历页面核心可点元素（每页 3-8 个）：input tap → sleep → 截图 → 抓日志
7. 返回键验证页面栈（backOrFinish 弹栈）
8. 对比：TestHub → Compose 对比入口 → 同页面截图 → 双图对比（P2）
9. 异常（白屏/无日志/⚠ Error）→ 记录 bug → 修复 → 单独 commit
```

## 五、产出与 commit 规范

- 每页面修复：`--story=0 fix: [页面名-中文名] 修复描述`
- 全局修复：`--story=0 fix: [全局] 描述`
- 测试不产生 commit（除修复）
- 最终产出：TEST_REPORT.md（每页面：截图对照 + 通过项/失败项清单）

## 六、测试点统计（子代理分析）

| 分组 | 测试点 |
|---|---|
| 设置系-偏好组（7页） | 64 |
| 设置系-模型供应商（4页） | 60 |
| 设置系-搜索语音 MCP（4页） | 60 |
| 设置系-杂项（5页） | 40 |
| 非设置系（11页） | 81 |
| **合计** | **305** |

P3（业务逻辑缺失：无持久化/无真实数据/无网络）共性问题统一记录、本轮不修，作为「Kuikly 迁移二期」需求。
