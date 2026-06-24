package com.streamnator.app.ui.home

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.streamnator.app.StreamNatorApp
import com.streamnator.app.data.db.PlaylistEntity
import com.streamnator.app.data.repository.PlaylistRepository
import kotlinx.coroutines.launch

class PlaylistMenuDialog(private val playlist: PlaylistEntity) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val items = arrayOf("Refresh", "Delete")
        return AlertDialog.Builder(requireContext())
            .setTitle(playlist.name)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> refreshPlaylist()
                    1 -> confirmDelete()
                }
            }
            .create()
    }

    private fun refreshPlaylist() {
        val ctx = requireContext().applicationContext
        val app = ctx as StreamNatorApp
        lifecycleScope.launch {
            val repo = PlaylistRepository(app.database)
            try {
                val count = repo.refreshPlaylist(playlist)
                Toast.makeText(ctx, "Loaded $count channels", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(ctx, "Refresh failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmDelete() {
        val ctx = requireContext().applicationContext
        val app = ctx as StreamNatorApp
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Playlist")
            .setMessage("Delete \"" + playlist.name + "\" and all its channels?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    PlaylistRepository(app.database).deletePlaylist(playlist.id)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
