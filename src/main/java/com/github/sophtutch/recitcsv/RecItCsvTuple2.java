package com.github.sophtutch.recitcsv;

import java.util.Objects;

class RecItCsvTuple2<F, S> {

    private F first;
    private S second;

    RecItCsvTuple2(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecItCsvTuple2<?, ?> that = (RecItCsvTuple2<?, ?>) o;
        return Objects.equals(first, that.first) && Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}