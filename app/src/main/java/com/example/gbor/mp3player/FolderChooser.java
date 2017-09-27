package com.example.gbor.mp3player;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.R.id.list;

public class FolderChooser extends AppCompatActivity {

    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_chooser);
        TextView textView =(TextView) findViewById(R.id.folder_chooser_name);


        ListView listView = (ListView) findViewById(R.id.folder_list);
        path = getResources().getString(R.string.default_folder);
        if(getIntent().hasExtra("path"))
            path=getIntent().getStringExtra("path");

        List values = new ArrayList();
        String[] list = new File(path).list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    values.add(file);
                }
            }
        }
        Collections.sort(values);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_2, android.R.id.text1, values);

        listView.setAdapter(arrayAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.save_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.setData(Uri.parse("this is the info"));
                setResult(RESULT_OK,data);
                finish();
            }
        });
    }

}
