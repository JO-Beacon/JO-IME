package com.kingzcheung.xime.ui.settings

import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import com.kingzcheung.xime.ui.keyboard.normalizeHandwritingPause
import com.kingzcheung.xime.ui.keyboard.normalizeCursorHoldSeconds
import com.kingzcheung.xime.ui.keyboard.normalizeLanguageMenuHoldSeconds
import com.kingzcheung.xime.ui.keyboard.normalizeSymbolKeyHoldSeconds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingzcheung.xime.ui.keyboard.SpaceHoldAction
import com.kingzcheung.xime.ui.keyboard.FormulaDelimiter
import androidx.compose.foundation.clickable
import com.kingzcheung.xime.ui.keyboard.CursorGestureMode
import com.kingzcheung.xime.ui.keyboard.cursorDistanceMillimeters
import com.kingzcheung.xime.ui.keyboard.normalizeCursorSwipeLines
import com.kingzcheung.xime.ui.keyboard.SymbolInputMode
import com.kingzcheung.xime.ui.keyboard.rememberKeyboardInputPreferences
import com.kingzcheung.xime.ui.keyboard.InputGestureLock
import com.kingzcheung.xime.ui.keyboard.InputGestureLocks
import com.kingzcheung.xime.ui.keyboard.KeyboardInputPreferences
import com.kingzcheung.xime.settings.SettingsPreferences

/** Groups of [InputExperienceSettings]; the 布局与显示 sub-pages each show some of them. */
internal enum class InputExperiencePart { SYMBOLS, FLICK_DISC, KANA, CURSOR, TIMING, HANDWRITING, KEY_TEXT }

/** 「时长与距离」里的几项（设置搜索点中其中一项时自动展开，见 [InputExperienceSettings] 的 collapsedSummary）。 */
internal val TIMING_SEARCH_KEYS = setOf("长按空格多久开始", "从中间滑到边缘走几行", "光标每移动一字所需距离",
    "长按语言键弹出列表", "符号键松手打开符号的时限", "时长与距离")

