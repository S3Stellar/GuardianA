package com.example.guardiana.adapters;

import android.animation.TypeEvaluator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.guardiana.R;
import com.example.guardiana.model.Address;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AddressAdapter extends ListAdapter<Address, AddressAdapter.AddressViewHolder> {

    private OnItemClickListener onItemClickListener;
    private Context context;
    public AddressAdapter() {
        super(DIFF_CALLBACK);
    }

    public static final DiffUtil.ItemCallback<Address> DIFF_CALLBACK = new DiffUtil.ItemCallback<Address>() {
        @Override
        public boolean areItemsTheSame(@NonNull Address oldItem, @NonNull Address newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Address oldItem, @NonNull Address newItem) {
            return oldItem.getPriority() == newItem.getPriority();
        }

    };

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.address_item, parent, false);
        return new AddressViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        holder.city.setText(getItem(position).getCityName());
        holder.cityAddress.setText(getItem(position).getCityAddress());
        setImage(holder.addressImageView, getItem(position).getPriority());
    }

    public Address getAddressPositionAt(int position) {
        return getItem(position);
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        public TextView city;
        public TextView cityAddress;
        public ImageView addressImageView;
        public AddressViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            city = itemView.findViewById(R.id.address_item_text_view_header);
            cityAddress = itemView.findViewById(R.id.address_item_text_view_content);
            addressImageView = itemView.findViewById(R.id.address_item_image);
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(position);
                    }
                }
            });
        }
    }

    private void setImage(ImageView imageView, int priority){
        int image;
        switch(priority) {

            case FavoriteOptions.HOME:
                image = R.drawable.house;
                break;
            case FavoriteOptions.WORK:
                image = R.drawable.work;
                break;
            case FavoriteOptions.FRIENDS:
                image = R.drawable.friends;
                break;
            case FavoriteOptions.KINDERGARTEN:
                image = R.drawable.kindergarten;
                break;
            case FavoriteOptions.SHOP:
                image = R.drawable.shops;
                break;
            case FavoriteOptions.CLINIC:
                image = R.drawable.clinic;
                break;
            case FavoriteOptions.HOSPITAL:
                image = R.drawable.hospital;
                break;
            default:
                image = R.drawable.ic_baseline_directions_bike_24;
        }
        Glide.with(context).load(image).into(imageView);
    }
}
