package com.demeth.massaudioplayer.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.backend.AlbumLoader;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;
import com.demeth.massaudioplayer.databinding.FragmentAudioEntryDisplayerBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.demeth.massaudioplayer.backend.models.objects.Audio}.
 */
public class AudioEntryRecyclerViewAdapter extends RecyclerView.Adapter<AudioEntryRecyclerViewAdapter.ViewHolder> {

    @FunctionalInterface
    public interface OnItemClicked {
        void onClicked(Audio entry, int index);
    }

    private OnItemClicked onItemClicked;

    private final List<Audio> mValues;

    public List<Audio> getValues() {
        return mValues;
    }

    private Audio highlighted_entry=null;

    @SuppressLint("NotifyDataSetChanged")
    public void setContent(Collection<? extends Audio> mValues){
        this.mValues.clear();
        this.mValues.addAll(mValues);
        notifyDataSetChanged();
    }

    public void setHighlighted(Audio entry) {
        Audio temp = highlighted_entry;
        highlighted_entry = entry;
        if(temp!=null)notifyItemChanged(mValues.indexOf(temp));
        notifyItemChanged(mValues.indexOf(highlighted_entry));
    }

    public AudioEntryRecyclerViewAdapter() {
        mValues = new ArrayList<>();
    }

    public void setOnItemClicked(OnItemClicked onItemClicked) {
        this.onItemClicked = onItemClicked;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentAudioEntryDisplayerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setContent(mValues.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener,Runnable {
        private final TextView title;
        private final CheckBox checkBox;
        private final ImageButton album;
        public Audio mItem;
        public int item_index=-1;

        public ViewHolder(FragmentAudioEntryDisplayerBinding binding) {
            super(binding.getRoot());
            title = binding.title;
            checkBox = binding.checkBox;
            album=binding.album;

            binding.holderLayout.setOnLongClickListener(ViewHolder.this);
            binding.holderLayout.setOnClickListener(ViewHolder.this);

            checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                if(b) MainActivity.selection_manager.select(mItem,AudioEntryRecyclerViewAdapter.this);
                else MainActivity.selection_manager.unselect(mItem,AudioEntryRecyclerViewAdapter.this);
            });
        }

        public void setContent(Audio new_entry, int index){
            mItem = new_entry;
            item_index=index;

            //text box
            title.setText(mItem.display_name);
            if(new_entry.equals(highlighted_entry)) title.setTextColor(title.getContext().getColor(R.color.foreground)); //TODO add shadow
            else title.setTextColor(title.getContext().getColor(R.color.white));

            //album cover
            album.setImageBitmap(AlbumLoader.getDefaultCover());
            this.itemView.removeCallbacks(this);
            this.itemView.postDelayed(this,200);

            // check box
            checkBox.setChecked(MainActivity.selection_manager.contains(mItem));
            /*make a thrust table to understand this (shoud hide when no element and show when elements*/
            if(mItem.type.equals(AudioType.PLAYLIST)){
                checkBox.setVisibility(View.GONE);
            }else if((MainActivity.selection_manager.size()>0) != (checkBox.getVisibility()==View.VISIBLE)){
                if(checkBox.getVisibility()==View.VISIBLE){
                    checkBox.setVisibility(View.GONE);
                }else{
                    //TODO animate
                    checkBox.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void run() {
            if(mItem != null) AlbumLoader.getAlbumImage(itemView,  this.mItem,Math.max(48,album.getWidth()), album::setImageBitmap);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + title.getText() + "'";
        }

        @Override
        public boolean onLongClick(View view) {
            MainActivity.selection_manager.select(mItem,AudioEntryRecyclerViewAdapter.this);
            return false;
        }

        @Override
        public void onClick(View view) {
            onItemClicked.onClicked(mItem,item_index);
        }
    }
}