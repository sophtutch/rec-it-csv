package com.github.sophtutch.recitcsv;

import java.util.Objects;

class RecItCsvTuple3<F, S, T> {

    private final F first;
    private final S second;
    private final T third;

    RecItCsvTuple3(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    public T getThird() {
        return third;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecItCsvTuple3<?, ?, ?> that = (RecItCsvTuple3<?, ?, ?>) o;
        return Objects.equals(first, that.first) && Objects.equals(second, that.second) && Objects.equals(third, that.third);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }
}