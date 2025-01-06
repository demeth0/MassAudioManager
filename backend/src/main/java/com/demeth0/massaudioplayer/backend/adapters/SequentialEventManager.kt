package com.demeth.massaudioplayer.backend.adapters

import com.demeth.massaudioplayer.backend.models.adapters.EventHandler
import com.demeth.massaudioplayer.backend.models.adapters.EventManager
import com.demeth.massaudioplayer.backend.models.objects.Event

import java.util.concurrent.ConcurrentHashMap

class SequentialEventManager : EventManager {
    private val handlers: ConcurrentHashMap<String, EventHandler> = ConcurrentHashMap()

    override fun trigger(event: Event) {
        handlers.forEach{ (_, value) ->
            value.handle(event)
        }
    }

    override fun registerHandler(id: String, handler: EventHandler) {
        handlers[id] = handler
    }

    override fun removeHandler(id: String){
        handlers.remove(id)
    }

}
