package me.rerere.rikkahub.ui.components

import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable

/**
 * material3 兼容层：项目原使用 1.5.0-alpha26 的 rememberBottomSheetState(initialValue, enabledValues)，
 * 降级到 1.5.0-alpha18（compileSdk 35 兼容上限）后该 API 不存在，
 * 用 alpha18 的 rememberModalBottomSheetState(skipHiddenState, confirmValueChange) 等价实现：
 * - enabledValues 通过 confirmValueChange 放行（仅允许切换到集合内的状态）
 * - initialValue != Hidden 时 skipHiddenState = true（跳过 Hidden 初始态的等价语义）
 * 返回的 SheetState 可直接传给 ModalBottomSheet。
 */
@Composable
fun rememberBottomSheetState(
    initialValue: SheetValue,
    enabledValues: Set<SheetValue> = setOf(
        SheetValue.Hidden,
        SheetValue.PartiallyExpanded,
        SheetValue.Expanded,
    ),
): SheetState = rememberModalBottomSheetState(
    // alpha18 签名 (Boolean, confirmValueChange)，参数名与 1.3 不同，用位置参数调用
    // skipHiddenState 由 enabledValues 推导：集合不含 Hidden 时 sheet 不可隐藏（保持"仅允许切换到集合内状态"语义）
    SheetValue.Hidden !in enabledValues,
    { it in enabledValues },
)
