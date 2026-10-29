package com.airvana.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import com.airvana.bluetooth.BluetoothEarphoneScanner
import com.airvana.intercom.IntercomSession
import com.airvana.location.PlaceReporter
import com.airvana.playback.PlaybackController
import com.airvana.theme.AirvanaPalette
import com.airvana.vana.Vana

private enum class Destination(val label: String) {
    Channel("Channel"),
    Map("Map"),
    Earphones("Earphones"),
    Settings("Settings"),
}

@Composable
fun AirvanaApp(
    session: IntercomSession,
    playback: PlaybackController,
    bluetoothScanner: BluetoothEarphoneScanner,
    vana: Vana,
    places: PlaceReporter,
) {
    var destination by remember { mutableStateOf(Destination.Channel) }
    val snackbarHostState = remember { SnackbarHostState() }
    val accessRequired = playback.nowPlaying.value.accessRequired

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = AirvanaPalette.Surface) {
                Destination.entries.forEach { item ->
                    val selected = destination == item
                    val tint = if (selected) AirvanaPalette.Black else AirvanaPalette.Muted
                    NavigationBarItem(
                        selected = selected,
                        onClick = { destination = item },
                        icon = {
                            when (item) {
                                Destination.Channel -> ChannelIcon(tint)
                                Destination.Map -> MapIcon(tint)
                                Destination.Earphones -> EarphoneIcon(tint)
                                Destination.Settings -> SettingsIcon(tint)
                            }
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AirvanaPalette.Black,
                            selectedTextColor = AirvanaPalette.White,
                            unselectedIconColor = AirvanaPalette.Muted,
                            unselectedTextColor = AirvanaPalette.Muted,
                            indicatorColor = AirvanaPalette.White,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (destination) {
                Destination.Channel -> {
                    val audioOutput = bluetoothScanner.scanResult.value.activeOutput
                    ChannelScreen(
                        session = session,
                        vana = vana,
                        activeAudioOutput = audioOutput,
                    )
                }
                Destination.Map -> MapScreen(
                    session = session,
                    reporter = places,
                )
                Destination.Earphones -> EarphonesScreen(
                    playback = playback,
                    bluetoothScanner = bluetoothScanner,
                )
                Destination.Settings -> SettingsScreen()
            }
        }
    }

    if (accessRequired) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Button(
                    onClick = playback::openAccessSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AirvanaPalette.White,
                        contentColor = AirvanaPalette.Black,
                    ),
                ) {
                    Text("Allow access", fontWeight = FontWeight.Medium)
                }
            },
            title = { Text("Notification access required") },
            text = {
                Text(
                    "Airvana needs notification access to see what is playing and show Pause, Play, and skip. This step is required. Airvana does not read your messages.",
                )
            },
            containerColor = AirvanaPalette.Surface,
            titleContentColor = AirvanaPalette.White,
            textContentColor = AirvanaPalette.Muted,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        )
    }
}
