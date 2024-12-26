package com.demeth.massaudioplayer.backend.models.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * A queue is another implementation of the {@link Playlist} object that is made to reproduce a queue system that would take priority to the playlist.
 * This implementation reproduce the functions of a playlist with some limitations and other behavior.
 * A Queue object can only temporarily stock audio for instantaneous diffusion like a pile.
 */
public class Queue {
    private List<Audio> audios;

    /**
     * Create a Queue object that can temporarily stock audio for instantaneous diffusion.
     */
    public Queue(){
        audios = new ArrayList<>();
    }
    public void add(Audio audio){
        audios.add(audio);
    }
    public void set(Audio audio){
        while(!audios.get(0).equals(audio)){
            audios.remove(0);
        }
    }

    /**
     * Empty the queue of pending audio.
     */
    public void clear(){
        audios.clear();
    }

    /**
     * @return The number of audio pending in the queue.
     */
    public int size(){
        return audios.size();
    }

    /**
     * @return The next audio in the queue.
     */
    public Audio next(){
        if(audios.size()==0)
            return null;
        return audios.remove(0);
    }

    /**
     * @return A copy of all audio pending in the queue.
     */
    public List<Audio> view(){
        return new ArrayList<>(audios);
    }
}
