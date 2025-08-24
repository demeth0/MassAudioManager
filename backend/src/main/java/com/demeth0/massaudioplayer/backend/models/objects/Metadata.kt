package com.demeth0.massaudioplayer.backend.models.objects

import android.net.Uri

/**
 * This object is used to save data specific to each {@link Audio} that could be used to for example load the album cover or get the streaming data...
 * Metadata is the base class and doesn't contain any useful methods or data, it should always be casted depending on the audio type used.
 *
 * @see FileAudioMetadata
 */
sealed interface Metadata {
    /**
     * {@link Metadata} used with an audio of the type {@link AudioType#LOCAL}.
     * Create a local audio file metadata object that will stock the uri to the file for loading the audio and album cover.
     * @param uri Uri of the audio file following Android path convention.
     */
    data class FileAudioMetadata(var uri :Uri?) : Metadata {

        /**
         * Create a local file metadata object uninitialized.
         */
        constructor():this(null)
    }
}
