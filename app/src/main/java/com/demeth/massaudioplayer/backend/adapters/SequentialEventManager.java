package com.demeth.massaudioplayer.backend.adapters;

import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.Event;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class SequentialEventManager implements EventManager {
    private LinkedHashMap<String,EventHandler> handlers;

    public SequentialEventManager(){
        handlers = new LinkedHashMap<>();
    }

    @Override
    public void trigger(Event event) {
        handlers.entrySet().forEach(h->h.getValue().handle(event));
    }

    @Override
    public void registerHandler(String ID, EventHandler handler) {
        handlers.put(ID,handler);
    }
}
