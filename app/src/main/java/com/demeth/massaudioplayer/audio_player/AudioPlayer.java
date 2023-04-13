package com.demeth.massaudioplayer.audio_player;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.demeth.massaudioplayer.database.AudioLibrary;
import com.demeth.massaudioplayer.database.IdentifiedEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * the AudioPlayer can control a set of tracks from differents sources such as Local source but also in the future source like Spotify or Youtube.
 * This class implement a state machine that follow the track management and error evasion. the set of track can be controlled with two functionalities,
 * a loop mode to control the chain of track playing and a random mode to control the order.
 */
public class AudioPlayer implements Playable{
    /**
     * three loop mode are supported NONE the list of track pause after the queue is read fully.
     * SINGLE, the current track will be repeated indefinitely ignoring the queue.
     * ALL when all track are read then start again and mix again the order if in random mode.
     */
    public enum LoopMode{
        NONE,
        SINGLE,
        ALL
    }

    /**
     * The global states of the player. LOADED when the player is ready to call play(). PLAYING when an audio is currently playing.
     * PAUSED when the current audio was interrupted and waiting for resume.
     * COMPLETED when the player finished playing the queue and is waiting for next action.
     * EMPTY when the player is empty and no track are loaded inside it.
     */
    public enum State{
        LOADED, //the audio is loaded but not playing
        PLAYING, //the audio is currently playing
        PAUSED, //the audio was paused
        COMPLETED, //the audio is finished
        EMPTY //there is no audio in the player
    }

    /**
     * listener class that will be called when the following actions are completed.
     */
    public interface AudioPlayerListener{
        /**
         * called after calling play when the audio is started
         * @param player the player self reference
         */
        void onPlay(AudioPlayer player);

        /**
         * called after the current audio was paused
         * @param player the player self reference
         */
        void onPause(AudioPlayer player);
        void onCompleted(AudioPlayer player);

        /**
         * called after the queue was edited
         * @param player the player self reference
         */
        void onPlaylistChanged(AudioPlayer player);

        /**
         * called when a previously paused audio is resumed
         * @param player the player self reference
         */
        void onResume(AudioPlayer player);

        /**
         * called when the loop mode is changed
         * @param player the player self reference
         */
        void onLoopModeChanged(AudioPlayer player);

        /**
         * called when the random mode is changed
         * @param player the player self reference
         */
        void onRandomModeChanged(AudioPlayer player);

    }

    private AudioPlayerListener audioPlayerListener = new AudioPlayerListener(){
        @Override public void onPlay(AudioPlayer player) {}
        @Override public void onPause(AudioPlayer player) {}
        @Override public void onCompleted(AudioPlayer player) {}
        @Override public void onPlaylistChanged(AudioPlayer player) {}
        @Override public void onResume(AudioPlayer player) { }
        @Override public void onLoopModeChanged(AudioPlayer player) {}
        @Override public void onRandomModeChanged(AudioPlayer player) {}
    };

    private final ArrayList<Integer> play_order = new ArrayList<>();
    private final ArrayList<IdentifiedEntry> playlist = new ArrayList<>();
    private int audio_index=0;

    private boolean random=false;
    private State state=State.EMPTY;
    private LoopMode loop_mode=LoopMode.NONE;

    private final PlayersManager manager;

    /**
     * build the audio player based on the context given. It is advised to use a service Context.
     * @param context hte context to wich the audio player is bound
     */
    public AudioPlayer(Context context){
        this.manager= new PlayersManager(context) {
            @Override
            public void onPlay() {
                state=State.PLAYING;
                audioPlayerListener.onPlay(AudioPlayer.this);
            }

            @Override
            public void onCompleted() {
                Log.d("AudioPlayer","music completed");
                audioPlayerListener.onCompleted(AudioPlayer.this);
                switch(loop_mode){
                    case SINGLE:
                        play();
                        break;
                    case ALL:
                        next();
                        break;
                    case NONE:
                        if(audio_index==play_order.size()-1)
                            state=State.COMPLETED;
                        else
                            next();
                        break;
                }
            }

            @Override
            public void onPause() {
                state=State.PAUSED;
                audioPlayerListener.onPause(AudioPlayer.this);
            }

            @Override
            public void onResume() {
                state=State.PLAYING;
                audioPlayerListener.onResume(AudioPlayer.this);
            }
        };
    }

    //region listeners

    /**
     *
     * @param listener set the event listener of the player. Only one listener is referenced.
     */
    public void setListener(AudioPlayerListener listener){
        audioPlayerListener = listener;
    }

    //endregion

    /**
     * close the player and stop all AudioPlayer implementation, should free all resources
     */
    public void close(){
        //pause(); //cause reopenning the server
        manager.close();
    }

    //audio management

    /**
     * set the queue to play and pause the current audio if any.
     * @param playlist the new queue
     */
    public void setPlaylist(Collection<? extends IdentifiedEntry> playlist){
        _clearPlaylist();

        this.playlist.addAll(playlist);
        for(int i=0;i<this.playlist.size();i++){
            this.play_order.add(i);
        }

        if(random) Collections.shuffle(play_order);
        state=State.LOADED;

        audioPlayerListener.onPlaylistChanged(AudioPlayer.this);
    }

    /**
     * add to the current queue the following tracks
     * @param playlist the tracks to append
     * @param after_current if we want to append after first song or at the end of the queue
     */
    public void appendToPlaylist(Collection<? extends IdentifiedEntry> playlist, boolean after_current){
        if(after_current){
            int i=0;
            if(this.play_order.size()<1){ //if list empty
                this.play_order.add(0);
            }
            for(;i<playlist.size();i++){
                this.play_order.add(1,this.playlist.size()+i);
            }
        }else{
            for(int i=0;i<playlist.size();i++){
                this.play_order.add(this.playlist.size()+i);
            }
        }

        this.playlist.addAll(playlist);
        audioPlayerListener.onPlaylistChanged(AudioPlayer.this);
    }

