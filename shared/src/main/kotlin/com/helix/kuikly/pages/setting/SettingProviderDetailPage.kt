package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.velse
import com.tencent.kuikly.core.directives.velseif
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.ConfirmSheet
import com.helix.kuikly.components.ui.EditSheet
import com.helix.kuikly.components.ui.EmptyView
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.NavBarAction
import com.helix.kuikly.components.ui.OptionSheet
import com.helix.kuikly.components.ui.SheetOption
import com.helix.kuikly.components.ui.SwitchRow
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

private const val UI_LOG = "HelixKuikly"
private const val TAG = "[SettingProviderDetailPage]"

internal data class ProviderModelItem(val uid: String, val modelId: String, val displayName: String, val type: String)

/** 弹层字段标识 */
private object SheetField {
    const val NAME = "name"
    const val BASE_URL = "baseUrl"
    const val API_KEY = "apiKey"
    const val API_PATH = "chatCompletionsPath"
    const val BALANCE_API = "balanceApiPath"
    const val BALANCE_RESULT = "balanceResultPath"
    const val MODEL_ID = "modelId"
    const val MODEL_NAME = "displayName"
}

/**
 * 供应商详情页（对齐 Compose：底部双Tab 配置/模型 + 完整编辑能力）
 *
 * 配置Tab：类型切换、4字段编辑(EditSheet弹层)、类型特有开关组、余额配置与拉取、
 *         测试连通性、重置BaseURL、删除(二次确认)
 * 模型Tab：列表(上移/下移/删除/编辑)、从API拉取添加、手动添加
 * 顶栏：分享供应商配置
 */
@Page("SettingProviderDetailPage")
internal class SettingProviderDetailPage : HelixBasePage() {

    // ===== 基础 =====
    private var providerId by observable("")
    private var providerName by observable("")
    private var providerType by observable("")
    private var enabled by observable(true)
    private var builtIn by observable(false)
    private var isDefaultBaseUrl by observable(true)

    // ===== 配置字段 =====
    private var baseUrl by observable("")
    private var apiKey by observable("")
    private var chatCompletionsPath by observable("")
    private var useResponseApi by observable(false)
    private var includeHistoryReasoning by observable(true)
    private var promptCaching by observable(false)
    private var promptCacheTtl by observable("FIVE_MINUTES")
    private var vertexAI by observable(false)
    private var balanceEnabled by observable(false)
    private var balanceApiPath by observable("")
    private var balanceResultPath by observable("")
    private var balanceText by observable("")

    private val models by observableList<ProviderModelItem>()

    // ===== Tab / 弹层 / 状态 =====
    private var currentTab by observable(0) // 0=配置 1=模型
    private var editSheetVisible by observable(false)
    private var editField by observable("")
    private var optionSheetVisible by observable(false)
    private var optionField by observable("") // type / ttl / fetchModels / modelAction / modelType
    private var optionTargetUid by observable("")
    private var fetchOptions by observableList<SheetOption>()
    private var confirmDeleteVisible by observable(false)
    private var manualAddStep by observable(0) // 0=无 1=输入modelId 2=输入显示名
    private var manualModelId by observable("")
    private var manualModelType by observable("CHAT")
    private var testing by observable(false)
    private var testResult by observable("")

    override fun created() {
        super.created()
        providerId = runCatching { pageData.params.optString("providerId") }.getOrDefault("")
        KLog.i(UI_LOG, "🖼 [Render] $TAG 页面创建 providerId=$providerId")
        if (providerId.isNotEmpty()) loadDetail()
    }

