package hu.application.gbor.mp3player;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gbor.mp3player.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class PlaylistDialogFragment extends DialogFragment {
    private OnPlaylistDialogFragmentInteractionListener mListener;

    RecyclerView mPlaylistList;
    Button mRemoveButton;
    Button mAddButton;
    boolean mIsSelecting = false;
//    TreeMap<String,Boolean> mItemSelectionMap = new TreeMap<>(new PlaylistComparator());
    PlaylistAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    PlaylistFragment.DialogTypes mType;
    String mPlaylistName;
    ArrayList<String> mPlaylistItems;
    PlayerViewModel mViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = (PlaylistFragment.DialogTypes) getArguments().getSerializable("type");
            mPlaylistName = getArguments().getString("name");
        }
        if (requireActivity() instanceof OnPlaylistDialogFragmentInteractionListener) {
            mListener = (OnPlaylistDialogFragmentInteractionListener) requireActivity();
        } else {
            throw new RuntimeException(requireActivity().toString()
                    + " must implement OnPlayerFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_dialog, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setTitle(getString(R.string.playlist_choosing));
        }
        mPlaylistList = view.findViewById(R.id.playlist_list);
        mRemoveButton = view.findViewById(R.id.remove_button);
        mViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        CustomDividerItemDecoration itemDecor = new CustomDividerItemDecoration(requireActivity(),
                ContextCompat.getColor(requireActivity(),R.color.background_color),2);
        mPlaylistList.addItemDecoration(itemDecor);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mPlaylistList.setLayoutManager(mLayoutManager);
        mViewModel.getPlaylistFileList().observe(getViewLifecycleOwner(), playlistFileList -> {
            mAdapter = new PlaylistAdapter(playlistFileList);
            mPlaylistList.setAdapter(mAdapter);

                }
        );
        mAddButton = view.findViewById(R.id.add_button);

        mAddButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            final EditText editText = new EditText(getActivity());
            builder.setTitle(getString(R.string.new_playlist));
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(editText);
            builder.setPositiveButton(R.string.create, (dialog, which) -> {
                String name = editText.getText().toString();
                name = mListener.playlistAddButton(name);
//                mItemSelectionMap.put(name,false);
                mPlaylistItems.add(name);
                Collections.sort(mAdapter.mItemData,new PlaylistComparator());
                mAdapter.notifyDataSetChanged();
                dialog.dismiss();
            });
            builder.show();
        });

        mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                List<String> names = new ArrayList<>();
//                for (String name:mAdapter.mItemData){
//                    if (!name.equals(getString(R.string.all_songs)) &&
//                            mItemSelectionMap.get(name))
//                        names.add(name);
//                }
//
//                mPlaylistItems.removeAll(names);
//                mItemSelectionMap.keySet().removeAll(names);
//                selecting(0);
//                mAdapter.notifyDataSetChanged();
//                mListener.playlistRemoveButton(names);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        List<String> array = new ArrayList<>();
//        if (mType == PlaylistFragment.DialogTypes.SWITCHING) {
//            mItemSelectionMap.put(getString(R.string.all_songs),true);
//
//        }
//        for (Map.Entry<String, ?> entry : mPlaylistIDs.getAll().entrySet()){
//            if (!entry.getKey().equals(mPlaylistName) || mType == PlaylistFragment.DialogTypes.SWITCHING)
//                array.add(entry.getKey());
//        }
//        for (String string : array)
//            mItemSelectionMap.put(string,false);
//        mPlaylistItems = new ArrayList<>(mItemSelectionMap.keySet());
//        mAdapter = new PlaylistAdapter(mPlaylistItems);
//        mLayoutManager = new LinearLayoutManager(getActivity());
//        mPlaylistList.setLayoutManager(mLayoutManager);
//        CustomDividerItemDecoration itemDecor = new CustomDividerItemDecoration(getActivity(),
//                ContextCompat.getColor(getActivity(),R.color.background_color),2);
//        mPlaylistList.addItemDecoration(itemDecor);
//        mPlaylistList.setAdapter(mAdapter);
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

