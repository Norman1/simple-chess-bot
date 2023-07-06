package com.nghood.simplechess.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class TimeMeasurement {

    private static Map<Category, TimeMeasurementEntry> timesMeasurements = new HashMap<>();

    private static boolean ENABLE_MEASUREMENTS = true;
    static {
        timesMeasurements.put(Category.ALL, new TimeMeasurementEntry());
        timesMeasurements.put(Category.GET_BOARD_VALUE, new TimeMeasurementEntry());
        timesMeasurements.put(Category.FOLLOWUP_BOARD_STATES, new TimeMeasurementEntry());
        timesMeasurements.put(Category.KING_LOSS_CHECK, new TimeMeasurementEntry());
        timesMeasurements.put(Category.COPY_BOARD, new TimeMeasurementEntry());
        timesMeasurements.put(Category.CLONE_PIECE_ARRAY, new TimeMeasurementEntry());
        timesMeasurements.put(Category.ATTACK_BOARD_STATE, new TimeMeasurementEntry());
    }


    private static Stack<Long> openMeasurements = new Stack<>();

    public static void start() {
        if(!ENABLE_MEASUREMENTS){
            return;
        }
        long startTime = System.currentTimeMillis();
        openMeasurements.push(startTime);

    }

    public static void stop(Category category) {
        if(!ENABLE_MEASUREMENTS){
            return;
        }
        long endTime = System.currentTimeMillis();
        long startTime = openMeasurements.pop();
        long executionTime = endTime - startTime;
        long alreadyPresentTime = timesMeasurements.get(category).time;
        int alreadyPresentCalls = timesMeasurements.get(category).amountCalls;
        timesMeasurements.put(category,new TimeMeasurementEntry(executionTime + alreadyPresentTime,alreadyPresentCalls+1));
    }

    public static void printTimes() {
        System.out.println("Execution times:");
        for (Category category : timesMeasurements.keySet()) {
            long timeInSeconds = timesMeasurements.get(category).getTime() / 1000;
            System.out.println(category + ": " + timeInSeconds + " seconds with "+timesMeasurements.get(category).getAmountCalls()+" calls");
        }
    }

    public enum Category {
        ALL, GET_BOARD_VALUE, FOLLOWUP_BOARD_STATES, KING_LOSS_CHECK,COPY_BOARD,CLONE_PIECE_ARRAY,ATTACK_BOARD_STATE
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TimeMeasurementEntry {

        private long time = 0L;
        private int amountCalls = 0;
    }

}
