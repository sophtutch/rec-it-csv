package com.github.sophtutch.recitcsv;

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
}