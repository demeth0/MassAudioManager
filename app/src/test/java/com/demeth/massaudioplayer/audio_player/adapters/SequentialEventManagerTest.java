package com.demeth.massaudioplayer.audio_player.adapters;

import static org.junit.Assert.*;

import com.demeth.massaudioplayer.backend.adapters.SequentialEventManager;
import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.Event;

import org.junit.Before;
import org.junit.Test;

public class SequentialEventManagerTest {
    EventManager manager;
    boolean triggered1,triggered2;

    @Before
    public void setup(){
        manager = new SequentialEventManager();
        triggered1 = false;
        triggered2 = false;
    }

    @Test
    public void register_handler() {

        manager.registerHandler((event)->{
            if(event.getCode()==10)
                triggered1=true;
        });
        manager.trigger(new Event(10));
        assertTrue(triggered1);
    }

    @Test
    public void register_multiple_handler_same_event() {
        manager.registerHandler((event)->{
            if(event.getCode()==10)
                triggered1=true;
        });
        manager.registerHandler((event)->{
            if(event.getCode()==10)
                triggered2=true;
        });
        manager.trigger(new Event(10));
        assertTrue(triggered1);
        assertTrue(triggered2);
    }

    @Test
    public void register_multiple_handler() {
        manager.registerHandler((event)->{
            if(event.getCode()==10)
                triggered1=true;
        });
        manager.registerHandler((event)->{
            if(event.getCode()==15)
                triggered2=true;
        });
        manager.trigger(new Event(15));
        assertFalse(triggered1);
        assertTrue(triggered2);
    }

    @Test
    public void register_handler_pass_data() {
        final Object data = new Object();
        manager.registerHandler((event)->{
            if(event.getCode()==10){
                triggered1=true;
                assertEquals(data, event.getData());
            }
        });
        manager.trigger(new Event(10,data));
        assertTrue(triggered1);
    }
}