//    private void selecting(int position){
//        List<String> keys = new ArrayList<>(mItemSelectionMap.keySet());
//        if (position != 0 || mType == PlaylistFragment.DialogTypes.ADDING) {
//            if(mItemSelectionMap.get(keys.get(position))){
//                mItemSelectionMap.put(keys.get(position),false);
//            }else {
//                mItemSelectionMap.put(keys.get(position),true);
//            }
//        }
//        if (!mItemSelectionMap.containsValue(true)) {
//            mIsSelecting = false;
//            mRemoveButton.setEnabled(false);
//            mAddButton.setEnabled(true);
//            mItemSelectionMap.put(keys.get(0),true);
//        }
//
//    }

    private class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

        ArrayList<PlaylistFile> mItemData;



        class PlaylistViewHolder extends RecyclerView.ViewHolder{
            TextView playlistName;

            PlaylistViewHolder(View itemView) {
                super(itemView);
                playlistName = itemView.findViewById(R.id.dialog_playlist_name);
            }
        }

        PlaylistAdapter( ArrayList<PlaylistFile> itemData) {
            mItemData = itemData;
        }

        @NonNull
        @Override
        public PlaylistViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(getActivity()).
                    inflate(R.layout.item_playlist_dialog,parent,false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = mPlaylistList.getChildAdapterPosition(v);
                    if (mIsSelecting){
//                        selecting(position);
                        mAdapter.notifyDataSetChanged();
                    }else {
                        switch (mType) {
                            case SWITCHING:
//                                mListener.playlistSelection(mAdapter.mItemData.get(position));
                                break;
                            case ADDING:
//                                mListener.addSelectedSongs(mAdapter.mItemData.get(position));
                                break;
                        }
                        dismiss();
                    }

                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = mPlaylistList.getChildAdapterPosition(v);
                    mIsSelecting = true;
                    mRemoveButton.setEnabled(true);
                    mAddButton.setEnabled(false);
//                    mItemSelectionMap.put((String) mItemSelectionMap.keySet().toArray()[0], false);
//                    selecting(position);
                    mAdapter.notifyDataSetChanged();
                    return true;
                }
            });
            return new PlaylistViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
            holder.playlistName.setText(mItemData.get(position).getmName());
            if (position > 0 || mType == PlaylistFragment.DialogTypes.ADDING) {
//                if(mItemSelectionMap.get(mItemData.get(position))) {
//                    holder.itemView.setBackgroundResource(R.drawable.list_select_bg);
//                    holder.playlistName.setTextColor(ContextCompat.getColor(getActivity(),R.color.list_color_playlist));
//                }else {
                    holder.itemView.setBackgroundResource(R.drawable.list_dark_bg);
                    holder.playlistName.setTextColor(ContextCompat.getColor(getActivity(),R.color.list_color));
//                }
//            }else if (!mItemSelectionMap.get(mItemData.get(position))) {
//                holder.itemView.setEnabled(false);
//                holder.playlistName.setEnabled(false);
            }else {
                holder.itemView.setEnabled(true);
                holder.playlistName.setEnabled(true);
            }
        }

        @Override
        public int getItemCount() {
            return mItemData.size();
        }

        @Override
        public long getItemId(int position) {
            return mItemData.get(position).hashCode();
        }
    }




    interface OnPlaylistDialogFragmentInteractionListener {
        void playlistRemoveButton(List<String> names);
        String playlistAddButton(String name);
        void playlistSelection(String name);
        void addSelectedSongs(String name);
    }

    static class PlaylistComparator implements Comparator<PlaylistFile>{
        @Override
        public int compare(PlaylistFile o1, PlaylistFile o2) {
            if (o1.getmName().equalsIgnoreCase("all songs"))
                return -1;
            if (o2.getmName().equalsIgnoreCase("all songs"))
                return 1;
            return o1.getmName().compareToIgnoreCase(o2.getmName());
        }
    }

}
