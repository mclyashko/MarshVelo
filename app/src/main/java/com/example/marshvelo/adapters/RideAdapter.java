package com.example.marshvelo.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.example.marshvelo.R;
import com.example.marshvelo.database.Ride;

public class RideAdapter extends ListAdapter<Ride, RideViewHolder> {

    public RideAdapter(@NonNull DiffUtil.ItemCallback<Ride> diffCallback) {
        super(diffCallback);
    }

    @Override
    public RideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return RideViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(RideViewHolder holder, int position) {
        Ride current = getItem(position);
        holder.bind(current.getName(), current.getImg(), current.getTimestamp(), current.getDistanceInMeters(), current.getTimeInMillis());
    }

    public static class RideDiff extends DiffUtil.ItemCallback<Ride> {

        @Override
        public boolean areItemsTheSame(@NonNull Ride oldItem, @NonNull Ride newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Ride oldItem, @NonNull Ride newItem) {
            return oldItem.getId() == newItem.getId();
        }
    }
}

class RideViewHolder extends RecyclerView.ViewHolder {
    // private final TextView wordItemView;
    private final TextView name;
    private final ImageView img;
    private final TextView timestamp; // When your ride was
    private final TextView distanceInMeters;
    private final TextView timeInSeconds; // How long your ride was
    private final Button deleteButton;


    private RideViewHolder(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.name_item);
        img = itemView.findViewById(R.id.img_route);
        timestamp = itemView.findViewById(R.id.timestamp);
        distanceInMeters = itemView.findViewById(R.id.distance_in_meters);
        timeInSeconds = itemView.findViewById(R.id.timeInMillis);
        deleteButton = itemView.findViewById(R.id.button);
    }

    public void bind(String name, Bitmap img, long timestamp, int distanceInMeters, long timeInMillis) {
        this.name.setText(name);
        this.img.setImageBitmap(img);
        this.distanceInMeters.setText(Integer.toString(distanceInMeters) + " м");
        this.timeInSeconds.setText(Integer.toString((int) (timeInMillis / 1000)) + " c");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
        this.timestamp.setText(simpleDateFormat.format(timestamp));
    }

    static RideViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route, parent, false);
        return new RideViewHolder(view);
    }
}

