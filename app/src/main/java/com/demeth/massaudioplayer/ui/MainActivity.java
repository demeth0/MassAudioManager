package com.demeth.massaudioplayer.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.database.AlbumLoader;
import com.demeth.massaudioplayer.database.IdentifiedEntry;
import com.demeth.massaudioplayer.ui.fragments.AudioEntryDisplayer;
import com.demeth.massaudioplayer.ui.viewmodel.DiffusionViewModel;
import com.demeth.massaudioplayer.ui.viewmodel.ListViewModel;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {

    public static final SelectionManager selection_manager = new SelectionManager();

    private ListViewModel listViewModel;
    private DiffusionViewModel diffusionViewModel;

    RadioGroup group=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //pre init
        listViewModel = new ViewModelProvider(this).get(ListViewModel.class);
        diffusionViewModel = new ViewModelProvider(this).get(DiffusionViewModel.class);
        AlbumLoader.open(this);
        setContentView(R.layout.activity_main);

        //init
        group = findViewById(R.id.fragment_list);

        group.setOnCheckedChangeListener((radioGroup, i) -> {
            RadioButton rb = radioGroup.findViewById(i);
            //listViewModel.setActiveList(Category.valueOf());
            Bundle bun = new Bundle();
            bun.putString("category",rb.getText().toString());
            getSupportFragmentManager().beginTransaction().replace(R.id.list_displayer_fragment_container,AudioEntryDisplayer.class,bun).setReorderingAllowed(true).commit();
        });

        addCategory(Category.PLAYLISTS.getName());
        addCategory(Category.PISTES.getName()).setChecked(true);
        addCategory(Category.QUEUE.getName());



        setupController();
        setupSearchBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AlbumLoader.close();
    }

    private void setupController(){
        FragmentContainerView controller = findViewById(R.id.small_controller_fragment_container);
        controller.setVisibility(View.GONE);
        Observer<IdentifiedEntry> controller_apparition_observer = new Observer<IdentifiedEntry>() {
            @Override
            public void onChanged(IdentifiedEntry identifiedEntry) {
                if(identifiedEntry!=null){
                    controller.setVisibility(View.VISIBLE);
                    diffusionViewModel.getEntry().removeObserver(this);
                }
            }
        };

        diffusionViewModel.getEntry().observe(this,controller_apparition_observer);
    }

    private void setupSearchBar(){
        AutoCompleteTextView search_field = findViewById(R.id.search_field);
        SearchFieldAutoCompleter searchFieldAutoCompleter = new SearchFieldAutoCompleter(this,android.R.layout.simple_list_item_1);
        search_field.setAdapter(searchFieldAutoCompleter);
        search_field.setThreshold(1);

        listViewModel.getList().observe(this, searchFieldAutoCompleter::setContent);

        ImageButton search_button = findViewById(R.id.search_button);
        search_button.setOnClickListener(view -> {
            listViewModel.setFilterMask(search_field.getText().toString());
            search_field.clearFocus();
        });
        search_field.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            boolean ret = true;
            //IME Input Methode
            if(actionId== EditorInfo.IME_ACTION_DONE){
                listViewModel.setFilterMask(search_field.getText().toString());
                search_field.clearFocus();
                ret = false;
            }
            //si action consumée ? (succes ou echec) pour fermer ou non le clavier
            return ret;
        });
    }

    private RadioButton addCategory(String str){
        RadioButton but = new RadioButton(this);
        RadioGroup.LayoutParams param = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int spacing = (int)getResources().getDimension(R.dimen.category_button_spacing);
        but.setPadding(spacing,0,spacing,0);
        but.setLayoutParams(param);

        but.setButtonDrawable(null);
        but.setBackground(AppCompatResources.getDrawable(this,R.drawable.radiobutton_selector));
        but.setText(str);
        but.setTextColor(this.getColor(R.color.white));
        group.addView(but);
        return but;
    }
}