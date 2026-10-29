package com.airvana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.toColorInt
import com.airvana.bluetooth.AndroidBluetoothEarphoneScanner
import com.airvana.intercom.AndroidIntercomSession
import com.airvana.location.AndroidPlaceReporter
import com.airvana.playback.AndroidPlaybackController
import com.airvana.vana.AndroidVana

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothScanner: AndroidBluetoothEarphoneScanner
    private lateinit var playbackController: AndroidPlaybackController
    private lateinit var intercomSession: AndroidIntercomSession
    private lateinit var vana: AndroidVana
    private lateinit var places: AndroidPlaceReporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playbackController = AndroidPlaybackController(this)
        intercomSession = AndroidIntercomSession(
            activity = this,
            playback = playbackController,
        )
        vana = AndroidVana(
            activity = this,
            session = intercomSession,
            playback = playbackController,
        )
        places = AndroidPlaceReporter(
            activity = this,
            session = intercomSession,
        )
        bluetoothScanner = AndroidBluetoothEarphoneScanner(this)
        val background = "#070708".toColorInt()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(background),
            navigationBarStyle = SystemBarStyle.dark(background),
        )
        setContent {
            App(
                session = intercomSession,
                playback = playbackController,
                bluetoothScanner = bluetoothScanner,
                vana = vana,
                places = places,
            )
        }
        bluetoothScanner.refreshAudioOutput()
        playbackController.refresh()
    }

    override fun onResume() {
        super.onResume()
        if (::playbackController.isInitialized) playbackController.refresh()
        if (::places.isInitialized) places.refresh()
    }

    override fun onDestroy() {
        places.dispose()
        vana.dispose()
        intercomSession.dispose()
        bluetoothScanner.dispose()
        playbackController.dispose()
        super.onDestroy()
    }
}
