package com.example.gbor.mp3player;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class OptionsFragment extends Fragment {

    private OnOptionsFragmentInteractionListener mInteractionListener;
    private OptionsAdapter mOptionsAdapter;
    private HashMap<String, String> mOptionData;
    private RecyclerView mOptionsList;
    private Context mContext;

    public interface OnOptionsFragmentInteractionListener {
        void optionOperations(int position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<HashMap<String, String>> optionDataList = new ArrayList<>();
        if (getContext() != null)
            mContext = getContext();
        for (String option : getResources().getStringArray(R.array.options_list)) {
            mOptionData = new HashMap<>();
            mOptionData.put("optionName", option);
            if (getArguments() != null) {
                mOptionData.put("optionValue", getArguments().getString("path"));
            }
            optionDataList.add(mOptionData);
        }

        mOptionsAdapter = new OptionsAdapter(optionDataList);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options, container, false);
        mOptionsList = view.findViewById(R.id.options_list);
        mOptionsList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mOptionsList.setLayoutManager(mLayoutManager);
        CustomDividerItemDecoration itemDecor = new CustomDividerItemDecoration(mContext, mLayoutManager.getOrientation());
        itemDecor.setDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.playlist_divider));
        mOptionsList.addItemDecoration(itemDecor);
        mOptionsList.setAdapter(mOptionsAdapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOptionsFragmentInteractionListener) {
            mInteractionListener = (OnOptionsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnOptionsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInteractionListener = null;
    }

    public void updateUI( String path){
        mOptionData.put("optionValue",path);
        mOptionsAdapter.notifyDataSetChanged();

    }



    private class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.OptionsViewHolder> {

        private ArrayList<HashMap<String, String>> mItemData;

        class OptionsViewHolder extends RecyclerView.ViewHolder{
            TextView optionName;
            TextView optionValue;

            OptionsViewHolder(View itemView) {
                super(itemView);
                optionName = itemView.findViewById(R.id.option_name);
                optionValue = itemView.findViewById(R.id.option_value);
            }
        }

        OptionsAdapter(ArrayList<HashMap<String, String>> itemData) {
            this.mItemData = itemData;
        }

        @NonNull
        @Override
        public OptionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.item_options,parent,false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mInteractionListener.optionOperations(mOptionsList.getChildAdapterPosition(v));
                }
            });
            return new OptionsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OptionsViewHolder holder, int position) {
            holder.optionName.setText(mItemData.get(position).get("optionName"));
            holder.optionValue.setText(mItemData.get(position).get("optionValue"));
        }

        @Override
        public int getItemCount() {
            return mItemData.size();
        }
    }
}
