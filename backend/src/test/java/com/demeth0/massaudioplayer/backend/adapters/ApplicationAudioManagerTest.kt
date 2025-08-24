package com.demeth0.massaudioplayer.backend.adapters;

import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import io.mockk.every
import io.mockk.confirmVerified
import org.junit.After

import com.demeth0.massaudioplayer.backend.models.adapters.AudioPlayer;
import com.demeth0.massaudioplayer.backend.models.adapters.AudioPlayerFactory;
import com.demeth0.massaudioplayer.backend.models.adapters.AudioProvider;
import com.demeth0.massaudioplayer.backend.models.adapters.EventHandler;
import com.demeth0.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth0.massaudioplayer.backend.models.adapters.PlayerNotImplementedException;
import com.demeth0.massaudioplayer.backend.models.objects.Audio;
import com.demeth0.massaudioplayer.backend.models.objects.AudioType;
import com.demeth0.massaudioplayer.backend.models.objects.Event;
import com.demeth0.massaudioplayer.backend.models.objects.EventCodeMap;
import com.demeth0.massaudioplayer.backend.models.objects.LoopMode
import com.demeth0.massaudioplayer.backend.models.objects.Playlist;
import com.demeth0.massaudioplayer.backend.models.objects.Timestamp;
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import kotlin.math.abs

class ApplicationAudioManagerTest {
    /*
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
        public void setProgress(double progress) {
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
    }*/

    @MockK
    lateinit var player_factory: AudioPlayerFactory

    @MockK
    lateinit var audio_player: AudioPlayer

    lateinit var test_data: MutableList<Audio>
    lateinit var event_manager: EventManager
    lateinit var audio_provider: AudioProvider
    lateinit var manager: ApplicationAudioManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
//        player_factory = new StubAudioPlayerFactory();
        event_manager = SequentialEventManager()
//        audio_provider=new SmartAudioProvider();
        audio_provider = IndependentAudioProvider()
        manager = ApplicationAudioManager(player_factory, event_manager, audio_provider)
        test_data = mutableListOf()
        for (i in 0..100) {
            test_data.add(Audio("bbb $i", "aaa $i", AudioType.LOCAL))
        }

        every { player_factory.provide(any()) } returns audio_player
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun play_audio() {
        audio_provider.setPlaylist(Playlist(test_data))
        audio_provider.moveToNext()
        manager.play()
        verify { player_factory.provide(any()) }
        verify { audio_player.play(test_data.get(0)) }
        confirmVerified(audio_player)
        confirmVerified(player_factory)
    }

    @Test
    fun pause_and_resume() {
        audio_provider.setPlaylist(Playlist(test_data))
        audio_provider.moveToNext()
        manager.play()
        verify { audio_player.play(any()) }
        manager.pause()
        verify { audio_player.pause() }
        manager.play()
        verify { audio_player.resume() }
        confirmVerified(audio_player)
    }


    @Test
    fun stop() {
        audio_provider.setPlaylist(Playlist(test_data));
        audio_provider.moveToNext();
        manager.play();
        verify { audio_player.play(test_data[0]) }
        event_manager.trigger(Event(EventCodeMap.EVENT_AUDIO_COMPLETED));
        verify { audio_player.play(test_data[1]) }
    }

    @Test
    fun pause_status() {
        audio_provider.setPlaylist(Playlist(test_data))
        audio_provider.moveToNext()
        assert(manager.isPaused())
        manager.play()
        assert(!manager.isPaused())
        manager.pause()
        assert(manager.isPaused())
        audio_provider.setAudioFromPlaylist(test_data.size - 1)
        manager.play()
        event_manager.trigger(Event(EventCodeMap.EVENT_AUDIO_COMPLETED))
        assert(manager.isPaused())
    }

    @Test
    fun play_variable_position() {
        audio_provider.setPlaylist(Playlist(test_data))
        audio_provider.setAudioFromPlaylist(5)
        manager.play()
        verify { audio_player.play(test_data[5]) }
    }

