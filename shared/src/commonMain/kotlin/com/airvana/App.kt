package com.airvana

import androidx.compose.runtime.Composable
import com.airvana.bluetooth.BluetoothEarphoneScanner
import com.airvana.bluetooth.UnavailableBluetoothEarphoneScanner
import com.airvana.intercom.IntercomSession
import com.airvana.intercom.UnavailableIntercomSession
import com.airvana.location.PlaceReporter
import com.airvana.location.UnavailablePlaceReporter
import com.airvana.playback.PlaybackController
import com.airvana.playback.UnavailablePlaybackController
import com.airvana.theme.AirvanaTheme
import com.airvana.ui.AirvanaApp
import com.airvana.vana.UnavailableVana
import com.airvana.vana.Vana

@Composable
fun App(
    session: IntercomSession = UnavailableIntercomSession(),
    playback: PlaybackController = UnavailablePlaybackController(),
    bluetoothScanner: BluetoothEarphoneScanner = UnavailableBluetoothEarphoneScanner(),
    vana: Vana = UnavailableVana(),
    places: PlaceReporter = UnavailablePlaceReporter(),
) {
    AirvanaTheme {
        AirvanaApp(
            session = session,
            playback = playback,
            bluetoothScanner = bluetoothScanner,
            vana = vana,
            places = places,
        )
    }
}
