package com.my965fm.radio

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class RadioService : MediaSessionService() {
    private var session: MediaSession? = null
    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        player.setMediaItem(MediaItem.Builder()
            .setUri(STREAM_URL)
            .setMediaMetadata(MediaMetadata.Builder().setTitle("MY96.5 FM").setArtist("Your Music, Your Station").build())
            .build())
        player.prepare()
        session = MediaSession.Builder(this, player)
            .setSessionActivity(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            .build()
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session
    override fun onDestroy() { session?.player?.release(); session?.release(); session = null; super.onDestroy() }
    companion object {
        // Replace with the verified permanent direct audio stream URL from your hosting dashboard.
        const val STREAM_URL = "http://stream-175.surfernetwork.com/4udquzh4t8quv"
    }
}
