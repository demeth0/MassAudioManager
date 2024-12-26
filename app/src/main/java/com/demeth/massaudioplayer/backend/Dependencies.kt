package com.demeth.massaudioplayer.backend

import android.content.Context
import android.util.Log
import com.demeth.massaudioplayer.backend.adapters.ApplicationAudioManager
import com.demeth.massaudioplayer.backend.adapters.FileAudioPlayer
import com.demeth.massaudioplayer.backend.adapters.HashMapDatabase
import com.demeth.massaudioplayer.backend.adapters.LoadedAudioPlayerFactory
import com.demeth.massaudioplayer.backend.adapters.LocalFileDatabaseProvider
import com.demeth.massaudioplayer.backend.adapters.SequentialEventManager
import com.demeth.massaudioplayer.backend.adapters.SmartAudioProvider
import com.demeth.massaudioplayer.backend.models.adapters.AudioManager
import com.demeth.massaudioplayer.backend.models.adapters.AudioProvider
import com.demeth.massaudioplayer.backend.models.adapters.Database
import com.demeth.massaudioplayer.backend.models.adapters.EventManager
import com.demeth.massaudioplayer.backend.models.objects.AudioType

data class Dependencies(
    private var _eventManager: EventManager,
    private var _database: Database,
    private var _audioManager: AudioManager,
    private var _audioProvider: AudioProvider
    ) {

    val eventManager : EventManager get() = _eventManager
    val database: Database get() = _database
    val audioManager: AudioManager get() = _audioManager
    val audioProvider: AudioProvider get() = _audioProvider

    companion object {
        @JvmStatic
        fun injectDependencies(context: Context): Dependencies{
            Log.d("[abc]","initializing dependencies")
            val eventManager = SequentialEventManager()
            val localProvider = LocalFileDatabaseProvider()
            val database = HashMapDatabase(context,localProvider)

            val fileAudioPlayer = FileAudioPlayer(eventManager,database,context)

            val audioPlayerFactory = LoadedAudioPlayerFactory()
            audioPlayerFactory.register(AudioType.LOCAL,fileAudioPlayer)
            val audioProvider = SmartAudioProvider()
            val audioManager = ApplicationAudioManager(audioPlayerFactory,eventManager,audioProvider)

            return Dependencies(
                eventManager,
                database,
                audioManager,
                audioProvider
            )
        }
    }
}
