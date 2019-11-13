package com.abdelrahmman.movieshows.network;

import com.abdelrahmman.movieshows.util.Constants;
import com.abdelrahmman.movieshows.util.LiveDataCallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.abdelrahmman.movieshows.util.Constants.CONNECTION_TIMEOUT;
import static com.abdelrahmman.movieshows.util.Constants.READ_TIMEOUT;
import static com.abdelrahmman.movieshows.util.Constants.WRITE_TIMEOUT;

public class RetrofitClient {

    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build();

    private static Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addCallAdapterFactory(new LiveDataCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = retrofitBuilder.build();

    private static MoviesApi moviesApi = retrofit.create(MoviesApi.class);

    public static MoviesApi getMoviesApi(){
        return moviesApi;
    }

}
