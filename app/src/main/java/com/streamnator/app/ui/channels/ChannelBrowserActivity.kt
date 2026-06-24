package com.streamnator.app.ui.channels

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.streamnator.app.R
import com.streamnator.app.StreamNatorApp
import com.streamnator.app.data.db.ChannelEntity
import com.streamnator.app.data.repository.ChannelRepository
import com.streamnator.app.ui.player.PlayerActivity
import com.streamnator.app.ui.epg.EpgGridActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChannelBrowserActivity : AppCompatActivity() {

    private lateinit var channelRepo: ChannelRepository
    private lateinit var searchBox: EditText
    private lateinit var channelList: RecyclerView
    private lateinit var favoritesRow: RecyclerView
    private lateinit var recentRow: RecyclerView
    private lateinit var groupList: RecyclerView
    private val channelAdapter = ChannelAdapter()
    private val favoritesAdapter = HorizontalChannelAdapter()
    private val recentAdapter = HorizontalChannelAdapter()
    private val groupAdapter = GroupAdapter()
    private var playlistId: Long = -1
    private val favoriteIds = mutableSetOf<Long>()
    private var lastWatchedChannel: ChannelEntity? = null
    private var allChannels = listOf<ChannelEntity>()
    private var isSearching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channels)

        val db = (application as StreamNatorApp).database
        channelRepo = ChannelRepository(db.channelDao(), db.favoriteDao(), db.recentlyWatchedDao())
        playlistId = (application as StreamNatorApp).prefs.getActivePlaylistId()

        setupSearch()
        setupDashboard()
        setupSidebar()

        findViewById<View>(R.id.btn_back_home).setOnClickListener { finish() }
        findViewById<View>(R.id.epg_btn).setOnClickListener {
            startActivity(Intent(this, EpgGridActivity::class.java))
        }
        findViewById<View>(R.id.continue_watching_item)?.setOnClickListener {
            lastWatchedChannel?.let { playChannel(it) }
        }

        loadDashboard()
    }

    private fun setupSearch() {
        searchBox = findViewById(R.id.search_box)
        searchBox.setOnEditorActionListener { _, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
                || (event?.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                val q = searchBox.text.toString().trim()
                if (q.length >= 2) filterChannels(q) else showAllChannels()
                true
            } else false
        }
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim() ?: ""
                if (q.length >= 2) filterChannels(q) else showAllChannels()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupDashboard() {
        favoritesRow = findViewById(R.id.favorites_row)
        favoritesRow.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        favoritesRow.adapter = favoritesAdapter
        favoritesAdapter.onItemClick = { ch -> playChannel(ch) }
        favoritesAdapter.onItemLongClick = { ch -> toggleFavorite(ch) }

        recentRow = findViewById(R.id.recent_row)
        recentRow.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recentRow.adapter = recentAdapter
        recentAdapter.onItemClick = { ch -> playChannel(ch) }
        recentAdapter.onItemLongClick = { ch -> toggleFavorite(ch) }

        channelList = findViewById(R.id.channel_list)
        channelList.layoutManager = LinearLayoutManager(this)
        channelList.adapter = channelAdapter
        channelAdapter.onChannelClick = { ch -> playChannel(ch) }
        channelAdapter.onChannelLongClick = { ch -> toggleFavorite(ch) }
    }

    private fun setupSidebar() {
        groupList = findViewById(R.id.group_list)
        groupList.layoutManager = LinearLayoutManager(this)
        groupList.adapter = groupAdapter
        groupAdapter.onGroupClick = { group -> selectGroup(group) }
    }

    private fun loadDashboard() {
        loadContinueWatching()
        loadFavoritesRow()
        loadRecentRow()
        loadAllChannels()
        loadGroups()
    }

    private fun loadContinueWatching() {
        channelRepo.getRecentlyWatched(playlistId, limit = 1).observe(this) { channels ->
            if (channels.isNotEmpty()) {
                lastWatchedChannel = channels.first()
                findViewById<LinearLayout>(R.id.continue_watching_section).visibility = View.VISIBLE
                findViewById<TextView>(R.id.continue_watching_name).text = channels.first().name
                val iv = findViewById<ImageView>(R.id.continue_watching_icon)
                if (channels.first().tvgLogo.isNotEmpty()) {
                    iv.load(channels.first().tvgLogo) {
                        crossfade(true)
                        error(android.R.drawable.ic_media_play)
                        placeholder(android.R.drawable.ic_media_play)
                    }
                }
            }
        }
    }

    private fun loadFavoritesRow() {
        channelRepo.getFavorites(playlistId).observe(this) { channels ->
            favoritesAdapter.submitList(channels)
            favoriteIds.clear()
            favoriteIds.addAll(channels.map { it.id })
            val header = findViewById<TextView>(R.id.favorites_header)
            if (channels.isEmpty()) header.visibility = View.GONE else header.visibility = View.VISIBLE
        }
    }

    private fun loadRecentRow() {
        channelRepo.getRecentlyWatched(playlistId, limit = 10).observe(this) { channels ->
            recentAdapter.submitList(channels)
            val header = findViewById<TextView>(R.id.recent_header)
            if (channels.isEmpty()) header.visibility = View.GONE else header.visibility = View.VISIBLE
        }
    }

    private fun loadAllChannels() {
        channelRepo.getChannelsByPlaylist(playlistId).observe(this) { channels ->
            allChannels = channels
            if (!isSearching) {
                channelAdapter.submitList(channels)
            }
        }
    }

    private fun loadGroups() {
        channelRepo.getGroups(playlistId).observe(this) { groups ->
            val all = mutableListOf("Dashboard", "All Channels", "Favorites", "Recently Watched")
            all.addAll(groups)
            groupAdapter.submitList(all)
        }
    }

    private fun selectGroup(group: String) {
        isSearching = false
        searchBox.text.clear()
        when (group) {
            "Dashboard" -> showDashboardView()
            "All Channels" -> { showListView(); showAllChannels() }
            "Favorites" -> {
                showListView()
                channelRepo.getFavorites(playlistId).observe(this) { channelAdapter.submitList(it) }
            }
            "Recently Watched" -> {
                showListView()
                channelRepo.getRecentlyWatched(playlistId).observe(this) { channelAdapter.submitList(it) }
            }
            else -> {
                showListView()
                channelRepo.getChannelsByGroup(playlistId, group).observe(this) { channelAdapter.submitList(it) }
            }
        }
    }

    private fun showDashboardView() {
        findViewById<View>(R.id.dashboard_scroll).visibility = View.VISIBLE
        findViewById<View>(R.id.channel_list_container).visibility = View.GONE
    }

    private fun showListView() {
        findViewById<View>(R.id.dashboard_scroll).visibility = View.GONE
        findViewById<View>(R.id.channel_list_container).visibility = View.VISIBLE
    }

    private fun filterChannels(query: String) {
        isSearching = true
        val filtered = allChannels.filter {
            it.name.contains(query, ignoreCase = true) || it.groupTitle.contains(query, ignoreCase = true)
        }
        channelAdapter.submitList(filtered)
        channelList.post { channelList.requestFocus() }
    }

    private fun showAllChannels() {
        isSearching = false
        channelAdapter.submitList(allChannels)
    }

    private fun toggleFavorite(channel: ChannelEntity) {
        lifecycleScope.launch {
            channelRepo.toggleFavorite(channel.id, playlistId)
            withContext(Dispatchers.Main) {
                if (favoriteIds.contains(channel.id)) {
                    favoriteIds.remove(channel.id)
                    Toast.makeText(this@ChannelBrowserActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
                } else {
                    favoriteIds.add(channel.id)
                    Toast.makeText(this@ChannelBrowserActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
                }
                loadFavoritesRow()
            }
        }
    }

    private fun playChannel(channel: ChannelEntity) {
        lifecycleScope.launch {
            channelRepo.addToRecentlyWatched(channel.id, playlistId)
            withContext(Dispatchers.Main) {
                startActivity(Intent(this@ChannelBrowserActivity, PlayerActivity::class.java).apply {
                    putExtra("channel_url", channel.url)
                    putExtra("channel_name", channel.name)
                    putExtra("channel_id", channel.id)
                    putExtra("playlist_id", playlistId)
                })
            }
        }
    }
}

// --- Regular vertical channel list adapter ---
class ChannelAdapter : RecyclerView.Adapter<ChannelAdapter.VH>() {
    var onChannelClick: ((ChannelEntity) -> Unit)? = null
    var onChannelLongClick: ((ChannelEntity) -> Unit)? = null
    var favoriteIds = emptySet<Long>()
    private var items = listOf<ChannelEntity>()

    fun submitList(list: List<ChannelEntity>) { items = list; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
        // Wrapper is NOT focusable — channel_info and btn_fav are the focus targets
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
        // channel_info is the focusable row (not the wrapper)
        holder.channelRow.setOnClickListener { onChannelClick?.invoke(ch) }
        holder.channelRow.setOnLongClickListener { onChannelLongClick?.invoke(ch); true }
    }

    override fun getItemCount() = items.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val channelRow: View = view.findViewById(R.id.channel_info)
        val name: TextView = view.findViewById(R.id.channel_name)
        val group: TextView = view.findViewById(R.id.channel_group)
        val favBtn: Button = view.findViewById(R.id.btn_fav)
    }
}

// --- Horizontal row adapter (favorites / recent) ---
class HorizontalChannelAdapter : RecyclerView.Adapter<HorizontalChannelAdapter.VH>() {
    var onItemClick: ((ChannelEntity) -> Unit)? = null
    var onItemLongClick: ((ChannelEntity) -> Unit)? = null
    private var items = listOf<ChannelEntity>()

    fun submitList(list: List<ChannelEntity>) { items = list; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_channel_card, parent, false)
        view.isFocusable = true; view.isFocusableInTouchMode = true
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val ch = items[pos]
        holder.name.text = ch.name
        if (ch.tvgLogo.isNotEmpty()) {
            holder.logo.load(ch.tvgLogo) {
                crossfade(true)
                error(android.R.drawable.ic_media_play)
                placeholder(android.R.drawable.ic_media_play)
            }
        }
        holder.itemView.setOnClickListener { onItemClick?.invoke(ch) }
        holder.itemView.setOnLongClickListener { onItemLongClick?.invoke(ch); true }
    }

    override fun getItemCount() = items.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.card_channel_name)
        val logo: ImageView = view.findViewById(R.id.card_channel_logo)
    }
}

// --- Sidebar group adapter ---
class GroupAdapter : RecyclerView.Adapter<GroupAdapter.VH>() {
    var onGroupClick: ((String) -> Unit)? = null
    private var items = listOf<String>()

    fun submitList(list: List<String>) { items = list; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        view.isFocusable = true; view.isFocusableInTouchMode = true
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, pos: Int) {
        holder.name.text = items[pos]
        holder.itemView.setOnClickListener { onGroupClick?.invoke(items[pos]) }
    }

    override fun getItemCount() = items.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.group_name)
    }
}