package com.helix.kuikly.pager

import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.pager.Pager
import com.helix.kuikly.modules.HelixBridgeModule

/**
 * Helix Kuikly 页面基类
 *
 * 所有 Helix Kuikly 页面应继承此类，自动注册 HelixBridgeModule，
 * 使页面可通过 acquireModule<HelixBridgeModule> 调用原生能力。
 */
abstract class HelixBasePage : Pager() {

    override fun createExternalModules(): Map<String, Module>? {
        val externalModules = hashMapOf<String, Module>()
        externalModules[HelixBridgeModule.MODULE_NAME] = HelixBridgeModule()
        return externalModules
    }

    /** 获取桥接 Module 的快捷方法 */
    protected fun bridge(): HelixBridgeModule =
        acquireModule(HelixBridgeModule.MODULE_NAME)

    /** 关闭当前页面 */
    protected fun closePage() {
        acquireModule<RouterModule>(RouterModule.MODULE_NAME).closePage()
    }

    /** 打开页面 */
    protected fun openPage(pageName: String, params: JSONObject? = null) {
        acquireModule<RouterModule>(RouterModule.MODULE_NAME).openPage(pageName, params)
    }

    override fun body(): ViewBuilder {
        return {
        }
    }
}

