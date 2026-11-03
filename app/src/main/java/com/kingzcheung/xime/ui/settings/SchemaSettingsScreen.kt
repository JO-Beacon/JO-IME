package com.kingzcheung.xime.ui.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kingzcheung.xime.settings.SchemaManager
import com.kingzcheung.xime.settings.SchemaMeta
import com.kingzcheung.xime.settings.InputLanguage
import com.kingzcheung.xime.settings.LanguagePreferences
import com.kingzcheung.xime.settings.InputModes
import com.kingzcheung.xime.viewmodel.SchemaSettingsViewModel
import com.kingzcheung.xime.ui.menubar.rememberDragOrder
import com.kingzcheung.xime.ui.menubar.dragOrderItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchemaSettingsContent(
    onBack: () -> Unit,
    onNavigateToRimeFileBrowser: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: SchemaSettingsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExtraSchemas by remember { mutableStateOf(false) }
    var englishEnabled by remember { mutableStateOf(InputLanguage.ENGLISH in LanguagePreferences.enabled(context)) }
    var modeOrderRevision by remember { mutableIntStateOf(0) }
    // F6: 从方案市场/导入返回时自动重扫描，新装方案立即出现
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
        englishEnabled = InputLanguage.ENGLISH in LanguagePreferences.enabled(context)
        modeOrderRevision++
    }
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    uiState.deployReport?.let { report ->
        AlertDialog(
            onDismissRequest = { viewModel.clearDeployReport() },
            title = { Text(if (report.startsWith("部署失败")) "部署失败" else "部署完成") },
            text = { Text(report) },
            confirmButton = { TextButton(onClick = { viewModel.clearDeployReport() }) { Text("知道了") } },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("已安装的方案") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val installedIds = uiState.allSchemas.map { it.schemaId }.toSet()
            val visibleSchemas = uiState.allSchemas.filter {
                com.kingzcheung.xime.settings.CyimeInputDefaults.visibleSchema(it.schemaId, installedIds)
            }
            val schemaById = visibleSchemas.associateBy { it.schemaId }
            val fullOrder = remember(uiState.allSchemas, modeOrderRevision) {
                orderedInstalledModeIds(context, uiState.allSchemas)
            }
            val enabledIds = uiState.enabledSchemas.toSet()
            val savedEnabledIds = fullOrder.filter { id ->
                (id == InputModes.ENGLISH && englishEnabled) || (id in enabledIds && id in schemaById)
            }
            val dragOrder = rememberDragOrder(savedEnabledIds) { reordered ->
                if (reorderEnabledModes(context, uiState.allSchemas, enabledIds, englishEnabled, reordered)) modeOrderRevision++
            }
            val orderedEnabledIds = dragOrder.order
            // 和词库、布局管理同一套：组名 + 连成一张卡片的行 + 卡片下的小字说明（不再用红字）。
            LazyColumn(
                state = dragOrder.list,
                modifier = Modifier.settingsGutterScroll(dragOrder.list)
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                item {
                    SettingsGroupTitle("已启用的输入组合", Modifier.padding(top = 8.dp))
                }

                if (orderedEnabledIds.isEmpty()) {
                    item {
                        Text(
                            text = "暂未启用任何方案",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                itemsIndexed(orderedEnabledIds, key = { _, id -> id }) { row, id ->
                    val isEnglish = id == InputModes.ENGLISH
                    val schema = if (isEnglish) SchemaMeta(id, "英文") else schemaById.getValue(id)
                    SchemaToggleItem(
                        schema = schema,
                        enabled = true,
                        isCompiled = isEnglish || SchemaManager.isSchemaCompiled(context, id),
                        isCurrent = !isEnglish && id == uiState.currentSchema,
                        onToggle = { if (!isEnglish) viewModel.toggleSchema(schema) },
                        onSelect = { if (!isEnglish) viewModel.selectSchema(schema) },
                        isBuiltIn = isEnglish,
                        isDragging = dragOrder.dragging == id,
                        isDropTarget = false,
                        reorderModifier = dragOrderItem(dragOrder, id, orderedEnabledIds.size > 1),
                        groupIndex = row,
                        groupSize = orderedEnabledIds.size,
                    )
                }

                item {
                    SettingsNote("长按可排序；语言和默认组合在「语言与方案」选择。",
                        Modifier.padding(top = 4.dp))
                    SettingsNote("安装或启停其他方案后，点「应用方案更改」生效。")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 关着的方案：常用的（14 键、双拼）一直列出；其余收在「其他已安装方案（高级）」里，折叠行写明有几个、带展开箭头。
                // 以前按收起后的列表判断，收起时写「没有其他可用方案」，点开却是一长串（用户 10-31）。
                val disabledAll = visibleSchemas.filter { it.schemaId !in uiState.enabledSchemas }
                val listedOff = disabledAll.filter { it.schemaId in com.kingzcheung.xime.settings.CyimeInputDefaults.listedWhenOff }
                val extraOff = disabledAll.filterNot { it.schemaId in com.kingzcheung.xime.settings.CyimeInputDefaults.listedWhenOff }

                itemsIndexed(listedOff, key = { _, schema -> schema.schemaId }) { row, schema ->
                    SchemaToggleItem(
                        schema = schema,
                        enabled = false,
                        isCompiled = SchemaManager.isSchemaCompiled(context, schema.schemaId),
                        isCurrent = false,
                        onToggle = { viewModel.toggleSchema(schema) },
                        onSelect = { viewModel.selectSchema(schema) },
                        groupIndex = row,
                        groupSize = listedOff.size,
                    )
                }

                if (extraOff.isNotEmpty()) item(key = "extra-schemas-fold") {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp))
                            .clickable { showExtraSchemas = !showExtraSchemas }
                            .testTag("extra-schemas-fold")
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(if (showExtraSchemas) "收起其他已安装方案" else "其他已安装方案（高级）",
                            modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary)
                        Text("${extraOff.size} 个", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Icon(if (showExtraSchemas) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showExtraSchemas) "收起" else "展开",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }

                if (disabledAll.isEmpty()) {
                    item {
                        Text(
                            text = "没有其他可用方案",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                if (showExtraSchemas) itemsIndexed(extraOff, key = { _, schema -> schema.schemaId }) { row, schema ->
                    SchemaToggleItem(
                        schema = schema,
                        enabled = false,
                        isCompiled = SchemaManager.isSchemaCompiled(context, schema.schemaId),
                        isCurrent = false,
                        onToggle = { viewModel.toggleSchema(schema) },
                        onSelect = { viewModel.selectSchema(schema) },
                        groupIndex = row,
                        groupSize = extraOff.size,
                    )
                }
                // 直接看、改 Rime 文件只用来排错，放在高级方案后面。
                item {
                    TextButton(onClick = onNavigateToRimeFileBrowser) {
                        Text("Rime 配置文件（高级）")
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            Button(
                onClick = { viewModel.deploySchema() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                if (uiState.isDeploying) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                }
                Text("应用方案更改")
            }
        }
    }
}

internal fun orderedInstalledModeIds(context: Context, schemas: List<SchemaMeta>): List<String> =
    InputModes.ordered(context, schemas.map { schema ->
        schema.toSchemaInfo()
    }).map { it.schemaId }

/** Keep inactive modes in their saved slots while reordering the visible cards. */
internal fun reorderEnabledModes(context: Context, schemas: List<SchemaMeta>, enabledIds: Set<String>,
    englishEnabled: Boolean, reordered: List<String>): Boolean {
    val installedIds = schemas.map { it.schemaId }.toSet()
    val visibleIds = schemas.filter {
        com.kingzcheung.xime.settings.CyimeInputDefaults.visibleSchema(it.schemaId, installedIds)
    }.mapTo(mutableSetOf()) { it.schemaId }
    val fullOrder = orderedInstalledModeIds(context, schemas)
    val enabledOrder = fullOrder.filter { id ->
        (id == InputModes.ENGLISH && englishEnabled) || (id in enabledIds && id in visibleIds)
    }
    return InputModes.saveReorderedModes(context, fullOrder, enabledOrder, reordered)
}

@Composable
internal fun SchemaToggleItem(
    schema: SchemaMeta,
    enabled: Boolean,
    isCompiled: Boolean,
    isCurrent: Boolean,
    onToggle: () -> Unit,
    onSelect: () -> Unit,
    isBuiltIn: Boolean = false,
    reorderModifier: Modifier = Modifier,
    isDragging: Boolean = false,
    isDropTarget: Boolean = false,
    /** 在同一张卡片里的第几行、共几行（见 SettingsGroupRow）；单独一行时四角都圆。正拖着的那行单独浮起、四角圆。 */
    groupIndex: Int = 0,
    groupSize: Int = 1,
) {
    val radius = 12.dp
    val top = if (isDragging || groupIndex == 0) radius else 0.dp
    val bottom = if (isDragging || groupIndex >= groupSize - 1) radius else 0.dp
    val groupColor = MaterialTheme.colorScheme.surfaceContainerLow
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schema-card:${schema.schemaId}")
            .then(reorderModifier)
            .then(if (isBuiltIn) Modifier else Modifier.clickable { if (enabled && isCompiled) onSelect() }),
        shape = RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging)
                MaterialTheme.colorScheme.secondaryContainer
            else if (isDropTarget)
                MaterialTheme.colorScheme.primaryContainer
            else if (isCurrent)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f).compositeOver(groupColor)
            else
                groupColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 6.dp else 0.dp),
    ) {
        if (groupIndex > 0 && !isDragging) SettingsRowDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = schema.toSchemaInfo().resourceLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.outline
                    )
                    if (isCurrent && isCompiled) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "当前方案",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                val profile = schema.toSchemaInfo().profile
                Text("${profile.language.displayName} · ${profile.summary}",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                com.kingzcheung.xime.settings.CyimeInputDefaults.description(schema.schemaId)?.let { description ->
                    Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isBuiltIn) {
                    Text("内置英文模式 · 始终开启", style = MaterialTheme.typography.labelSmall,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.outline)
                } else Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (schema.version.isNotEmpty()) {
                        Text(
                            text = "${schema.version}",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    if (schema.author.isNotEmpty()) {
                        Text(
                            text = schema.author,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    if (isCompiled) {
                        Text(
                            text = "已编译",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Switch(
                checked = enabled,
                enabled = !isBuiltIn,
                onCheckedChange = { if (!isBuiltIn) onToggle() },
                colors = settingsSwitchColors(),
                modifier = Modifier.testTag("schema-toggle:${schema.schemaId}")
            )
        }
    }
}

