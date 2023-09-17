package com.demeth.massaudioplayer.backend.adapters;

import com.demeth.massaudioplayer.backend.models.adapters.AudioManager;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayer;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayerFactory;
import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap;
import com.demeth.massaudioplayer.backend.models.objects.Timestamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implement the version of the audio manager for Android application.
 */
public class ApplicationAudioManager implements AudioManager {
    private final static int PAUSED=1,PLAYING=0,INACTIVE=3;


    // constants
    public static final int LOOP_NONE = 0;
    public static final int LOOP_SINGLE = 1;
    public static final int LOOP_ALL = 2;


    // dependencies
    private final AudioPlayerFactory audio_players_factory;

    // fields
    private List<Audio> play_list=new ArrayList<>();
    private int cursor=0;

    private int play_status=INACTIVE;

    private boolean shuffle_mode=false;
    private int loop_mode=LOOP_NONE;

    /**
     * Create a audio manager specific for this project implementation.
     * @param player_factory The provider that will give correct adapters to read the audio entries.
     * @param event_manager The event manager to react to audio player's events.
     */
    public ApplicationAudioManager(AudioPlayerFactory player_factory, EventManager event_manager){
        this.audio_players_factory=player_factory;
        event_manager.registerHandler((event)->{
            // handle events
            if(event.getCode()== EventCodeMap.EVENT_AUDIO_COMPLETED){
                load_next();
                boolean should_stop = cursor==0 && loop_mode==LOOP_NONE;
                if(should_stop){
                    play_status=INACTIVE;
                }else{
                    play();
                }
            }
        });
    }
    @Override
    public void play_previous(){
        Timestamp stamp = this.timestamp();
        if(stamp.getDuration()*stamp.getProgress()>4){
            setTimestampProgress(0d);
        }else if(loop_mode!=LOOP_SINGLE && cursor>0){
            cursor--;
        }
        play();
    }

    /**
     * Compute the next cursor value depending on loop value, shuffle mode, ect...
     */
    private void load_next(){
        if(loop_mode!=LOOP_SINGLE)
            cursor++;
        if(cursor>=play_list.size()) {
            cursor = 0;
            if(this.isShuffled())
                this.shuffle(true); // If the list is shuffled then reshuffle the list.

        }
    }

    @Override
    public void play_next(){
        load_next();
        play();
    }

    /**
     * @return The audio player compatible with the current audio file. TODO Or crash the app for now.
     */
    private AudioPlayer get_audio_player(){
        try {
            Audio audio = play_list.get(this.cursor);
            return this.audio_players_factory.provide(audio.type);
        } catch (AudioPlayerFactory.PlayerNotImplementedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Timestamp timestamp() {
        if(this.cursor>=this.play_list.size())
            return new Timestamp(0,0); // Never nester principle

        AudioPlayer audio_player = get_audio_player();
        return new Timestamp(audio_player.duration(),audio_player.progress());
    }

    @Override
    public void setTimestampProgress(double progress) {
        if(this.cursor>=this.play_list.size())
            return;

        AudioPlayer audio_player = get_audio_player();
        audio_player.set_progress(progress);
    }

    @Override
    public void play() {
        play(cursor);
    }

    @Override
    public void play(int index) {
        if(index>=this.play_list.size())
            return; // Never nester principle
        this.cursor=index;

        Audio audio = play_list.get(this.cursor);
        AudioPlayer audio_player = get_audio_player();
        if(play_status==PAUSED){
            audio_player.resume();
        }else{
            audio_player.play(audio);
        }
        play_status=PLAYING; // Event handler will change this value in case of exceptions
    }

    @Override
    public void pause() {
        if(this.cursor>=this.play_list.size())
            return;
        if(play_status!=PLAYING)
            return;

        AudioPlayer audio_player = get_audio_player();
        audio_player.pause();
        play_status=PAUSED;
    }

    @Override
    public boolean isPaused() {
        return this.play_status!=PLAYING;
    }

    @Override
    public List<Audio> get() {
        return play_list;
    }

    @Override
    public Audio current() {
        return play_list.get(cursor);
    }

    @Override
    public void set(List<Audio> list) {
        pause(); //Pause before switching audio and prevent changing audio player
        play_status=INACTIVE;

        play_list=list;
        cursor=0;
        this.shuffle(shuffle_mode); // apply shuffle mode to new list
    }

    @Override
    public void shuffle(boolean mode) {
        this.shuffle_mode=mode;
        if(this.cursor>=this.play_list.size())
            return; // Never nester principle

        if(shuffle_mode){
            Audio cur = this.play_list.remove(this.cursor);
            Collections.shuffle(this.play_list);
            this.play_list.add(0,cur);
        }else{
            Collections.sort(this.play_list);
        }
    }

    @Override
    public boolean isShuffled() {
        return this.shuffle_mode;
    }

    @Override
    public void loop(int mode) {
        this.loop_mode=mode;
    }

    @Override
    public int getLoopMode() {
        return this.loop_mode;
    }
}
