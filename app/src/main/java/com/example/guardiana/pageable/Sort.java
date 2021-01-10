package com.example.guardiana.pageable;

import com.google.firebase.firestore.Query;

public class Sort {
    private String sortBy;
    private Query.Direction direction;

    public Sort(String sortBy, Query.Direction direction) {
        this.sortBy = sortBy;
        this.direction = direction;
    }

    public Query.Direction getDirection() {
        return direction;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setDirection(Query.Direction direction) {
        this.direction = direction;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    @Override
    public String toString() {
        return "Sort{" +
                "sortBy='" + sortBy + '\'' +
                ", direction=" + direction +
                '}';
    }
}
