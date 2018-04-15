package com.github.recitcsv;

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
}