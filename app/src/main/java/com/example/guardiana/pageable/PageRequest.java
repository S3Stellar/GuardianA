package com.example.guardiana.pageable;

public class PageRequest extends AbstractPageRequest {
    private final Sort sort;

    protected PageRequest(int page, int size, String sortBy, Direction direction){
        super(page, size);
        sort = new Sort(sortBy, direction);
    }

    public static PageRequest of(int page, int size, String sortBy, Direction direction){
        return new PageRequest(page, size, sortBy, direction);
    }

    public static PageRequest of(int limit, String sortBy, Direction direction) {
        return new PageRequest(0, limit, sortBy, direction);
    }

    @Override
    public Sort getSort() {
        return sort;
    }

}
