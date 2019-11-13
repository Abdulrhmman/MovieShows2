package com.abdelrahmman.movieshows.network;

import androidx.lifecycle.LiveData;

import com.abdelrahmman.movieshows.models.Movie;
import com.abdelrahmman.movieshows.models.MoviesResponse;
import com.abdelrahmman.movieshows.network.responses.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MoviesApi {

    // Main feed
    @GET("movie/popular")
    LiveData<ApiResponse<MoviesResponse>> mainMovies(
            @Query("api_key") String api_key,
            @Query("page") int page
    );

    // Get movie details
    @GET("movie/{movie_id}")
    LiveData<ApiResponse<Movie>> movieDetails(
            @Path("movie_id") int movie_id,
            @Query("api_key") String api_key
    );

    // Search Movie
    @GET("search/movie")
    LiveData<ApiResponse<MoviesResponse>> searchMovies(
            @Query("api_key") String api_key,
            @Query("page") int page,
            @Query("query") String query
    );

}
