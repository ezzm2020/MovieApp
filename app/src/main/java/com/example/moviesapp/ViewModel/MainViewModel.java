package com.example.moviesapp.ViewModel;

import android.app.Application;


import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.moviesapp.database.AppDatabase;
import com.example.moviesapp.database.FavoriteEntry;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<FavoriteEntry>> movies;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(getApplication());
//        movies = database.favoriteDao().loadAllMovies();
        movies = database.favoriteDao().loadAllFavorite();
    }

    public LiveData<List<FavoriteEntry>> getMovies() {
        return movies;
    }
}