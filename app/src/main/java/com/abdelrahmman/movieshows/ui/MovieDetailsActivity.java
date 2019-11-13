package com.abdelrahmman.movieshows.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.abdelrahmman.movieshows.BaseActivity;
import com.abdelrahmman.movieshows.R;
import com.abdelrahmman.movieshows.models.Movie;
import com.abdelrahmman.movieshows.util.Resource;
import com.abdelrahmman.movieshows.viewmodels.MovieDetailsViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import static com.abdelrahmman.movieshows.util.Constants.IMAGE_BASE_URL;

public class MovieDetailsActivity extends BaseActivity {

    private static final String TAG = "MovieDetailsActivity";

    private AppCompatImageView mainImage, posterImage;
    private TextView title, releaseDate, runtime, description;

    private MovieDetailsViewModel movieDetailsViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        mainImage = findViewById(R.id.movie_image);
        posterImage = findViewById(R.id.poster_image);
        title = findViewById(R.id.movie_title);
        releaseDate = findViewById(R.id.release_date);
        runtime = findViewById(R.id.runtime);
        description = findViewById(R.id.description);

        movieDetailsViewModel = ViewModelProviders.of(this).get(MovieDetailsViewModel.class);

        showProgressBar(true);
        getIncomingIntent();

    }

    private void subscribeObservers(final int id) {
        movieDetailsViewModel.movieDetailsApi(id).observe(this, new Observer<Resource<Movie>>() {
            @Override
            public void onChanged(Resource<Movie> movieResource) {
                if (movieResource != null) {
                    if (movieResource.data != null) {
                        switch (movieResource.status) {
                            case LOADING: {
                                showProgressBar(true);
                                break;
                            }
                            case ERROR: {
                                Log.e(TAG, "onChanged: status: ERROR, Movie: " + movieResource.data.getTitle());
                                Log.e(TAG, "onChanged: status: ERROR message: " + movieResource.message);
                                showProgressBar(false);
                                setMoviesProperties(movieResource.data);
                                break;
                            }
                            case SUCCESS: {
                                Log.d(TAG, "onChanged: cache has been refreshed.");
                                Log.d(TAG, "onChanged: status: SUCCESS, Movie: " + movieResource.data.getTitle());
                                showProgressBar(false);
                                setMoviesProperties(movieResource.data);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void displayErrorScreen(String errorMessage){
        title.setText(errorMessage);

        RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.ic_launcher_background);

        Glide.with(this)
                .setDefaultRequestOptions(requestOptions)
                .load(R.drawable.ic_launcher_background)
                .into(mainImage);

        Glide.with(this)
                .setDefaultRequestOptions(requestOptions)
                .load(R.drawable.ic_launcher_background)
                .into(posterImage);

        showProgressBar(false);
    }

    private void getIncomingIntent() {
        if (getIntent().hasExtra("movie")) {
            Movie movie = getIntent().getParcelableExtra("movie");
            Log.d(TAG, "onChanged: title: " + movie.getTitle());
            subscribeObservers(movie.getId());
        }
    }

    private void setMoviesProperties(Movie movie){
        if (movie != null){
            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.black_background).error(R.drawable.black_background);

            Glide.with(this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(IMAGE_BASE_URL + movie.getBackdropPath())
                    .into(mainImage);

            Glide.with(this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(IMAGE_BASE_URL + movie.getPosterPath())
                    .into(posterImage);

            title.setText(movie.getTitle());
            releaseDate.setText(movie.getReleaseDate());
            runtime.setText(movie.getRuntime() + "m");
            description.setText(movie.getDescription());

            description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    description.setMaxLines(Integer.MAX_VALUE);
                }
            });

        } else {
            displayErrorScreen(String.valueOf(R.string.error_message));
        }

        showProgressBar(false);
    }
}
