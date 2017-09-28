package com.example.gbor.mp3player;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.R.attr.data;
import static android.R.attr.resource;
import static android.R.attr.value;

public class DirecoryChooser extends AppCompatActivity {

    private class DirectoryAdapter extends ArrayAdapter<String>{

        private final Activity context;
        private ArrayList<String> itemData;

        public DirectoryAdapter(Activity context, ArrayList<String> itemData) {
            super(context,R.layout.directory_chooser_item, itemData);
            this.context = context;
            this.itemData = itemData;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater li = context.getLayoutInflater();
            View v = li.inflate(R.layout.directory_chooser_item, null, true);
            TextView fileName = (TextView) v.findViewById(R.id.file_name);
            TextView fileSize = (TextView) v.findViewById(R.id.file_size);
            ImageView icon = (ImageView) v.findViewById(R.id.icon_view);

            fileName.setText(itemData.get(position));
            File file = new File(itemData.get(position));
            String asdas =  file.getAbsoluteFile().getPath();

            if(file.getAbsoluteFile().isDirectory()) {
                fileSize.setText(getResources().getString(R.string.dir));
                icon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.folder_icon));
            }else{
                fileSize.setText(String.valueOf(file.getTotalSpace()));
                icon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.file_icon));
            }
            return v;
        }
    }


    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_chooser);
        TextView textView =(TextView) findViewById(R.id.directory_chooser_name);


        ListView listView = (ListView) findViewById(R.id.directory_list);
        path = getResources().getString(R.string.default_folder);
        if(getIntent().hasExtra("path"))
            path=getIntent().getStringExtra("path");

        final ArrayList<String> values = new ArrayList<String>();
        String[] list = new File(path).list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    values.add(path+file);
                }
            }
        }

        categorize(values);

        DirectoryAdapter directoryAdapter = new DirectoryAdapter(this,values);

        listView.setAdapter(directoryAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                path=values.get(position);
                Toast.makeText(view.getContext(),path,Toast.LENGTH_LONG).show();
            }
        });


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

    private void categorize(ArrayList<String> values){
        List valueDir = new ArrayList();
        List valueFile = new ArrayList();

        for(String value:values){
            if(new File(value).isDirectory())
                valueDir.add(value);
            else
                valueFile.add(value);
        }
        Collections.sort(valueDir);
        Collections.sort(valueFile);
        values.clear();
        values.addAll(valueDir);
        values.addAll(valueFile);
    }



}