    /**
     * change the current audio playing to another in the queue
     * @param s the audio to set as the current
     */
    public void moveToAudio(IdentifiedEntry s){
        audio_index = play_order.indexOf(playlist.indexOf(s));
    }

    /**
     * empty the track queue and stop the current audio playing
     */
    public void clearPlaylist(){
        _clearPlaylist();
        audioPlayerListener.onPlaylistChanged(AudioPlayer.this);
    }

    private void _clearPlaylist(){
        this.playlist.clear();
        this.play_order.clear();
        this.pause();
        audio_index=0;
        state=State.EMPTY;
    }

    /**
     * playorder argument is only useful for when random is enabled. It will return the queue in the
     * shuffled order. If you don't want the shuffled order or if it is notrelevant this parameter should
     * be set to false for better performances.
     * @param playorder if we want to return the queue in the play order or saved order
     * @return the queue
     */
    public Collection<IdentifiedEntry> getPlaylist(boolean playorder){
        if(playorder){
            ArrayList<IdentifiedEntry> p2=new ArrayList<>();
            for(int i : play_order){
                p2.add(playlist.get(i));
            }
            return p2;
        }else{
            return playlist;
        }
    }

    /**
     * automatically use the shuffled queue if random mode enabled
     * @param index the position in the queue
     * @return the audio at the given index
     */
    public @Nullable IdentifiedEntry getAudio(int index){
        return playlist.get(play_order.get(index));
    }

    /**
     * @return the current audio to be played or playing, in case of error return null
     */
    public IdentifiedEntry getCurrentAudio(){
        if(audio_index>=play_order.size()) return null;
        return playlist.get(play_order.get(audio_index));
    }

    /**
     * play the current audio, or resume the audio paused. If the audio is already playing reset to the beginning.
     * @return success of the operation
     */
    //region playable interface
    public boolean play(){
        boolean ret=true;
        if(!state.equals(State.EMPTY) && !state.equals(State.PAUSED)){
            IdentifiedEntry a = getAudio(audio_index);
            Log.d("AudioPlayer","loading "+(a!=null?a.hashCode():0));
            manager.load(a);
            //state changed in listener
            manager.play();
        }else{
            ret=!resume();
        }
        return ret;
    }

    /**
     * @return resume a paused audio. you can also call play it redirect to resume if the audio is paused
     */
    public boolean resume(){
        boolean res = false;
        if(state.equals(State.PAUSED)){
            res=manager.resume();
        }
        return res;
    }

    /**
     *
     * @param pos set the time position in millis in the current audio
     * @return success of the operation
     */
    @Override
    public boolean seekTo(int pos) {
        return manager.seekTo(pos);
    }

    /**
     * see specific implementations for return in case of error
     * @return the total duration of the audio
     */
    @Override
    public int getDuration() {
        return manager.getDuration();
    }

    /**
     * see specific implementations for return in case of error
     * @return the current position of the audio playing in millis
     */
    @Override
    public int getPosition() {
        return manager.getPosition();
    }

    /**
     * pause the currently playing audio
     * @return success of the operation
     */
    public boolean pause(){
        boolean ret=false;
        if(state.equals(State.PLAYING)){
            ret=manager.pause();
            //state changed in listener
        }
        return ret;
    }

    /**
     * go to next audio in the queue, bypass LoopMode.SINGLE? if at the end of the queue will shuffle again in random mode.
     * @return success of the operation
     */
    public boolean next(){
        if(!loop_mode.equals(LoopMode.SINGLE)){
            audio_index=(audio_index+1)%play_order.size();
            if(audio_index==0) Collections.shuffle(play_order); //reshuffle for better random
            Log.d("AudioPlayer","index : "+audio_index);
        }

        return play();
    }

    /**
     * go to previous audio in the queue, bypass LoopMode.SINGLE. result may be random if first entry in the queue and in random mode.
     * @return success of the operation
     */
    public boolean previous(){
        boolean ret;
        if((state.equals(State.PLAYING) || state.equals(State.PAUSED)) && getPosition()>3000){
            ret = seekTo(0);
        }else{
            audio_index=(audio_index-1);
            if(audio_index<0)audio_index=play_order.size()-1;
            ret = play();
        }
        return ret;
    }

    /**
     * @param rand if the queue play order should be shuffled
     */
    public void setRandom(boolean rand){
        if(rand != this.random){
            if(rand && !state.equals(State.EMPTY)) {
                Log.d("Audioplayer","Mixing playlist");
                int cur=play_order.get(audio_index);
                //to keep the current music as the first music
                Collections.shuffle(play_order);
                play_order.set(audio_index,play_order.get(0));
                play_order.set(0,cur);
                audioPlayerListener.onPlaylistChanged(this);
            }
            else{
                Collections.sort(play_order);
            }
        }

        this.random = rand;
        audioPlayerListener.onRandomModeChanged(this);
    }

    /**
     * @return if the queue play order is shuffled
     */
    public boolean isRandom() {
        return random;
    }

    /**
     * @see LoopMode
     * @param mode the loop mode
     */
    public void setLoop(LoopMode mode){
        loop_mode=mode;
        audioPlayerListener.onLoopModeChanged(this);
    }

    /**
     * @see LoopMode
     * @return the current loop mode
     */
    public LoopMode getLoopMode() {
        return loop_mode;
    }

    /**
     * @see State
     * @return the state of the player
     */
    public State getState() {
        return state;
    }

    //endregion
}
