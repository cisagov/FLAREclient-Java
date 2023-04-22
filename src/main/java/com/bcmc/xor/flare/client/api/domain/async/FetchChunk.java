package com.bcmc.xor.flare.client.api.domain.async;

import java.util.Objects;

public class FetchChunk<T> {

    private T begin;
    private T end;

    public FetchChunk() {
    }

    public FetchChunk(T begin, T end) {
        this.begin = begin;
        this.end = end;
    }

    public T getBegin() {
        return begin;
    }

    public void setBegin(T begin) {
        this.begin = begin;
    }

    public T getEnd() {
        return end;
    }

    public void setEnd(T end) {
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FetchChunk<?> that = (FetchChunk<?>) o;
        return Objects.equals(begin, that.begin) &&
            Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {

        return Objects.hash(begin, end);
    }
}
