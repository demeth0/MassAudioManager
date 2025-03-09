package com.demeth0.massaudioplayer.backend.adapters

import android.content.Context
import com.demeth0.massaudioplayer.backend.models.adapters.Database
import com.demeth0.massaudioplayer.backend.models.adapters.DatabaseContentProvider
import com.demeth0.massaudioplayer.backend.models.objects.Audio
import com.demeth0.massaudioplayer.backend.models.objects.AudioType
import com.demeth0.massaudioplayer.backend.models.objects.Metadata

class HashMapDatabase(context: Context,vararg contentProviders: DatabaseContentProvider) :
    Database {

    class DuplicateEntriesException : Exception("Entry already added to the collection, duplicate ?")

    // Database content
    private val fileAudioMetadata = HashMap<String, Metadata.FileAudioMetadata>()
    private val audioEntries = HashMap<String, Audio>()

    private var providers: Array<out DatabaseContentProvider> = contentProviders

    init {
        reload(context)
    }

    private fun clear(){
        audioEntries.clear()
        fileAudioMetadata.clear()
    }

    private fun saveMetadata(audio: Audio, metadata: Metadata){
        when(audio.type){
            AudioType.LOCAL -> {
                fileAudioMetadata[audio.path] = metadata as Metadata.FileAudioMetadata
            }
            else -> {}
        }
    }

    private fun loadProvider(provider: DatabaseContentProvider){
        while(provider.hasNext()){
            val content = provider.next()
            content?.apply {
                if(audioEntries.containsKey(audio.path)) {
                    TODO("in case two audio from different type have the same path this will crash.")
                }
                metadata?.let {
                    audioEntries[audio.path] = audio
                    saveMetadata(audio,metadata)
                }
            }
        }
    }

    override fun reload(context: Context) {
        clear()
        providers.forEach {
            it.open(context)
            loadProvider(it)
            it.close()
        }
    }

    /************************************/

    override fun getMetadata(audio: Audio): Metadata? {
        return when(audio.type){
            AudioType.LOCAL -> fileAudioMetadata[audio.path]
            else -> {null}
        }
    }

    override fun getEntries(): List<Audio> = audioEntries.values.toList()

    override fun getEntries(filter: (Audio)->Boolean): Collection<Audio> = audioEntries.values.filter(filter).toList()



}