    @Test
    fun play_multiple() {
        audio_provider.setPlaylist(Playlist(test_data))
        audio_provider.setAudioFromPlaylist(5)
        manager.play()
        verify { audio_player.play(test_data[5]) }

        every { audio_player.progress() } returns 5000
        every { audio_player.duration() } returns 10000

        audio_provider.setAudioFromPlaylist(5)
        manager.play()
        verify { audio_player.play(test_data[5]) }

        every { audio_player.progress() } returns 6000
        manager.play()
        verify { audio_player.play(test_data[5]) }
    }

    @Test
    fun playPrevious() {
        audio_provider.setPlaylist(Playlist(test_data))
        audio_provider.setAudioFromPlaylist(5)
        manager.play()

        every { audio_player.progress() } returns 0
        every { audio_player.duration() } returns 10000

        manager.playPrevious()
        verify { audio_player.play(test_data[4]) }
    }

    @Test
    fun play_previous_rewind() {
        audio_provider.setPlaylist(Playlist(test_data))
        audio_provider.setAudioFromPlaylist(5)
        manager.play()

        every { audio_player.progress() } returns 5000
        every { audio_player.duration() } returns 10000

        manager.playPrevious()
        verify { audio_player.setProgress(match { abs(it) <= 0.02 }) }
        verify { audio_player.play(test_data[5]) }
    }

    @Test
    fun playNext() {
        audio_provider.setPlaylist(Playlist(test_data))
        audio_provider.setAudioFromPlaylist(5)
        manager.play()

        every { audio_player.progress() } returns 0
        every { audio_player.duration() } returns 1000

        manager.playNext();
        verify { audio_player.play(test_data[6]) }
    }

    @Test
    fun play_next_end() {
        audio_provider.setPlaylist(Playlist(test_data))

        audio_provider.setAudioFromPlaylist(test_data.size - 1)
        manager.play()

        every { audio_player.progress() } returns 1000
        every { audio_player.duration() } returns 1000

        event_manager.trigger(Event(EventCodeMap.EVENT_AUDIO_COMPLETED))

        Assert.assertNull(audio_provider.getAudio())
    }

    @Test
    fun play_previous_zero_loop_all() {
        audio_provider.setLoop(LoopMode.ALL)
        audio_provider.setPlaylist(Playlist(test_data))
        audio_provider.moveToNext()
        manager.play()

        every { audio_player.progress() } returns 0
        every { audio_player.duration() } returns 1000

        manager.playPrevious()
        verify { audio_player.play(test_data[100]) }
    }

    @Test
    fun play_previous_zero_loop_none() {
        audio_provider.setLoop(LoopMode.NONE)
        audio_provider.setPlaylist(Playlist(test_data))
        audio_provider.moveToNext()
        manager.play()

        every { audio_player.progress() } returns 0
        every { audio_player.duration() } returns 1000

        manager.playPrevious()
        verify { audio_player.play(test_data[0]) }
    }

    @Test
    fun get_timestamp() {
        var time = manager.timestamp()

        Assert.assertEquals(0.0, time.progress, 0.02)
        Assert.assertEquals(0, time.duration)
        audio_provider.setPlaylist(Playlist(test_data))
        audio_provider.moveToNext()
        every { audio_player.progress() } returns 0
        every { audio_player.duration() } returns 1000
        time = manager.timestamp()
        verify { audio_player.progress() }
        verify { audio_player.duration() }
        Assert.assertEquals(0.0, time.progress, 0.02)
        Assert.assertEquals(1000, time.duration)
    }

    @Test
    fun play_next_from_queue_playlist_empty() {
        audio_provider.addToQueue(test_data[0])
        manager.playNext()
        manager.playNext()
        Assert.assertNull(audio_provider.getAudio());
        manager.playNext();
        Assert.assertNull(audio_provider.getAudio());
    }

    @Test
    fun play_prev_from_queue_playlist_empty() {
        audio_provider.addToQueue(test_data.get(0))
        manager.playPrevious()
        manager.playPrevious()
        Assert.assertNull(audio_provider.getAudio())
        manager.playPrevious()
        Assert.assertNull(audio_provider.getAudio())
    }
}