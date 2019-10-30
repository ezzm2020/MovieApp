package com.example.moviesapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.moviesapp.ViewModel.MainViewModel;
import com.example.moviesapp.adapter.MoviesAdapter;
import com.example.moviesapp.api.Client;
import com.example.moviesapp.api.Service;
import com.example.moviesapp.data.FavoriteDbHelper;
import com.example.moviesapp.database.FavoriteEntry;
import com.example.moviesapp.model.Movie;
import com.example.moviesapp.model.MoviesResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    String API_KEY_VALUE = "4374ef2c013c2f14cff36be52a3fb3b4";

    RecyclerView recyclerView;
    MoviesAdapter adapter;
    List<Movie> movieList;
    ProgressDialog dialog;

    FavoriteDbHelper favoriteDbHelper;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.main_content);
        init();
        favoriteDbHelper=new FavoriteDbHelper(getBaseContext());
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_orange_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                init();

                // Toast.makeText(MainActivity.this, "Refresh", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public Activity getActivity() {
        Context context = this;
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;

    }

    private void init() {

        movieList = new ArrayList<>();
        adapter = new MoviesAdapter(this, movieList);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        }
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        loadJSON();
    }

    private void loadJSON() {

        Client Client = new Client();
        Service apiService =
                Client.getClient().create(Service.class);
        Call<MoviesResponse> call = apiService.getPopularMovies(API_KEY_VALUE);
        call.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                List<Movie> movies = response.body().getResults();
                recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                recyclerView.smoothScrollToPosition(0);
                if (swipeRefreshLayout.isRefreshing())
                {
                    swipeRefreshLayout.setRefreshing(false);
                }
//                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.d("Error", t.getMessage());
                Toast.makeText(MainActivity.this, "Error Fetching Data!", Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                loadDB();
                return true;
            case R.id.menu_top:
                loadJSONtop();
            case R.id.menu_pop:
                loadJSON();

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private void loadDB() {
        MainViewModel mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        LiveData<List<FavoriteEntry>> favourite = mainViewModel.getMovies();
        favourite.observe(this, new Observer<List<FavoriteEntry>>() {
            @Override
            public void onChanged(@Nullable List<FavoriteEntry> favouriteDBS) {
                for (int i = 0; i < favouriteDBS.size(); i++) {
                    String favPoster = favouriteDBS.get(i).getPosterpath();
                    int id = favouriteDBS.get(i).getId();
                    String favOriginalTitle = favouriteDBS.get(i).getTitle();
                    String favReleaseDate = favouriteDBS.get(i).getOverview();
                    Double favVoteAverage = favouriteDBS.get(i).getUserrating();
                    Movie dataView = new Movie(id,favOriginalTitle,favPoster, favReleaseDate, favVoteAverage);
                    movieList.add(dataView);
                }
                adapter.setMovies(movieList);
            }
        });

    }

    public class LoadFav extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            loadDB();
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            adapter.notifyDataSetChanged();
        }
    }




    private void loadJSONtop() {

        Client Client = new Client();
        Service apiService =
                Client.getClient().create(Service.class);
        Call<MoviesResponse> call = apiService.getTopRatedMovies(API_KEY_VALUE);
        call.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                List<Movie> movies = response.body().getResults();
                recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                recyclerView.smoothScrollToPosition(0);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
//                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.d("Error", t.getMessage());
                Toast.makeText(MainActivity.this, "Error Fetching Data!", Toast.LENGTH_SHORT).show();

            }
        });

    }


}
