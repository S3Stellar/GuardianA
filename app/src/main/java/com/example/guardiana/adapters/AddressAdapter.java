package com.example.guardiana.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guardiana.R;
import com.example.guardiana.model.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addressList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public AddressAdapter(List<Address> addressList) {
        this.addressList = addressList;

    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.address_item, parent, false);
        return new AddressViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        holder.city.setText(addressList.get(position).getCity());
        holder.cityAddress.setText(addressList.get(position).getStreetAddress());
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        public TextView city;
        public TextView cityAddress;
        public AddressViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            city = itemView.findViewById(R.id.address_item_text_view_header);
            cityAddress = itemView.findViewById(R.id.address_item_text_view_content);
            itemView.setOnClickListener(v -> {
                if(onItemClickListener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        onItemClickListener.onItemClick(position);
                    }
                }
            });
        }
    }
}
