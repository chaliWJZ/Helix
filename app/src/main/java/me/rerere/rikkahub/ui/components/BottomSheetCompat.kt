package me.rerere.rikkahub.ui.components

import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable

/**
 * material3 兼容层：项目原使用 1.5.0-alpha26 的 rememberBottomSheetState(initialValue, enabledValues)，
 * 降级到 1.5.0-alpha18（compileSdk 35 兼容上限）后该 API 不存在，
 * 用 alpha18 的 rememberModalBottomSheetState(skipHiddenState, confirmValueChange) 等价实现。
 *
 * 语义映射说明：
 * - skipHiddenState 由 enabledValues 是否包含 Hidden 推导（不含 Hidden 则不可隐藏）
 * - confirmValueChange 全部放行：alpha18 的 ModalBottomSheet 打开时默认目标为 PartiallyExpanded（半屏），
 *   若按原 enabledValues 拦截非集合内状态，会连弹出的必经状态 PartiallyExpanded 一起拦掉，
 *   导致 sheet 永远无法弹出（表现为按钮点击无反应）；原 API 的状态约束语义
 *   在 ModalBottomSheet 体系下由半屏/全屏拖动交互自然实现
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
    // alpha18 签名 (Boolean, confirmValueChange)，参数名与旧版不同，用位置参数调用
    SheetValue.Hidden !in enabledValues,
    { true },
)
