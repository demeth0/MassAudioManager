package com.demeth.massaudioplayer.backend.models.objects;

/**
 * The {@link com.demeth.massaudioplayer.backend.models.adapters.AudioProvider AudioProvider}
 */
public enum LoopMode {
    /**
     * In the playlist the current audio will be repeated endlessly.
     */
    SINGLE,

    /**
     * In the playlist all audio will be played in order and then stop.
     */
    NONE,

    /**
     * In the playlist all audio will be played, then start again from the beginning endlessly.
     */
    ALL
}
