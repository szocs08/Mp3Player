package com.example.gbor.mp3player;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivityDirectoryChooser extends AppCompatActivity {

    private class DirectoryAdapter extends ArrayAdapter<String>{

        private final Activity context;
        private ArrayList<String> itemData;

        private DirectoryAdapter(Activity context, ArrayList<String> itemData) {
            super(context,R.layout.directory_chooser_item, itemData);
            this.context = context;
            this.itemData = itemData;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater li = context.getLayoutInflater();
            ViewHolder holder;
            if (convertView == null){
                convertView = li.inflate(R.layout.directory_chooser_item, null);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon_view);
                holder.fileName = (TextView) convertView.findViewById(R.id.file_name);
                holder.fileSize = (TextView) convertView.findViewById(R.id.file_size);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            holder.fileName.setText(itemData.get(position).substring(itemData.get(position).lastIndexOf('/')+1));
            File file = new File(itemData.get(position));
            if(!itemData.get(position).equalsIgnoreCase("..")) {
                if (file.isDirectory()) {
                    holder.fileSize.setText(getString(R.string.dir));
                    holder.icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.folder_icon));} else {
                    holder.fileSize.setText(String.valueOf(file.length() +
                            getString(R.string.bytes)));
                    holder.icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.file_icon));
                }
            }else{
                holder.icon.setImageDrawable(getDrawable(R.drawable.back_icon));
                holder.fileSize.setText(getString(R.string.previous));

            }
            return convertView;
        }
    }


    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_chooser);
        ListView listView = (ListView) findViewById(R.id.directory_list);
        path = getString(R.string.default_folder);
        if(getIntent().hasExtra("path"))
            path=getIntent().getStringExtra("path");

        final ArrayList<String> values = new ArrayList<String>();
        values.add("..");
        String[] list = new File(path).list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    values.add(path+"/"+file);
                }
            }
        }

        categorize(values);

        final DirectoryAdapter directoryAdapter = new DirectoryAdapter(this,values);

        listView.setAdapter(directoryAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!values.get(position).equalsIgnoreCase("..")) {
                    if(new File(values.get(position)).isDirectory()) {
                        path = values.get(position);
                        values.clear();
                        values.add("..");
                        String[] list = new File(path).list();
                        if (list != null) {
                            for (String file : list) {
                                if (!file.startsWith(".")) {
                                    values.add(path+"/"+file);
                                }
                            }
                        }
                        categorize(values);
                        directoryAdapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(ActivityDirectoryChooser.this, getString(R.string.directory_message), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(!path.equalsIgnoreCase(getString(R.string.default_folder))) {
                        path = path.substring(0, path.lastIndexOf('/'));
                        values.clear();
                        values.add("..");
                        String[] list = new File(path).list();
                        if (list != null) {
                            for (String file : list) {
                                if (!file.startsWith(".")) {
                                    values.add(path + "/" + file);
                                }
                            }
                        }
                        categorize(values);
                        directoryAdapter.notifyDataSetChanged();
                    }
                }
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.save_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.setData(Uri.parse(path));
                setResult(RESULT_OK,data);
                finish();
            }
        });
    }

    private void categorize(ArrayList<String> values){
        List valueDir = new ArrayList();
        List valueFile = new ArrayList();

        for(String value:values){
            if (new File(value).isFile()){
                if(new File(value).isDirectory())
                    valueDir.add(value);
                else {
                    valueFile.add(value);
                }
            }else{
                valueDir.add(value);
            }
        }
        Collections.sort(valueDir);
        Collections.sort(valueFile);
        values.clear();
        values.addAll(valueDir);
        values.addAll(valueFile);
    }


    static class ViewHolder {
        TextView fileName;
        TextView fileSize;
        ImageView icon;
    }
}
