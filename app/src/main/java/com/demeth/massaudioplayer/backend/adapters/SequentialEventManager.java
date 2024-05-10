package com.demeth.massaudioplayer.backend.adapters;

import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.Event;

import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SequentialEventManager implements EventManager {
    private final ConcurrentHashMap<String,EventHandler> handlers;

    public SequentialEventManager(){
        handlers = new ConcurrentHashMap<>();
    }

    @Override
    public void trigger(Event event) {
        handlers.forEach((key, value) -> value.handle(event));
    }

    @Override
    public void registerHandler(String ID, EventHandler handler) {
        handlers.put(ID,handler);
    }

    @Override
    public void removeHandler(String ID) {
        handlers.remove(ID);
    }
}
