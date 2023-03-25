package com.demeth.massaudioplayer.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.database.AlbumLoader;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {

    RadioGroup group=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlbumLoader.open(this);

        setContentView(R.layout.activity_main);
        group = findViewById(R.id.fragment_list);
        addCategory("TEST");
        addCategory("TEST1");
        addCategory("TEST2");
        addCategory("TEST3");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AlbumLoader.close();
    }

    private void addCategory(String str){
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
    }
}