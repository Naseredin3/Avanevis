package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TranscriptItem
import com.example.ui.components.AiToolsDialog
import com.example.ui.components.ExportDialog
import com.example.ui.theme.AccentAmber
import com.example.ui.viewmodel.AiToolType
import com.example.ui.viewmodel.TranscribeViewModel
import com.example.util.FileExportHelper

@Composable
fun EditorScreen(
    viewModel: TranscribeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTranscript by viewModel.currentTranscript.collectAsStateWithLifecycle()
    val aiToolLoading by viewModel.aiToolLoading.collectAsStateWithLifecycle()
    val aiToolResult by viewModel.aiToolResult.collectAsStateWithLifecycle()

    var showExportDialog by remember { mutableStateOf(false) }
    var showAiToolsDialog by remember { mutableStateOf(false) }
    var selectedSubTab by remember { mutableIntStateOf(0) }

    if (currentTranscript == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "هیچ متنی برای ویرایش باز نشده است",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "برای شروع، از زبانه 'استخراج جدید' ویدیوی خود را انتخاب کنید یا یکی از موارد آرشیو را باز نمایید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { viewModel.setActiveTab(0) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("رفتن به صفحه استخراج")
                }
            }
        }
        return
    }

    val item = currentTranscript!!

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title Bar with Editable Title & Favorite Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = item.title,
                    onValueChange = { viewModel.updateCurrentTranscriptTitle(it) },
                    label = { Text("عنوان فایل") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transcript_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )
            }

            IconButton(
                onClick = { viewModel.toggleFavorite(item) },
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "نشانه‌گذاری",
                    tint = if (item.isFavorite) AccentAmber else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Action Buttons Row (Save, Copy, Export, AI Tools)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.saveCurrentTranscript() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ذخیره", style = MaterialTheme.typography.labelSmall)
            }

            FilledTonalButton(
                onClick = { showExportDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("خروجی", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedButton(
                onClick = { showAiToolsDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("هوش‌مصنوعی", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedButton(
                onClick = {
                    FileExportHelper.copyToClipboard(context, item.rawText, item.title)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "کپی متن", modifier = Modifier.size(16.dp))
            }
        }

        // Sub-tabs (متن اصلی, زمان‌بندی SRT, خلاصه و نکات)
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("متن قابل ویرایش", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("زیرنویس و زمان (SRT)", style = MaterialTheme.typography.labelSmall) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("خلاصه و نکات کلیدی", style = MaterialTheme.typography.labelSmall) }
            )
        }

        // Sub-tab Content
        when (selectedSubTab) {
            0 -> {
                // Editable Text Area
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "تعداد کلمات: ${item.wordCount} | کاراکتر: ${item.rawText.length}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "منبع: ${item.sourceFileName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = item.rawText,
                        onValueChange = { viewModel.updateCurrentTranscriptText(it) },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("transcript_editor_textarea"),
                        shape = RoundedCornerShape(14.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 28.sp,
                            fontSize = 15.sp
                        ),
                        placeholder = { Text("متن استخراج شده از ویدیو در اینجا قرار می‌گیرد...") }
                    )
                }
            }

            1 -> {
                // Subtitles / SRT View
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "فرمت زیرنویس زمان‌بندی شده (SRT / VTT):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = item.timestampsText ?: FileExportHelper.generateExportContent(item, com.example.util.ExportFormat.SRT),
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 20.sp,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            2 -> {
                // Summary & Key Points View
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("خلاصه هوشمند ویدیو", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = item.summary ?: "هنوز خلاصه‌ای تولید نشده است. از منوی هوش مصنوعی برای تولید خلاصه استفاده کنید.",
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.FormatListBulleted, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Text("نکات کلیدی و مباحث مهم", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = item.keyPoints ?: "نکات کلیدی هنوز استخراج نشده است.",
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            transcriptItem = item,
            onDismiss = { showExportDialog = false }
        )
    }

    // AI Tools Dialog
    if (showAiToolsDialog) {
        AiToolsDialog(
            isLoading = aiToolLoading,
            resultText = aiToolResult,
            onSelectTool = { tool ->
                viewModel.applyAiTool(tool)
            },
            onTranslateCustom = { langName ->
                viewModel.translateCustomOnline(langName)
            },
            onReplaceInEditor = {
                viewModel.replaceCurrentTranscriptWithAiResult()
            },
            onAppendToEditor = {
                viewModel.appendAiResultToCurrentTranscript()
            },
            onSaveAsNew = {
                viewModel.saveAiResultAsNewTranscript()
            },
            onDismiss = {
                viewModel.dismissAiToolResult()
                showAiToolsDialog = false
            }
        )
    }
}
