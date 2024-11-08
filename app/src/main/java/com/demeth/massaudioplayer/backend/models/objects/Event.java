package com.demeth.massaudioplayer.backend.models.objects;

import androidx.annotation.Nullable;

/**
 * The event class represent a bundle that can be passed through the {@link com.demeth.massaudioplayer.backend.models.adapters.EventManager EventManager} when an event
 * is triggered. It is used to identify the type of event triggered and to pass data that could be relevant for the corresponding handlers.
 */
public class Event {
    private int code=-1;
    private Object data=null;

    /**
     * Create an event with an identification code and a data bundle.
     * @param code The unique identifier used to designate the event to process.
     * @param data The data relevant to the event.
     */
    public Event(int code, Object data){
        this(code);
        this.data=data;
    }

    /**
     * Create an event with an identification code and an empty data bundle.
     * @param code The unique identifier used to designate the event to process.
     */
    public Event(int code){
        this.code = code;
    }

    /**
     * @return The code that identify the event triggered.
     */
    public int getCode() {
        return code;
    }

    /**
     * @return The data bundle related to the triggered event.
     */
    public @Nullable Object getData() {
        return data;
    }
}
