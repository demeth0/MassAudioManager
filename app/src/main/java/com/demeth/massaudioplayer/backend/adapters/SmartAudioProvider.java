package com.demeth.massaudioplayer.backend.adapters;

import com.demeth.massaudioplayer.backend.models.adapters.AudioProvider;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.LoopMode;
import com.demeth.massaudioplayer.backend.models.objects.Playlist;
import com.demeth.massaudioplayer.backend.models.objects.Queue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SmartAudioProvider implements AudioProvider {
    private Queue queue;
    private Playlist playlist;
    private LoopMode loop_mode=LoopMode.NONE;
    private boolean random_mode=false;
    Audio current_audio=null;

    public SmartAudioProvider(){
        queue=new Queue();
        playlist=null;
    }

    /*@Override
    public Queue get_queue() {
        return queue;
    }

    @Nullable
    @Override
    public Playlist get_playlist() {
        return playlist;
    }*/

    @Override
    public void set_playlist(Playlist p) {
        playlist=p;
        if(playlist!=null) playlist.random(random_mode);

        //move_to_next();
    }

    @Override
    public void set_random(boolean mode) {
        random_mode = mode;
        if(playlist!=null) playlist.random(random_mode);
    }

    @Override
    public boolean get_random() {
        return random_mode;
    }

    @Override
    public void set_loop(LoopMode mode) {
        loop_mode=mode;
    }

    @Override
    public LoopMode get_loop() {
        return loop_mode;
    }

    @Override
    public void add_to_queue(Audio audio) {
        queue.add(audio);
    }

    @Override
    public void add_to_playlist(List<Audio> audios) {
        if(playlist==null) set_playlist(new Playlist(audios));
        else playlist.extend(audios);
    }

    @Override
    public List<Audio> view_playlist() {
        if(playlist!=null)
            return playlist.view();
        else
            return Collections.emptyList();
    }

    @Override
    public List<Audio> view_queue() {
        return queue.view();
    }

    @Override
    public Audio get_audio() {
        return current_audio;
    }

    @Override
    public void set_audio_from_queue(int audio_index) {
        for(int i=0;i<=audio_index;i++)
            current_audio = queue.next();
    }

    @Override
    public void set_audio_from_playlist(int audio_index){
        if(playlist==null) return;
        playlist.set(audio_index);
        current_audio=playlist.get();
    }

    /**
     * This function has a similar behavior as advance_to_next but it will loop back to the begining if it's the last song.
     * After loading a new play list you should always call move_to_next or the cursor will continue to be undefined.
     * This function is to prevent starting listening when loading playlist and force 1 more check up.
     */
    @Override
    public void move_to_next() {
        if(loop_mode.equals(LoopMode.SINGLE)){
            current_audio=current_audio;
        }else{
            current_audio = queue.next();
            if(current_audio==null){
                if(playlist!=null){
                    current_audio=playlist.next();
                }
            }
        }
    }

    /**
     * This function has a similar behavior as move_to_next but it will stop if it's the last song.
     */
    @Override
    public void advance_to_next() {
        if(loop_mode.equals(LoopMode.SINGLE)){
            current_audio=current_audio;
        }else if(loop_mode.equals(LoopMode.ALL)){
            current_audio = queue.next();
            if(current_audio==null){
                if(playlist!=null)
                    current_audio=playlist.next();
            }
        }else{
            current_audio = queue.next();
            if(current_audio==null && !Playlist.is_last_audio(playlist)){
                current_audio=playlist.next();
            }
        }
    }

    @Override
    public void move_to_prev() {
        if(!loop_mode.equals(LoopMode.SINGLE))
            if(playlist!=null)
                current_audio = playlist.prev();
    }

    @Override
    public void clear_queue() {
        queue.clear();
    }
}
