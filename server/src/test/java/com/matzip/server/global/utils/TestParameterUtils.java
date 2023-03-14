package com.matzip.server.global.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestParameterUtils {

    public static class Pair<T1, T2> {
        public T1 first;
        public T2 second;

        public Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }
    }

    @SafeVarargs
    public static List<Pair<List<Object>, Pair<Integer, Integer>>> makeFieldList(
            List<Pair<Object, Integer>>... fields
    ) {
        ArrayList<Pair<List<Object>, Pair<Integer, Integer>>> main = new ArrayList<>();
        List<Object> defaultValues = Arrays.stream(fields)
                .map(list -> list.stream().findFirst().orElseThrow().first).toList();

        for (int i = 0; i < fields.length; i++) {
            List<Pair<Object, Integer>> field = fields[i];
            int idx = i;
            field.forEach( p -> {
                if (p.first == defaultValues.get(idx)) return;
                List<Object> copy = new ArrayList<>(defaultValues);
                copy.set(idx, p.first);
                main.add(new Pair<>(copy, new Pair<>(p.second == null ? -1 : idx, p.second)));
            });
        }

        return main;
    }

}
