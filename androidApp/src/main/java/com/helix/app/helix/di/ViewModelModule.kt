package com.helix.app.di

import com.helix.app.ui.pages.assistant.AssistantVM
import com.helix.app.ui.pages.assistant.detail.AssistantDetailVM
import com.helix.app.ui.pages.backup.BackupVM
import com.helix.app.ui.pages.chat.ChatDrawerVM
import com.helix.app.ui.pages.chat.ChatVM
import com.helix.app.ui.pages.debug.DebugVM
import com.helix.app.ui.pages.favorite.FavoriteVM
import com.helix.app.ui.pages.search.SearchVM
import com.helix.app.ui.pages.history.HistoryVM
import com.helix.app.ui.pages.stats.StatsVM
import com.helix.app.ui.pages.imggen.ImgGenVM
import com.helix.app.ui.pages.extensions.PromptVM
import com.helix.app.ui.pages.extensions.QuickMessagesVM
import com.helix.app.ui.pages.extensions.skills.SkillDetailVM
import com.helix.app.ui.pages.extensions.skills.SkillsVM
import com.helix.app.ui.pages.extensions.workspace.WorkspaceDetailVM
import com.helix.app.ui.pages.extensions.workspace.WorkspaceVM
import com.helix.app.ui.pages.setting.SettingVM
import com.helix.app.ui.pages.share.handler.ShareHandlerVM
import com.helix.app.ui.pages.translator.TranslatorVM
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModel<ChatVM> { params ->
        ChatVM(
            id = params.get(),
            context = get(),
            settingsStore = get(),
            conversationRepo = get(),
            chatService = get(),
            updateChecker = get(),
            filesManager = get(),
            favoriteRepository = get(),
        )
    }
    viewModelOf(::ChatDrawerVM)
    viewModelOf(::SettingVM)
    viewModelOf(::DebugVM)
    viewModelOf(::HistoryVM)
    viewModelOf(::AssistantVM)
    viewModel<AssistantDetailVM> {
        AssistantDetailVM(
            id = it.get(),
            settingsStore = get(),
            memoryRepository = get(),
            filesManager = get(),
            skillManager = get(),
            workspaceRepository = get(),
        )
    }
    viewModelOf(::TranslatorVM)
    viewModel<ShareHandlerVM> {
        ShareHandlerVM(
            text = it.get(),
            settingsStore = get(),
        )
    }
    viewModelOf(::BackupVM)
    viewModelOf(::ImgGenVM)
    viewModelOf(::PromptVM)
    viewModelOf(::QuickMessagesVM)
    viewModelOf(::SkillsVM)
    viewModelOf(::SkillDetailVM)
    viewModelOf(::WorkspaceVM)
    viewModel<WorkspaceDetailVM> {
        WorkspaceDetailVM(
            id = it.get(),
            repository = get(),
        )
    }
    viewModelOf(::FavoriteVM)
    viewModelOf(::SearchVM)
    viewModelOf(::StatsVM)
}
