package com.example.guardiana.pageable;

public class Sort {
    private String sortBy;
    private Direction direction;

    public Sort(String sortBy, Direction direction){
        this.sortBy = sortBy;
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setDirection(Direction direction) {
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
