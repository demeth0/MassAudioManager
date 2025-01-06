package com.demeth.massaudioplayer.backend.models.adapters


import android.content.Context
import com.demeth.massaudioplayer.backend.models.objects.Audio
import com.demeth.massaudioplayer.backend.models.objects.Metadata


interface Database {
    /**
     * Get the metadata related to this specific Audio.
     * @param audio The audio from which we retrieve the metadata.
     * @return The audio metadata.
     */
    fun getMetadata(audio: Audio): Metadata?

    /**
     *
     * @return All the audio entries in the database.
     */
    fun getEntries(): List<Audio>

    /**
     * Get the entries filtered.
     * @param filter The filter.
     * @return The list of entries selected by the filter.
     */
     fun getEntries(filter: (Audio)->Boolean): Collection<Audio>


    /**
     * Reload the database.
     * @param context The context to use to reload the data.
     */
    fun reload(context: Context)
}
