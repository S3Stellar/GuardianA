package com.example.guardiana.clustermap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.guardiana.model.UserMarker;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;


public class UserClusterMarker implements ClusterItem {
    private String snippet;
    private UserMarker userMarker;
    private LatLng position;
    private int iconPicture;

    public UserClusterMarker() {
    }

    public UserClusterMarker(String snippet, UserMarker userMarker) {
        this.snippet = snippet;
        this.userMarker = userMarker;
        position = new LatLng(userMarker.getLocation().getLat(), userMarker.getLocation().getLng());
        iconPicture = userMarker.getIcon();
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return userMarker.getEmail();
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public UserMarker getUserMarker() {
        return userMarker;
    }

    public void setElement(UserMarker userMarker) {
        this.userMarker = userMarker;
    }

    public int getIconPicture() {
        return iconPicture;
    }

    public void setIconPicture(int iconPicture) {
        this.iconPicture = iconPicture;
    }

}