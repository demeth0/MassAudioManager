package com.demeth.massaudioplayer.backend.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayer;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayerFactory;
import com.demeth.massaudioplayer.backend.models.adapters.AudioProvider;
import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;
import com.demeth.massaudioplayer.backend.models.objects.Event;
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap;
import com.demeth.massaudioplayer.backend.models.objects.Playlist;
import com.demeth.massaudioplayer.backend.models.objects.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        EventHandler handler=null;
        @Override
        public void trigger(Event event) {
            if(!Objects.isNull(this.handler)) this.handler.handle(event);
        }

        @Override
        public void registerHandler(String ID, EventHandler handler) {
            this.handler = handler;
        }

        @Override
        public void removeHandler(String ID) {
            handler = null;
        }
    }

    ApplicationAudioManager manager;
    StubAudioPlayerFactory player_factory;
    StubEventManager event_manager;

    List<Audio> test_data;
    AudioProvider audio_provider;
    @Before
    public void setUp() {
        player_factory = new StubAudioPlayerFactory();
        event_manager = new StubEventManager();
        audio_provider=new SmartAudioProvider();
        manager = new ApplicationAudioManager(player_factory, event_manager,audio_provider);
        test_data = new ArrayList<>();
        for(int i = 0; i<100 ; i++){
            test_data.add(new Audio("bbb "+i,"aaa "+i,AudioType.LOCAL));
        }
    }



    @Test
    public void play_audio(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.move_to_next();
        manager.play();
        assertEquals(test_data.get(0),player_factory.dummy_player.active_audio);
    }

    @Test
    public void pause_and_resume(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.move_to_next();
        manager.play();  // play
        manager.pause(); //pause
        manager.play();  //resume
        assertEquals(1,player_factory.dummy_player.pause_calls);
        assertEquals(1,player_factory.dummy_player.resume_calls);
    }

    @Test
    public void stop(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.move_to_next();
        manager.play();  // play
        event_manager.trigger(new Event(EventCodeMap.EVENT_AUDIO_COMPLETED));
        assertEquals(test_data.get(1),player_factory.dummy_player.active_audio);
    }

    @Test
    public void pause_status(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.move_to_next();
        assertTrue(manager.isPaused());
        manager.play();  // play
        assertFalse(manager.isPaused());
        manager.pause();
        assertTrue(manager.isPaused());
        audio_provider.set_audio_from_playlist(test_data.size()-1);
        manager.play();
        event_manager.trigger(new Event(EventCodeMap.EVENT_AUDIO_COMPLETED));
        assertTrue(manager.isPaused());
    }

    @Test
    public void play_variable_position(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(5);
        manager.play();  // play
        assertEquals(test_data.get(5),player_factory.dummy_player.active_audio);
    }

    @Test
    public void play_multiple(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(5);
        manager.play();  // play

        player_factory.dummy_player.progress_set = 0.5d;
        player_factory.dummy_player.duration_set = 10;
        audio_provider.set_audio_from_playlist(5);
        manager.play();
        assertEquals(0.0d,player_factory.dummy_player.progress_set,0.02d);

        player_factory.dummy_player.progress_set = 0.5d;
        manager.play();
        assertEquals(0.0d,player_factory.dummy_player.progress_set,0.02d);
    }

    @Test
    public void play_previous(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(5);
        manager.play();  // play

        player_factory.dummy_player.progress_set = 0.0d;
        player_factory.dummy_player.duration_set = 1;


        manager.play_previous();
        assertEquals(test_data.get(4),player_factory.dummy_player.active_audio);
    }

    @Test
    public void play_previous_rewind(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(5);
        manager.play();  // play

        player_factory.dummy_player.progress_set = 5;
        player_factory.dummy_player.duration_set = 10;


        manager.play_previous();
        assertEquals(test_data.get(5),player_factory.dummy_player.active_audio);
        assertEquals(0.0d,player_factory.dummy_player.progress_set,0.05d);
    }

    @Test
    public void play_next(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(5);
        manager.play();  // play

        player_factory.dummy_player.progress_set = 0.0d;
        player_factory.dummy_player.duration_set = 1;


        manager.play_next();
        assertEquals(test_data.get(6),player_factory.dummy_player.active_audio);
    }

    @Test
    public void play_next_end(){
        audio_provider.set_playlist(new Playlist(test_data));

        audio_provider.set_audio_from_playlist(test_data.size()-1);
        manager.play();  // play

        player_factory.dummy_player.progress_set = 0.0d;
        player_factory.dummy_player.duration_set = 1;

        event_manager.trigger(new Event(EventCodeMap.EVENT_AUDIO_COMPLETED));

        assertNull(audio_provider.get_audio());
    }

    @Test
    public void play_previous_zero(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.move_to_next();
        manager.play();  // play

        player_factory.dummy_player.progress_set = 0.0d;
        player_factory.dummy_player.duration_set = 1;


        manager.play_previous();
        assertEquals(test_data.get(99),player_factory.dummy_player.active_audio);
    }


    @Test
    public void get_timestamp(){
        Timestamp time = manager.timestamp();
        assertEquals(0.0d,time.getProgress(),0.02d);
        assertEquals(0L,time.getDuration());
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.move_to_next();
        time = manager.timestamp();
        assertEquals(0.0d,time.getProgress(),0.02d);
        assertEquals(10L,time.getDuration());
    }

    @Test
    public void play_next_from_queue_playlist_empty(){
        audio_provider.add_to_queue(test_data.get(0));
        manager.play_next();
        manager.play_next();
        assertNull(audio_provider.get_audio());
        manager.play_next();
        assertNull(audio_provider.get_audio());
    }

    @Test
    public void play_prev_from_queue_playlist_empty(){
        audio_provider.add_to_queue(test_data.get(0));
        manager.play_previous();
        manager.play_previous();
        assertNull(audio_provider.get_audio());
        manager.play_previous();
        assertNull(audio_provider.get_audio());
    }
}