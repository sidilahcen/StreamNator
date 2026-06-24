package com.streamnator.app.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.streamnator.app.R
import com.streamnator.app.StreamNatorApp
import com.streamnator.app.data.db.ChannelEntity
import com.streamnator.app.data.repository.ChannelRepository
import com.streamnator.app.ui.player.PlayerActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private val adapter = SearchAdapter()
    private var playlistId: Long = -1
    private var channelRepo: ChannelRepository? = null
    private val favoriteIds = mutableSetOf<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val db = (application as StreamNatorApp).database
        channelRepo = ChannelRepository(db.channelDao(), db.favoriteDao(), db.recentlyWatchedDao())
        playlistId = (application as StreamNatorApp).prefs.getActivePlaylistId()

        searchInput = findViewById(R.id.search_input)
        val resultsList = findViewById<RecyclerView>(R.id.search_results)
        resultsList.layoutManager = LinearLayoutManager(this)
        resultsList.adapter = adapter

        adapter.onChannelClick = { channel ->
            lifecycleScope.launch {
                channelRepo?.addToRecentlyWatched(channel.id, playlistId)
                withContext(Dispatchers.Main) {
                    startActivity(Intent(this@SearchActivity, PlayerActivity::class.java).apply {
                        putExtra("channel_url", channel.url)
                        putExtra("channel_name", channel.name)
                        putExtra("channel_id", channel.id)
                        putExtra("playlist_id", playlistId)
                    })
                }
            }
        }

        adapter.onChannelLongClick = { channel ->
            lifecycleScope.launch {
                channelRepo?.toggleFavorite(channel.id, playlistId)
                withContext(Dispatchers.Main) {
                    if (favoriteIds.contains(channel.id)) {
                        favoriteIds.remove(channel.id)
                        Toast.makeText(this@SearchActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
                    } else {
                        favoriteIds.add(channel.id)
                        Toast.makeText(this@SearchActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
                    }
                    adapter.favoriteIds = favoriteIds
                    adapter.notifyDataSetChanged()
                }
            }
        }

        searchInput.setOnEditorActionListener { _: TextView, actionId: Int, event: KeyEvent? ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val query = searchInput.text.toString()
                if (query.length >= 2) performSearch(query)
                true
            } else false
        }

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
    }

    private fun performSearch(query: String) {
        channelRepo?.searchChannels(playlistId, query)?.observe(this) { list ->
            adapter.submitList(list)
        }
    }
}

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.VH>() {
    var onChannelClick: ((ChannelEntity) -> Unit)? = null
    var onChannelLongClick: ((ChannelEntity) -> Unit)? = null
    var favoriteIds = emptySet<Long>()
    private var items = listOf<ChannelEntity>()

    fun submitList(list: List<ChannelEntity>) { items = list; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val ch = items[pos]
        holder.name.text = ch.name
        holder.group.text = ch.groupTitle

        val isFav = favoriteIds.contains(ch.id)
        holder.favBtn.text = if (isFav) "\u2605" else "\u2606"
        holder.favBtn.setTextColor(if (isFav) 0xFFE50914.toInt() else 0xFFFFFFFF.toInt())
        holder.favBtn.setOnClickListener { onChannelLongClick?.invoke(ch) }

        holder.channelRow.setOnClickListener { onChannelClick?.invoke(ch) }
        holder.channelRow.setOnLongClickListener {
            onChannelLongClick?.invoke(ch)
            true
        }
    }

    override fun getItemCount() = items.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val channelRow: View = view.findViewById(R.id.channel_info)
        val name: TextView = view.findViewById(R.id.channel_name)
        val group: TextView = view.findViewById(R.id.channel_group)
        val favBtn: Button = view.findViewById(R.id.btn_fav)
    }
}