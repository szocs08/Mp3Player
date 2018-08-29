package com.example.gbor.mp3player;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DirectoryChooserActivity extends AppCompatActivity {

    private String mPath;
    private RecyclerView mDirectoryList;
    private DirectoryAdapter mDirectoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_chooser);
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
        mDirectoryList = findViewById(R.id.directory_list);
        mDirectoryAdapter = new DirectoryAdapter(values);

        mDirectoryList.setAdapter(mDirectoryAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mDirectoryList.setLayoutManager(mLayoutManager);


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



    private class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.DirectoryViewHolder>{

        private ArrayList<String> mItemData;

        class DirectoryViewHolder extends RecyclerView.ViewHolder{
            TextView fileName;
            TextView fileSize;
            ImageView icon;

            DirectoryViewHolder(View itemView) {
                super(itemView);
                fileName = itemView.findViewById(R.id.file_name);
                fileSize = itemView.findViewById(R.id.file_size);
                icon = itemView.findViewById(R.id.icon_view);
            }
        }

        private DirectoryAdapter(ArrayList<String> itemData) {
            mItemData = itemData;
        }

        @NonNull
        @Override
        public DirectoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.item_file,parent,false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = mDirectoryList.getChildAdapterPosition(v);
                    if(!mItemData.get(position).equalsIgnoreCase("..")) {
                        if(new File(mItemData.get(position)).isDirectory()) {
                            mPath = mItemData.get(position);
                            mItemData.clear();
                            mItemData.add("..");
                            String[] list = new File(mPath).list();
                            if (list != null) {
                                for (String file : list) {
                                    if (!file.startsWith(".")) {
                                        mItemData.add(mPath +"/"+file);
                                    }
                                }
                            }
                            categorize(mItemData);
                            mDirectoryAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(DirectoryChooserActivity.this, getString(R.string.directory_message), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if(!mPath.equalsIgnoreCase(Environment.getRootDirectory().toString())) {
                            mPath = mPath.substring(0, mPath.lastIndexOf('/'));
                            mItemData.clear();
                            mItemData.add("..");
                            String[] list = new File(mPath).list();
                            if (list != null) {
                                for (String file : list) {
                                    if (!file.startsWith(".")) {
                                        mItemData.add(mPath + "/" + file);
                                    }
                                }
                            }
                            categorize(mItemData);
                            mDirectoryAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
            return new DirectoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DirectoryViewHolder holder, int position) {
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

        }

        @Override
        public int getItemCount() {
            return mItemData.size();
        }



//        @NonNull
//        @Override
//        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//            LayoutInflater li = mContext.getLayoutInflater();
//            DirectoryViewHolder holder;
//            if (convertView == null){
//                convertView = li.inflate(R.layout.item_file, parent, false);
//                holder = new DirectoryViewHolder();
//                holder.icon = convertView.findViewById(R.id.icon_view);
//                holder.fileName = convertView.findViewById(R.id.file_name);
//                holder.fileSize = convertView.findViewById(R.id.file_size);
//                convertView.setTag(holder);
//            }else{
//                holder = (DirectoryViewHolder) convertView.getTag();
//            }
//
//            holder.fileName.setText(mItemData.get(position).substring(mItemData.get(position).lastIndexOf('/')+1));
//            File file = new File(mItemData.get(position));
//            if(!mItemData.get(position).equalsIgnoreCase("..")) {
//                if (file.isDirectory()) {
//                    holder.fileSize.setText(getString(R.string.dir));
//                    holder.icon.setImageResource(R.drawable.ic_folder);
//                } else {
//                    holder.icon.setImageResource(R.drawable.ic_non_audio_file);
//                    try {
//                        if(URLConnection.guessContentTypeFromName(file.getAbsolutePath()).startsWith("audio"))
//                            holder.icon.setImageResource(R.drawable.ic_audio_file);
//                    } catch (NullPointerException e) {
//                        e.printStackTrace();
//                    }
//
//                    holder.fileSize.setText(String.valueOf(file.length() +
//                            getString(R.string.bytes)));
//
//                }
//            }else{
//                holder.icon.setImageResource(R.drawable.ic_back);
//                holder.fileSize.setText(getString(R.string.previous));
//
//            }
//            return convertView;
//        }
    }

}
