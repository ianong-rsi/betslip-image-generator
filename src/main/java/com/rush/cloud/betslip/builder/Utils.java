package com.rush.cloud.betslip.builder;

import java.util.function.Supplier;

public class Utils {
    public static <T> T logMethodExecTime(Supplier<T> r, String process) {
        long start = System.nanoTime();
        T returnVal = r.get();
        long end = System.nanoTime();

        long duration = (end - start) / 1000000;  //divide by 1000000 to get milliseconds.
        System.out.println("[DURATION] - " + process + " - " + duration);
        return returnVal;
    }


}
