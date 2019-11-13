package com.abdelrahmman.movieshows;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {

    private static AppExecutors instance;

    public static AppExecutors getInstance(){
        if (instance == null){
            instance = new AppExecutors();
        }
        return instance;
    }

    private final Executor diskIO = Executors.newSingleThreadExecutor();

    private final Executor mainThreadExecutor = new MainThreadExecutor();

    public Executor diskIO(){
        return diskIO;
    }

    public Executor mainThread(){
        return mainThreadExecutor;
    }

    private static class MainThreadExecutor implements Executor{
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
