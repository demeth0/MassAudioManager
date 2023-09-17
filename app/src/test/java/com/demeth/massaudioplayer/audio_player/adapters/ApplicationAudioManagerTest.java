package com.demeth.massaudioplayer.audio_player.adapters;

import static org.junit.Assert.*;

import com.demeth.massaudioplayer.backend.adapters.ApplicationAudioManager;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayer;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayerFactory;
import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;
import com.demeth.massaudioplayer.backend.models.objects.Event;
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap;
import com.demeth.massaudioplayer.backend.models.objects.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ApplicationAudioManagerTest {


    static class StubAudioPlayer implements AudioPlayer{
        public Audio active_audio;
        public int pause_calls=0,
                   resume_calls=0,
                   stop_calls=0;
        public double progress_set = 0;

        public int duration_set = 10;

        @Override
        public void play(Audio audio) {
            active_audio=audio;
            progress_set=0;
        }

        @Override
        public void pause() {
            pause_calls++;
        }

        @Override
        public void resume() {
            resume_calls++;
        }

        @Override
        public void set_progress(double progress) {
            progress_set=progress;
        }

        @Override
        public double progress() {
            return progress_set;
        }

        @Override
        public int duration() {
            return duration_set;
        }

        @Override
        public void stop() {
            stop_calls++;
        }
    }

    static class StubAudioPlayerFactory implements AudioPlayerFactory{
        final StubAudioPlayer dummy_player = new StubAudioPlayer();
        @Override
        public AudioPlayer provide(AudioType type) throws PlayerNotImplementedException {
            if(type == AudioType.SPOTIFY)
                throw new PlayerNotImplementedException("Spotify player not implemented");
            return dummy_player;
        }

        @Override
        public void register(AudioType type, AudioPlayer player) {}
    }

    static class StubEventManager implements EventManager{
        EventHandler handler;
        @Override
        public void trigger(Event event) {
            this.handler.handle(event);
        }

        @Override
        public void registerHandler(EventHandler handler) {
            this.handler = handler;
        }
    }

    ApplicationAudioManager manager;
    StubAudioPlayerFactory player_factory;
    StubEventManager event_manager;

    List<Audio> test_data;

    @Before
    public void setUp() {
        player_factory = new StubAudioPlayerFactory();
        event_manager = new StubEventManager();
        manager = new ApplicationAudioManager(player_factory, event_manager);
        test_data = new ArrayList<>();
        for(int i = 0; i<100 ; i++){
            test_data.add(new Audio("bbb "+i,"aaa "+i,AudioType.LOCAL));
        }
    }

    @Test
    public void set_playlist(){
        manager.set(test_data);
        assertEquals(test_data, manager.get());
    }

    @Test
    public void play_audio(){
        manager.set(test_data);
        manager.play();
        assertEquals(test_data.get(0),player_factory.dummy_player.active_audio);
    }

    @Test
    public void pause_and_resume(){
        manager.set(test_data);
        manager.play();  // play
        manager.pause(); //pause
        manager.play();  //resume
        assertEquals(1,player_factory.dummy_player.pause_calls);
        assertEquals(1,player_factory.dummy_player.resume_calls);
    }

    @Test
    public void stop(){
        manager.set(test_data);
        manager.play();  // play
        event_manager.trigger(new Event(EventCodeMap.EVENT_AUDIO_COMPLETED));
        assertEquals(test_data.get(1),player_factory.dummy_player.active_audio);
    }

    @Test
    public void pause_status(){
        manager.set(test_data);
        assertTrue(manager.isPaused());
        manager.play();  // play
        assertFalse(manager.isPaused());
        manager.pause();
        assertTrue(manager.isPaused());

        manager.play(test_data.size()-1);
        event_manager.trigger(new Event(EventCodeMap.EVENT_AUDIO_COMPLETED));
        assertTrue(manager.isPaused());
    }

    @Test
    public void play_variable_position(){
        manager.set(test_data);
        manager.play(5);  // play
        assertEquals(test_data.get(5),player_factory.dummy_player.active_audio);
    }

    @Test
    public void play_multiple(){
        manager.set(test_data);
        manager.play(5);  // play

        player_factory.dummy_player.progress_set = 0.5d;
        player_factory.dummy_player.duration_set = 10;

        manager.play(5);
        assertEquals(0.0d,player_factory.dummy_player.progress_set,0.02d);

        player_factory.dummy_player.progress_set = 0.5d;
        manager.play();
        assertEquals(0.0d,player_factory.dummy_player.progress_set,0.02d);
    }

    @Test
    public void play_previous(){
        manager.set(test_data);
        manager.play(5);  // play

        player_factory.dummy_player.progress_set = 0.0d;
        player_factory.dummy_player.duration_set = 1;


        manager.play_previous();
        assertEquals(test_data.get(4),player_factory.dummy_player.active_audio);
    }

    @Test
    public void play_previous_rewind(){
        manager.set(test_data);
        manager.play(5);  // play

        player_factory.dummy_player.progress_set = 0.5d;
        player_factory.dummy_player.duration_set = 10;


        manager.play_previous();
        assertEquals(test_data.get(5),player_factory.dummy_player.active_audio);
        assertEquals(0.0d,player_factory.dummy_player.progress_set,0.05d);
    }

    @Test
    public void play_next(){
        manager.set(test_data);
        manager.play(5);  // play

        player_factory.dummy_player.progress_set = 0.0d;
        player_factory.dummy_player.duration_set = 1;


        manager.play_next();
        assertEquals(test_data.get(6),player_factory.dummy_player.active_audio);
    }

    @Test
    public void play_next_end(){
        manager.set(test_data);
        manager.play(test_data.size()-1);  // play

        player_factory.dummy_player.progress_set = 0.0d;
        player_factory.dummy_player.duration_set = 1;


        manager.play_next();
        assertEquals(test_data.get(0),player_factory.dummy_player.active_audio);
    }

    @Test
    public void play_previous_zero(){
        manager.set(test_data);
        manager.play(0);  // play

        player_factory.dummy_player.progress_set = 0.0d;
        player_factory.dummy_player.duration_set = 1;


        manager.play_previous();
        assertEquals(test_data.get(0),player_factory.dummy_player.active_audio);
    }

    @Test
    public void shuffle_mode(){
        List<Audio> copy_data = new ArrayList<>(test_data);
        manager.set(copy_data);
        manager.shuffle(true);
        assertNotEquals(test_data,manager.get());
    }

    @Test
    public void shuffle_mode_after(){
        List<Audio> copy_data = new ArrayList<>(test_data);
        manager.shuffle(true);
        manager.set(copy_data);
        assertNotEquals(test_data,manager.get());
    }

    @Test
    public void shuffle_mode_reset(){
        List<Audio> copy_data = new ArrayList<>(test_data);
        manager.shuffle(true);
        manager.set(copy_data);

        List<Audio> copy_data_2 = new ArrayList<>(manager.get());

        manager.play(copy_data.size()-1);
        manager.play_next();

        assertNotEquals(copy_data_2,manager.get());
    }

    // TODO sort for disabling shuffle mode not yet implemented

    @Test
    public void loop_none_playlist_end(){
        manager.set(test_data);
        manager.play(test_data.size()-1);
        event_manager.trigger(new Event(EventCodeMap.EVENT_AUDIO_COMPLETED));
        assertEquals(test_data.get(test_data.size()-1),player_factory.dummy_player.active_audio);
        manager.play();
        assertEquals(test_data.get(0),player_factory.dummy_player.active_audio);
        assertEquals(ApplicationAudioManager.LOOP_NONE,manager.getLoopMode());
    }

    @Test
    public void loop_all_playlist_end(){
        manager.set(test_data);
        manager.loop(ApplicationAudioManager.LOOP_ALL);
        manager.play(test_data.size()-1);
        event_manager.trigger(new Event(EventCodeMap.EVENT_AUDIO_COMPLETED));

        assertEquals(test_data.get(0),player_factory.dummy_player.active_audio);
        assertEquals(ApplicationAudioManager.LOOP_ALL,manager.getLoopMode());
    }

    @Test
    public void loop_single_play_end(){
        manager.set(test_data);
        manager.loop(ApplicationAudioManager.LOOP_SINGLE);
        manager.play(5);
        event_manager.trigger(new Event(EventCodeMap.EVENT_AUDIO_COMPLETED));
        assertEquals(test_data.get(5),player_factory.dummy_player.active_audio);
        assertEquals(ApplicationAudioManager.LOOP_SINGLE,manager.getLoopMode());
    }

    @Test
    public void loop_single_skip(){
        manager.set(test_data);
        manager.loop(ApplicationAudioManager.LOOP_SINGLE);
        manager.play(5);
        manager.play_next();
        assertEquals(test_data.get(5),player_factory.dummy_player.active_audio);
    }

    @Test
    public void loop_single_previous_rewind(){
        manager.set(test_data);
        manager.loop(ApplicationAudioManager.LOOP_SINGLE);

        manager.play(5);
        player_factory.dummy_player.progress_set = 0.15d; // more than 4 seconds
        player_factory.dummy_player.duration_set = 40;

        manager.play_previous();
        assertEquals(test_data.get(5),player_factory.dummy_player.active_audio);
        assertEquals(0d,player_factory.dummy_player.progress_set,0.02);
    }

    @Test
    public void loop_single_previous(){
        manager.set(test_data);
        manager.loop(ApplicationAudioManager.LOOP_SINGLE);

        manager.play(5);
        player_factory.dummy_player.progress_set = 0.05d; // more than 4 seconds
        player_factory.dummy_player.duration_set = 40;


        manager.play_previous();
        assertEquals(test_data.get(5),player_factory.dummy_player.active_audio);
        assertEquals(0d,player_factory.dummy_player.progress_set,0.02);
    }

    @Test
    public void get_timestamp(){
        Timestamp time = manager.timestamp();
        assertEquals(0.0d,time.getProgress(),0.02d);
        assertEquals(0L,time.getDuration());
        manager.set(test_data);
        time = manager.timestamp();
        assertEquals(0.0d,time.getProgress(),0.02d);
        assertEquals(10L,time.getDuration());

    }
}