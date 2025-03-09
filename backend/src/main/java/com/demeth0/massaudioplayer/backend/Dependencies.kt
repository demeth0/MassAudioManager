package com.demeth0.massaudioplayer.backend

import android.content.Context
import android.util.Log
import com.demeth0.massaudioplayer.backend.adapters.ApplicationAudioManager
import com.demeth0.massaudioplayer.backend.adapters.FileAudioPlayer
import com.demeth0.massaudioplayer.backend.adapters.HashMapDatabase
import com.demeth0.massaudioplayer.backend.adapters.IndependentAudioProvider
import com.demeth0.massaudioplayer.backend.adapters.LoadedAudioPlayerFactory
import com.demeth0.massaudioplayer.backend.adapters.LocalFileDatabaseProvider
import com.demeth0.massaudioplayer.backend.adapters.SequentialEventManager
import com.demeth0.massaudioplayer.backend.models.adapters.AudioManager
import com.demeth0.massaudioplayer.backend.models.adapters.AudioProvider
import com.demeth0.massaudioplayer.backend.models.adapters.Database
import com.demeth0.massaudioplayer.backend.models.adapters.EventManager
import com.demeth0.massaudioplayer.backend.models.objects.AudioType

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
        fun injectDependencies(context: Context): Dependencies {
            Log.d("[abc]","initializing dependencies")
            val eventManager = SequentialEventManager()
            val localProvider = LocalFileDatabaseProvider()
            val database = HashMapDatabase(context,localProvider)

            val fileAudioPlayer = FileAudioPlayer(eventManager,database,context)

            val audioPlayerFactory = LoadedAudioPlayerFactory()
            audioPlayerFactory.register(AudioType.LOCAL,fileAudioPlayer)
            val audioProvider = IndependentAudioProvider()
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
