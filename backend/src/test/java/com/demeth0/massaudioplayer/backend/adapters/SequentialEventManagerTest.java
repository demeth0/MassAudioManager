package com.demeth0.massaudioplayer.backend.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.demeth0.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth0.massaudioplayer.backend.models.objects.Event;
import com.demeth0.massaudioplayer.backend.models.objects.EventCodeMap;

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

    private final static EventCodeMap code = EventCodeMap.EVENT_AUDIO_COMPLETED;

    @Test
    public void register_handler() {

        manager.registerHandler("test",(event)->{
            if(event.getCode()== code)
                triggered1=true;
        });
        manager.trigger(new Event(code));
        assertTrue(triggered1);
    }

    @Test
    public void register_multiple_handler_same_event() {
        manager.registerHandler("test1",(event)->{
            if(event.getCode()==code)
                triggered1=true;
        });
        manager.registerHandler("test2",(event)->{
            if(event.getCode()==code)
                triggered2=true;
        });
        manager.trigger(new Event(code));
        assertTrue(triggered1);
        assertTrue(triggered2);
    }

    @Test
    public void register_multiple_handler() {
        manager.registerHandler("test",(event)->{
            if(event.getCode()==code)
                triggered1=true;
        });
        manager.registerHandler("test",(event)->{
            if(event.getCode()==EventCodeMap.EVENT_AUDIO_PAUSED)
                triggered2=true;
        });
        manager.trigger(new Event(EventCodeMap.EVENT_AUDIO_PAUSED));
        assertFalse(triggered1);
        assertTrue(triggered2);
    }

    @Test
    public void register_handler_pass_data() {
        final Object data = new Object();
        manager.registerHandler("test",(event)->{
            if(event.getCode()==code){
                triggered1=true;
                assertEquals(data, event.getData());
            }
        });
        manager.trigger(new Event(code,data));
        assertTrue(triggered1);
    }

    @Test
    public void remove_handler_pass_data() {
        triggered2=false;
        manager.registerHandler("test",(event)->{
            if(event.getCode()==code){
                triggered1=true;
                triggered2=!triggered2;
            }
        });
        manager.trigger(new Event(code,null));
        assertTrue(triggered1);

        manager.removeHandler(("test"));
        assertTrue(triggered2);

        manager.removeHandler("no handler");
    }

    @Test
    public void remove_unknown_handler_pass_data() {
        try{
            manager.removeHandler("no handler");
        }catch(Exception e){
            fail();
        }

    }

}