    private fun loadDetail() {
        bridge().getProviderDetail(providerId) { json ->
            safeRun("$TAG getProviderDetail") {
                val j = json ?: return@safeRun
                if (j.optString("error").isNotEmpty()) {
                    KLog.e(UI_LOG, "⚠ [Error] $TAG 拉取失败: ${j.optString("error")}")
                    return@safeRun
                }
                providerName = j.optString("name")
                providerType = j.optString("type")
                enabled = j.optBoolean("enabled")
                builtIn = j.optBoolean("builtIn")
                isDefaultBaseUrl = j.optBoolean("isDefaultBaseUrl")
                baseUrl = j.optString("baseUrl")
                apiKey = j.optString("apiKey")
                chatCompletionsPath = j.optString("chatCompletionsPath")
                useResponseApi = j.optBoolean("useResponseApi")
                includeHistoryReasoning = j.optBoolean("includeHistoryReasoning")
                promptCaching = j.optBoolean("promptCaching")
                promptCacheTtl = j.optString("promptCacheTtl").ifEmpty { "FIVE_MINUTES" }
                vertexAI = j.optBoolean("vertexAI")
                balanceEnabled = j.optBoolean("balanceEnabled")
                balanceApiPath = j.optString("balanceApiPath")
                balanceResultPath = j.optString("balanceResultPath")
                models.clear()
                j.optJSONArray("models")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        arr.optJSONObject(i)?.let { o ->
                            models.add(
                                ProviderModelItem(
                                    uid = o.optString("uid"),
                                    modelId = o.optString("modelId"),
                                    displayName = o.optString("displayName"),
                                    type = o.optString("type")
                                )
                            )
                        }
                    }
                }
                KLog.i(UI_LOG, "📦 [Data] $TAG 详情 [$providerName] type=$providerType models=${models.size}")
            }
        }
    }

    // ==================== 配置 Tab 操作 ====================

    /** 通用字段编辑保存（即时持久化） */
    private fun saveField(field: String, value: String) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 保存字段[$field] → ${value.take(32)}")
        val params = JSONObjectParams.providerParams(providerId).apply {
            when (field) {
                SheetField.NAME -> put("name", value)
                SheetField.BASE_URL -> put("baseUrl", value)
                SheetField.API_KEY -> put("apiKey", value)
                SheetField.API_PATH -> put("chatCompletionsPath", value)
                SheetField.BALANCE_API -> put("balanceApiPath", value)
                SheetField.BALANCE_RESULT -> put("balanceResultPath", value)
            }
        }
        bridge().updateProviderRaw(params) { json ->
            safeRun("$TAG updateProvider[$field]") {
                val j = json ?: return@safeRun
                if (j.optBoolean("ok")) {
                    when (field) {
                        SheetField.NAME -> providerName = value
                        SheetField.BASE_URL -> { baseUrl = value; isDefaultBaseUrl = false }
                        SheetField.API_KEY -> apiKey = value
                        SheetField.API_PATH -> chatCompletionsPath = value
                        SheetField.BALANCE_API -> balanceApiPath = value
                        SheetField.BALANCE_RESULT -> balanceResultPath = value
                    }
                    ctxToast("保存成功")
                    KLog.i(UI_LOG, "✅ [Bridge] $TAG 字段[$field]已持久化")
                } else ctxToast("保存失败")
            }
        }
    }

    /** 布尔开关保存 */
    private fun saveBool(field: String, value: Boolean) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 开关[$field] → $value")
        val params = JSONObjectParams.providerParams(providerId).apply {
            when (field) {
                "useResponseApi" -> put("useResponseApi", value)
                "includeHistoryReasoning" -> put("includeHistoryReasoning", value)
                "promptCaching" -> put("promptCaching", value)
                "vertexAI" -> put("vertexAI", value)
                "balanceEnabled" -> put("balanceEnabled", value)
            }
        }
        bridge().updateProviderRaw(params) { json ->
            safeRun("$TAG updateProvider[$field]") {
                if (json?.optBoolean("ok") == true) {
                    when (field) {
                        "useResponseApi" -> useResponseApi = value
                        "includeHistoryReasoning" -> includeHistoryReasoning = value
                        "promptCaching" -> promptCaching = value
                        "vertexAI" -> vertexAI = value
                        "balanceEnabled" -> balanceEnabled = value
                    }
                }
            }
        }
    }

    /** TTL 选择 */
    private fun saveTtl(v: String) {
        bridge().updateProviderRaw(JSONObjectParams.providerParams(providerId).apply { put("promptCacheTtl", v) }) { json ->
            safeRun("$TAG saveTtl") {
                if (json?.optBoolean("ok") == true) promptCacheTtl = v
            }
        }
    }

    /** 类型切换 */
    private fun switchType(newType: String) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 类型切换 → $newType")
        bridge().switchProviderType(providerId, newType) { json ->
            safeRun("$TAG switchProviderType") {
                if (json?.optBoolean("ok") == true) loadDetail()
            }
        }
    }

    /** 启停供应商 */
    private fun toggleEnabled(value: Boolean) {
        bridge().setProviderEnabled(providerId, value) { json ->
            safeRun("$TAG setProviderEnabled") {
                if (json?.optBoolean("ok") == true) enabled = value
            }
        }
    }

    /** 重置 BaseURL */
    private fun resetBaseUrl() {
        KLog.i(UI_LOG, "👆 [Click] $TAG 重置BaseURL")
        bridge().resetProviderBaseUrl(providerId) { json ->
            safeRun("$TAG resetProviderBaseUrl") {
                if (json?.optBoolean("ok") == true) loadDetail()
            }
        }
    }

    /** 拉取余额 */
    private fun fetchBalance() {
        KLog.i(UI_LOG, "👆 [Click] $TAG 拉取余额")
        balanceText = "查询中..."
        bridge().getProviderBalance(providerId) { json ->
            safeRun("$TAG getProviderBalance") {
                val j = json ?: return@safeRun
                balanceText = if (j.has("balance")) j.optString("balance") else "失败: ${j.optString("error").take(40)}"
            }
        }
    }

    /** 测试连通性 */
    private fun testConnection() {
        if (testing) return
        testing = true
        testResult = "测试中..."
        KLog.i(UI_LOG, "👆 [Click] $TAG 测试连通性 [$providerName]")
        bridge().testProvider(providerId) { json ->
            safeRun("$TAG testProvider") {
                val j = json ?: return@safeRun
                testing = false
                if (j.optBoolean("ok")) {
                    testResult = "✅ 连通成功，拉到 ${j.optInt("modelCount")} 个模型"
                } else {
                    testResult = "❌ 失败: ${j.optString("error").take(50)}"
                }
            }
        }
    }

    /** 分享供应商配置 */
    private fun share() {
        KLog.i(UI_LOG, "👆 [Click] $TAG 分享 [$providerName]")
        bridge().shareProviderConfig(providerId) { json ->
            safeRun("$TAG shareProviderConfig") {
                val j = json ?: return@safeRun
                val config = j.optString("config")
                if (config.isNotEmpty()) {
                    bridge().shareText(config)
                } else {
                    ctxToast("导出失败")
                }
            }
        }
    }

    /** 删除供应商 */
    private fun deleteProvider() {
        KLog.i(UI_LOG, "👆 [Click] $TAG 删除 [$providerName]")
        bridge().deleteProvider(providerId) { json ->
            safeRun("$TAG deleteProvider") {
                if (json?.optBoolean("ok") == true) closePage()
            }
        }
    }

    // ==================== 模型 Tab 操作 ====================

    /** 从 API 拉取可选模型 */
    private fun openFetchSheet() {
        KLog.i(UI_LOG, "👆 [Click] $TAG 从API拉取模型")
        fetchOptions.clear()
        optionField = "fetchModels"
        optionSheetVisible = true
        bridge().fetchProviderModels(providerId) { json ->
            safeRun("$TAG fetchProviderModels") {
                val j = json ?: return@safeRun
                val arr = j.optJSONArray("models")
                val list = mutableListOf<SheetOption>()
                arr?.let { a ->
                    for (i in 0 until a.length()) {
                        a.optJSONObject(i)?.let { o ->
                            list.add(
                                SheetOption(
                                    value = o.optString("modelId"),
                                    label = o.optString("modelId"),
                                    subtitle = o.optString("displayName")
                                )
                            )
                        }
                    }
                }
                fetchOptions.clear()
                fetchOptions.addAll(list)
                KLog.i(UI_LOG, "📦 [Data] $TAG API拉取到 ${list.size} 个可选模型")
            }
        }
    }

    /** 添加模型（手动流程/拉取选择） */
    private fun addModel(modelId: String, displayName: String, type: String) {
        bridge().addProviderModel(providerId, modelId, displayName, type) { json ->
            safeRun("$TAG addProviderModel") {
                if (json?.optBoolean("ok") == true) {
                    loadDetail()
                    ctxToast("已添加 $modelId")
                }
            }
        }
    }

    /** 模型项操作菜单（编辑modelId/编辑显示名/删除） */
    private fun modelAction(uid: String, action: String) {
        val target = models.firstOrNull { it.uid == uid } ?: return
        when (action) {
            "editId" -> {
                optionTargetUid = uid
                editField = SheetField.MODEL_ID
                editSheetVisible = true
            }
            "editName" -> {
                optionTargetUid = uid
                editField = SheetField.MODEL_NAME
                editSheetVisible = true
            }
            "delete" -> deleteModel(uid)
        }
    }

    private fun saveModelEdit(value: String) {
        val uid = optionTargetUid
        val target = models.firstOrNull { it.uid == uid } ?: return
        val newId = if (editField == SheetField.MODEL_ID) value else target.modelId
        val newName = if (editField == SheetField.MODEL_NAME) value else target.displayName
        bridge().updateProviderModel(providerId, uid, newId, newName, target.type) { json ->
            safeRun("$TAG updateProviderModel") {
                if (json?.optBoolean("ok") == true) loadDetail()
            }
        }
    }

    private fun deleteModel(uid: String) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 删除模型 uid=$uid")
        bridge().deleteProviderModel(providerId, uid) { json ->
            safeRun("$TAG deleteProviderModel") {
                if (json?.optBoolean("ok") == true) loadDetail()
            }
        }
    }

    private fun moveModel(index: Int, to: Int) {
        if (to !in models.indices) return
        bridge().moveProviderModel(providerId, index, to) { json ->
            safeRun("$TAG moveProviderModel") {
                if (json?.optBoolean("ok") == true) loadDetail()
            }
        }
    }

    private fun ctxToast(msg: String) = bridge().toast(msg)

    /** 编辑弹层当前值/label */
    private fun editSheetArgs(): Triple<String, String, String> = when (editField) {
        SheetField.NAME -> Triple("编辑供应商名称", "名称", providerName)
        SheetField.BASE_URL -> Triple("编辑 API Base URL", "Base URL（当前${if (isDefaultBaseUrl) "为默认" else "非默认"}）", baseUrl)
        SheetField.API_KEY -> Triple("编辑 API Key", "API Key（${if (apiKey.isEmpty()) "未配置" else "已配置"}）", "")
        SheetField.API_PATH -> Triple("编辑 API 路径", "chatCompletionsPath", chatCompletionsPath)
        SheetField.BALANCE_API -> Triple("编辑余额 API 路径", "apiPath", balanceApiPath)
        SheetField.BALANCE_RESULT -> Triple("编辑余额 JSON 路径", "resultPath", balanceResultPath)
        SheetField.MODEL_ID -> Triple("编辑模型 ID", "modelId", models.firstOrNull { it.uid == optionTargetUid }?.modelId ?: "")
        SheetField.MODEL_NAME -> Triple("编辑模型显示名", "displayName", models.firstOrNull { it.uid == optionTargetUid }?.displayName ?: "")
        "manualId" -> Triple("手动添加模型（${manualModelType}）", "modelId（显示名默认同 ID）", "")
        else -> Triple("编辑", "", "")
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = if (ctx.providerName.isNotEmpty()) ctx.providerName else "供应商详情",
                    showBack = true,
                    onBack = { ctx.closePage() },
                    rightActions = listOf(NavBarAction("分享") { ctx.share() })
                )
                // ===== 底部双 Tab 栏（vif/velse 重建保证高亮响应） =====
                View {
                    attr {
                        flexDirectionRow()
                        marginLeft(16f)
                        marginRight(16f)
                        marginTop(8f)
                        marginBottom(8f)
                        borderRadius(12f)
                        backgroundColor(HelixTheme.current.surfaceContainerLow)
                        padding(4f)
                    }
                    vif({ ctx.currentTab == 0 }) {
                        TabButton("配置", true) { ctx.currentTab = 0 }
                        TabButton("模型", false) { ctx.currentTab = 1 }
                    }
                    velse {
                        TabButton("配置", false) { ctx.currentTab = 0 }
                        TabButton("模型", true) { ctx.currentTab = 1 }
                    }
                }
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                    }

                    // ==================== 配置 Tab ====================
                    vif({ ctx.currentTab == 0 }) {
                        // 启用开关
                        View {
                            attr {
                                marginLeft(16f); marginRight(16f); marginBottom(8f)
                                borderRadius(12f)
                                backgroundColor(HelixTheme.current.surfaceContainerLow)
                            }
                            SwitchRow(
                                title = "启用供应商",
                                subtitle = "禁用后不会出现在模型选择列表",
                                checked = { ctx.enabled },
                                onCheckedChange = { ctx.toggleEnabled(it) }
                            )
                        }

                        // 基础配置（可编辑）
                        CardGroup(
                            items = listOf(
                                CardGroupItem(
                                    title = "供应商类型",
                                    subtitle = ctx.providerType + "（点击切换）",
                                    icon = "📦",
                                    onClick = { ctx.optionField = "type"; ctx.optionSheetVisible = true }
                                ),
                                CardGroupItem(
                                    title = "名称",
                                    subtitle = ctx.providerName,
                                    icon = "🏷️",
                                    onClick = { ctx.editField = SheetField.NAME; ctx.editSheetVisible = true }
                                ),
                                CardGroupItem(
                                    title = "API Base URL",
                                    subtitle = ctx.baseUrl,
                                    icon = "🔗",
                                    onClick = { ctx.editField = SheetField.BASE_URL; ctx.editSheetVisible = true }
                                ),
                                CardGroupItem(
                                    title = "API Key",
                                    subtitle = if (ctx.apiKey.isEmpty()) "未配置" else "已配置（点击编辑）",
                                    icon = "🔑",
                                    onClick = { ctx.editField = SheetField.API_KEY; ctx.editSheetVisible = true }
                                )
                            )
                        )

                        // OpenAI 特有
                        vif({ ctx.providerType == "openai" }) {
                            CardGroup(
                                items = listOf(
                                    CardGroupItem(
                                        title = "API 路径",
                                        subtitle = ctx.chatCompletionsPath,
                                        icon = "🛣️",
                                        onClick = { ctx.editField = SheetField.API_PATH; ctx.editSheetVisible = true }
                                    )
                                )
                            )
                            View {
                                attr {
                                    marginLeft(16f); marginRight(16f); marginBottom(8f)
                                    borderRadius(12f)
                                    backgroundColor(HelixTheme.current.surfaceContainerLow)
                                }
                                SwitchRow(
                                    title = "Response API",
                                    subtitle = "使用 OpenAI 新版 Response API（官方域名警告自动忽略）",
                                    checked = { ctx.useResponseApi },
                                    onCheckedChange = { ctx.saveBool("useResponseApi", it) }
                                )
                                SwitchRow(
                                    title = "回传历史思考过程",
                                    subtitle = "多轮对话携带 reasoning",
                                    checked = { ctx.includeHistoryReasoning },
                                    onCheckedChange = { ctx.saveBool("includeHistoryReasoning", it) }
                                )
                            }
                            // 余额配置
                            View {
                                attr {
                                    marginLeft(16f); marginRight(16f); marginBottom(8f)
                                    borderRadius(12f)
                                    backgroundColor(HelixTheme.current.surfaceContainerLow)
                                }
                                SwitchRow(
                                    title = "获取账户余额",
                                    subtitle = "自定义 API 路径与 JSON 路径",
                                    checked = { ctx.balanceEnabled },
                                    onCheckedChange = { ctx.saveBool("balanceEnabled", it) }
                                )
                                vif({ ctx.balanceEnabled }) {
                                    CardGroup(
                                        items = listOf(
                                            CardGroupItem(
                                                title = "余额 API 路径",
                                                subtitle = ctx.balanceApiPath,
                                                icon = "💰",
                                                onClick = { ctx.editField = SheetField.BALANCE_API; ctx.editSheetVisible = true }
                                            ),
                                            CardGroupItem(
                                                title = "余额 JSON 路径",
                                                subtitle = ctx.balanceResultPath,
                                                icon = "📄",
                                                onClick = { ctx.editField = SheetField.BALANCE_RESULT; ctx.editSheetVisible = true }
                                            ),
                                            CardGroupItem(
                                                title = "查询余额",
                                                subtitle = if (ctx.balanceText.isEmpty()) "点击拉取最新余额" else ctx.balanceText,
                                                icon = "💵",
                                                onClick = { ctx.fetchBalance() }
                                            )
                                        )
                                    )
                                }
                            }
                        }

                        // Claude 特有
                        vif({ ctx.providerType == "claude" }) {
                            View {
                                attr {
                                    marginLeft(16f); marginRight(16f); marginBottom(8f)
                                    borderRadius(12f)
                                    backgroundColor(HelixTheme.current.surfaceContainerLow)
                                }
                                SwitchRow(
                                    title = "提示词缓存",
                                    subtitle = "Prompt Caching（按 TTL 计费）",
                                    checked = { ctx.promptCaching },
                                    onCheckedChange = { ctx.saveBool("promptCaching", it) }
                                )
                                CardGroup(
                                    items = listOf(
                                        CardGroupItem(
                                            title = "缓存 TTL",
                                            subtitle = if (ctx.promptCacheTtl == "ONE_HOUR") "1 小时" else "5 分钟",
                                            icon = "⏱️",
                                            onClick = { ctx.optionField = "ttl"; ctx.optionSheetVisible = true }
                                        )
                                    )
                                )
                            }
                        }

                        // Google 特有
                        vif({ ctx.providerType == "google" }) {
                            View {
                                attr {
                                    marginLeft(16f); marginRight(16f); marginBottom(8f)
                                    borderRadius(12f)
                                    backgroundColor(HelixTheme.current.surfaceContainerLow)
                                }
                                SwitchRow(
                                    title = "Vertex AI",
                                    subtitle = "使用 Google Cloud Vertex AI 端点（服务账号配置在 Compose 版）",
                                    checked = { ctx.vertexAI },
                                    onCheckedChange = { ctx.saveBool("vertexAI", it) }
                                )
                            }
                        }

                        // 工具行：测试 / 重置BaseURL
                        CardGroup(
                            items = buildList {
                                add(
                                    CardGroupItem(
                                        title = "测试连通性",
                                        subtitle = if (ctx.testResult.isEmpty()) "从供应商拉取模型列表验证配置" else ctx.testResult,
                                        icon = "📡",
                                        onClick = { ctx.testConnection() }
                                    )
                                )
                                if (!ctx.isDefaultBaseUrl) {
                                    add(
                                        CardGroupItem(
                                            title = "重置 Base URL",
                                            subtitle = "恢复默认地址",
                                            icon = "♻️",
                                            onClick = { ctx.resetBaseUrl() }
                                        )
                                    )
                                }
                            }
                        )

                        // 删除（非内置供应商）
                        vif({ !ctx.builtIn }) {
                            View {
                                attr {
                                    height(44f)
                                    borderRadius(22f)
                                    justifyContentCenter()
                                    alignItemsCenter()
                                    marginLeft(16f); marginRight(16f)
                                    marginTop(8f); marginBottom(32f)
                                    backgroundColor(HelixTheme.current.errorContainer)
                                }
                                event {
                                    click {
                                        safeRun("$TAG 删除按钮") { ctx.confirmDeleteVisible = true }
                                    }
                                }
                                Text {
                                    attr {
                                        text("删除供应商")
                                        fontSize(15f)
                                        color(HelixTheme.current.onErrorContainer)
                                    }
                                }
                            }
                        }
                    }

                    // ==================== 模型 Tab ====================
                    velseif({ ctx.currentTab == 1 }) {
                        vif({ ctx.models.isEmpty() }) {
                            EmptyView(
                                message = "暂无模型\n点击下方按钮从 API 拉取或手动添加",
                                icon = "📦"
                            )
                        }
                        vfor({ ctx.models }) { m ->
                            val index = ctx.models.indexOfFirst { it.uid == m.uid }
                            View {
                                attr {
                                    flexDirectionColumn()
                                    marginLeft(16f); marginRight(16f); marginBottom(8f)
                                    padding(12f)
                                    borderRadius(12f)
                                    backgroundColor(HelixTheme.current.surfaceContainerLow)
                                }
                                // 主体（点击弹操作菜单）
                                View {
                                    attr { flexDirectionRow(); alignItemsCenter() }
                                    event {
                                        click {
                                            safeRun("$TAG 模型项[${m.displayName}]") {
                                                KLog.i(UI_LOG, "👆 [Click] $TAG 模型项 [${m.modelId}]")
                                                ctx.optionTargetUid = m.uid
                                                ctx.optionField = "modelAction"
                                                ctx.optionSheetVisible = true
                                            }
                                        }
                                    }
                                    View {
                                        attr { flex(1f) }
                                        Text {
                                            attr {
                                                text(m.displayName.ifEmpty { m.modelId })
                                                fontSize(14f)
                                                fontWeight700()
                                                color(HelixTheme.current.onSurface)
                                            }
                                        }
                                        Text {
                                            attr {
                                                text("${m.modelId} · ${m.type}")
                                                fontSize(10f)
                                                fontFamily("monospace")
                                                color(HelixTheme.current.onSurfaceVariant)
                                                marginTop(2f)
                                            }
                                        }
                                    }
                                    // 上移/下移
                                    Text {
                                        attr {
                                            text("↑"); fontSize(14f); color(HelixTheme.current.primary)
                                            marginLeft(8f); marginRight(4f); marginTop(8f); marginBottom(8f)
                                        }
                                        event {
                                            click { safeRun("$TAG 上移[${m.modelId}]") { ctx.moveModel(index, index - 1) } }
                                        }
                                    }
                                    Text {
                                        attr {
                                            text("↓"); fontSize(14f); color(HelixTheme.current.primary)
                                            marginLeft(4f); marginRight(4f); marginTop(8f); marginBottom(8f)
                                        }
                                        event {
                                            click { safeRun("$TAG 下移[${m.modelId}]") { ctx.moveModel(index, index + 1) } }
                                        }
                                    }
                                }
                            }
                        }
                        // 添加按钮行
                        View {
                            attr {
                                flexDirectionRow()
                                marginLeft(16f); marginRight(16f)
                                marginTop(8f); marginBottom(32f)
                            }
                            View {
                                attr {
                                    height(44f); flex(1f)
                                    borderRadius(22f)
                                    justifyContentCenter(); alignItemsCenter()
                                    backgroundColor(HelixTheme.current.primary)
                                    marginRight(4f)
                                }
                                event {
                                    click { safeRun("$TAG 从API拉取") { ctx.openFetchSheet() } }
                                }
                                Text {
                                    attr {
                                        text("从 API 拉取")
                                        fontSize(14f)
                                        color(HelixTheme.current.onPrimary)
                                    }
                                }
                            }
                            View {
                                attr {
                                    height(44f); flex(1f)
                                    borderRadius(22f)
                                    justifyContentCenter(); alignItemsCenter()
                                    backgroundColor(HelixTheme.current.secondaryContainer)
                                    marginLeft(4f)
                                }
                                event {
                                    click {
                                        safeRun("$TAG 手动添加") {
                                            KLog.i(UI_LOG, "👆 [Click] $TAG 手动添加模型")
                                            ctx.optionField = "modelType"
                                            ctx.optionSheetVisible = true
                                        }
                                    }
                                }
                                Text {
                                    attr {
                                        text("手动添加")
                                        fontSize(14f)
                                        color(HelixTheme.current.onSecondaryContainer)
                                    }
                                }
                            }
                        }
                    }
                }

                // ==================== 弹层 ====================
                val (sheetTitle, sheetLabel, sheetValue) = ctx.editSheetArgs()
                EditSheet(
                    visible = { ctx.editSheetVisible },
                    title = sheetTitle,
                    label = sheetLabel,
                    initialValue = sheetValue,
                    maskDisplay = ctx.editField == SheetField.API_KEY,
                    onSave = { v ->
                        ctx.editSheetVisible = false
                        when (ctx.editField) {
                            SheetField.MODEL_ID, SheetField.MODEL_NAME -> ctx.saveModelEdit(v)
                            "manualId" -> ctx.addModel(v, v, ctx.manualModelType)
                            else -> ctx.saveField(ctx.editField, v)
                        }
                    },
                    onDismiss = { ctx.editSheetVisible = false }
                )

                OptionSheet(
                    visible = { ctx.optionSheetVisible },
                    title = when (ctx.optionField) {
                        "type" -> "切换供应商类型"
                        "ttl" -> "缓存 TTL"
                        "modelAction" -> "模型操作"
                        "modelType" -> "选择模型类型"
                        "fetchModels" -> "从 API 拉取（点击添加）"
                        else -> ""
                    },
                    options = when (ctx.optionField) {
                        "type" -> listOf(
                            SheetOption("openai", "OpenAI 兼容", "chatCompletions/Response API"),
                            SheetOption("google", "Google Gemini", "generativelanguage"),
                            SheetOption("claude", "Anthropic Claude", "messages API")
                        )
                        "ttl" -> listOf(
                            SheetOption("FIVE_MINUTES", "5 分钟"),
                            SheetOption("ONE_HOUR", "1 小时")
                        )
                        "modelAction" -> listOf(
                            SheetOption("editId", "编辑模型 ID"),
                            SheetOption("editName", "编辑显示名"),
                            SheetOption("delete", "删除此模型", "不可恢复")
                        )
                        "modelType" -> listOf(
                            SheetOption("CHAT", "CHAT", "对话模型"),
                            SheetOption("IMAGE", "IMAGE", "图像生成"),
                            SheetOption("EMBEDDING", "EMBEDDING", "向量化")
                        )
                        "fetchModels" -> ctx.fetchOptions.toList()
                        else -> emptyList()
                    },
                    currentValue = when (ctx.optionField) {
                        "type" -> ctx.providerType
                        "ttl" -> ctx.promptCacheTtl
                        else -> ""
                    },
                    onSelect = { o ->
                        ctx.optionSheetVisible = false
                        when (ctx.optionField) {
                            "type" -> ctx.switchType(o.value)
                            "ttl" -> ctx.saveTtl(o.value)
                            "modelAction" -> ctx.modelAction(ctx.optionTargetUid, o.value)
                            "modelType" -> {
                                ctx.manualModelType = o.value
                                ctx.manualAddStep = 1
                                ctx.editField = "manualId"
                                ctx.editSheetVisible = true
                            }
                            "fetchModels" -> ctx.addModel(o.value, o.value, ctx.manualModelType)
                        }
                    },
                    onDismiss = { ctx.optionSheetVisible = false }
                )

                ConfirmSheet(
                    visible = { ctx.confirmDeleteVisible },
                    title = "删除供应商",
                    message = "确定删除「${ctx.providerName}」？该操作不可恢复，已配置的模型列表将一并删除。",
                    confirmText = "删除",
                    onConfirm = {
                        ctx.confirmDeleteVisible = false
                        ctx.deleteProvider()
                    },
                    onDismiss = { ctx.confirmDeleteVisible = false }
                )
            }
        }
    }

}

/** Tab 按钮小组件（顶层扩展，避免多 implicit receiver 歧义） */
private fun ViewContainer<*, *>.TabButton(label: String, selected: Boolean, onClick: () -> Unit) {
    View {
        attr {
            height(40f)
            flex(1f)
            borderRadius(10f)
            justifyContentCenter()
            alignItemsCenter()
            backgroundColor(
                if (selected) HelixTheme.current.primary else HelixTheme.current.surfaceContainerLow
            )
        }
        event {
            click {
                safeRun("$TAG Tab[$label]") {
                    KLog.i(UI_LOG, "👆 [Click] $TAG 切换到[$label]")
                    onClick()
                }
            }
        }
        Text {
            attr {
                text(label)
                fontSize(14f)
                fontWeight700()
                color(if (selected) HelixTheme.current.onPrimary else HelixTheme.current.onSurfaceVariant)
            }
        }
    }
}

/** 桥参数构造辅助（避免 Kuikly JSONObject 直接依赖扩散） */
internal object JSONObjectParams {
    fun providerParams(id: String): com.tencent.kuikly.core.nvi.serialization.json.JSONObject =
        com.tencent.kuikly.core.nvi.serialization.json.JSONObject().apply { put("id", id) }
}
