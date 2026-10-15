package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.viewmodel.AiToolType
import com.example.util.FileExportHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiToolsDialog(
    isLoading: Boolean,
    resultText: String?,
    onSelectTool: (AiToolType) -> Unit,
    onTranslateCustom: (String) -> Unit,
    onReplaceInEditor: () -> Unit,
    onAppendToEditor: () -> Unit,
    onSaveAsNew: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf("انگلیسی") }

    val languages = listOf(
        "انگلیسی" to "English",
        "عربی" to "Arabic",
        "ترکی استانبولی" to "Turkish",
        "فرانسوی" to "French",
        "آلمانی" to "German",
        "اسپانیایی" to "Spanish",
        "روسی" to "Russian",
        "چینی" to "Chinese",
        "ایتالیایی" to "Italian"
    )

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "ابزارهای هوش مصنوعی و ترجمه آنلاین",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "بستن")
                        }
                    }

                    if (isLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "در حال پردازش و ترجمه آنلاین با مدل Gemini...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (resultText != null) {
                        // Display Result
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "نتیجه پردازش / ترجمه هوش مصنوعی:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = resultText,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Actions on the result
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            onReplaceInEditor()
                                            onDismiss()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.FindReplace, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("جایگزینی در ویرایشگر", style = MaterialTheme.typography.labelSmall)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            onAppendToEditor()
                                            onDismiss()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("افزودن به انتهای متن", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            onSaveAsNew()
                                            onDismiss()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ذخیره به عنوان سند جدید", style = MaterialTheme.typography.labelSmall)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            FileExportHelper.copyToClipboard(context, resultText, "نتیجه هوش مصنوعی")
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "کپی", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    } else {
                        // Section 1: Online AI Translation
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = "ترجمه آنلاین هوشمند (Online Translation)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "زبان مقصد را برای ترجمه فوری آنلاین متن انتخاب کنید:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    languages.forEach { (faName, enName) ->
                                        FilterChip(
                                            selected = selectedLanguage == faName,
                                            onClick = { selectedLanguage = faName },
                                            label = { Text(faName, style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        val targetEn = languages.firstOrNull { it.first == selectedLanguage }?.second ?: "English"
                                        onTranslateCustom(targetEn)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("شروع ترجمه آنلاین به $selectedLanguage")
                                }
                            }
                        }

                        // Section 2: Other AI Tools
                        Text(
                            text = "سایر ابزارهای هوش مصنوعی:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            AiToolCard(
                                title = "خلاصه‌سازی متن (Summarize)",
                                description = "تولید خلاصه روان و فشرده از کل متن استخراج شده",
                                icon = Icons.Default.ShortText,
                                iconColor = MaterialTheme.colorScheme.primary,
                                onClick = { onSelectTool(AiToolType.SUMMARIZE) }
                            )

                            AiToolCard(
                                title = "اصلاح نگارش و علائم (Punctuate)",
                                description = "تصحیح علائم نگارشی فارسی، ویرگول، نقطه و فاصله‌ها",
                                icon = Icons.Default.EditNote,
                                iconColor = MaterialTheme.colorScheme.secondary,
                                onClick = { onSelectTool(AiToolType.PUNCTUATE) }
                            )

                            AiToolCard(
                                title = "استخراج نکات کلیدی (Key Points)",
                                description = "تفکیک سرفصل‌ها و تصمیمات اصلی به صورت بولت‌پوینت",
                                icon = Icons.Default.FormatListBulleted,
                                iconColor = AccentAmber,
                                onClick = { onSelectTool(AiToolType.KEY_POINTS) }
                            )

                            AiToolCard(
                                title = "تولید زیرنویس SRT (Subtitles)",
                                description = "تبدیل متن به قطعات زمان‌بندی شده برای استفاده در پلیر فیلم",
                                icon = Icons.Default.Subtitles,
                                iconColor = AccentEmerald,
                                onClick = { onSelectTool(AiToolType.GENERATE_SUBTITLE) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
