package com.demeth0.massaudioplayer.backend.adapters

import com.demeth0.massaudioplayer.backend.models.adapters.AudioProvider
import com.demeth0.massaudioplayer.backend.models.objects.Audio
import com.demeth0.massaudioplayer.backend.models.objects.LoopMode
import com.demeth0.massaudioplayer.backend.models.objects.Playlist
import com.demeth0.massaudioplayer.backend.models.objects.Queue

import java.util.Collections

class SmartAudioProvider : AudioProvider {
    private var queue: Queue = Queue()
    private var playlist: Playlist?
    private var loopMode: LoopMode = LoopMode.NONE
    private var randomMode =false
    private var currentAudio: Audio? =null

    init {
        playlist=null
    }

    override fun setPlaylist(p: Playlist?) {
        playlist=p
        playlist?.random(randomMode)

        //move_to_next();
    }

    override fun setRandom(mode: Boolean) {
        randomMode = mode
        playlist?.random(randomMode)
    }

    override fun getRandom(): Boolean = randomMode


    override fun setLoop(mode: LoopMode) {
        loopMode=mode
    }

    override fun getLoop(): LoopMode = loopMode

    override fun addToQueue(audio: Audio) {
        queue.add(audio)
    }

    override fun addToPlaylist(audios: List<Audio>) {
        if(playlist==null) setPlaylist(Playlist(audios))
        else playlist?.extend(audios)
    }

    override fun viewPlaylist(): List<Audio> = playlist?.view()?:Collections.emptyList()

    override fun viewQueue(): List<Audio> = queue.view()

    override fun getAudio(): Audio? = currentAudio

    override fun setAudioFromQueue(audioIndex: Int) {
        for(i in 0..<audioIndex)
            currentAudio = queue.next()
    }

    override fun setAudioFromPlaylist(audioIndex: Int){
        if(playlist==null) return
        playlist?.set(audioIndex)
        currentAudio=playlist?.get()
    }

    /**
     * This function has a similar behavior as advance_to_next but it will loop back to the beginning if it's the last song.
     * After loading a new play list you should always call move_to_next or the cursor will continue to be undefined.
     * This function is to prevent starting listening when loading playlist and force 1 more check up.
     */
    override fun moveToNext() {
        //if(loop_mode.equals(LoopMode.SINGLE)){
        //    current_audio=current_audio;
        //}else{
            currentAudio = queue.next()
        if(currentAudio==null){
                currentAudio=playlist?.next()
        }
        //}
    }

    /**
     * This function has a similar behavior as move_to_next but it will stop if it's the last song.
     */
    override fun advanceToNext() {
        if(loopMode == LoopMode.SINGLE){
            Unit
            // current_audio=current_audio
        }else if(loopMode == LoopMode.ALL){
            currentAudio = queue.next()
            if(currentAudio==null){
                currentAudio=playlist?.next()
            }
        }else{
            currentAudio = queue.next()
            if(currentAudio==null && !Playlist.isLastAudio(playlist)){
                currentAudio=playlist?.next()
            }
        }
    }

    override fun moveToPrev() {
        if(loopMode != LoopMode.SINGLE)
            currentAudio = playlist?.prev()
    }

    override fun clearQueue() = queue.clear()
}
