package org.example.controllers;


import java.util.*;

public class Utility {

    //только для матриц
    //делает колонки рядами и наоборот
    public static <T> List<List<T>> reverseList(List<List<T>> source){
        List<List<T>> target = new ArrayList<>();
        int n = source.get(0).size();

        for (int i = 0; i < n; i++) {
            List<T> newRow = new ArrayList<>();

            for (List<T> row : source) {
                newRow.add(row.get(i));
            }

            target.add(newRow);
        }

        return target;
    }
}
