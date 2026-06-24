package com.streamnator.app.ui.home

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.streamnator.app.R
import com.streamnator.app.StreamNatorApp
import com.streamnator.app.data.db.PlaylistEntity
import com.streamnator.app.data.repository.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddPlaylistDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_playlist, null)
        val typeRadio = view.findViewById<RadioGroup>(R.id.type_radio)
        val nameInput = view.findViewById<EditText>(R.id.name_input)
        val urlInput = view.findViewById<EditText>(R.id.url_input)
        val userAgentInput = view.findViewById<EditText>(R.id.user_agent_input)
        val xtreamLayout = view.findViewById<LinearLayout>(R.id.xtream_layout)
        val xtreamHost = view.findViewById<EditText>(R.id.xtream_host)
        val xtreamUser = view.findViewById<EditText>(R.id.xtream_user)
        val xtreamPass = view.findViewById<EditText>(R.id.xtream_pass)

        typeRadio.setOnCheckedChangeListener { _, id ->
            xtreamLayout.visibility = if (id == R.id.type_xtream) View.VISIBLE else View.GONE
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Add Playlist")
            .setView(view)
            .setPositiveButton("Save") { dialog, _ ->
                val name = nameInput.text.toString().ifEmpty { "Playlist" }
                val url = urlInput.text.toString()
                val userAgent = userAgentInput.text.toString().ifEmpty { "StreamNator/1.0" }
                val db = (requireActivity().application as StreamNatorApp).database
                val repo = PlaylistRepository(db)
                val prefs = (requireActivity().application as StreamNatorApp).prefs

                val type = when (typeRadio.checkedRadioButtonId) {
                    R.id.type_xtream -> "xtream"
                    else -> "m3u"
                }

                lifecycleScope.launch {
                    val playlist = PlaylistEntity(
                        name = name,
                        url = if (type == "m3u") url else null,
                        type = type,
                        userAgent = userAgent,
                        xtreamHost = if (type == "xtream") xtreamHost.text.toString() else null,
                        xtreamUser = if (type == "xtream") xtreamUser.text.toString() else null,
                        xtreamPass = if (type == "xtream") xtreamPass.text.toString() else null,
                        autoRefresh = true
                    )
                    val id = repo.insertPlaylist(playlist)
                    val savedPlaylist = playlist.copy(id = id)

                    // Set as active playlist immediately
                    prefs.setActivePlaylistId(id)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Playlist added. Refreshing...", Toast.LENGTH_SHORT).show()
                    }
                    try {
                        val count = repo.refreshPlaylist(savedPlaylist)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Loaded $count channels", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Failed to refresh: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}