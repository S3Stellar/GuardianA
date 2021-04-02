package com.example.guardiana.clustermap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.guardiana.model.Element;
import com.example.guardiana.utility.ReportType;
import com.example.guardiana.utility.ReportTypesEnum;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ReportClusterMarker implements ClusterItem {
    private String snippet;
    private Element element;
    private LatLng position;
    private int iconPicture;

    public ReportClusterMarker() { }

    public ReportClusterMarker(String snippet, Element element) {
        this.snippet = snippet;
        this.element = element;
        position = new LatLng(element.getLocation().getLat(), element.getLocation().getLng());
        iconPicture = ReportType.getReportImage(ReportTypesEnum.valueOf(element.getType().toUpperCase()));
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
        return element.getName();
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public int getIconPicture() {
        iconPicture = ReportType.getReportImage(ReportTypesEnum.valueOf(element.getType()));
        return iconPicture;
    }

    public void setIconPicture(int iconPicture) {
        this.iconPicture = iconPicture;
    }

}