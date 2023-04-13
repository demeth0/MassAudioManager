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

public class AudioPlayer implements Playable{
    public enum LoopMode{
        NONE,
        SINGLE,
        ALL
    }

    public enum State{
        LOADED, //the audio is loaded but not playing
        PLAYING, //the audio is currently playing
        PAUSED, //the audio was paused
        COMPLETED, //the audio is finished
        EMPTY //there is no audio in the player
    }

    public interface AudioPlayerListener{
        void onPlay(AudioPlayer player);
        void onPause(AudioPlayer player);
        void onCompleted(AudioPlayer player);
        void onPlaylistChanged(AudioPlayer player);
        void onResume(AudioPlayer player);

        void onLoopModeChanged(AudioPlayer player);
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
    public void setListener(AudioPlayerListener listener){
        audioPlayerListener = listener;
    }

    //endregion

    public void close(){
        //pause(); //cause reopenning the server
        manager.close();
    }

    //audio management
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

    public void moveToAudio(IdentifiedEntry s){
        audio_index = play_order.indexOf(playlist.indexOf(s));
    }

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

    public @Nullable IdentifiedEntry getAudio(int index){
        return playlist.get(play_order.get(index));
    }

    public IdentifiedEntry getCurrentAudio(){
        if(audio_index>=play_order.size()) return null;
        return playlist.get(play_order.get(audio_index));
    }

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

    public boolean resume(){
        boolean res = false;
        if(state.equals(State.PAUSED)){
            res=manager.resume();
        }
        return res;
    }

    @Override
    public boolean seekTo(int pos) {
        return manager.seekTo(pos);
    }

    @Override
    public int getDuration() {
        return manager.getDuration();
    }

    @Override
    public int getPosition() {
        return manager.getPosition();
    }

    public boolean pause(){
        boolean ret=false;
        if(state.equals(State.PLAYING)){
            ret=manager.pause();
            //state changed in listener
        }
        return ret;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean next(){
        if(!loop_mode.equals(LoopMode.SINGLE)){
            audio_index=(audio_index+1)%play_order.size();
            if(audio_index==0) Collections.shuffle(play_order); //reshuffle for better random
            Log.d("AudioPlayer","index : "+audio_index);
        }

        return play();
    }

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

    public boolean isRandom() {
        return random;
    }

    public void setLoop(LoopMode mode){
        loop_mode=mode;
        audioPlayerListener.onLoopModeChanged(this);
    }

    public LoopMode getLoopMode() {
        return loop_mode;
    }

    public State getState() {
        return state;
    }

    //endregion
}
