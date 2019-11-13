package com.abdelrahmman.movieshows.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abdelrahmman.movieshows.BaseActivity;
import com.abdelrahmman.movieshows.R;
import com.abdelrahmman.movieshows.adapters.MovieRecyclerAdapter;
import com.abdelrahmman.movieshows.adapters.OnMovieClickListener;
import com.abdelrahmman.movieshows.models.Movie;
import com.abdelrahmman.movieshows.util.Resource;
import com.abdelrahmman.movieshows.viewmodels.MoviesViewModel;
import com.abdelrahmman.movieshows.util.SpacingItemDecorator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

import java.util.List;

import static com.abdelrahmman.movieshows.util.Constants.QUERY_SIZE;
import static com.abdelrahmman.movieshows.viewmodels.MoviesViewModel.QUERY_EXHAUSTED;

public class MoviesActivity extends BaseActivity implements OnMovieClickListener {

    private static final String TAG = "MoviesActivity";

    private MoviesViewModel moviesViewModel;
    private RecyclerView recyclerView;
    private MovieRecyclerAdapter adapter;
    private ProgressBar progressBar;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        searchView = findViewById(R.id.search_view);

        moviesViewModel = ViewModelProviders.of(this).get(MoviesViewModel.class);

        initRecyclerView();
        subscribeObservers();
        initSearchView();
    }

    private void subscribeObservers() {

        moviesViewModel.getMovies().observe(this, new Observer<Resource<List<Movie>>>() {
            @Override
            public void onChanged(Resource<List<Movie>> listResource) {
                if (listResource != null) {
                    Log.d(TAG, "onChanged: status: " + listResource.status);
                    if (listResource.data != null) {

                        switch (listResource.status) {
                            case LOADING: {
                                progressBar.setVisibility(View.VISIBLE);
                                break;
                            }
                            case ERROR: {
                                // can't refresh the cache
                                progressBar.setVisibility(View.GONE);
                                adapter.setMovies(listResource.data);
                                Toast.makeText(MoviesActivity.this, listResource.message, Toast.LENGTH_SHORT).show();
                                if (listResource.message.equals(QUERY_EXHAUSTED)) {
                                    adapter.setQueryExhausted();
                                }
                                break;
                            }
                            case SUCCESS: {
                                // cache refreshed
                                progressBar.setVisibility(View.GONE);
                                adapter.setMovies(listResource.data);
                                break;
                            }
                        }
                    }
                }
            }
        });

        moviesViewModel.getViewState().observe(this, new Observer<MoviesViewModel.ViewState>() {
            @Override
            public void onChanged(MoviesViewModel.ViewState viewState) {
                if (viewState != null) {
                    switch (viewState) {
                        case SEARCH: {
                            break;
                        }
                        case MAIN: {
                            displayMainMovies();
                            break;
                        }
                    }
                }
            }
        });
    }

    private RequestManager initGlide() {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.black_background)
                .error(R.drawable.black_background);

        return Glide.with(this).setDefaultRequestOptions(requestOptions);
    }

    private void searchMoviesApi(String query) {
        moviesViewModel.searchMoviesApi(query, 1);
        searchView.clearFocus();
    }

    private void initRecyclerView() {
        ViewPreloadSizeProvider<String> preloadSizeProvider = new ViewPreloadSizeProvider<>();
        adapter = new MovieRecyclerAdapter(this, initGlide(), preloadSizeProvider);
        SpacingItemDecorator itemDecorator = new SpacingItemDecorator(20, 10);
        recyclerView.addItemDecoration(itemDecorator);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        RecyclerViewPreloader<String> preloader = new RecyclerViewPreloader<String>(Glide.with(this),
                adapter, preloadSizeProvider, QUERY_SIZE);

        recyclerView.addOnScrollListener(preloader);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView1, int newState) {
                if (!recyclerView.canScrollVertically(1)) {
                    if (moviesViewModel.getViewState().getValue() == MoviesViewModel.ViewState.MAIN){
                        moviesViewModel.mainNextPage();
                    } else if (moviesViewModel.getViewState().getValue() == MoviesViewModel.ViewState.SEARCH){
                        moviesViewModel.searchNextPage();
                    }

                }
            }
        });

        recyclerView.setAdapter(adapter);

    }

    private void initSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchMoviesApi(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onMovieClick(int position) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra("movie", adapter.getSelectedMovie(position));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (moviesViewModel.getViewState().getValue() == MoviesViewModel.ViewState.MAIN) {
            super.onBackPressed();
        } else {
            moviesViewModel.cancelSearchRequest();
            moviesViewModel.setViewMain();
        }
    }
    private void displayMainMovies() {
        moviesViewModel.mainMoviesApi(1);
    }
}
