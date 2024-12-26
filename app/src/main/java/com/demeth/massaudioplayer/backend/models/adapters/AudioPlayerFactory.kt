package com.demeth.massaudioplayer.backend.models.adapters;

import com.demeth.massaudioplayer.backend.models.objects.AudioType;

/**
 * This factory is meant to provide the corresponding Audio player depending on the audio type given.
 */
public interface AudioPlayerFactory {

    /**
     * Provide an audio player if available for the given type.
     * @param type Type of audio player to load.
     * @return The audio player.
     */
    AudioPlayer provide(AudioType type) throws PlayerNotImplementedException;

    /**
     * Register a new audio player for a specific type of audio object.
     * @param type The type of audio object to map to this player.
     * @param player The player implementation.
     */
    void register(AudioType type, AudioPlayer player);

    class PlayerNotImplementedException extends Exception {
        public PlayerNotImplementedException(String details) {
            super("This audio player isn't currently supported : "+details);
        }
    }
}
