package com.example.guardiana.async;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncTaskExecutor {
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Handler handler;

    public static void execute() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            //Background work here
            handler.post(() -> {
                //UI Thread work here
            });
        });
    }


}

