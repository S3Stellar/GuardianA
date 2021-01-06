package com.example.guardiana.pageable;

public interface Pageable {

    int getPageNumber();

    int getPageSize();

    long getOffset();

    Sort getSort();

    boolean hasPrevious();



}
