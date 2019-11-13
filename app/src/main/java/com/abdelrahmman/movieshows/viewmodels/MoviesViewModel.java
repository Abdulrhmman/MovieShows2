package com.abdelrahmman.movieshows.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.abdelrahmman.movieshows.models.Movie;
import com.abdelrahmman.movieshows.repositories.MovieRepository;
import com.abdelrahmman.movieshows.util.Resource;

import java.util.List;

public class MoviesViewModel extends AndroidViewModel {

    private static final String TAG = "MoviesViewModel";

    public static final String QUERY_EXHAUSTED = "No more results.";

    public enum ViewState {MAIN, SEARCH}

    private MutableLiveData<ViewState> viewState;
    private MediatorLiveData<Resource<List<Movie>>> movies = new MediatorLiveData<>();
    private MovieRepository movieRepository;

    private boolean isQueryExhausted;
    private boolean isPerformingQuery;
    private int pageNumber;
    private String query;
    private boolean cancelRequest;
    private long requestStartTime;

    public MoviesViewModel(@NonNull Application application) {
        super(application);
        movieRepository = MovieRepository.getInstance(application);
        init();
    }

    private void init() {
        if (viewState == null) {
            viewState = new MutableLiveData<>();
            viewState.setValue(ViewState.MAIN);
        }
    }

    public LiveData<ViewState> getViewState() {
        return viewState;
    }

    public LiveData<Resource<List<Movie>>> getMovies(){
        return movies;
    }

    public void setViewMain(){
        viewState.setValue(ViewState.MAIN);
    }

    public void mainMoviesApi(int pageNumber){
        if (!isPerformingQuery){
            if (pageNumber == 0){
                pageNumber = 1;
            }
            this.pageNumber = pageNumber;
            isQueryExhausted = false;
            getMainMovies();
        }
    }

    public void mainNextPage(){
        if (!isQueryExhausted && !isPerformingQuery){
            pageNumber++;
            getMainMovies();
        }
    }

    private void getMainMovies(){

        isPerformingQuery = true;
        viewState.setValue(ViewState.MAIN);

        final LiveData<Resource<List<Movie>>> repositorySource = movieRepository.mainMovieApi(pageNumber);

        movies.addSource(repositorySource, new Observer<Resource<List<Movie>>>() {
            @Override
            public void onChanged(Resource<List<Movie>> listResource) {
                if (listResource != null){
                    movies.setValue(listResource);
                    if (listResource.status == Resource.Status.SUCCESS){
                        isPerformingQuery = false;
                        if (listResource.data != null){
                            if (listResource.data.size() == 0){
                                Log.d(TAG, "onChanged: query is exhausted");
                                movies.setValue(new Resource<List<Movie>>(
                                        Resource.Status.ERROR,
                                        listResource.data,
                                        QUERY_EXHAUSTED
                                ));
                            }
                        }
                        movies.removeSource(repositorySource);
                    } else if (listResource.status == Resource.Status.ERROR){
                        isPerformingQuery = false;
                        movies.removeSource(repositorySource);
                    }
                } else {
                    movies.removeSource(repositorySource);
                }
            }
        });
    }

    public void searchMoviesApi(String query, int pageNumber){
        if (!isPerformingQuery){
            if (pageNumber == 0){
                pageNumber = 1;
            }
            this.pageNumber = pageNumber;
            this.query = query;
            isQueryExhausted = false;
            executeSearch();
        }
    }

    public void searchNextPage(){
        if (!isQueryExhausted && !isPerformingQuery){
            pageNumber++;
            executeSearch();
        }
    }

    private void executeSearch(){
        requestStartTime = System.currentTimeMillis();
        cancelRequest = false;
        isPerformingQuery = true;
        viewState.setValue(ViewState.SEARCH);

        final LiveData<Resource<List<Movie>>> repositorySource = movieRepository.searchMovieApi(query, pageNumber);

        movies.addSource(repositorySource, new Observer<Resource<List<Movie>>>() {
            @Override
            public void onChanged(Resource<List<Movie>> listResource) {
                if(!cancelRequest){
                    if (listResource != null){
                        movies.setValue(listResource);
                        if (listResource.status == Resource.Status.SUCCESS){
                            Log.d(TAG, "onChanged: REQUEST TIME: " + (System.currentTimeMillis() - requestStartTime) / 1000 + " seconds.");
                            isPerformingQuery = false;
                            if (listResource.data != null){
                                if (listResource.data.size() == 0){
                                    Log.d(TAG, "onChanged: query is exhausted");
                                    movies.setValue(new Resource<List<Movie>>(
                                            Resource.Status.ERROR,
                                            listResource.data,
                                            QUERY_EXHAUSTED
                                    ));
                                }
                            }
                            movies.removeSource(repositorySource);
                        } else if (listResource.status == Resource.Status.ERROR){
                            Log.d(TAG, "onChanged: REQUEST TIME: " + (System.currentTimeMillis() - requestStartTime) / 1000 + " seconds.");
                            isPerformingQuery = false;
                            movies.removeSource(repositorySource);
                        }
                    } else {
                        movies.removeSource(repositorySource);
                    }
                } else {
                    movies.removeSource(repositorySource);
                }
            }
        });
    }

    public void cancelSearchRequest(){
        if (isPerformingQuery){
            cancelRequest = true;
            isPerformingQuery= false;
            pageNumber = 1;
        }
    }
}
