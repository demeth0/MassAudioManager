package com.demeth.massaudioplayer.backend.adapters

import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayer
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayerFactory
import com.demeth.massaudioplayer.backend.models.adapters.PlayerNotImplementedException
import com.demeth.massaudioplayer.backend.models.objects.AudioType

import java.util.HashMap
import kotlin.jvm.Throws

class LoadedAudioPlayerFactory : AudioPlayerFactory {
    private val registeredDependencies=HashMap<AudioType, AudioPlayer>()

    override fun register(type: AudioType,player: AudioPlayer){
        registeredDependencies[type] = player
    }

    @Throws(PlayerNotImplementedException::class)
    override fun  provide(type: AudioType): AudioPlayer {
        if(registeredDependencies.containsKey(type))
            return this.registeredDependencies[type]!!
        throw PlayerNotImplementedException(type.toString())
    }
}
