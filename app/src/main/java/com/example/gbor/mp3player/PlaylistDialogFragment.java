package com.example.gbor.mp3player;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class PlaylistDialogFragment extends DialogFragment {
    private OnPlaylistDialogFragmentInteractionListener mListener;
    private static final String PLAYLIST_FILE = "com.example.gbor.mp3player.Playlist";
    private SharedPreferences mPlaylistIDs;


    ListView mListView;
    Button mRemoveButton;
    Button mAddButton;
    boolean mIsSelecting = false;
    TreeMap<String,Boolean> mItemSelectionMap = new TreeMap<>(new PlaylistComparator());
    PlaylistAdapter mAdapter;
    PlaylistFragment.DialogTypes mType;
    String mPlaylistName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlaylistIDs = getActivity().getSharedPreferences(PLAYLIST_FILE, Context.MODE_PRIVATE);
        mType = (PlaylistFragment.DialogTypes) getArguments().getSerializable("type");
        mPlaylistName = getArguments().getString("name");
        if (getActivity() instanceof OnPlaylistDialogFragmentInteractionListener) {
            mListener = (OnPlaylistDialogFragmentInteractionListener) getActivity();
        } else {
            throw new RuntimeException(getActivity().toString()
                    + " must implement OnPlayerFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_dialog, container, false);

        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setTitle(getString(R.string.playlist_choosing));
        }
        mListView = view.findViewById(R.id.playlist_list);
        mRemoveButton = view.findViewById(R.id.remove_button);
        mAddButton = view.findViewById(R.id.add_button);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mIsSelecting){
                    selecting(position);
                    mAdapter.notifyDataSetChanged();
                }else {
                    switch (mType) {
                        case SWITCHING:
                            mListener.playlistSelection(mAdapter.mItemData.get(position));
                            break;
                        case ADDING:
                            mListener.addSelectedSongs(mAdapter.mItemData.get(position));
                            break;
                    }
                    dismiss();
                }
            }
        });



        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mIsSelecting = true;
                mRemoveButton.setEnabled(true);
                mAddButton.setEnabled(false);
                mItemSelectionMap.put((String) mItemSelectionMap.keySet().toArray()[0],false);
                selecting(position);
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final EditText editText = new EditText(getActivity());
                builder.setTitle(getString(R.string.new_playlist));
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(editText);
                builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = editText.getText().toString();
                        name = mListener.playlistAddButton(name);
                        mItemSelectionMap.put(name,false);
                        mAdapter.add(name);
                        Collections.sort(mAdapter.mItemData,new PlaylistComparator());
                        mAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> names = new ArrayList<>();
                for (String name:mAdapter.mItemData){
                    if (!name.equals(getString(R.string.all_songs)) &&
                            mItemSelectionMap.get(name))
                        names.add(name);
                }

                mAdapter.mItemData.removeAll(names);
                mAdapter.notifyDataSetChanged();
                mListener.playlistRemoveButton(names);
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<String> array = new ArrayList<>();
        if (mType == PlaylistFragment.DialogTypes.SWITCHING) {
            mItemSelectionMap.put(getString(R.string.all_songs),true);

        }
        for (Map.Entry<String, ?> entry : mPlaylistIDs.getAll().entrySet()){
            if (!entry.getKey().equals(mPlaylistName) || mType == PlaylistFragment.DialogTypes.SWITCHING)
                array.add(entry.getKey());
        }
        for (String string : array)
            mItemSelectionMap.put(string,false);

        mAdapter = new PlaylistAdapter(getActivity(),
                new ArrayList<>(mItemSelectionMap.keySet()));
        mListView.setAdapter(mAdapter);




    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void selecting(int position){
        List<String> keys = new ArrayList<>(mItemSelectionMap.keySet());
        if (position != 0 || mType == PlaylistFragment.DialogTypes.ADDING) {
            if(mItemSelectionMap.get(keys.get(position))){
                mItemSelectionMap.put(keys.get(position),false);
            }else {
                mItemSelectionMap.put(keys.get(position),true);
            }
        }
        if (!mItemSelectionMap.containsValue(true)) {
            mIsSelecting = false;
            mRemoveButton.setEnabled(false);
            mAddButton.setEnabled(true);
            mItemSelectionMap.put(keys.get(0),true);
        }

    }


    static class ViewHolder{
        TextView playlistName;
    }

    private class PlaylistAdapter extends ArrayAdapter<String> {

        Activity mContext;
        ArrayList<String> mItemData;


        PlaylistAdapter(Activity context, ArrayList<String> itemData) {
            super(context,R.layout.item_playlist_dialog,itemData);
            mContext=context;
            mItemData = itemData;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            ViewHolder holder;
            if(convertView == null){
                holder = new ViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist_dialog,parent,false);
                holder.playlistName =  convertView.findViewById(R.id.dialog_playlist_name);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            holder.playlistName.setText(mItemData.get(position));
            if (position > 0 || mType == PlaylistFragment.DialogTypes.ADDING) {
                if(mItemSelectionMap.get(mItemData.get(position))) {
                    convertView.setBackgroundResource(R.drawable.list_select_bg);
                    holder.playlistName.setTextColor(ContextCompat.getColor(getContext(),R.color.list_color_playlist));
                }else {
                    convertView.setBackgroundResource(R.drawable.list_dark_bg);
                    holder.playlistName.setTextColor(ContextCompat.getColor(getContext(),R.color.list_color));
                }
            }else if (!mItemSelectionMap.get(mItemData.get(position))) {
                convertView.setEnabled(false);
                holder.playlistName.setEnabled(false);
            }else {
                convertView.setEnabled(true);
                holder.playlistName.setEnabled(true);
            }
            return convertView;
        }
    }




    interface OnPlaylistDialogFragmentInteractionListener {
        void playlistRemoveButton(List<String> names);
        String playlistAddButton(String name);
        void playlistSelection(String name);
        void addSelectedSongs(String name);
    }

    static class PlaylistComparator implements Comparator<String>{
        @Override
        public int compare(String o1, String o2) {
            if (o1.equalsIgnoreCase(o2))
                return o1.compareToIgnoreCase(o2);
            if (o1.equalsIgnoreCase("all songs"))
                return -1;
            if (o2.equalsIgnoreCase("all songs"))
                return 1;
            return o1.compareToIgnoreCase(o2);
        }
    }

}
