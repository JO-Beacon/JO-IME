package com.kingzcheung.xime.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.twotone.Inventory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * 手写的设置都放在这一页（用户 10-21 要求和语音分开）：停顿识别、公式标记、手写模型；
 * 手写训练和数据采集只在「装到我的设备」和调试包里出现。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandwritingSettingsContent(
    onBack: () -> Unit,
    onOpenModels: () -> Unit = {},
    onOpenTraining: () -> Unit = {},
    onOpenCapture: () -> Unit = {},
    devTools: Boolean = com.kingzcheung.xime.BuildConfig.DEV_TOOLS || com.kingzcheung.xime.BuildConfig.DEBUG,
) {
    Scaffold(modifier = Modifier.testTag("handwriting-settings"), containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(title = { Text("手写") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
            }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface))
        }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            HandwritingSettings()
            SettingsSection(title = "手写模型", content = {
                SettingsItem(Icons.TwoTone.Inventory, "已安装的手写模型",
                    "内置手写、公式手写模型和个人手写模型；下载、切换和删除", onClick = onOpenModels, showArrow = true)
            })
        }
    }
}
