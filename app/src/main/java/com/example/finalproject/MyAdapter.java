package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private TreeMap<Integer, ParkingCell> mData;
    private Context context;
    private String formatTime(long seconds)
    {
        String text = "";
        int hours = (int)(seconds / (60 * 60)); // gets the hours, 60 for seconds per minute and anotehr 60 for minutes in an hour
        int secondsLeft = (int)(seconds - (hours * 60 * 60));
        int minutes = secondsLeft / 60;
        secondsLeft = secondsLeft - (minutes * 60);

        if (hours < 10)
        {
            text += "0";
        }
        text += hours + ":";

        if (minutes < 10)
        {
            text += "0";
        }
        text += minutes + ":";

        if (secondsLeft < 10)
        {
            text += "0";
        }
        text += secondsLeft ;

        return text;
    }
    public MyAdapter(Context ctx, TreeMap<Integer, ParkingCell> data) {
        mData = data;
        context = ctx;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Set<Map.Entry<Integer, ParkingCell> > entrySet = mData.entrySet();

        // Converting entrySet to ArrayList to get by index
        List<Map.Entry<Integer, ParkingCell> > entryList = new ArrayList<>(entrySet);

        ParkingCell cellData = entryList.get(position).getValue();
        Geocoder gcoder = new Geocoder(context, Locale.getDefault());

        holder.name.setText("Test");

        long timeSinceParking = Timestamp.now().getSeconds() - cellData.creationTime.getSeconds();
        Date date = new Date(timeSinceParking);

        holder.timeSinceParking.setText("Free since: " + formatTime(timeSinceParking) );//formatTime(timeSinceParking));
        holder.name.setText("Reported at " + cellData.creationTime.toDate());

        holder.timeToGetThere.setText(cellData.getTimeToGetThere() + " away");
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView timeSinceParking;
        TextView timeToGetThere;

        public ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            timeSinceParking = itemView.findViewById(R.id.timeSinceParking);
            timeToGetThere = itemView.findViewById(R.id.timeToGetThere);
        }
    }
}