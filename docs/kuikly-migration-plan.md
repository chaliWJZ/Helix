# Kuikly 设置系逐页精复刻 SOP（宏观操作流程）

> 目标：把 Compose 版设置系页面**原封不动**复刻为 Kuikly 版（UI 形态 + 点击效果），禁止兜底实现。
> 工作模式：**一次会话只做 1 个页面**，做完用户清上下文，下页新会话继续。
> 本文档是每次新会话的**唯一入口**——开场先读本文件再动手。

---

## 一、宏观流程（每页 7 步，严格顺序）

```
Step 1  读 Compose 源码        → 完整读该页 Compose 代码，列出全部 UI 元素清单
Step 2  Compose 真机截图       → adb 进入 Compose 版对应页（helix://compose-debug 入口），每个状态截图
Step 3  Kuikly MCP 查姿势      → 对照实现前，把该页用到的每个 Kuikly 组件/手势/DSL 都先查知识库（见第二节）
Step 4  复刻实现               → Kuikly 页面重写，逐元素对齐（图标/布局/手势/弹层形态），禁止兜底
Step 5  kuikly_lint + 编译     → lint 检查（注意自定义函数误报）+ gradle 双模块编译
Step 6  真机对比验证           → 装包后真实坐标（截图×2）逐元素点击，logcat 断言 + 截图与 Step 2 对照
Step 7  提交 commit            → 带 Phase hash（见第四节映射表），用户清上下文
```

### Step 1/2 细则：Compose 对照方法（二选一或并用）
- **代码法**：在 `androidApp/src/main/java/com/helix/app/helix/ui/pages/` 找到对应页，**完整读完**（不是 grep 片段）。重点记录：
  - TopBar `actions` 里每个按钮（图标 + 触发的弹层类型）
  - 列表项结构（图标/文字/Trailing/启停状态视觉）
  - 每个 clickable 的 onClick 打开什么（AlertDialog / ModalBottomSheet / 跳页 / toast）
  - Tab 结构（TabRow / NavigationBar / HorizontalPager）
  - 手势（拖拽 ReorderableItem / 滑动删除 SwipeToDismissBox）
- **真机法**：`adb shell am start` Compose 版（`helix://compose-debug` 或 RouteActivity），**每个可点元素点一遍并截图**，存 `/tmp/compose_<页面>.png` 系列

### Step 6 细则：真机测试坐标校准（必读，踩过坑）
- 设备 571337b7：**物理分辨率 1440x3200，density 600（1dp=3.75px）**
- `screencap -p` 输出的截图是**压缩一半**的 → **截图坐标 × 2 = 真实点击坐标**
- 例：截图上元素在 y=473 → `input tap 720 946`
- logcat 断言：`adb logcat -s HelixKuikly`，看六类日志 `🖼 Render / 👆 Click / 🔗 Module-Call / ✅ Module-Done / 📦 Data / ⚠ Error`

---

## 二、Kuikly MCP 工具集成（每次 plan 执行必须用）

MCP 服务器 `kuikly-mcp`，4 个工具在流程中的固定使用点：

| 工具 | 使用时机 | 用法 |
|---|---|---|
| **kuikly_framework** | Step 3 前置：遇到任何框架机制问题（响应式/生命周期/手势/事件） | 提问后它会返回检索指引，按 P1业务知识库 → P2官方文档(`Reference/KuiklyUI/docs`) → P3源码 的顺序自己 grep 查证 |
| **kuikly_search_component** | Step 3：页面用到不熟的组件（List/ActionSheet/Refresh/Slider…） | 传组件名，返回完整文档+示例 |
| **kuikly_lint** | Step 5：每个改动的 .kt 文件 | `filePath`+`dsl:kuikly`。**注意**：自定义函数+trailing lambda 会被误报"vfor 多子节点"，以 gradle 编译为准 |
| **kuikly_ask_question_back** | 知识库路径访问不到时的兜底重问 | 同一问题重新提问 |

**强制规则**：写任何 Kuikly DSL 前，如果对该用法没有 100% 把握（尤其 vfor/vif/手势/响应式/新组件），**必须先查 kuikly-mcp，不许凭记忆瞎写**。本会话所有重大 bug（弹窗不响应、拖动不跟手）都是没查知识库瞎写造成的。

---

## 三、已验证的 Kuikly DSL 经验（血泪坑，新会话必读）

### 3.1 响应式（官方 DevGuide/reactive-update.md）
- **attr 内读响应式字段会自动更新**，但必须**直接引用**，不能提前取出存普通变量
- 组件参数要响应式 → **传 getter** `() -> T`，组件内部 attr 里调用 `getter()`
  ```kotlin
  // ❌ 不响应：提前求值
  DragPreview(pageY = ctx.dragPageY)
  // ✅ 响应：getter
  DragPreview(pageY = { ctx.dragPageY })   // 组件内 attr { top(pageY() - 60f) }
  ```
- 弹层显隐：**必须 `vif({ visible() })`，禁止 `if (visible())`**（后者 build 一次永不更新，弹窗弹不出的根因）

### 3.2 vfor 限制（官方 DevGuide/directive.md）
- **vfor 闭包内必须且只能有 1 个根子节点**
- vfor 内条件渲染必须 **View 包裹 vif**：
  ```kotlin
  vfor({ list }) { item ->
      View {                    // ← 必须包这层
          vif({ item.visible }) { Text { } }
      }
  }
  ```
- 局部刷新 trick：`this[index] = this[index]`（显式 set 触发单项更新）

### 3.3 Input 渲染坑
- 列布局内裸 Input 不渲染 → **行布局容器（flexDirectionRow + height）+ Input flex(1f)**（参考 SettingProviderPage 搜索框）

