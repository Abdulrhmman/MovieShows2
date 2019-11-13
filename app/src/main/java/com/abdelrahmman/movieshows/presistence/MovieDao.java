package com.abdelrahmman.movieshows.presistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.abdelrahmman.movieshows.models.Movie;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface MovieDao {

    @Insert(onConflict = REPLACE)
    void insertMovies(Movie... movies);

    @Insert(onConflict = REPLACE)
    void insertMovie(Movie movie);

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' ORDER BY popularity DESC LIMIT (:pageNumber * 20)")
    LiveData<List<Movie>> searchMovies(String query, int pageNumber);

    @Query("SELECT * FROM movies ORDER BY popularity DESC LIMIT (:pageNumber * 20)")
    LiveData<List<Movie>> mainMovies(int pageNumber);

    @Query("SELECT * FROM movies WHERE id = :id")
    LiveData<Movie> getMovie(int id);

}




