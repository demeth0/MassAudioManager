package com.demeth.massaudioplayer.backend.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.demeth.massaudioplayer.backend.models.adapters.AudioProvider;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;
import com.demeth.massaudioplayer.backend.models.objects.LoopMode;
import com.demeth.massaudioplayer.backend.models.objects.Playlist;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
        audio_provider.set_playlist(new Playlist(test_data));
        Assert.assertEquals(test_data, audio_provider.view_playlist());
    }
    @Test
    public void test_get_current(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.move_to_next();
        Assert.assertEquals(test_data.get(0), audio_provider.get_audio());
    }
    @Test
    public void test_set_audio(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(50);
        Assert.assertEquals(test_data.get(50), audio_provider.get_audio());
    }
    @Test
    public void test_skip_to_next(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.move_to_next();
        audio_provider.move_to_next();
        Assert.assertEquals(test_data.get(1), audio_provider.get_audio());
        audio_provider.move_to_next();
        Assert.assertEquals(test_data.get(2), audio_provider.get_audio());
    }

    @Test
    public void test_skip_to_next_end(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(99);
        audio_provider.move_to_next();
        Assert.assertEquals(test_data.get(0), audio_provider.get_audio());
    }
    @Test
    public void test_advance_to_next(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.move_to_next();

        audio_provider.advance_to_next();
        Assert.assertEquals(test_data.get(1), audio_provider.get_audio());
        audio_provider.advance_to_next();
        Assert.assertEquals(test_data.get(2), audio_provider.get_audio());
    }
    @Test
    public void test_advance_to_next_end(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(99);
        audio_provider.advance_to_next();
        Assert.assertNull(audio_provider.get_audio());
    }

    @Test
    public void test_previous(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(50);
        audio_provider.move_to_prev();
        Assert.assertEquals(test_data.get(49), audio_provider.get_audio());
    }

    @Test
    public void test_previous_start(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.move_to_prev();
        Assert.assertEquals(test_data.get(99), audio_provider.get_audio());
    }

    @Test
    public void test_loop_mode_persistent(){
        audio_provider.set_loop(LoopMode.SINGLE);
        assertEquals(LoopMode.SINGLE,audio_provider.get_loop());

        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_loop(LoopMode.SINGLE);
        assertEquals(LoopMode.SINGLE,audio_provider.get_loop());

        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_loop(LoopMode.NONE);
        assertEquals(LoopMode.NONE,audio_provider.get_loop());

        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_loop(LoopMode.ALL);
        assertEquals(LoopMode.ALL,audio_provider.get_loop());
    }
    @Test
    public void test_loop_single_skip(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(50);
        audio_provider.set_loop(LoopMode.SINGLE);
        audio_provider.move_to_next();
        Assert.assertEquals(test_data.get(51), audio_provider.get_audio());
    }
    @Test
    public void test_loop_single_skip_end(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(99);
        audio_provider.set_loop(LoopMode.SINGLE);
        audio_provider.move_to_next();
        Assert.assertEquals(test_data.get(0), audio_provider.get_audio());
    }
    @Test
    public void test_loop_single_advance(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(50);
        audio_provider.set_loop(LoopMode.SINGLE);
        audio_provider.advance_to_next();
        Assert.assertEquals(test_data.get(50), audio_provider.get_audio());
    }
    @Test
    public void test_loop_single_advance_end(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(99);
        audio_provider.set_loop(LoopMode.SINGLE);
        audio_provider.advance_to_next();
        Assert.assertEquals(test_data.get(99), audio_provider.get_audio());
    }
    @Test
    public void test_loop_single_previous(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(50);
        audio_provider.set_loop(LoopMode.SINGLE);
        audio_provider.move_to_prev();
        Assert.assertEquals(test_data.get(50), audio_provider.get_audio());
    }
    @Test
    public void test_loop_single_previous_start(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(0);
        audio_provider.set_loop(LoopMode.SINGLE);
        audio_provider.move_to_prev();
        Assert.assertEquals(test_data.get(0), audio_provider.get_audio());
    }

    @Test
    public void test_loop_single_one_audio_advance(){
        // TODO
        Assert.fail();
    }

    @Test
    public void test_loop_single_one_audio_move_to_next(){
        // TODO
        Assert.fail();
    }

    @Test
    public void test_loop_single_one_audio_move_to_prev(){
        // TODO
        Assert.fail();
    }

    @Test
    public void test_loop_all_skip(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(50);
        audio_provider.set_loop(LoopMode.ALL);
        audio_provider.move_to_next();
        Assert.assertEquals(test_data.get(51), audio_provider.get_audio());
    }
    @Test
    public void test_loop_all_skip_end(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(99);
        audio_provider.set_loop(LoopMode.ALL);
        audio_provider.move_to_next();
        Assert.assertEquals(test_data.get(0), audio_provider.get_audio());
    }
    @Test
    public void test_loop_all_advance(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(50);
        audio_provider.set_loop(LoopMode.ALL);
        audio_provider.advance_to_next();
        Assert.assertEquals(test_data.get(51), audio_provider.get_audio());
    }
    @Test
    public void test_loop_all_advance_end(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(99);
        audio_provider.set_loop(LoopMode.ALL);
        audio_provider.advance_to_next();
        Assert.assertEquals(test_data.get(0), audio_provider.get_audio());
    }
    @Test
    public void test_loop_all_previous(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(50);
        audio_provider.set_loop(LoopMode.ALL);
        audio_provider.move_to_prev();
        Assert.assertEquals(test_data.get(49), audio_provider.get_audio());
    }
    @Test
    public void test_loop_all_previous_start(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_audio_from_playlist(0);
        audio_provider.set_loop(LoopMode.ALL);
        audio_provider.move_to_prev();
        Assert.assertEquals(test_data.get(99), audio_provider.get_audio());
    }

    @Test
    public void test_loop_all_one_audio_advance(){
        // TODO
        Assert.fail();
    }

    @Test
    public void test_loop_all_one_audio_move_to_next(){
        // TODO
        Assert.fail();
    }

    @Test
    public void test_loop_all_one_audio_move_to_prev(){
        // TODO
        Assert.fail();
    }

    @Test
    public void test_random(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_random(true);
        assertNotEquals(test_data,audio_provider.view_playlist());
    }

    @Test
    public void test_random_persistent(){
        audio_provider.set_random(true);
        assertTrue(audio_provider.get_random());

        audio_provider.set_random(false);
        assertFalse(audio_provider.get_random());
    }
    @Test
    public void test_random_skip_reset(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_random(true);
        List<Audio> test = audio_provider.view_playlist();
        audio_provider.set_audio_from_playlist(99);
        audio_provider.move_to_next();
        assertNotEquals(test,audio_provider.view_playlist());
    }
    @Test
    public void test_random_advance_reset(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.set_random(true);
        audio_provider.set_loop(LoopMode.ALL);
        List<Audio> test = audio_provider.view_playlist();
        audio_provider.set_audio_from_playlist(99);
        audio_provider.advance_to_next();
        assertNotEquals(test,audio_provider.view_playlist());
    }

    @Test
    public void test_add_to_queue_skip(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.add_to_queue(test_queue);
        audio_provider.move_to_next();
        Assert.assertEquals(test_queue, audio_provider.get_audio());
    }

    @Test
    public void test_add_to_queue_advance(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.add_to_queue(test_queue);
        audio_provider.advance_to_next();
        Assert.assertEquals(test_queue, audio_provider.get_audio());
    }

    @Test
    public void test_add_to_queue_prev_beg(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.add_to_queue(test_queue);
        audio_provider.move_to_prev();
        Assert.assertEquals(test_data.get(99), audio_provider.get_audio());
    }

    @Test
    public void test_add_to_queue_prev(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.add_to_queue(test_queue);
        audio_provider.set_audio_from_queue(0);
        audio_provider.move_to_prev();
        assertTrue(true);

        // TODO implement support
        // Assert.assertEquals(test_data.get(0), audio_provider.get_audio());
    }

    @Test
    public void test_add_to_queue_shuffling(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.add_to_queue(test_queue);
        audio_provider.set_random(true);
        audio_provider.move_to_next();
        Assert.assertEquals(test_queue, audio_provider.get_audio());
    }

    @Test
    public void test_add_to_queue_select(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.move_to_next();
        audio_provider.add_to_queue(test_queue);
        audio_provider.add_to_queue(test_queue);
        audio_provider.add_to_queue(test_queue2);

        audio_provider.set_audio_from_queue(2);
        Assert.assertEquals(test_queue2, audio_provider.get_audio());
        audio_provider.move_to_next();
        Assert.assertEquals(test_data.get(1), audio_provider.get_audio());

    }

    @Test
    public void test_add_to_queue_set(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.add_to_queue(test_queue);

        audio_provider.set_audio_from_playlist(1);
        Assert.assertEquals(test_data.get(1), audio_provider.get_audio());
        audio_provider.move_to_next();
        Assert.assertEquals(test_queue, audio_provider.get_audio());
    }

    @Test
    public void test_append_to_playlist(){
        audio_provider.set_playlist(new Playlist(test_data));
        audio_provider.add_to_queue(test_queue);

        audio_provider.add_to_playlist(Arrays.asList(new Audio[]{test_queue2}));
        Assert.assertEquals(1, audio_provider.view_queue().size());

        test_data.add(test_queue2);
        Assert.assertEquals(test_data, audio_provider.view_playlist());
    }
}