### 3.4 手势（Event API）
- `event { pan { params -> } }`：params.state = "start"/"move"/"end"，含 pageX/pageY（根视图坐标）
- `event { longPress { } }`：同状态机
- 拖拽排序参考实现：`ReorderableItem.kt`（pan 状态机 + 触觉桥 vibrate + DragPreview 跟手浮层）

### 3.5 桥接架构（已稳定，直接复用）
- `HelixBridgeModule.callNative(method, json, callback)` ↔ `HelixRenderModule`（Koin 注入 SettingsStore/ProviderManager/WebServerManager 等，写操作走 AppScope 协程 `settings.copy()+update()` 原子更新）
- 已有 60+ 桥方法：providers/models/tts/asr/mcp/web/backup/search/prompt/assistant/logs/vibrate/clipboard 等——**新页先查现有桥，缺了再加**
- 触觉：`bridge().vibrate(50)`

### 3.6 页面模式（直接复用）
- HelixBasePage + `pageDidAppear()` 刷新（返回自动同步）+ observableList/vfor + vif + safeRun 异常兜底
- 弹层三件套：`EditSheet`（文本编辑）/ `OptionSheet`（单选）/ `ConfirmSheet`（二次确认）——全部已改 vif 响应式
- 拖拽：`ReorderableItem` + `DragPreview`（均 getter 响应式）

---

## 四、Git 规范：Phase hash 映射表

每页补齐 commit **必须**带对应 Phase hash（可追溯 2026/8/24 原始迁移 commit）：

| Phase | Hash | 内容 |
|---|---|---|
| Phase2 | `bd92ff98` / `3029f668` | 公共组件库（26组件） |
| Phase3 一批 | `f4783a83` | 设置页 6 页 |
| Phase3 二批 | `cf21e665` | 设置页 10 页（供应商/搜索/语音/MCP/Web 等） |
| Phase4 | `71caef32` | 列表页 10 页（含 extensions） |
| Phase5 | `6e6732ed` | 聊天核心页 |
| Phase6 | `bfe1dd53` | Markdown 渲染器 |
| Phase7 | `47c72f24` | 原生 Module 桥接 |
| Phase8 | `9bb27079` | 剩余 14 页（Backup/Assistant 等） |

**commit 格式**：
```
CharlieJ --story=0 fix: [SettingXxxPage-中文名] 精复刻Compose UI-XXX+YYY 补齐PhaseN遗漏(hash: xxxxxxxx)
```

---

## 五、设置系页面状态追踪表（逐页更新）

| # | 页面 | 状态 | 待精复刻要点（对照 Compose） | Phase hash |
|---|---|---|---|---|
| 1 | SettingProviderPage | ✅ 完成 | TopBar 3 emoji 按钮/搜索框/拖拽手柄+跟手浮层/触觉 已对齐 | cf21e665 |
| 2 | **SettingProviderDetailPage** | 🔴 下一个 | 4字段 TextField 直接编辑形态（现为 CardGroup+弹层）、底部保存/重置/删除工具栏行、模型 Tab 的 ModelPicker 批量勾选、连接测试 3 项带 loading/错误栈 BottomSheet | cf21e665 |
| 3 | SettingMcpPage | 🔴 缺口大 | 用户点名"几乎所有内容都没有"——对照 Compose 补完整形态 | cf21e665 |
| 4 | SettingModelPage | 🟡 功能✅ | 7槽位+ModelListSheet 已真实化，形态待逐项对照 | cf21e665 |
| 5 | SettingSearchPage | 🟡 功能✅ | 操作菜单模式，Compose 形态未对照 | cf21e665 |
| 6 | SettingSpeechPage | 🟡 功能✅ | 双Tab+TTS/ASR 管理已真实化，形态待对照 | cf21e665 |
| 7 | TTSProviderConfigurePage | 🟡 部分 | 音色/试听已做，引擎切换 11 种/数值参数 Slider 未做 | cf21e665 |
| 8 | SettingWebPage | 🟡 功能✅ | 密码编辑已做，形态待对照 | cf21e665 |
| 9 | BackupPage | 🟡 功能✅ | 双Tab+配置编辑+测试+备份已做，Compose 4Tab（导入导出/提醒）待对照 | 9bb27079 |
| 10 | LogPage | ✅ 真实化 | — | 新增 |
| 11 | SettingPage + Preferences 5 页 | 🟡 开关✅ | 形态待对照 | cf21e665 |
| 12 | PromptPage | 🟡 功能✅ | 双Tab+CRUD 已做，条目编辑器形态待对照 | 71caef32 |
| 13 | AssistantPage | 🟡 传id✅ | 列表富化（图标/标签/描述）未做 | 9bb27079 |
| 14 | AssistantDetailPage | 🟡 3Tab简化 | Compose 是 7Tab，4 个深度 Tab 待补 | 9bb27079 |
| 15 | SettingFilesPage / About / Donate / ModelPrompt | ⚪ 待审 | 未逐项对照 | 各自 |

---

## 六、新会话开场模板（复制即用）

```
继续逐页精复刻 Kuikly 设置系。先读 docs/kuikly-migration-plan.md（SOP+状态表），
本次做【第 N 页：页面名】：
1. 完整读 Compose 源码 androidApp/.../ui/pages/xxx 页
2. 真机 Compose 版截图对照
3. 用 kuikly-mcp 查该页所有组件/手势姿势后再写
4. 精复刻 UI（禁止兜底），lint+编译+真机验证（坐标×2）
5. commit 带 hash: xxxxxxxx
```
