package com.demeth.massaudioplayer.backend.models.objects;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A Playlist is a complex object that stock an ordered list of {@link Audio} and offer access and management to this list.<br><br>
 *
 * After creation the playlist index is undefined, any attempt to get an audio from the playlist will throw a {@link ArrayIndexOutOfBoundsException}.
 * To init to the first index, a first call to {@link Playlist#next} should be made before any attempt of {@link Playlist#get}
 */
public class Playlist {

    private List<Audio> audios;
    private List<Integer> order;
    private int current=-1;
    private boolean random = false;

    /**
     * Create a playlist based of an ordered ArrayList of audio track.<br><br>
     * Note: The current implementation copy the playlist passed in argument and is unaffected by change of the list passed in argument.
     * <br>
     * @param audios The list of audio to base the playlist of.
     * @see Playlist
     */
    public Playlist(List<Audio> audios){
        this.audios = new ArrayList<>(audios);
        order = new ArrayList<>();
        for(int i=0;i<audios.size();i++){
            order.add(i);
        }
    }

    /**
     * Create a playlist based of finite table of audio tracks.<br>
     * @param audios Finite table of audios.
     * @see Playlist
     */
    public Playlist(Audio ...audios){
        this(Arrays.asList(audios));
    }

    /**
     * Load the next track from the playlist. In case the current track is the last, the next track will be reset to track at index 0.
     * In case the random option is enabled the playlist will be re-shuffled before loading the next track.
     * @return The new track loaded after increasing the cursor.
     * @see #random(boolean)
     */
    @Nullable
    public Audio next(){
        current=(current+1)%order.size();
        if(current==0 && random){
            shuffle_list();
        }
        return get();
    }

    /**
     * Move the audio cursor in the playlist to the previous audio. If the current audio is the first(0) then loop back to the last entry of the playlist.
     * @return The previous audio in the playlist queue.
     */
    @Nullable
    public Audio prev(){
        if(current==-1) current = order.size()-1;
        else            current = (current-1+order.size())%order.size();
        return get();
    }

    /**
     * Set the current cursor position in the queue to the index of the audio passed in parameter. This method may raise an exception if the audio is not contained in the playlist.
     * @param audio The audio that we want to set as the curent audio selected in the playlist.
     * @see #set(int)
     */
    public void set(Audio audio){
        current = audios.indexOf(audio);
    }

    /**
     * Set the current audio selected in the playlist as the audio at the absolute position passed as parameter.
     * @param index The index of the audio that we want to set as current.
     * @see #set(Audio)
     */
    public void set(int index){
        if(index<audios.size())
            current = index;
    }

    /**
     * This method can be used to detect if we got to the end of the playlist.
     * @return If the current audio is the last in the playlist.
     * @see #is_last_audio(Playlist)
     */
    public boolean is_last_audio(){
        return current == order.size()-1;
    }

    /**
     * Static version of the method that check if the current audio is the last.
     * @param p The playlist from which we check if the current audio is the last.
     * @return If the current audio is the last in the playlist.
     * @see #is_last_audio()
     */
    public static boolean is_last_audio(Playlist p){
        if(p==null) return true;
        return p.is_last_audio();
    }

    /**
     * The playlist stock the last index in the audio queue. This method return the current selected audio.
     * @return Get the audio currently selected.
     */
    @Nullable
    public Audio get(){
        if(current>order.size()) return null;
        return audios.get(order.get(current));
    }

    /**
     * This method return the index value of the selected audio. In case the random mode is applied the current index may not refer to the
     * index of the music selected in the shuffled queue but the index of the queue in order before randomization.
     * @return Index of selected audio.
     */
    public int index(){
        return current;
    }

    /**
     * Enable a shuffling system to mix the play order of the musics in the playlist. If the random mode is enabled then every time the playlist is
     * looped again to the beginning, the play order will be lost and reshuffled.
     * @param mode True for enabling the shuffling mode and false for ordered.
     */
    public void random(boolean mode){
        this.random=mode;
        if(mode){
            int cur_audio = 0;
            if(current!=-1) cur_audio = order.remove(current);
            shuffle_list();
            if(current!=-1) order.add(0,cur_audio);
        }else{
            order_list();
        }
    }

    private void shuffle_list(){
        Collections.shuffle(order);
    }
    private void order_list(){
        Collections.sort(order);
    }

    /**
     * @return If the current playlist is in shuffle mode.
     * @see #random(boolean)
     */
    public boolean is_random(){
        return random;
    }

    /**
     * Add a list of audio to the current playlist. The new audio added will be placed at the end of the playlist. In case the shuffle mode is enabled,
     * The new added audio will not be shuffled in the playlist so you will need to use {@link #random} again.
     * @param extension The list of audio to add to the playlist.
     */
    public void extend(List<Audio> extension){
        for(int i=0;i<extension.size();i++){ //resize audio order list to match audio
            order.add(i+audios.size());
        }
        audios.addAll(extension);
    }

    /**
     * The playlist audios will be ordered depending on the random mode and will also be placed in a new list.
     * @return The list of the audio of the playlist.
     */
    public List<Audio> view(){
        List<Audio> audio_list = new ArrayList<>(audios.size());
        order.forEach(i -> audio_list.add(audios.get(i)));
        return audio_list;
    }
}
