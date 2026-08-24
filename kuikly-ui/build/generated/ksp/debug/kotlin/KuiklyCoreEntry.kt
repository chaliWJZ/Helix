// HelixTestPage
// this file is generating by ksp
// please do not modified it!!!
package com.tencent.kuikly.core.android

import com.tencent.kuikly.core.IKuiklyCoreEntry
import com.tencent.kuikly.core.IKuiklyCoreEntry.Delegate
import com.tencent.kuikly.core.manager.BridgeManager
import com.tencent.kuikly.core.nvi.NativeBridge
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.Unit

public class KuiklyCoreEntry : IKuiklyCoreEntry {
  private var hadRegisterNativeBridge: Boolean = false

  public override var `delegate`: Delegate? = null

  public override fun catchException(): Boolean = BridgeManager.catchException

  public override fun callKotlinMethod(
    methodId: Int,
    arg0: Any?,
    arg1: Any?,
    arg2: Any?,
    arg3: Any?,
    arg4: Any?,
    arg5: Any?
  ): Unit {
    if (!hadRegisterNativeBridge) {

    triggerRegisterPages()

              hadRegisterNativeBridge = true
                  NativeBridge.isContextThread = true
                  val nativeBridge = NativeBridge()
                  nativeBridge.delegate = object : NativeBridge.NativeBridgeDelegate {
                      override fun callNative(
                            methodId: Int,
                            arg0: Any?,
                            arg1: Any?,
                            arg2: Any?,
                            arg3: Any?,
                            arg4: Any?,
                            arg5: Any?
                      ): Any? {
                          return delegate?.callNative(methodId, arg0, arg1, arg2, arg3, arg4,
                                arg5)
                      }
                  }
                  BridgeManager.registerNativeBridge(arg0 as String, nativeBridge)
              }
    BridgeManager.callKotlinMethod(methodId, arg0, arg1, arg2, arg3, arg4, arg5)
  }

  public override fun triggerRegisterPages(): Unit {
    BridgeManager.registerPageRouter("HelixTestPage") {
        com.helix.kuikly.pager.HelixTestPage()
        }
  }
}
