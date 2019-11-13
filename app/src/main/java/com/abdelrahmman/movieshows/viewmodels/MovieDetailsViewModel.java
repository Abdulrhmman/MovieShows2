package com.abdelrahmman.movieshows.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.abdelrahmman.movieshows.models.Movie;
import com.abdelrahmman.movieshows.repositories.MovieRepository;
import com.abdelrahmman.movieshows.util.Resource;

public class MovieDetailsViewModel extends AndroidViewModel {

    private MovieRepository movieRepository;

    public MovieDetailsViewModel(@NonNull Application application) {
        super(application);
        movieRepository = MovieRepository.getInstance(application);
    }

    public LiveData<Resource<Movie>> movieDetailsApi(int id){
        return movieRepository.movieDetailsApi(id);
    }
}
