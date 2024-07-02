package com.shindekalpesharun.appshortcut

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.shindekalpesharun.appshortcut.ui.theme.AppShortcutTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            AppShortcutTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            16.dp, Alignment.CenterVertically
                        )
                    ) {
                        when (viewModel.shortcutType) {
                            ShortcutType.STATIC -> Text("Static Shortcut Clicked")
                            ShortcutType.DYNAMIC -> Text("Dynamic Shortcut Clicked")
                            ShortcutType.PINNED -> Text("Pinned Shortcut Clicked")
                            null -> Unit
                        }
                        Button(onClick = { addDynamicShortcut() }) {
                            Text("Add Dynamic")
                        }
                        Button(onClick = { addPinnedShortcut() }) {
                            Text("Add Pinned")
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun addPinnedShortcut() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val shortcutManager = getSystemService<ShortcutManager>()!!

        if (shortcutManager.isRequestPinShortcutSupported) {
            val shortcut =
                ShortcutInfo.Builder(applicationContext, "pinned").setShortLabel("Send Message")
                    .setLongLabel("This sends message to a friend").setIcon(
                        Icon.createWithResource(
                            applicationContext, R.drawable.ic_launcher_foreground
                        )
                    ).setIntent(Intent(applicationContext, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        putExtra("shortcut_id", "pinned")
                    }).build()

            val callbackIntent = shortcutManager.createShortcutResultIntent(shortcut)
            val successPendingIntent = PendingIntent.getBroadcast(
                applicationContext, 0, callbackIntent, PendingIntent.FLAG_MUTABLE
            )

            shortcutManager.requestPinShortcut(shortcut, successPendingIntent.intentSender)
        }
    }

    private fun addDynamicShortcut() {
        val shortcut =
            ShortcutInfoCompat.Builder(applicationContext, "dynamic").setLongLabel("Call Home")
                .setShortLabel("Clicking this will call your Home").setIcon(
                    IconCompat.createWithResource(
                        applicationContext, R.drawable.ic_launcher_foreground
                    ),
                ).setIntent(Intent(applicationContext, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    putExtra("shortcut_id", "dynamic")
                }).build()
        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, shortcut)

    }

    private fun handleIntent(intent: Intent) {
        intent.let {
            when (intent.getStringExtra("shortcut_id")) {
                "static" -> viewModel.onShortcutClicked(ShortcutType.STATIC)
                "dynamic" -> viewModel.onShortcutClicked(ShortcutType.DYNAMIC)
                "pinned" -> viewModel.onShortcutClicked(ShortcutType.PINNED)
            }
        }

    }
}
