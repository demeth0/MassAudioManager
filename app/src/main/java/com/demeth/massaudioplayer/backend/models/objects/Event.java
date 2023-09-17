package com.demeth.massaudioplayer.backend.models.objects;

public class Event {
    private int code=-1;
    private Object data=null;

    public Event(int code, Object data){
        this(code);
        this.data=data;
    }

    public Event(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }
}
