package com.my965fm.radio

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture

class MainActivity : ComponentActivity() {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var playing by mutableStateOf(false)
    private var status by mutableStateOf("Connecting to live radio…")
    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) { playing = isPlaying }
        override fun onPlaybackStateChanged(playbackState: Int) {
            status = when (playbackState) {
                Player.STATE_BUFFERING -> "Buffering…"
                Player.STATE_READY -> "LIVE • MY96.5 FM"
                Player.STATE_ENDED -> "Stream ended"
                else -> "Connecting to live radio…"
            }
        }
        override fun onPlayerError(error: androidx.media3.common.PlaybackException) { status = "Stream unavailable — check the URL" }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFFFD447), background = Color(0xFF080F20))) {
                Column(Modifier.fillMaxSize().background(Color(0xFF080F20)).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("● LIVE RADIO", color = Color(0xFFFFD447), fontSize = 14.sp)
                    Spacer(Modifier.height(36.dp))
                    Text("MY96.5", color = Color.White, fontWeight = FontWeight.Black, fontSize = 48.sp)
                    Text("FM", color = Color(0xFFFFD447), fontWeight = FontWeight.Bold, fontSize = 35.sp)
                    Spacer(Modifier.height(14.dp))
                    Text("Your Music, Your Station", color = Color(0xFFB4C9EA), fontSize = 18.sp)
                    Spacer(Modifier.height(58.dp))
                    Text(status, color = Color.White, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { controller?.let { if (it.isPlaying) it.pause() else it.play() } }, shape = RoundedCornerShape(30.dp), modifier = Modifier.fillMaxWidth().height(60.dp)) {
                        Text(if (playing) "❚❚  PAUSE" else "▶  PLAY LIVE", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(18.dp))
                    OutlinedButton(onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "Listen to MY96.5 FM — Your Music, Your Station!") }
                        startActivity(Intent.createChooser(send, "Share MY96.5 FM"))
                    }, modifier = Modifier.fillMaxWidth()) { Text("SHARE STATION") }
                    Spacer(Modifier.height(32.dp))
                    Text("NOW PLAYING", color = Color(0xFFFFD447), fontSize = 12.sp)
                    Text("MY96.5 FM • Live Stream", color = Color.White)
                }
            }
        }
        val token = SessionToken(this, ComponentName(this, RadioService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        controllerFuture = future
        future.addListener({
            try {
                controller = future.get()
                controller?.addListener(listener)
                controller?.play() // Autoplay on app launch
            } catch (e: Exception) { status = "Unable to connect to player" }
        }, androidx.core.content.ContextCompat.getMainExecutor(this))
    }
    override fun onDestroy() {
        controller?.removeListener(listener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onDestroy()
    }
}
