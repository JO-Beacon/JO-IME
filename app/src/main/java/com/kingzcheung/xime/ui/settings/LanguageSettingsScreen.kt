package com.kingzcheung.xime.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.twotone.ViewModule
import androidx.compose.material.icons.twotone.Inventory
import androidx.compose.material.icons.twotone.Schedule
import androidx.compose.material.icons.twotone.Translate
import androidx.compose.material.icons.twotone.SpaceBar
import androidx.compose.material.icons.twotone.EmojiEmotions
import androidx.compose.material.icons.twotone.Functions
import androidx.compose.material.icons.twotone.FontDownload
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.kingzcheung.xime.settings.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.kingzcheung.xime.ui.theme.MaterialLevel
import com.kingzcheung.xime.ui.theme.VisualStyles
import com.kingzcheung.xime.ui.theme.visualMaterial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsContent(onOpenSchemaResources: () -> Unit = {}, onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(modifier = Modifier.testTag("language-settings"), containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopAppBar(title = { Text("语言与方案") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
        }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)) }
    ) { padding ->
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        SettingsSearchAutoScroll(listState)
        LazyColumn(modifier = Modifier.settingsGutterScroll(listState).fillMaxSize().testTag("language-settings-list").padding(padding)
            .padding(horizontal = 16.dp, vertical = 8.dp),
            state = listState, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item(key = "chinese-output") {
                val input = com.kingzcheung.xime.ui.keyboard.rememberKeyboardInputPreferences()
                SettingsSection(title = "中文输出") {
                    SettingsToggleItem(Icons.TwoTone.Translate, "繁体输出",
                        "候选和上屏改用台湾正体（台灣、計畫、了解），全拼、双拼、九键、14 键和仓颉都适用（关着时仓颉也出简体）；注音始终出正体。词库和学习记录不变，开启时不给中文联想。",
                        checked = input.traditionalOutput, onCheckedChange = { input.copy(traditionalOutput = it).save(context) })
                }
            }
            item(key = "english-input") {
                val input = com.kingzcheung.xime.ui.keyboard.rememberKeyboardInputPreferences()
                SettingsSection(title = "英文输入") {
                    SettingsToggleItem(Icons.TwoTone.SpaceBar, "选词后自动加空格",
                        "英文（含西班牙语）点候选栏的词后自动补一个空格，接着打下一个词不用再按空格。紧接着按空格不会多出一个；" +
                            "打 , . ? ! : ; 时标点挪到空格前面；退格删掉这个空格。",
                        checked = input.englishAutoSpace, onCheckedChange = { input.copy(englishAutoSpace = it).save(context) })
                    HorizontalDivider()
                    SettingsToggleItem(Icons.TwoTone.SpaceBar, "标点后自动加空格",
                        "英文（含西班牙语）键盘在字母后面打 , . ? ! : ; 时自动跟一个空格（Hello, world）。紧接着按空格不会多出一个，" +
                            "连打 ... ?! 空格留在最后；3.14、1,000、10:30 和网址、邮箱、密码框不加；退格删掉这个空格。",
                        checked = input.englishPunctuationSpace, onCheckedChange = { input.copy(englishPunctuationSpace = it).save(context) })
                }
            }
            // 已装的方案组合（启停、应用更改）和导入，原首页「输入资源」。
            item(key = "schema-resources") {
                SettingsSection(title = "方案资源") {
                    SettingsItem(Icons.TwoTone.Inventory, "已安装的方案",
                        "启停语言、方案与布局的组合", onClick = onOpenSchemaResources, showArrow = true)
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
