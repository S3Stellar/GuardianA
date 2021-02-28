package com.example.guardiana.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guardiana.R;
import com.example.guardiana.model.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends ListAdapter<Address, AddressAdapter.AddressViewHolder> {

    private OnItemClickListener onItemClickListener;

    public AddressAdapter() {
        super(DIFF_CALLBACK);
    }

    public static final DiffUtil.ItemCallback<Address> DIFF_CALLBACK = new DiffUtil.ItemCallback<Address>() {
        @Override
        public boolean areItemsTheSame(@NonNull Address oldItem, @NonNull Address newItem) {
//            Log.i("TAG", "areItemsTheSame: " + oldItem.getCityName().equals(newItem.getCityName()));
            return oldItem.getCityName().equals(newItem.getCityName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Address oldItem, @NonNull Address newItem) {
            return oldItem.getCreatedTimestamp().equals(newItem.getCreatedTimestamp());
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.address_item, parent, false);
        return new AddressViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        holder.city.setText(getItem(position).getCityName());
        holder.cityAddress.setText(getItem(position).getCityAddress());
    }

//    @Override
//    public void submitList(final List<Address> list) {
//        super.submitList(list != null ? new ArrayList<>(list) : null);
//    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        public TextView city;
        public TextView cityAddress;

        public AddressViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            city = itemView.findViewById(R.id.address_item_text_view_header);
            cityAddress = itemView.findViewById(R.id.address_item_text_view_content);
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
}
