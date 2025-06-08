package com.example.finalproject5points;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapterMembership extends BaseAdapter {
    private Context context;
    private ArrayList<String> names;
    private LayoutInflater inflater;

    public CustomAdapterMembership(Context context, ArrayList<String> names){
        this.context = context;
        this.names = names;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * @return Length of the items list
     */
    @Override
    public int getCount() {
        return names.size();
    }

    /**
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return Position of the item whose data we want within the adapter's
     *      *                 data set.
     */
    @Override
    public Object getItem(int position) {
        return position;
    }

    /**
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param view The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return
     */
    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.custom_membership_lv, parent, false);
        TextView str = view.findViewById(R.id.AdaptertV);
        str.setText(names.get(position));
        return view;
    }
}
