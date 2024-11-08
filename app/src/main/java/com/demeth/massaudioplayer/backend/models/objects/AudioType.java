package com.demeth.massaudioplayer.backend.models.objects;

/**
 * differentiate between all the type of audio track implementation available for different audio players.
 * @see Audio
 * @see com.demeth.massaudioplayer.backend.models.adapters.AudioPlayer AudioPlayer
 */
public enum AudioType {
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
