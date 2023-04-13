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
import com.demeth.massaudioplayer.database.AlbumLoader;
import com.demeth.massaudioplayer.database.DataType;
import com.demeth.massaudioplayer.database.IdentifiedEntry;
import com.demeth.massaudioplayer.databinding.FragmentAudioEntryDisplayerBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link IdentifiedEntry}.
 */
public class AudioEntryRecyclerViewAdapter extends RecyclerView.Adapter<AudioEntryRecyclerViewAdapter.ViewHolder> {

    @FunctionalInterface
    public interface OnItemClicked {
        void oClicked(IdentifiedEntry entry);
    }

    private OnItemClicked onItemClicked;

    private final List<IdentifiedEntry> mValues;

    public List<IdentifiedEntry> getValues() {
        return mValues;
    }

    private IdentifiedEntry highlighted_entry=null;

    @SuppressLint("NotifyDataSetChanged")
    public void setContent(Collection<? extends IdentifiedEntry> mValues){
        this.mValues.clear();
        this.mValues.addAll(mValues);
        notifyDataSetChanged();
    }

    public void setHighlighted(IdentifiedEntry entry) {
        IdentifiedEntry temp = highlighted_entry;
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
        holder.setContent(mValues.get(position));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener,Runnable {
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
                if(b) MainActivity.selection_manager.select(mItem,AudioEntryRecyclerViewAdapter.this);
                else MainActivity.selection_manager.unselect(mItem,AudioEntryRecyclerViewAdapter.this);
            });
        }

        public void setContent(IdentifiedEntry new_entry){
            mItem = new_entry;

            //text box
            title.setText(mItem.getName());
            if(new_entry.equals(highlighted_entry)) title.setTextColor(title.getContext().getColor(R.color.foreground)); //TODO add shadow
            else title.setTextColor(title.getContext().getColor(R.color.white));

            //album cover
            album.setImageBitmap(AlbumLoader.getDefaultCover());
            this.itemView.removeCallbacks(this);
            this.itemView.postDelayed(this,200);

            // check box
            checkBox.setChecked(MainActivity.selection_manager.contains(mItem));
            /*make a thrust table to understand this (shoud hide when no element and show when elements*/
            if(mItem.getType().equals(DataType.PLAYLIST)){
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
            onItemClicked.oClicked(mItem);
        }
    }
}