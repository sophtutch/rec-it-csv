package com.github.recitcsv;

class RecItCsvTuple {

    private String before;
    private Object after;

    RecItCsvTuple(String before, Object after) {
        this.before = before;
        this.after = after;
    }

    public String getBefore() {
        return before;
    }

    public Object getAfter() {
        return after;
    }
}