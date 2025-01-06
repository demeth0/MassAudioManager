package com.demeth.massaudioplayer.backend.models.adapters

import com.demeth.massaudioplayer.backend.models.objects.AudioType
import kotlin.jvm.Throws

class PlayerNotImplementedException(details : String) : Exception("This audio player isn't currently supported : $details")

/**
 * This factory is meant to provide the corresponding Audio player depending on the audio type given.
 */
interface AudioPlayerFactory {

    /**
     * Provide an audio player if available for the given type.
     * @param type Type of audio player to load.
     * @return The audio player.
     */
    @Throws(PlayerNotImplementedException::class)
    fun provide(type: AudioType): AudioPlayer?

    /**
     * Register a new audio player for a specific type of audio object.
     * @param type The type of audio object to map to this player.
     * @param player The player implementation.
     */
    fun register(type: AudioType, player: AudioPlayer)


}
