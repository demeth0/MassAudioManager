package com.demeth0.massaudioplayer.backend.models.objects

/**
 * differentiate between all the type of audio track implementation available for different audio players.
 * @see Audio
 *
 * @see com.demeth0.massaudioplayer.backend.models.adapters.AudioPlayer AudioPlayer
 */
enum class AudioType {
    /**
     * Designate all audio loaded from the spotify API.
     */
    SPOTIFY,

    /**
     * Designate all audio saved on the system as local data files.
     */
    LOCAL,

    /**
     * Designate all audio loaded from Youtube.
     */
    YOUTUBE,
    PLAYLIST //TODO keep ?
}


/**
 * Object that stock information related to an audio track.
 *
 * @param displayName The name that should if needed be printed for human reading.
 * @param path A string identifier that should uniquely identify the track.
 * @param type The source of the audio track from which it was loaded.
 */
data class Audio(val displayName: String, val path: String, val type: AudioType) : Comparable<Audio>{

    /**
     * The comparison system was override to allow two audio track coming from different media to be considered the same.
     *
     * @param other
     * @return
     */
    override fun compareTo(other: Audio): Int {
        return this.displayName.compareTo(other.displayName)
    }

    override fun toString(): String = "Audio [${type.name}:$path]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Audio

        if (displayName != other.displayName) return false
        if (path != other.path) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = displayName.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}
