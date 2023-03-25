package com.demeth.massaudioplayer.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.demeth.massaudioplayer.database.IdentifiedEntry;
import com.demeth.massaudioplayer.databinding.FragmentAudioEntryDisplayerBinding;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link RecyclerView.Adapter} that can display a {@link IdentifiedEntry}.
 */
public class AudioEntryRecyclerViewAdapter extends RecyclerView.Adapter<AudioEntryRecyclerViewAdapter.ViewHolder> {

    //TODO global uncheck all /check all

    private final List<IdentifiedEntry> mValues;
    private final Set<Integer> selected_values = new HashSet<>();

    @SuppressLint("NotifyDataSetChanged")
    public void select(IdentifiedEntry entry){
        if(!selected_values.contains(entry.getId())){
            selected_values.add(entry.getId());
            if(selected_values.size()==1) this.notifyDataSetChanged();
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    public void unselect(IdentifiedEntry entry){
        if(selected_values.contains(entry.getId())){
            selected_values.remove(entry.getId());

            if(selected_values.size()<=0) this.notifyDataSetChanged();
        }

    }

    public AudioEntryRecyclerViewAdapter(List<IdentifiedEntry> items) {
        mValues = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentAudioEntryDisplayerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setContent(mValues.get(position));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
        private final TextView title;
        private final CheckBox checkBox;
        private final ImageButton album;
        public IdentifiedEntry mItem;

        public ViewHolder(FragmentAudioEntryDisplayerBinding binding) {
            super(binding.getRoot());
            title = binding.title;
            checkBox = binding.checkBox;
            album=binding.album;

            binding.holderLayout.setOnLongClickListener(ViewHolder.this);
            binding.holderLayout.setOnClickListener(ViewHolder.this);

            checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                if(b) select(mItem);
                else unselect(mItem);
            });
        }

        public void setContent(IdentifiedEntry new_entry){
            mItem = new_entry;
            title.setText(mItem.getName());

            checkBox.setChecked(selected_values.contains(mItem.getId()));

            /*make a thrust table to understand this (shoud hide when no element and show when elements*/
            if((selected_values.size()>0) != (checkBox.getVisibility()==View.VISIBLE)){
                if(checkBox.getVisibility()==View.VISIBLE){
                    checkBox.setVisibility(View.GONE);
                    //TODO animate
                }else{
                    //TODO animate
                    checkBox.setVisibility(View.VISIBLE);
                }

            }
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + title.getText() + "'";
        }

        @Override
        public boolean onLongClick(View view) {
            select(mItem);
            return false;
        }

        @Override
        public void onClick(View view) {
            //TODO start the audio
        }
    }
}