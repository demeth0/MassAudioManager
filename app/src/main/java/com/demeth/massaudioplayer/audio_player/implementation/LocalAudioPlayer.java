package com.demeth.massaudioplayer.audio_player.implementation;

import android.media.AudioAttributes;
import android.media.MediaPlayer;

import com.demeth.massaudioplayer.audio_player.AbstractAudioPlayer;
import com.demeth.massaudioplayer.audio_player.PlayersManager;
import com.demeth.massaudioplayer.database.DataType;
import com.demeth.massaudioplayer.database.IdentifiedEntry;
import com.demeth.massaudioplayer.database.data_structures.LocalEntry;

import java.io.IOException;

public class LocalAudioPlayer extends AbstractAudioPlayer<LocalEntry> {
    private enum State{
        UNINITIALIZED,
        LOADED, //the audio is loaded but not playing
        PLAYING, //the audio is currently playing
        PAUSED, //the audio was paused
        COMPLETED, //the audio is finished
        EMPTY //there is no audio in the player
    }

    private MediaPlayer mp;
    private State state=State.UNINITIALIZED;

    public LocalAudioPlayer(PlayersManager manager) {
        super(manager, DataType.LOCAL);
    }

    @Override
    public boolean open() {
        if(state.equals(State.UNINITIALIZED)){
            mp = new MediaPlayer();
            mp.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) //music player
                            .setUsage(AudioAttributes.USAGE_MEDIA) //on media
                            .build()
            );

            mp.setOnCompletionListener((_mp)->{
                manager.onCompleted();
                state=State.COMPLETED;
            });
            mp.setOnPreparedListener((_mp)->{
                //démarre la diffusion
                mp.start();
                state=State.PLAYING;
                manager.onPlay();
            });
            //signale que le media player est prét
            state=State.EMPTY;
        }
        return true;
    }

    @Override
    public void close() {
        if(!state.equals(State.UNINITIALIZED)){
            state=State.UNINITIALIZED;
            mp.release();
        }
    }

    @Override
    public void load(IdentifiedEntry audio) {
        super.load(audio);
        if(audio==null) state = State.EMPTY;
        else state=State.LOADED;
    }

    @Override
    public boolean play() {
        if(state.equals(State.UNINITIALIZED)) return false;//error if not initialised

        boolean success=true;
        if(state.equals(State.PAUSED)){
            mp.start();
            state=State.PLAYING;
        }else if(audio!=null){
            try {
                state = State.LOADED;
                mp.reset();
                mp.setDataSource(manager.getContext(), audio.getPath());
                mp.prepare();
            } catch (IOException e) {success=false; e.printStackTrace(); close();}
        }
        return success;
    }

    @Override
    public boolean pause() {
        if(state.equals(State.PLAYING)){
            mp.pause();
            state=State.PAUSED;
            manager.onPause();
        }
        return state.equals(State.PAUSED);
    }

    @Override
    public boolean resume() {
        boolean res=play();
        manager.onResume();
        return res;
    }

    @Override
    public boolean seekTo(int pos) {
        boolean ret=!state.equals(State.UNINITIALIZED);
        if(ret){
            mp.seekTo(pos);

        }
        return ret;
    }

    @Override
    public int getDuration() {
        return state.equals(State.UNINITIALIZED)?0:mp.getDuration();
    }

    @Override
    public int getPosition() {
        return state.equals(State.UNINITIALIZED) || state.equals(State.LOADED)? 0 : mp.getCurrentPosition();
    }
}
