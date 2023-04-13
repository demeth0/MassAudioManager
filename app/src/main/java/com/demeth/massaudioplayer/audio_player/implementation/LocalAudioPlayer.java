package com.demeth.massaudioplayer.audio_player.implementation;

import android.media.AudioAttributes;
import android.media.MediaPlayer;

import androidx.annotation.Nullable;

import com.demeth.massaudioplayer.audio_player.AbstractAudioPlayer;
import com.demeth.massaudioplayer.audio_player.PlayersManager;
import com.demeth.massaudioplayer.database.DataType;
import com.demeth.massaudioplayer.database.IdentifiedEntry;
import com.demeth.massaudioplayer.database.data_structure.LocalEntry;

import java.io.IOException;

/**
 * implement local file player using android MediaPlayer. Based on a State machine this player should never throw errors. This player is also not mean to be initialized outside the AudioPlayer class that manage this player's instanciations.
 *
 * @see com.demeth.massaudioplayer.audio_player.AudioPlayer
 */
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

    /**
     * initialize the player and bind it to a manager
     * @param manager the player manager
     */
    public LocalAudioPlayer(PlayersManager manager) {
        super(manager, DataType.LOCAL);
    }

    /**
     * reserve resources for the media player
     * @return the success of the operation
     */
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

    /**
     * free resources of the player
     */
    @Override
    public void close() {
        if(!state.equals(State.UNINITIALIZED)){
            state=State.UNINITIALIZED;
            mp.release();
        }
    }

    /**
     * load a track in this player
     * @param audio the audio to load
     */
    @Override
    public void load(@Nullable IdentifiedEntry audio) {
        super.load(audio);
        if(audio==null) state = State.EMPTY;
        else state=State.LOADED;
    }

    /**
     * play the loaded audio
     * @return the success of the operation
     */
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

    /**
     * pause the audio currently playing
     * @return the success of the operation
     */
    @Override
    public boolean pause() {
        if(state.equals(State.PLAYING)){
            mp.pause();
            state=State.PAUSED;
            manager.onPause();
        }
        return state.equals(State.PAUSED);
    }

    /**
     * resume the current audio if it was paused
     * @return the success of the operation
     */
    @Override
    public boolean resume() {
        boolean res=play();
        manager.onResume();
        return res;
    }

    /**
     * if an audio is loaded, change the current position in the track play
     * @param pos the position in milliseconds
     * @return the success of the operation
     */
    @Override
    public boolean seekTo(int pos) {
        boolean ret=!state.equals(State.UNINITIALIZED);
        if(ret){
            mp.seekTo(pos);

        }
        return ret;
    }

    /**
     * @return the current position in the track or 0 if unable to obtain
     */
    @Override
    public int getDuration() {
        return state.equals(State.UNINITIALIZED)?0:mp.getDuration();
    }

    /**
     * @return the total duration of the track or 0 if unable to obtain
     */
    @Override
    public int getPosition() {
        return state.equals(State.UNINITIALIZED) || state.equals(State.LOADED)? 0 : mp.getCurrentPosition();
    }
}