@Composable
internal fun InputExperienceSettings(
    parts: Set<InputExperiencePart> = InputExperiencePart.entries.toSet(),
    title: String = "共通输入",
    /** 默认收起，点标题行展开（用户 10-31：「按键与手势」的时长、距离滑条收起来）；设置搜索点中里面的项时自动展开。 */
    collapsedSummary: String? = null,
) {
    val context = LocalContext.current
    val saved = rememberKeyboardInputPreferences()
    var settings by remember(saved) { mutableStateOf(saved) }
    var expanded by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(collapsedSummary == null) }
    val searchRequest = SettingsSearchFocus.request
    LaunchedEffect(searchRequest) {
        if (searchRequest != null && searchRequest.keys.any { it in TIMING_SEARCH_KEYS } &&
            InputExperiencePart.TIMING in parts) expanded = true
    }
    SettingsSection(title = title, content = {
        if (collapsedSummary != null) {
            Row(Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp)
                .testTag("settings-fold:$title"), verticalAlignment = Alignment.CenterVertically) {
                Text(if (expanded) "收起" else collapsedSummary, modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(if (expanded) androidx.compose.material.icons.Icons.Default.ExpandLess
                    else androidx.compose.material.icons.Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "收起$title" else "展开$title")
            }
        }
        if (expanded) Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            var first = true
            @Composable fun divider() { if (!first) HorizontalDivider(); first = false }
            if (InputExperiencePart.SYMBOLS in parts) {
            divider()
            Row(Modifier.settingsSearchAnchor("Z X C V 角标做撤销、剪切、复制、粘贴", bleed = 16.dp, bleedVertical = 6.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Z X C V 角标做撤销、剪切、复制、粘贴", style = MaterialTheme.typography.titleSmall)
                    Text("26 键 Z X C V " +
                        (if (settings.symbolInputMode == SymbolInputMode.LONG_PRESS) "长按" else if (settings.reverseSymbolSwipe) "上拉" else "下拉") +
                        "就执行；按住 !@# 当 Ctrl 照常可用。",
                        style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = settings.zxcvEditShortcuts, colors = settingsSwitchColors(),
                    modifier = Modifier.testTag("zxcv-edit-shortcuts"), onCheckedChange = {
                        settings = settings.copy(zxcvEditShortcuts = it); settings.save(context)
                    })
            }
            }
            if (InputExperiencePart.CURSOR in parts) {
            divider()
            // 底部留白滑动和上面三选一互不影响，单独一行开关（用户 10-31 要求新增，默认关）。
            Row(Modifier.settingsSearchAnchor("底部留白滑动移动光标", bleed = 16.dp, bleedVertical = 6.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("底部留白滑动移动光标", style = MaterialTheme.typography.titleSmall)
                    Text("固定键盘在「调节键盘」里调出底部留白后，在键盘下面空出的那一条左右滑动移动光标，按住 Shift 滑是选字。没有留白或悬浮时不起作用。",
                        style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = settings.bottomLiftCursor, modifier = Modifier.testTag("bottom-lift-cursor-switch"),
                    onCheckedChange = { settings = settings.copy(bottomLiftCursor = it); settings.save(context) })
            }
            if (settings.cursorGesture != CursorGestureMode.NONE || settings.bottomLiftCursor) {
                // 只在总开关打开时显示：「按键效果」里关了振动或按键音，这里的开关不起作用。
                @Composable fun cursorStepSwitch(title: String, description: String, checked: Boolean, tag: String,
                    update: (Boolean) -> KeyboardInputPreferences) {
                    Row(Modifier.settingsSearchAnchor(title, bleed = 16.dp, bleedVertical = 6.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.titleSmall)
                            Text(description, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = checked, modifier = Modifier.testTag(tag), onCheckedChange = {
                            settings = update(it); settings.save(context)
                        })
                    }
                }
                if (SettingsPreferences.getHapticMode(context) != "disabled") cursorStepSwitch("移动光标时振动",
                    "光标每移动一字轻震一下。", settings.cursorStepHaptic, "cursor-step-haptic") { settings.copy(cursorStepHaptic = it) }
                if (SettingsPreferences.isSoundEnabled(context)) cursorStepSwitch("移动光标时播放按键音",
                    "光标每移动一字响一下按键音。", settings.cursorStepSound, "cursor-step-sound") { settings.copy(cursorStepSound = it) }
            }
            }
            if (InputExperiencePart.TIMING in parts) {
            divider()
            // 触发时长跟着空格当前的长按功能放，标题写明它控制的是哪个功能。
            @Composable fun spaceHoldSlider() {
                val actionText = when (settings.effectiveSpaceHold) {
                    SpaceHoldAction.CURSOR -> "移动光标"; SpaceHoldAction.REPEAT -> "连续输入空格"; SpaceHoldAction.VOICE_TOGGLE -> "说话"
                }
                Column(Modifier.settingsSearchAnchor("长按空格多久开始", bleed = 16.dp, bleedVertical = 6.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("长按空格多久开始$actionText：${"%.1f".format(settings.cursorHoldSeconds)} 秒")
                    Slider(value = settings.cursorHoldSeconds, modifier = Modifier.testTag("cursor-hold-slider"),
                        onValueChange = { settings = settings.copy(cursorHoldSeconds = normalizeCursorHoldSeconds(it)) },
                        onValueChangeFinished = { settings.save(context) }, valueRange = 0.1f..1f, steps = 8)
                    Text("默认 0.2 秒。", style = MaterialTheme.typography.bodySmall)
                }
            }
            spaceHoldSlider()
            // 只显示当前生效的项：滑动移动光标关闭且底部留白滑动也关着时不显示光标距离。
            if (settings.cursorGesture != CursorGestureMode.NONE || settings.bottomLiftCursor) {
                Column(Modifier.settingsSearchAnchor("从中间滑到边缘走几行", bleed = 16.dp, bleedVertical = 6.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("从中间滑到屏幕边缘走：${"%.2f".format(settings.cursorSwipeLines).trimEnd('0').trimEnd('.')} 行")
                    Text("按输入框一行能放几个字换算，换屏幕、横竖屏不用重调", style = MaterialTheme.typography.bodySmall)
                    Slider(value = settings.cursorSwipeLines, onValueChange = { settings = settings.copy(cursorSwipeLines = normalizeCursorSwipeLines(it)) },
                        onValueChangeFinished = { settings.save(context) }, valueRange = 0.5f..3f, steps = 9)
                }
                Column(Modifier.settingsSearchAnchor("光标每移动一字所需距离", bleed = 16.dp, bleedVertical = 6.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("光标每移动一字所需距离：${settings.cursorStepDp.toInt()} 档（约 ${"%.1f".format(cursorDistanceMillimeters(settings.cursorStepDp))} 毫米）")
                    Text("只在读不到一行几个字的输入框里用；越小越灵敏", style = MaterialTheme.typography.bodySmall)
                    Slider(value = settings.cursorStepDp, onValueChange = { settings = settings.copy(cursorStepDp = it) },
                        onValueChangeFinished = { settings.save(context) }, valueRange = 6f..24f, steps = 17)
                }
            }
            Column(Modifier.settingsSearchAnchor("长按语言键弹出列表", bleed = 16.dp, bleedVertical = 6.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("长按语言键弹出列表：${"%.2f".format(settings.languageMenuHoldSeconds)} 秒")
                Text("默认 0.15 秒。", style = MaterialTheme.typography.bodySmall)
                Slider(value = settings.languageMenuHoldSeconds, modifier = Modifier.testTag("language-menu-hold-slider"),
                    onValueChange = { settings = settings.copy(languageMenuHoldSeconds = normalizeLanguageMenuHoldSeconds(it)) },
                    onValueChangeFinished = { settings.save(context) }, valueRange = 0.05f..1f, steps = 18)
            }
            Column(Modifier.settingsSearchAnchor("符号键松手打开符号的时限", bleed = 16.dp, bleedVertical = 6.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("符号键松手打开符号的时限：${"%.1f".format(settings.symbolKeyHoldSeconds)} 秒")
                Text("默认 1 秒。26 键按住 !@# 是 Ctrl，拖到字母上松手发快捷键（如 C 复制）；超过时限或拖出去再松手不打开符号。按满时限字母键才显示快捷键。", style = MaterialTheme.typography.bodySmall)
                Slider(value = settings.symbolKeyHoldSeconds, modifier = Modifier.testTag("symbol-key-hold-slider"),
                    onValueChange = { settings = settings.copy(symbolKeyHoldSeconds = normalizeSymbolKeyHoldSeconds(it)) },
                    onValueChangeFinished = { settings.save(context) }, valueRange = 0.3f..2f, steps = 16)
            }
            }
            if (InputExperiencePart.HANDWRITING in parts) {
            divider()
            Column(Modifier.settingsSearchAnchor("手写停顿识别", bleed = 16.dp, bleedVertical = 6.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("手写停顿识别：${"%.1f".format(settings.handwritingPauseSeconds)} 秒")
                Text("默认 0.5 秒；继续书写会重新计时。", style = MaterialTheme.typography.bodySmall)
                Slider(value = settings.handwritingPauseSeconds, modifier = Modifier.testTag("handwriting-pause-slider"),
                    onValueChange = { settings = settings.copy(handwritingPauseSeconds = normalizeHandwritingPause(it)) },
                    onValueChangeFinished = { settings.save(context) }, valueRange = 0.1f..2.5f, steps = 23)
            }
            Row(Modifier.settingsSearchAnchor("手写边写边识别", bleed = 16.dp, bleedVertical = 6.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("手写边写边识别", style = MaterialTheme.typography.titleSmall)
                    Text("每写完一笔就在后台先认，停顿一到马上出候选，不用停顿后再等识别。中文、日语手写可用；会多用一点电。",
                        style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = settings.handwritingRecognizeWhileWriting, colors = settingsSwitchColors(),
                    modifier = Modifier.testTag("handwriting-recognize-while-writing"), onCheckedChange = {
                        settings = settings.copy(handwritingRecognizeWhileWriting = it); settings.save(context)
                    })
            }
            Column(Modifier.settingsSearchAnchor("手写公式两边的标记", bleed = 16.dp, bleedVertical = 6.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("手写公式两边的标记", style = MaterialTheme.typography.titleSmall)
                Text("公式模式选中候选后上屏的样子，例如 x^{2} 上屏为 \$x^{2}\$ 或 \\[x^{2}\\]。", style = MaterialTheme.typography.bodySmall)
                // 没装公式手写模型时手写板没有公式模式，这一项暂时不起作用：照常显示并写明。
                val formulaReady = remember { com.kingzcheung.xime.handwriting.FormulaRecognizer.isAvailable(context) }
                if (!formulaReady) Text("公式手写模型还没下载，下载后（手写 › 已安装的手写模型）英文手写里才有公式模式，这里的标记才用得上。",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FormulaDelimiter.entries.forEach { option ->
                    Row(Modifier.fillMaxWidth().clickable { settings = settings.copy(formulaDelimiter = option); settings.save(context) }
                        .padding(vertical = 4.dp).testTag("formula-delimiter:${option.name}"),
                        verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = settings.formulaDelimiter == option,
                            onClick = { settings = settings.copy(formulaDelimiter = option); settings.save(context) })
                        Text(option.label)
                    }
                }
            }
            }
            if (InputExperiencePart.KEY_TEXT in parts) {
            divider()
            Column(Modifier.settingsSearchAnchor("按键字号", bleed = 16.dp, bleedVertical = 6.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("按键字号：${(settings.keyTextScale * 100).toInt()}%")
                Text("a b c  A B C  拼音", fontSize = (16f * settings.keyTextScale).sp)
                Slider(value = settings.keyTextScale, onValueChange = { settings = settings.copy(keyTextScale = it) },
                    onValueChangeFinished = { settings.save(context) }, valueRange = 0.8f..1.6f, steps = 15)
            }
            divider()
            run {
                val lock = InputGestureLocks.lockFor(settings, InputGestureLock.Option.UPPERCASE_KEY_LABELS, true)
                    ?.takeIf { !settings.uppercaseKeyLabels }
                Row(Modifier.settingsSearchAnchor("字母键显示大写", bleed = 16.dp, bleedVertical = 6.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("字母键显示大写", style = MaterialTheme.typography.titleSmall)
                        Text("只改键帽显示；英文大小写仍看 Shift 键上的箭头。",
                            style = MaterialTheme.typography.bodySmall)
                        if (lock != null) SettingsLockNote(lock.message)
                    }
                    Switch(checked = settings.uppercaseKeyLabels, enabled = lock == null, colors = settingsSwitchColors(),
                        modifier = Modifier.testTag("uppercase-key-labels"), onCheckedChange = {
                            settings = settings.copy(uppercaseKeyLabels = it); settings.save(context)
                        })
                }
            }
            divider()
            run {
                val lock = InputGestureLocks.lockFor(settings, InputGestureLock.Option.CHINESE_UPPERCASE_KEY_LABELS, true)
                    ?.takeIf { !settings.chineseUppercaseKeyLabels }
                Row(Modifier.settingsSearchAnchor("中文键盘字母显示大写", bleed = 16.dp, bleedVertical = 6.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("中文键盘字母显示大写", style = MaterialTheme.typography.titleSmall)
                        Text("像搜狗：中文时字母键显示大写，切到英文显示小写（按 Shift 才大写），不看空格键也知道现在是中文还是英文。只改键帽显示，26 键和 14 键都适用。",
                            style = MaterialTheme.typography.bodySmall)
                        if (lock != null) SettingsLockNote(lock.message)
                    }
                    Switch(checked = settings.chineseUppercaseKeyLabels, enabled = lock == null, colors = settingsSwitchColors(),
                        modifier = Modifier.testTag("chinese-uppercase-key-labels"), onCheckedChange = {
                            settings = settings.copy(chineseUppercaseKeyLabels = it); settings.save(context)
                        })
                }
            }
            }
        }
    })
}
