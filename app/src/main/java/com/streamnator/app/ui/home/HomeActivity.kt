package com.streamnator.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.streamnator.app.R
import com.streamnator.app.StreamNatorApp
import com.streamnator.app.data.db.PlaylistEntity
import com.streamnator.app.data.repository.PlaylistRepository
import com.streamnator.app.ui.channels.ChannelBrowserActivity
import com.streamnator.app.ui.settings.SettingsActivity
import com.streamnator.app.util.FocusHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var playlistRepo: PlaylistRepository
    private lateinit var recyclerView: RecyclerView
    private val adapter = PlaylistAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val db = (application as StreamNatorApp).database
        playlistRepo = PlaylistRepository(db)

        recyclerView = findViewById(R.id.playlist_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapter.onItemClick = { playlist ->
            (application as StreamNatorApp).prefs.setActivePlaylistId(playlist.id)
            startActivity(Intent(this, ChannelBrowserActivity::class.java))
        }

        adapter.onItemLongClick = { playlist ->
            showPlaylistMenu(playlist)
        }

        findViewById<View>(R.id.add_playlist_btn).setOnClickListener {
            showAddPlaylistDialog()
        }

        findViewById<View>(R.id.settings_btn).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        playlistRepo.getAllPlaylists().observe(this) { playlists ->
            adapter.submitList(playlists)
            if (playlists.isNotEmpty()) {
                recyclerView.requestFocus()
            }
        }

        lifecycleScope.launch {
            val prefs = (application as StreamNatorApp).prefs
            if (prefs.getAutoRefresh()) {
                playlistRepo.getAllPlaylists().observe(this@HomeActivity) { list ->
                    lifecycleScope.launch {
                        for (item in list) {
                            if (item.autoRefresh) {
                                try { playlistRepo.refreshPlaylist(item) } catch (_: Exception) {}
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showAddPlaylistDialog() {
        AddPlaylistDialog().show(supportFragmentManager, "add_playlist")
    }

    private fun showPlaylistMenu(playlist: PlaylistEntity) {
        PlaylistMenuDialog(playlist).show(supportFragmentManager, "playlist_menu")
    }
}

class PlaylistAdapter : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    var onItemClick: ((PlaylistEntity) -> Unit)? = null
    var onItemLongClick: ((PlaylistEntity) -> Unit)? = null
    private var items = listOf<PlaylistEntity>()

    fun submitList(list: List<PlaylistEntity>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_card, parent, false)
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.nameTv.text = item.name
        holder.typeTv.text = item.type.uppercase()
        holder.dateTv.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(item.lastUpdated))
        holder.itemView.setOnClickListener { onItemClick?.invoke(item) }
        holder.itemView.setOnLongClickListener { onItemLongClick?.invoke(item); true }
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTv: TextView = view.findViewById(R.id.playlist_name)
        val typeTv: TextView = view.findViewById(R.id.playlist_type)
        val dateTv: TextView = view.findViewById(R.id.playlist_date)
    }
}