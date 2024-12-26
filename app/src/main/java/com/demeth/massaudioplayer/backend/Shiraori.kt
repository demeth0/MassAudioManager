package com.demeth.massaudioplayer.backend

import android.content.Context
import com.demeth.massaudioplayer.backend.models.adapters.EventHandler
import com.demeth.massaudioplayer.backend.models.objects.Audio
import com.demeth.massaudioplayer.backend.models.objects.Event
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap
import com.demeth.massaudioplayer.backend.models.objects.LoopMode
import com.demeth.massaudioplayer.backend.models.objects.Playlist
import com.demeth.massaudioplayer.backend.models.objects.Timestamp

/**
 * Backend core use cases for audio management.
 */
@Suppress("unused")
class Shiraori {
    companion object {
        /**
         * Load the dependencies for using the functionalities of this library.
         * @param context The application context used to load the various components.
         * @return The dependencies of the library used for every operations.
         */
        @JvmStatic
        fun openDependencies(context: Context) : Dependencies{
            val dep = Dependencies.injectDependencies(context)
            return dep
        }

        /**
         * Create an event handler that will react to Shiraori callback events.
         * @param id Unique handler identifier.
         * @param handler Callback to run on event triggered.
         * @param dependencies The backend dependencies.
         */
        fun setHandler(id: String, handler: EventHandler, dependencies: Dependencies){
            dependencies.eventManager.registerHandler(id,handler)
        }

        /**
         * Remove an event handler from the callback system.
         * @param id Unique handler identifier.
         * @param dependencies The backend dependencies.
         */
        fun unsetHandler(id: String, dependencies: Dependencies){
            dependencies.eventManager.removeHandler(id)
        }

        fun reloadDatabase(context: Context,dependencies: Dependencies){
            dependencies.database.reload(context)
            dependencies.eventManager.trigger(Event(EventCodeMap.EVENT_DATABASE_RELOADED))
        }

        /**
         * Get an array list of all the loaded audio in the database available for diffusion.
         * @param dependencies The backend dependencies.
         * @return A collection of all the playable audios.
         */
        fun getDatabaseEntries(dependencies: Dependencies): Collection<Audio> {
            return dependencies.database.getEntries()
        }

        /**
         * Check if the audio playlist system is currently in random reading mode.
         * @param dependencies The backend dependencies.
         * @return True if the random mode is enabled.
         */
        fun isRandomModeEnabled(dependencies: Dependencies): Boolean{
            return dependencies.audioProvider.getRandom()
        }



        /**
         * Set or unset the reading of the playlist audios in random mode.
         * @param value True to enabled random mode, False to disable.
         * @param dependencies The backend dependencies.
         */
        fun setRandomModeEnabled(value: Boolean, dependencies: Dependencies){
            dependencies.audioProvider.setRandom(value)
            dependencies.eventManager.trigger(Event(EventCodeMap.EVENT_RANDOM_MODE_CHANGED,dependencies.audioProvider.getRandom()))
        }

        fun getLoopMode(dependencies: Dependencies): LoopMode {
            return dependencies.audioProvider.getLoop()
        }

        fun setLoopMode(loopMode: LoopMode, dependencies: Dependencies){
            dependencies.audioProvider.setLoop(loopMode)
            dependencies.eventManager.trigger(Event(EventCodeMap.EVENT_LOOP_MODE_CHANGED, loopMode))
        }

        /**
         * Add an audio to the queue of the audio manager and play it.
         * @param audio Th audio to add to the queue and start instantly.
         * @param dependencies The backend dependencies.
         */
        fun playAudio(audio: Audio, dependencies: Dependencies){
            //TODO temp, create add_to_head function
            dependencies.audioProvider.clearQueue()
            dependencies.audioProvider.addToQueue(audio)
            dependencies.audioManager.playNext()
        }

        fun playInPlaylist(audios: Collection<Audio>, dependencies: Dependencies){
            dependencies.audioProvider.setPlaylist(Playlist(ArrayList(audios)))
            dependencies.audioManager.playNext()
        }

        /**
         * Skip to the next audio in the waiting list and play it.
         * @param dependencies The backend dependencies.
         */
        fun skipToNextAudio(dependencies: Dependencies){
            dependencies.audioManager.playNext()
        }

        fun skipToPreviousAudio(dependencies: Dependencies){
            dependencies.audioManager.playPrevious()
        }

        fun getTimestamp(dependencies: Dependencies): Timestamp {
            return dependencies.audioManager.timestamp()
        }

        fun setTimestamp(timestampProgress: Double, dependencies: Dependencies){
            dependencies.audioManager.setTimestampProgress(timestampProgress)
        }

        /**
         * Add a list of audio to the queue of the audio manager and play it.
         * @param audios A collection of audio to add to the queue and start immediately.
         * @param dependencies The backend dependencies.
         */
        fun playAudios(audios: Collection<Audio>, dependencies: Dependencies){
            //dependencies.audio_provider.clear_queue();

            audios.forEach{
                dependencies.audioProvider.addToQueue(it)
            }

            if(dependencies.audioManager.isPaused())
                dependencies.audioManager.playNext()
        }

        fun getCurrentAudio(dependencies: Dependencies): Audio? = dependencies.audioProvider.getAudio()


        /**
         * This function pause the audio. If the audio is paused or completed, will resume diffusion anyway or start from beginning.
         * @param dep The backend dependencies.
         */
        fun pauseAudio(dep: Dependencies){
            if(dep.audioManager.isPaused()){
                // TODO Temporary ? If the audio is null with the current implementation that mean that the audio is finished and is not paused.
                dep.audioManager.play()

                // TODO should we really trigger a resume event here ?
                dep.eventManager.trigger(Event(EventCodeMap.EVENT_AUDIO_RESUME))
            }else{
                dep.audioManager.pause()
                dep.eventManager.trigger(Event(EventCodeMap.EVENT_AUDIO_PAUSED))
            }
        }

        fun viewQueue(dependencies: Dependencies): List<Audio>{
            return dependencies.audioProvider.viewQueue()
        }

        fun viewPlaylist(dependencies: Dependencies): List<Audio>{
            return dependencies.audioProvider.viewPlaylist()
        }
    }
}

