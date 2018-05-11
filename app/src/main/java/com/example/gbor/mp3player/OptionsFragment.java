package com.example.gbor.mp3player;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class OptionsFragment extends ListFragment {

    private OnOptionsFragmentInteractionListener mInteractionListener;
    private OptionsAdapter mOptionsAdapter;
    private HashMap<String, String> mOptionData;

    public interface OnOptionsFragmentInteractionListener {
        void optionOperations(int position);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mInteractionListener.optionOperations(position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<HashMap<String, String>> optionDataList = new ArrayList<>();

        for (String option : getResources().getStringArray(R.array.options_list)) {
            mOptionData = new HashMap<>();
            mOptionData.put("optionName", option);
            mOptionData.put("optionValue", getArguments().getString("path"));
            optionDataList.add(mOptionData);
        }

        mOptionsAdapter = new OptionsAdapter(getActivity(),optionDataList);


        setListAdapter(mOptionsAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_options, container, false);
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

    static class ViewHolder{
        TextView optionName;
        TextView optionValue;
    }

    private class OptionsAdapter extends ArrayAdapter<HashMap<String, String>> {

        private final Activity mContext;
        private ArrayList<HashMap<String, String>> mItemData;

        OptionsAdapter(Activity context, ArrayList<HashMap<String, String>> itemData) {
            super(context, R.layout.item_options, itemData);
            this.mContext = context;
            this.mItemData = itemData;


        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater li = mContext.getLayoutInflater();
            ViewHolder holder;
            if(convertView == null){
                holder = new ViewHolder();
                convertView = li.inflate(R.layout.item_options, parent, false);
                holder.optionName = (TextView) convertView.findViewById(R.id.option_name);
                holder.optionValue = (TextView) convertView.findViewById(R.id.option_value);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }


            holder.optionName.setText(mItemData.get(position).get("optionName"));
            holder.optionValue.setText(mItemData.get(position).get("optionValue"));

            return convertView;
        }
    }

}
