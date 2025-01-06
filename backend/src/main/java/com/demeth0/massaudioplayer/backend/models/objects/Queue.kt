package com.demeth.massaudioplayer.backend.models.objects

import java.util.ArrayList

/**
 * A queue is another implementation of the {@link Playlist} object that is made to reproduce a queue system that would take priority to the playlist.
 * This implementation reproduce the functions of a playlist with some limitations and other behavior.
 * A Queue object can only temporarily stock audio for instantaneous diffusion like a pile.
 */
class Queue {
    private val audios = ArrayList<Audio>()

    fun add(audio: Audio) = audios.add(audio)

    fun set(audio: Audio){
        while(audios[0] != audio){
            audios.removeAt(0)
        }
    }

    /**
     * Empty the queue of pending audio.
     */
    fun clear() = audios.clear()


    /**
     * @return The number of audio pending in the queue.
     */
    fun size() = audios.size

    /**
     * @return The next audio in the queue.
     */
    fun next(): Audio? {
        if(audios.size==0)
            return null
        return audios.removeAt(0)
    }

    /**
     * @return A copy of all audio pending in the queue.
     */
    fun view(): List<Audio> = audios.toMutableList()
}
