package com.demeth0.massaudioplayer.backend.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.demeth0.massaudioplayer.backend.models.objects.Audio;
import com.demeth0.massaudioplayer.backend.models.objects.AudioType;
import com.demeth0.massaudioplayer.backend.models.objects.LoopMode;
import com.demeth0.massaudioplayer.backend.models.objects.Playlist;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SmartAudioProviderTest {
    List<Audio> test_data;
    Audio test_queue = new Audio("quick quick","file://ok",AudioType.LOCAL);
    Audio test_queue2 = new Audio("quick quick 2","file://ok2",AudioType.LOCAL);

    SmartAudioProvider audio_provider;
    @Before
    public void setUp() {
        audio_provider = new SmartAudioProvider();
        test_data = new ArrayList<>();
        for(int i = 0; i<100 ; i++){
            test_data.add(new Audio("bbb "+i,"aaa "+i, AudioType.LOCAL));
        }
    }

    @Test
    public void test_set_data(){
        audio_provider.setPlaylist(new Playlist(test_data));
        Assert.assertEquals(test_data, audio_provider.viewPlaylist());
    }
    @Test
    public void test_get_current(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.moveToNext();
        Assert.assertEquals(test_data.get(0), audio_provider.getAudio());
    }
    @Test
    public void test_set_audio(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(50);
        Assert.assertEquals(test_data.get(50), audio_provider.getAudio());
    }
    @Test
    public void test_skip_to_next(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.moveToNext();
        audio_provider.moveToNext();
        Assert.assertEquals(test_data.get(1), audio_provider.getAudio());
        audio_provider.moveToNext();
        Assert.assertEquals(test_data.get(2), audio_provider.getAudio());
    }

    @Test
    public void test_skip_to_next_end(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(99);
        audio_provider.moveToNext();
        Assert.assertEquals(test_data.get(0), audio_provider.getAudio());
    }
    @Test
    public void test_advance_toNext(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.moveToNext();

        audio_provider.advanceToNext();
        Assert.assertEquals(test_data.get(1), audio_provider.getAudio());
        audio_provider.advanceToNext();
        Assert.assertEquals(test_data.get(2), audio_provider.getAudio());
    }
    @Test
    public void test_advance_toNext_end(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(99);
        audio_provider.advanceToNext();
        Assert.assertNull(audio_provider.getAudio());
    }

    @Test
    public void test_previous(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(50);
        audio_provider.moveToPrev();
        Assert.assertEquals(test_data.get(49), audio_provider.getAudio());
    }

    @Test
    public void test_previous_start(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.moveToPrev();
        Assert.assertEquals(test_data.get(99), audio_provider.getAudio());
    }

    @Test
    public void test_loop_mode_persistent(){
        audio_provider.setLoop(LoopMode.SINGLE);
        assertEquals(LoopMode.SINGLE,audio_provider.getLoop());

        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setLoop(LoopMode.SINGLE);
        assertEquals(LoopMode.SINGLE,audio_provider.getLoop());

        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setLoop(LoopMode.NONE);
        assertEquals(LoopMode.NONE,audio_provider.getLoop());

        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setLoop(LoopMode.ALL);
        assertEquals(LoopMode.ALL,audio_provider.getLoop());
    }
    @Test
    public void test_loop_single_skip(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(50);
        audio_provider.setLoop(LoopMode.SINGLE);
        audio_provider.moveToNext();
        Assert.assertEquals(test_data.get(51), audio_provider.getAudio());
    }
    @Test
    public void test_loop_single_skip_end(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(99);
        audio_provider.setLoop(LoopMode.SINGLE);
        audio_provider.moveToNext();
        Assert.assertEquals(test_data.get(0), audio_provider.getAudio());
    }
    @Test
    public void test_loop_single_advance(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(50);
        audio_provider.setLoop(LoopMode.SINGLE);
        audio_provider.advanceToNext();
        Assert.assertEquals(test_data.get(50), audio_provider.getAudio());
    }
    @Test
    public void test_loop_single_advance_end(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(99);
        audio_provider.setLoop(LoopMode.SINGLE);
        audio_provider.advanceToNext();
        Assert.assertEquals(test_data.get(99), audio_provider.getAudio());
    }
    @Test
    public void test_loop_single_previous(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(50);
        audio_provider.setLoop(LoopMode.SINGLE);
        audio_provider.moveToPrev();
        Assert.assertEquals(test_data.get(50), audio_provider.getAudio());
    }
    @Test
    public void test_loop_single_previous_start(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(0);
        audio_provider.setLoop(LoopMode.SINGLE);
        audio_provider.moveToPrev();
        Assert.assertEquals(test_data.get(0), audio_provider.getAudio());
    }

    @Test
    public void test_loop_single_one_audio_advance(){
        // TODO
        Assert.fail();
    }

    @Test
    public void test_loop_single_one_audio_move_toNext(){
        // TODO
        Assert.fail();
    }

    @Test
    public void test_loop_single_one_audio_move_toPrev(){
        // TODO
        Assert.fail();
    }

    @Test
    public void test_loop_all_skip(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(50);
        audio_provider.setLoop(LoopMode.ALL);
        audio_provider.moveToNext();
        Assert.assertEquals(test_data.get(51), audio_provider.getAudio());
    }
    @Test
    public void test_loop_all_skip_end(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(99);
        audio_provider.setLoop(LoopMode.ALL);
        audio_provider.moveToNext();
        Assert.assertEquals(test_data.get(0), audio_provider.getAudio());
    }
    @Test
    public void test_loop_all_advance(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(50);
        audio_provider.setLoop(LoopMode.ALL);
        audio_provider.advanceToNext();
        Assert.assertEquals(test_data.get(51), audio_provider.getAudio());
    }
    @Test
    public void test_loop_all_advance_end(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(99);
        audio_provider.setLoop(LoopMode.ALL);
        audio_provider.advanceToNext();
        Assert.assertEquals(test_data.get(0), audio_provider.getAudio());
    }
    @Test
    public void test_loop_all_previous(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(50);
        audio_provider.setLoop(LoopMode.ALL);
        audio_provider.moveToPrev();
        Assert.assertEquals(test_data.get(49), audio_provider.getAudio());
    }
    @Test
    public void test_loop_all_previous_start(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setAudioFromPlaylist(0);
        audio_provider.setLoop(LoopMode.ALL);
        audio_provider.moveToPrev();
        Assert.assertEquals(test_data.get(99), audio_provider.getAudio());
    }

    @Test
    public void test_loop_all_one_audio_advance(){
        // TODO
        Assert.fail();
    }

    @Test
    public void test_loop_all_one_audio_move_toNext(){
        // TODO
        Assert.fail();
    }

    @Test
    public void test_loop_all_one_audio_move_toPrev(){
        // TODO
        Assert.fail();
    }

    @Test
    public void test_random(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setRandom(true);
        assertNotEquals(test_data,audio_provider.viewPlaylist());
    }

    @Test
    public void test_random_persistent(){
        audio_provider.setRandom(true);
        assertTrue(audio_provider.getRandom());

        audio_provider.setRandom(false);
        assertFalse(audio_provider.getRandom());
    }
    @Test
    public void test_random_skip_reset(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setRandom(true);
        List<Audio> test = audio_provider.viewPlaylist();
        audio_provider.setAudioFromPlaylist(99);
        audio_provider.moveToNext();
        assertNotEquals(test,audio_provider.viewPlaylist());
    }
    @Test
    public void test_random_advance_reset(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.setRandom(true);
        audio_provider.setLoop(LoopMode.ALL);
        List<Audio> test = audio_provider.viewPlaylist();
        audio_provider.setAudioFromPlaylist(99);
        audio_provider.advanceToNext();
        assertNotEquals(test,audio_provider.viewPlaylist());
    }

    @Test
    public void test_add_toQueue_skip(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.addToQueue(test_queue);
        audio_provider.moveToNext();
        Assert.assertEquals(test_queue, audio_provider.getAudio());
    }

    @Test
    public void test_add_toQueue_advance(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.addToQueue(test_queue);
        audio_provider.advanceToNext();
        Assert.assertEquals(test_queue, audio_provider.getAudio());
    }

    @Test
    public void test_add_toQueue_prev_beg(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.addToQueue(test_queue);
        audio_provider.moveToPrev();
        Assert.assertEquals(test_data.get(99), audio_provider.getAudio());
    }

    @Test
    public void test_add_toQueue_prev(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.addToQueue(test_queue);
        audio_provider.setAudioFromQueue(0);
        audio_provider.moveToPrev();
        assertTrue(true);

        // TODO implement support
        // Assert.assertEquals(test_data.get(0), audio_provider.get_audio());
    }

    @Test
    public void test_add_toQueue_shuffling(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.addToQueue(test_queue);
        audio_provider.setRandom(true);
        audio_provider.moveToNext();
        Assert.assertEquals(test_queue, audio_provider.getAudio());
    }

    @Test
    public void test_add_toQueue_select(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.moveToNext();
        audio_provider.addToQueue(test_queue);
        audio_provider.addToQueue(test_queue);
        audio_provider.addToQueue(test_queue2);

        audio_provider.setAudioFromQueue(2);
        Assert.assertEquals(test_queue2, audio_provider.getAudio());
        audio_provider.moveToNext();
        Assert.assertEquals(test_data.get(1), audio_provider.getAudio());

    }

    @Test
    public void test_add_toQueue_set(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.addToQueue(test_queue);

        audio_provider.setAudioFromPlaylist(1);
        Assert.assertEquals(test_data.get(1), audio_provider.getAudio());
        audio_provider.moveToNext();
        Assert.assertEquals(test_queue, audio_provider.getAudio());
    }

    @Test
    public void test_append_to_playlist(){
        audio_provider.setPlaylist(new Playlist(test_data));
        audio_provider.addToQueue(test_queue);

        audio_provider.addToPlaylist(Collections.singletonList(test_queue2));
        Assert.assertEquals(1, audio_provider.viewQueue().size());

        test_data.add(test_queue2);
        Assert.assertEquals(test_data, audio_provider.viewPlaylist());
    }
}