package com.demeth.massaudioplayer.backend.adapters;

import static org.junit.Assert.*;

import com.demeth.massaudioplayer.backend.adapters.LoadedAudioPlayerFactory;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayer;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayerFactory;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;

import org.junit.Before;
import org.junit.Test;

public class LoadedAudioPlayerFactoryTest {
    private LoadedAudioPlayerFactory factory;

    static final class StubAudioPlayer implements AudioPlayer{

        @Override
        public void play(Audio audio) {

        }

        @Override
        public void pause() {

        }

        @Override
        public void resume() {

        }

        @Override
        public void set_progress(double progress) {

        }

        @Override
        public double progress() {
            return 0;
        }

        @Override
        public int duration() {
            return 0;
        }

        @Override
        public void stop() {

        }
    }

    private final AudioPlayer dummy_player= new StubAudioPlayer();
    @Before
    public void setUp() throws Exception {
        factory = new LoadedAudioPlayerFactory();
        factory.register(AudioType.LOCAL,dummy_player);
    }

    @Test
    public void provide() throws Exception{
        assertEquals(dummy_player,factory.provide(AudioType.LOCAL));
    }

    @Test
    public void fail_provide() {
        assertThrows(AudioPlayerFactory.PlayerNotImplementedException.class,()->factory.provide(AudioType.SPOTIFY));
    }
}