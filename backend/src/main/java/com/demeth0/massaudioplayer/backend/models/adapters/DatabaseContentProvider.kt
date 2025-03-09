package com.demeth0.massaudioplayer.backend.models.adapters

import android.content.Context

import com.demeth0.massaudioplayer.backend.models.objects.Audio
import com.demeth0.massaudioplayer.backend.models.objects.Metadata

/**
 * This class is used as the adaptable return object of the database provider to return wrapped Audio and metadata objects.
 *
 * Create a content object containing one audio and his associated metadata.
 * @param audio The audio to wrap in the container.
 * @param metadata The metadata to wrap in the container.
 */
data class Content(val audio: Audio, val metadata: Metadata?) {
    /**
     * Create a content object containing one audio. The metadata are set to null.
     * @param audio The audio to wrap in the container.
     */
    constructor(audio: Audio) : this(audio, null)
}

/**
 * Provide specific implementations to load different categories of Audio. For example Spotify or Local...
 */
interface DatabaseContentProvider {


    /**
     * Iterator that provide loaded content for the database to save.
     * @return The next content loaded from this provider.
     */
    fun next(): Content?

    /**
     * Check if the provider still has data to return.
     * @return True if there is still data to load in the database.
     */
    fun hasNext(): Boolean

    /**
     * Open the provider for content extraction.
     * @param context The application context used to get authorization and access to the android API.
     */
    fun open(context: Context)

    /**
     * Close the provider after use to free resources.
     */
    fun close()
}
