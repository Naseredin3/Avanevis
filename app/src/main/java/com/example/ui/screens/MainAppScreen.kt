package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ApiKeyDialog
import com.example.ui.viewmodel.TranscribeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: TranscribeViewModel
) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val keyTestStatus by viewModel.keyTestStatus.collectAsStateWithLifecycle()
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "آوانویس",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "استخراج متن از ویدیو",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showApiKeyDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "تنظیمات API",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showInfoDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "درباره برنامه",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        icon = { Icon(Icons.Default.Movie, contentDescription = "استخراج جدید") },
                        label = { Text("استخراج جدید", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("tab_extract"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        icon = { Icon(Icons.Default.Description, contentDescription = "ویرایشگر متن") },
                        label = { Text("ویرایشگر متن", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("tab_editor"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { viewModel.setActiveTab(2) },
                        icon = { Icon(Icons.Default.Archive, contentDescription = "آرشیو فایل‌ها") },
                        label = { Text("آرشیو فایل‌ها", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("tab_archive"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeTab) {
                    0 -> ExtractScreen(viewModel = viewModel)
                    1 -> EditorScreen(viewModel = viewModel)
                    2 -> ArchiveScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentCustomKey = customApiKey,
            testStatus = keyTestStatus,
            onSaveKey = { viewModel.setCustomApiKey(it) },
            onClearKey = { viewModel.clearCustomApiKey() },
            onTestKey = { viewModel.testCurrentApiKey(it) },
            onDismiss = {
                viewModel.dismissKeyTestStatus()
                showApiKeyDialog = false
            }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("درباره برنامه آوانویس", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "برنامه آوانویس جهت استخراج دقیق متن و زیرنویس از صدای انواع فیلم‌ها، ویدیوها و فایل‌های صوتی با تکیه بر مدل‌های هوش مصنوعی زبانی و پشتیبانی کامل از زبان فارسی طراحی شده است.\n\nقابلیت‌ها:\n• استخراج کلمه به کلمه گفتار فارسی\n• تولید زیرنویس استاندارد SRT و VTT\n• ویرایشگر کامل متن با پشتیبانی RTL\n• خروجی در قالب فایل‌های TXT, SRT, VTT, MD, JSON\n• خلاصه‌سازی، اصلاح علائم نگارشی و استخراج نکات کلیدی با AI",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("متوجه شدم")
                }
            }
        )
    }
}
