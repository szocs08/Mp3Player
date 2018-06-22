package com.example.gbor.mp3player;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DirectoryChooserActivity extends AppCompatActivity {

    private String mPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_chooser);
        ListView listView = findViewById(R.id.directory_list);
        mPath = Environment.getExternalStorageDirectory().toString();
        if(getIntent().hasExtra("path"))
            mPath =getIntent().getStringExtra("path");

        final ArrayList<String> values = new ArrayList<>();
        values.add("..");
        String[] list = new File(mPath).list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    values.add(mPath +"/"+file);
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
                        mPath = values.get(position);
                        values.clear();
                        values.add("..");
                        String[] list = new File(mPath).list();
                        if (list != null) {
                            for (String file : list) {
                                if (!file.startsWith(".")) {
                                    values.add(mPath +"/"+file);
                                }
                            }
                        }
                        categorize(values);
                        directoryAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(DirectoryChooserActivity.this, getString(R.string.directory_message), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if(!mPath.equalsIgnoreCase(Environment.getRootDirectory().toString())) {
                        mPath = mPath.substring(0, mPath.lastIndexOf('/'));
                        values.clear();
                        values.add("..");
                        String[] list = new File(mPath).list();
                        if (list != null) {
                            for (String file : list) {
                                if (!file.startsWith(".")) {
                                    values.add(mPath + "/" + file);
                                }
                            }
                        }
                        categorize(values);
                        directoryAdapter.notifyDataSetChanged();
                    }
                }
            }
        });


        FloatingActionButton fab = findViewById(R.id.save_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.setData(Uri.parse(mPath));
                setResult(RESULT_OK,data);
                finish();
            }
        });
    }

    private void categorize(ArrayList<String> values){
        List<String> valueDir = new ArrayList<>();
        List<String> valueFile = new ArrayList<>();

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

    private class DirectoryAdapter extends ArrayAdapter<String>{

        private final Activity mContext;
        private ArrayList<String> mItemData;

        private DirectoryAdapter(Activity context, ArrayList<String> itemData) {
            super(context,R.layout.item_file, itemData);
            mContext = context;
            mItemData = itemData;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater li = mContext.getLayoutInflater();
            ViewHolder holder;
            if (convertView == null){
                convertView = li.inflate(R.layout.item_file, parent, false);
                holder = new ViewHolder();
                holder.icon = convertView.findViewById(R.id.icon_view);
                holder.fileName = convertView.findViewById(R.id.file_name);
                holder.fileSize = convertView.findViewById(R.id.file_size);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            holder.fileName.setText(mItemData.get(position).substring(mItemData.get(position).lastIndexOf('/')+1));
            File file = new File(mItemData.get(position));
            if(!mItemData.get(position).equalsIgnoreCase("..")) {
                if (file.isDirectory()) {
                    holder.fileSize.setText(getString(R.string.dir));
                    holder.icon.setImageResource(R.drawable.ic_folder);
                } else {
                    holder.icon.setImageResource(R.drawable.ic_non_audio_file);
                    try {
                        if(URLConnection.guessContentTypeFromName(file.getAbsolutePath()).startsWith("audio"))
                            holder.icon.setImageResource(R.drawable.ic_audio_file);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    holder.fileSize.setText(String.valueOf(file.length() +
                            getString(R.string.bytes)));

                }
            }else{
                holder.icon.setImageResource(R.drawable.ic_back);
                holder.fileSize.setText(getString(R.string.previous));

            }
            return convertView;
        }
    }

}
