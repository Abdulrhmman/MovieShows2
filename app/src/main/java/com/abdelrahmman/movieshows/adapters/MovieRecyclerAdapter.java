package com.abdelrahmman.movieshows.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.abdelrahmman.movieshows.R;
import com.abdelrahmman.movieshows.models.Movie;
import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

import java.util.Collections;
import java.util.List;

import static com.abdelrahmman.movieshows.util.Constants.IMAGE_BASE_URL;

public class MovieRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ListPreloader.PreloadModelProvider<String> {

    private static final int MOVIE_TYPE = 1;
    private static final int QUERY_EXHAUSTED_TYPE = 2;

    private List<Movie> movies;
    private OnMovieClickListener onMovieClickListener;
    private RequestManager requestManager;
    private ViewPreloadSizeProvider<String> preloadSizeProvider;

    public MovieRecyclerAdapter(OnMovieClickListener onMovieClickListener, RequestManager requestManager, ViewPreloadSizeProvider preloadSizeProvider) {
        this.onMovieClickListener = onMovieClickListener;
        this.requestManager = requestManager;
        this.preloadSizeProvider = preloadSizeProvider;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = null;
        switch (viewType) {
            case MOVIE_TYPE: {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_movie_list_item, parent, false);
                return new MoviesViewHolder(view, onMovieClickListener);
            }
            case QUERY_EXHAUSTED_TYPE: {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_query_exhausted, parent, false);
                return new QueryExhaustedViewHolder(view);
            }
            default: {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_movie_list_item, parent, false);
                return new MoviesViewHolder(view, onMovieClickListener);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        int itemViewType = getItemViewType(position);

        if (itemViewType == MOVIE_TYPE) {
            ((MoviesViewHolder)holder).onBind(movies.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (movies.get(position).getTitle().equals(String.valueOf(R.string.query_exhausted_constant))){
            return QUERY_EXHAUSTED_TYPE;
        } else {
            return MOVIE_TYPE;
        }
    }

    public void setQueryExhausted(){
        Movie movie = new Movie();
        movie.setTitle(String.valueOf(R.string.query_exhausted_constant));
        movies.add(movie);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (movies != null) {
            return movies.size();
        }
        return 0;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public List<String> getPreloadItems(int position) {
        String url = movies.get(position).getPosterPath();
        if (TextUtils.isEmpty(url)){
            return Collections.emptyList();
        }
        return Collections.singletonList(url);
    }

    @Nullable
    @Override
    public RequestBuilder<?> getPreloadRequestBuilder(@NonNull String item) {
        return requestManager.load(item);
    }

    public class MoviesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, voteAverage;
        AppCompatImageView image;
        OnMovieClickListener onMovieClickListener;

        public MoviesViewHolder(@NonNull View itemView, OnMovieClickListener onMovieClickListener) {
            super(itemView);

            this.onMovieClickListener = onMovieClickListener;
            title = itemView.findViewById(R.id.movie_title);
            voteAverage = itemView.findViewById(R.id.vote_average);
            image = itemView.findViewById(R.id.movie_image);

            itemView.setOnClickListener(this);
        }

        private void onBind(Movie movie){
            String image_url = IMAGE_BASE_URL + movie.getPosterPath();

            requestManager.load(image_url).into(image);

            title.setText(movie.getTitle());
            voteAverage.setText(String.valueOf(movie.getVoteAverage()));

            preloadSizeProvider.setView(image);
        }

        @Override
        public void onClick(View v) {
            onMovieClickListener.onMovieClick(getAdapterPosition());
        }
    }

    public Movie getSelectedMovie(int position) {
        if (movies != null) {
            if (movies.size() > 0) {
                return movies.get(position);
            }
        }
        return null;
    }

    public class QueryExhaustedViewHolder extends RecyclerView.ViewHolder {

        public QueryExhaustedViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
