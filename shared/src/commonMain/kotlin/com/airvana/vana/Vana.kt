package com.airvana.vana

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.airvana.intercom.ChannelPath

enum class VanaPhase {
    Off,
    Ready,
    Connecting,
    Listening,
    Speaking,
    Hearing,
}

data class VanaState(
    val enabled: Boolean = true,
    val phase: VanaPhase = VanaPhase.Ready,
    val line: String = "VANA will connect the other phone, and pause the music when someone talks.",
)

/**
 * VANA decides when music pauses and when this phone opens a voice link.
 * The channel screen still picks Bluetooth, hotspot, or internet.
 */
interface Vana {
    val state: State<VanaState>

    fun setEnabled(enabled: Boolean)

    fun noteRoom(roomCode: String, path: ChannelPath, password: String)

    fun connect(roomCode: String, path: ChannelPath, password: String)

    fun leave()

    fun toggleTalk()

    fun dispose()
}

class UnavailableVana : Vana {
    override val state: State<VanaState> = mutableStateOf(
        VanaState(
            enabled = false,
            phase = VanaPhase.Off,
            line = "VANA is available on Android.",
        ),
    )

    override fun setEnabled(enabled: Boolean) = Unit

    override fun noteRoom(roomCode: String, path: ChannelPath, password: String) = Unit

    override fun connect(roomCode: String, path: ChannelPath, password: String) = Unit

    override fun leave() = Unit

    override fun toggleTalk() = Unit

    override fun dispose() = Unit
}
