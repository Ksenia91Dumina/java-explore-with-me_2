package ru.practicum.addition;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class MyPageRequest extends PageRequest {

    private final int from;

    protected MyPageRequest(int from, int size, Sort sort) {
        super(from / size, size, sort);
        this.from = from;
    }

    public static MyPageRequest of(int from, int size) {
        return new MyPageRequest(from, size, Sort.unsorted());
    }

    @Override
    public long getOffset() {
        return from;
    }
}
