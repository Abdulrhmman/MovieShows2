package com.abdelrahmman.movieshows.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.abdelrahmman.movieshows.AppExecutors;
import com.abdelrahmman.movieshows.models.Movie;
import com.abdelrahmman.movieshows.models.MoviesResponse;
import com.abdelrahmman.movieshows.network.RetrofitClient;
import com.abdelrahmman.movieshows.network.responses.ApiResponse;
import com.abdelrahmman.movieshows.presistence.MovieDao;
import com.abdelrahmman.movieshows.presistence.MovieDatabase;
import com.abdelrahmman.movieshows.util.NetworkBoundResource;
import com.abdelrahmman.movieshows.util.Resource;

import java.util.List;

import static com.abdelrahmman.movieshows.util.Constants.API_KEY;
import static com.abdelrahmman.movieshows.util.Constants.MOVIE_REFRESH_TIME;

public class MovieRepository {

    private static final String TAG = "MovieRepository";

    private static MovieRepository instance;
    private MovieDao movieDao;

    public static MovieRepository getInstance(Context context) {
        if (instance == null) {
            instance = new MovieRepository(context);
        }
        return instance;
    }

    private MovieRepository(Context context) {
        movieDao = MovieDatabase.getInstance(context).getMovieDao();
    }

    public LiveData<Resource<List<Movie>>> mainMovieApi(final int pageNumber) {
        return new NetworkBoundResource<List<Movie>, MoviesResponse>(AppExecutors.getInstance()) {
            @Override
            protected void saveCallResult(@NonNull MoviesResponse item) {
                if (item.getResults() != null) {
                    Movie[] movies = new Movie[item.getResults().size()];
                    movieDao.insertMovies((Movie[]) (item.getResults().toArray(movies)));
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Movie> data) {
                return true;
            }

            @NonNull
            @Override
            protected LiveData<List<Movie>> loadFromDb() {
                return movieDao.mainMovies(pageNumber);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<MoviesResponse>> createCall() {
                return RetrofitClient.getMoviesApi().mainMovies(
                        API_KEY,
                        pageNumber
                );
            }
        }.getAsLiveData();
    }

    public LiveData<Resource<List<Movie>>> searchMovieApi(final String query, final int pageNumber) {
        return new NetworkBoundResource<List<Movie>, MoviesResponse>(AppExecutors.getInstance()) {
            @Override
            protected void saveCallResult(@NonNull MoviesResponse item) {
                if (item.getResults() != null) {
                    Movie[] movies = new Movie[item.getResults().size()];
                    movieDao.insertMovies((Movie[]) (item.getResults().toArray(movies)));
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Movie> data) {
                return true;
            }

            @NonNull
            @Override
            protected LiveData<List<Movie>> loadFromDb() {
                return movieDao.searchMovies(query, pageNumber);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<MoviesResponse>> createCall() {
                return RetrofitClient.getMoviesApi().searchMovies(
                        API_KEY,
                        pageNumber,
                        query
                );
            }
        }.getAsLiveData();
    }

    public LiveData<Resource<Movie>> movieDetailsApi(final int id) {
        return new NetworkBoundResource<Movie, Movie>(AppExecutors.getInstance()) {

            @Override
            protected void saveCallResult(@NonNull Movie item) {
                item.setTimestamp((int) (System.currentTimeMillis() / 1000));
                movieDao.insertMovie(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable Movie data) {
                Log.d(TAG, "shouldFetch: movie: " + data.toString());
                int currentTime = (int) (System.currentTimeMillis() / 1000);
                Log.d(TAG, "shouldFetch: currentTime: " + currentTime);
                int lastRefresh = data.getTimestamp();
                Log.d(TAG, "shouldFetch: lastRefresh: " + lastRefresh);
                Log.d(TAG, "shouldFetch: it's been " + ((currentTime - lastRefresh) / 60 / 60) + " hours since this movie was refreshed.");
                if ((currentTime - data.getTimestamp()) >= MOVIE_REFRESH_TIME) {
                    Log.d(TAG, "shouldFetch: " + true);
                    return true;
                } else {
                    Log.d(TAG, "shouldFetch: " + false);
                    return false;
                }
            }

            @NonNull
            @Override
            protected LiveData<Movie> loadFromDb() {
                return movieDao.getMovie(id);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<Movie>> createCall() {
                return RetrofitClient.getMoviesApi().movieDetails(
                        id,
                        API_KEY
                );
            }
        }.getAsLiveData();
    }

}
