package com.abdelrahmman.movieshows.util;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.abdelrahmman.movieshows.AppExecutors;
import com.abdelrahmman.movieshows.network.responses.ApiResponse;

public abstract class NetworkBoundResource<ResultType, RequestType> {

    private static final String TAG = "NetworkBoundResource";

    private AppExecutors appExecutors;
    private MediatorLiveData<Resource<ResultType>> results = new MediatorLiveData<>();

    public NetworkBoundResource(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        init();
    }

    private void init(){

        // update liveData for loading status
        results.setValue((Resource<ResultType>) Resource.loading(null));

        //observe liveData source from local database
        final LiveData<ResultType> dbSource = loadFromDb();

        results.addSource(dbSource, new Observer<ResultType>() {
            @Override
            public void onChanged(ResultType resultType) {
                results.removeSource(dbSource);

                if (shouldFetch(resultType)){
                    // get new data from the network
                    fetchFromNetwork(dbSource);
                } else {
                    results.addSource(dbSource, new Observer<ResultType>() {
                        @Override
                        public void onChanged(ResultType resultType) {
                            setValue(Resource.success(resultType));
                        }
                    });
                }
            }
        });
    }

    private void fetchFromNetwork(final LiveData<ResultType> dbSource){
        // update liveData for loading status
        results.addSource(dbSource, new Observer<ResultType>() {
            @Override
            public void onChanged(ResultType resultType) {
                setValue(Resource.loading(resultType));
            }
        });

        final LiveData<ApiResponse<RequestType>> apiResponse = createCall();

        results.addSource(apiResponse, new Observer<ApiResponse<RequestType>>() {
            @Override
            public void onChanged(final ApiResponse<RequestType> requestTypeApiResponse) {
                results.removeSource(dbSource);
                results.removeSource(apiResponse);

                if (requestTypeApiResponse instanceof  ApiResponse.ApiSuccessResponse){
                    Log.d(TAG, "onChanged: ApiSuccessResponse");

                    appExecutors.diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            //save the response to local database
                            saveCallResult((RequestType) processResponse((ApiResponse.ApiSuccessResponse)requestTypeApiResponse));

                            appExecutors.mainThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    results.addSource(loadFromDb(), new Observer<ResultType>() {
                                        @Override
                                        public void onChanged(ResultType resultType) {
                                            setValue(Resource.success(resultType));
                                        }
                                    });
                                }
                            });
                        }
                    });

                } else if (requestTypeApiResponse instanceof  ApiResponse.ApiEmptyResponse){
                    Log.d(TAG, "onChanged: ApiEmptyResponse");
                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            results.addSource(loadFromDb(), new Observer<ResultType>() {
                                @Override
                                public void onChanged(ResultType resultType) {
                                    setValue(Resource.success(resultType));
                                }
                            });
                        }
                    });
                }else if (requestTypeApiResponse instanceof  ApiResponse.ApiErrorResponse){
                    Log.d(TAG, "onChanged: ApiErrorResponse");
                    results.addSource(dbSource, new Observer<ResultType>() {
                        @Override
                        public void onChanged(ResultType resultType) {
                            setValue(Resource.error(((ApiResponse.ApiErrorResponse) requestTypeApiResponse).getErrorMessage(), resultType));
                        }
                    });
                }
            }
        });
    }

    private ResultType processResponse(ApiResponse.ApiSuccessResponse response){
        return (ResultType) response.getBody();
    }

    private void setValue(Resource<ResultType> newValue){
        if (results.getValue() != newValue){
            results.setValue(newValue);
        }
    }

    // Called to save the result of the API response into the database.
    @WorkerThread
    protected abstract void saveCallResult(@NonNull RequestType item);

    // Called with the data in the database to decide whether to fetch
    // potentially updated data from the network.
    @MainThread
    protected abstract boolean shouldFetch(@Nullable ResultType data);

    // Called to get the cached data from the database.
    @NonNull @MainThread
    protected abstract LiveData<ResultType> loadFromDb();

    // Called to create the API call.
    @NonNull @MainThread
    protected abstract LiveData<ApiResponse<RequestType>> createCall();

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    public final LiveData<Resource<ResultType>> getAsLiveData(){
        return results;
    };
}
