package com.airvana.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airvana.generated.resources.Res
import com.airvana.generated.resources.airvana_logo
import com.airvana.intercom.ChannelPath
import com.airvana.intercom.Earphone
import com.airvana.intercom.IntercomConnection
import com.airvana.intercom.IntercomSession
import com.airvana.vana.Vana
import com.airvana.theme.AirvanaPalette
import org.jetbrains.compose.resources.painterResource

@Composable
fun ChannelScreen(
    session: IntercomSession,
    vana: Vana,
    activeAudioOutput: Earphone? = null,
) {
    val intercom by session.state
    val vanaState by vana.state
    var roomCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedPath by remember { mutableStateOf(ChannelPath.Hotspot) }
    val inRoom = intercom.connection == IntercomConnection.Connecting ||
        intercom.connection == IntercomConnection.Connected
    val shownPath = if (inRoom) intercom.path else selectedPath
    val voiceActive = intercom.localTalking || intercom.remoteTalking

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel("Channel")
            StatusPill(
                when {
                    voiceActive && vanaState.enabled -> "VANA"
                    voiceActive -> "Voice"
                    intercom.connection == IntercomConnection.Connected -> intercom.path.label
                    intercom.connection == IntercomConnection.Connecting -> "Joining"
                    intercom.connection == IntercomConnection.Unavailable -> "Android"
                    else -> "Off"
                },
            )
        }

        Spacer(Modifier.height(20.dp))

        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.10f), Color.Transparent),
                        ),
                    ),
            )
            Image(
                painter = painterResource(Res.drawable.airvana_logo),
                contentDescription = "Airvana",
                modifier = Modifier.size(104.dp),
                contentScale = ContentScale.Fit,
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(text = "Airvana", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "VANA pauses the music when someone talks, and connects your voice to the other phone.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(22.dp))
        AirvanaCard {
            VanaRow(
                enabled = vanaState.enabled,
                onToggle = { vana.setEnabled(!vanaState.enabled) },
            )
            Spacer(Modifier.height(14.dp))
            PathPicker(
                selected = shownPath,
                enabled = !inRoom,
                onSelect = {
                    selectedPath = it
                    vana.noteRoom(roomCode, it, password)
                },
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = pathBlurb(shownPath, activeAudioOutput),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = when {
                    intercom.members.isEmpty() -> "No one on the channel"
                    else -> "${intercom.members.size} on the channel"
                },
                style = MaterialTheme.typography.titleMedium,
            )
            if (intercom.members.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = intercom.members.joinToString { member ->
                        if (member.talking) "${member.name} · talking" else member.name
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (!intercom.detail.isNullOrBlank() && intercom.members.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(text = intercom.detail.orEmpty(), style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(14.dp))
            ChannelField(
                value = if (inRoom) intercom.roomCode else roomCode,
                onValueChange = {
                    roomCode = it.take(16)
                    vana.noteRoom(roomCode, shownPath, password)
                },
                enabled = !inRoom,
                placeholder = "Room code",
                imeAction = ImeAction.Next,
                onDone = { vana.connect(roomCode, shownPath, password) },
            )
            Spacer(Modifier.height(10.dp))
            ChannelField(
                value = password,
                onValueChange = {
                    password = it.take(32)
                    vana.noteRoom(roomCode, shownPath, password)
                },
                enabled = !inRoom,
                placeholder = "Password",
                imeAction = ImeAction.Done,
                onDone = { vana.connect(roomCode, shownPath, password) },
                password = true,
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    if (inRoom) vana.leave() else vana.connect(roomCode, shownPath, password)
                },
                enabled = intercom.connection != IntercomConnection.Unavailable,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AirvanaPalette.Line),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AirvanaPalette.White,
                    disabledContentColor = AirvanaPalette.Dim,
                ),
            ) {
                Text(
                    text = when (intercom.connection) {
                        IntercomConnection.Connecting -> "Leave"
                        IntercomConnection.Connected -> "Leave channel"
                        else -> "Join channel"
                    },
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        TalkButton(
            talking = intercom.localTalking,
            onToggle = { vana.toggleTalk() },
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = if (vanaState.enabled) {
                vanaState.line
            } else {
                talkHint(intercom.localTalking, voiceActive, intercom.connection, intercom.detail)
            },
            color = AirvanaPalette.Muted,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun StatusPill(label: String) {
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(AirvanaPalette.SurfaceHigh)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        color = AirvanaPalette.Muted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun TalkButton(
    talking: Boolean,
    onToggle: () -> Unit,
) {
    val fill = if (talking) AirvanaPalette.White else Color.Transparent
    val mark = if (talking) AirvanaPalette.Black else AirvanaPalette.White
    Box(
        modifier = Modifier
            .size(148.dp)
            .semantics {
                role = Role.Button
                contentDescription = if (talking) "Stop talking" else "Start talking"
            }
            .clip(CircleShape)
            .background(fill)
            .border(width = 1.5.dp, color = AirvanaPalette.White, shape = CircleShape)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (talking) "Stop" else "Talk",
            color = mark,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.3).sp,
        )
    }
}

private fun talkHint(
    talking: Boolean,
    voiceActive: Boolean,
    connection: IntercomConnection,
    detail: String?,
): String {
    return when {
        talking && connection != IntercomConnection.Connected ->
            detail ?: "Join a channel first"
        talking || voiceActive -> "Talking — tap Stop when you are done"
        connection == IntercomConnection.Connected -> "Tap Talk to speak. Tap Stop to finish."
        else -> detail ?: "Pick Bluetooth, Hotspot, or Internet, then enter the same room code on every phone."
    }
}

@Composable
private fun VanaRow(
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "VANA", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Pauses the music and connects your voice.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        val mark = if (enabled) AirvanaPalette.Black else AirvanaPalette.White
        Text(
            text = if (enabled) "On" else "Off",
            color = mark,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .semantics {
                    role = Role.Button
                    contentDescription = if (enabled) "Turn VANA off" else "Turn VANA on"
                }
                .clip(RoundedCornerShape(100.dp))
                .background(if (enabled) AirvanaPalette.White else Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (enabled) AirvanaPalette.White else AirvanaPalette.Line,
                    shape = RoundedCornerShape(100.dp),
                )
                .clickable(onClick = onToggle)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun PathPicker(
    selected: ChannelPath,
    enabled: Boolean,
    onSelect: (ChannelPath) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ChannelPath.entries.forEach { path ->
            val on = path == selected
            val mark = when {
                on -> AirvanaPalette.Black
                enabled -> AirvanaPalette.White
                else -> AirvanaPalette.Dim
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = path.label
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (on) AirvanaPalette.White else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (on) AirvanaPalette.White else AirvanaPalette.Line,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clickable(enabled = enabled, onClick = { onSelect(path) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = path.label,
                    color = mark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ChannelField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    placeholder: String,
    imeAction: ImeAction,
    onDone: () -> Unit,
    password: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        placeholder = { Text(placeholder) },
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (password) KeyboardType.Password else KeyboardType.Text,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = placeholder },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AirvanaPalette.White,
            unfocusedTextColor = AirvanaPalette.White,
            disabledTextColor = AirvanaPalette.White,
            focusedBorderColor = AirvanaPalette.White,
            unfocusedBorderColor = AirvanaPalette.Line,
            disabledBorderColor = AirvanaPalette.Line,
            cursorColor = AirvanaPalette.White,
            focusedPlaceholderColor = AirvanaPalette.Dim,
            unfocusedPlaceholderColor = AirvanaPalette.Dim,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
        ),
    )
}

private fun pathBlurb(path: ChannelPath, activeAudioOutput: Earphone?): String {
    val output = activeAudioOutput?.let { " Plays on ${it.name}." }.orEmpty()
    val body = when (path) {
        ChannelPath.Bluetooth ->
            "Voice goes over Bluetooth. Stay close, with Bluetooth on, and use the same room code and password."
        ChannelPath.Hotspot ->
            "Voice goes over a Wi-Fi hotspot between phones. No internet needed. Use the same room code and password."
        ChannelPath.Internet ->
            "Voice goes over the internet. Each phone needs data or Wi-Fi, plus the same room code and password."
    }
    return body + output
}
