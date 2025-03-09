package com.demeth0.massaudioplayer.backend.adapters

import com.demeth0.massaudioplayer.backend.models.adapters.AudioProvider
import com.demeth0.massaudioplayer.backend.models.objects.Audio
import com.demeth0.massaudioplayer.backend.models.objects.LoopMode
import com.demeth0.massaudioplayer.backend.models.objects.Playlist
import kotlin.math.max

class IndependentAudioProvider: AudioProvider {
    private val queue: MutableList<Audio> = mutableListOf()
    private var playlist: List<Audio> = emptyList()
    private var playlistShuffled: List<Audio> = emptyList()

    private var index = 0
    private var audio: Audio? = null

    private var random = false
    private var loopMode = LoopMode.NONE

    override fun setPlaylist(p: Playlist?) {
        playlist = p?.view() ?: emptyList()
        playlistShuffled = p?.view()?.shuffled() ?: emptyList()

        // move to next and advance to next would skip first track if start at 0
        index=-1
    }

    override fun setRandom(mode: Boolean) {
        random = mode
    }

    override fun getRandom(): Boolean {
        return random
    }

    override fun setLoop(mode: LoopMode) {
        loopMode = mode
    }

    override fun getLoop(): LoopMode {
        return loopMode
    }

    override fun addToQueue(audio: Audio) {
        queue.add(audio)
    }

    override fun addToPlaylist(audios: List<Audio>) {
        playlist = playlist+audios
        playlistShuffled = playlistShuffled+audios
    }

    override fun viewQueue(): List<Audio> {
        return queue
    }

    override fun viewPlaylist(): List<Audio> {
        return if(random) playlistShuffled else playlist
    }

    override fun getAudio(): Audio? {
        return audio
    }

    private fun selectNextAudio(useQueue : Boolean = true){
        audio = when {
            useQueue && queue.isNotEmpty() -> queue.removeAt(0)
            playlist.isEmpty() -> null
            index >= playlist.size -> null
            !random -> playlist[index]
            random -> playlistShuffled[index]
            else -> null
        }
    }

    override fun setAudioFromQueue(audioIndex: Int) {
        if(audioIndex<queue.size){
            for(i in 0..audioIndex)
                queue.removeAt(0)

            selectNextAudio()
        } else
            throw IndexOutOfBoundsException("Index $audioIndex is above queue size")
    }

    override fun setAudioFromPlaylist(audioIndex: Int) {
        if(audioIndex<playlist.size){
            index=audioIndex
            selectNextAudio(false)
        }
        else
            throw IndexOutOfBoundsException("Index $audioIndex is above playlist size")
    }

    override fun moveToNext() {
        if(queue.isEmpty()) {
            index = when (loopMode) {
                LoopMode.SINGLE -> index + 1
                LoopMode.NONE -> index + 1
                LoopMode.ALL -> {
                    if (index == playlist.size) playlistShuffled = playlistShuffled.shuffled()
                    (index + 1) % playlist.size
                }
            }
        }
        selectNextAudio()
    }

    override fun advanceToNext() {
        if(queue.isEmpty()) {
            index = when (loopMode) {
                LoopMode.SINGLE -> index
                LoopMode.NONE -> index + 1
                LoopMode.ALL -> {
                    if (index == playlist.size) playlistShuffled = playlistShuffled.shuffled()
                    (index + 1) % playlist.size
                }
            }
        }
        selectNextAudio()
    }

    override fun moveToPrev() {
        index = when(loopMode){
            LoopMode.SINGLE -> index
            LoopMode.NONE -> {
                max(0, index-1) // only go to prev if not first audio
            }
            LoopMode.ALL -> {
                (index-1+playlist.size)%playlist.size
            }
        }
        selectNextAudio(false)
    }

    override fun clearQueue() {
        queue.clear()
    }
}