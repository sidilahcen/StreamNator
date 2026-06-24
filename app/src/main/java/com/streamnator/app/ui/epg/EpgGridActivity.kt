package com.streamnator.app.ui.epg

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.streamnator.app.R
import com.streamnator.app.StreamNatorApp
import com.streamnator.app.data.db.EpgSourceEntity
import com.streamnator.app.data.repository.EpgRepository
import com.streamnator.app.domain.model.EpgProgram
import kotlinx.coroutines.launch

class EpgGridActivity : AppCompatActivity() {

    private lateinit var epgRepo: EpgRepository
    private val programs = mutableListOf<EpgProgram>()
    private val adapter = EpgAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_epg)

        val db = (application as StreamNatorApp).database
        epgRepo = EpgRepository(db, this)

        val recyclerView = findViewById<RecyclerView>(R.id.epg_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<View>(R.id.btn_today).setOnClickListener { loadEpg() }
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        loadEpg()
    }

    private fun loadEpg() {
        lifecycleScope.launch {
            try {
                epgRepo.getEpgSources().observe(this@EpgGridActivity) { sources: List<EpgSourceEntity> ->
                    lifecycleScope.launch {
                        for (source: EpgSourceEntity in sources) {
                            try {
                                val progs: List<EpgProgram> = epgRepo.fetchEpg(source)
                                programs.clear()
                                programs.addAll(progs)
                                adapter.submitList(progs)
                            } catch (_: Exception) {}
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@EpgGridActivity, "EPG load failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class EpgAdapter : RecyclerView.Adapter<EpgAdapter.VH>() {
    private var items = listOf<EpgProgram>()

    fun submitList(list: List<EpgProgram>) { items = list.sortedBy { it.startTime }; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
        val view = android.view.LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        view.isFocusable = true; view.isFocusableInTouchMode = true
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val p = items[pos]
        (holder.itemView as android.widget.TwoLineListItem).apply {
            text1.text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(p.startTime)) + " - " + p.title
            text2.text = p.description
        }
    }

    override fun getItemCount() = items.size

    class VH(view: View) : RecyclerView.ViewHolder(view)
}