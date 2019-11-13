package com.abdelrahmman.movieshows.presistence;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.abdelrahmman.movieshows.models.Movie;

@Database(entities = {Movie.class}, version = 1)
public abstract class MovieDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "movies_db";

    private static MovieDatabase instance;

    public static MovieDatabase getInstance(final Context context){
        if (instance == null){
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    MovieDatabase.class,
                    DATABASE_NAME
            ).build();
        }
        return instance;
    }

    public abstract MovieDao getMovieDao();

}
