package com.demeth.massaudioplayer.ui;

import android.annotation.SuppressLint;

import androidx.recyclerview.widget.RecyclerView;

import com.demeth.massaudioplayer.database.IdentifiedEntry;

import java.util.HashSet;
import java.util.Set;

public class SelectionManager {

    public SelectionManager(){

    }

    private final Set<Integer> selected_values = new HashSet<>();

    @SuppressLint("NotifyDataSetChanged")
    public void select(IdentifiedEntry entry, RecyclerView.Adapter<?> list_adapter){
        if(!selected_values.contains(entry.getId())){
            selected_values.add(entry.getId());
            if(selected_values.size()==1) list_adapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void unselect(IdentifiedEntry entry, RecyclerView.Adapter<?> list_adapter){
        if(selected_values.contains(entry.getId())){
            selected_values.remove(entry.getId());

            if(selected_values.size()<=0) list_adapter.notifyDataSetChanged();
        }
    }

    public boolean contains(int u){
        return selected_values.contains(u);
    }

    public int size(){
        return selected_values.size();
    }
}
