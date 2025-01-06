package com.demeth.massaudioplayer.backend.models.adapters

import com.demeth.massaudioplayer.backend.models.objects.Audio
import com.demeth.massaudioplayer.backend.models.objects.LoopMode
import com.demeth.massaudioplayer.backend.models.objects.Playlist


/**
 * determine the audio to play and manage the list of audios
 */
interface AudioProvider {

    /**
     * Clear any actual playlist and replace it with the new given playlist.
     * @param p The new playlist to use for the audio diffusion.
     */
    fun setPlaylist(p: Playlist?)

    /**
     * The random mode shuffle the playlist play order but do not affect the queue.
     * @param mode The new value of the random mode.
     */
    fun setRandom(mode: Boolean)

    /**
     * The random mode shuffle the playlist play order but do not affect the queue.
     * @return The current value of the random mode.
     */
    fun getRandom(): Boolean

    /**
     * Set the new loop option for audio reading.
     * @param mode Loop mode to set.
     */
    fun setLoop(mode: LoopMode)

    /**
     *
     * @return The current state of the loop option.
     */
    fun getLoop(): LoopMode

    /**
     * Add an audio in the queue.
     * @param audio Audio to add to the queue.
     */
    fun addToQueue(audio: Audio)

    /**
     * Add a list of audio to the playlist.
     * @param audios The list of audio to add.
     */
    fun addToPlaylist(audios: List<Audio>)

    /**
     * @return The queue audio content.
     */
    fun viewQueue(): List<Audio>

    /**
     * @return The playlist audio content.
     */
    fun viewPlaylist(): List<Audio>

    /**
     * @return The currently selected audio.
     */
    fun getAudio(): Audio?

    /**
     * Use the index to select the next audio to play from the queue.
     * @param audioIndex The index in the queue audio list.
     */
    fun setAudioFromQueue(audioIndex: Int)

    /**
     * Use the index to select the next audio to play from the playlist.
     * @param audioIndex The index in the playlist audio list.
     */
    fun setAudioFromPlaylist(audioIndex: Int)

    /**
     * When user skip an audio.
     */
    fun moveToNext()

    /**
     * when an audio is completed and need to load the next.
     */
    fun advanceToNext()
    fun moveToPrev()

    /**
     * Clear the queue of any pending audio to play.
     */
    fun clearQueue()
}
