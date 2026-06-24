package com.streamnator.app.ui.player

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.streamnator.app.R
import com.streamnator.app.StreamNatorApp
import com.streamnator.app.data.repository.ChannelRepository
import kotlinx.coroutines.launch

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private var controlsView: View? = null
    private var hideControlsRunnable: Runnable? = null
    private var channelId: Long = -1
    private var playlistId: Long = -1
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val channelUrl = intent.getStringExtra("channel_url") ?: ""
        val channelName = intent.getStringExtra("channel_name") ?: ""
        channelId = intent.getLongExtra("channel_id", -1)
        playlistId = intent.getLongExtra("playlist_id", -1)

        val playerView = findViewById<PlayerView>(R.id.player_view)
        controlsView = findViewById(R.id.player_controls)
        val playPauseBtn = findViewById<Button>(R.id.btn_play_pause)
        val stopBtn = findViewById<Button>(R.id.btn_stop)
        val channelTitleTv = findViewById<TextView>(R.id.player_channel_title)
        val favBtn = findViewById<Button>(R.id.btn_fav)

        channelTitleTv.text = channelName

        // Check if already favorite
        lifecycleScope.launch {
            if (channelId > 0 && playlistId > 0) {
                val db = (application as StreamNatorApp).database
                val repo = ChannelRepository(db.channelDao(), db.favoriteDao(), db.recentlyWatchedDao())
                isFavorite = repo.isFavorite(channelId, playlistId)
                favBtn.text = if (isFavorite) "\u2605" else "\u2606"
                favBtn.setTextColor(if (isFavorite) 0xFFE50914.toInt() else 0xFFFFFFFF.toInt())
            }
        }

        favBtn.setOnClickListener {
            lifecycleScope.launch {
                val db = (application as StreamNatorApp).database
                val repo = ChannelRepository(db.channelDao(), db.favoriteDao(), db.recentlyWatchedDao())
                repo.toggleFavorite(channelId, playlistId)
                isFavorite = !isFavorite
                favBtn.text = if (isFavorite) "\u2605" else "\u2606"
                favBtn.setTextColor(if (isFavorite) 0xFFE50914.toInt() else 0xFFFFFFFF.toInt())
            }
        }

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        if (channelUrl.isNotEmpty()) {
            player?.setMediaItem(MediaItem.fromUri(channelUrl))
            player?.prepare()
            player?.playWhenReady = true
        }

        playPauseBtn.setOnClickListener {
            if (player?.isPlaying == true) {
                player?.pause()
            } else {
                player?.play()
            }
        }

        stopBtn.setOnClickListener { player?.stop(); finish() }
        playerView.setOnClickListener { showControls() }
        showControls()
    }

    private fun showControls() {
        controlsView?.visibility = View.VISIBLE
        hideControlsRunnable?.let { controlsView?.removeCallbacks(it) }
        hideControlsRunnable = Runnable { controlsView?.visibility = View.GONE }
        controlsView?.postDelayed(hideControlsRunnable!!, 5000)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> { showControls(); true }
        KeyEvent.KEYCODE_BACK -> { player?.stop(); finish(); true }
        else -> super.onKeyDown(keyCode, event)
    }

    override fun onStop() { super.onStop(); player?.pause() }

    override fun onDestroy() { super.onDestroy(); player?.release(); player = null }
}