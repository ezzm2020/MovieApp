package com.example.moviesapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.moviesapp.adapter.TrailerAdapter;
import com.example.moviesapp.api.Client;
import com.example.moviesapp.api.Service;
import com.example.moviesapp.data.FavoriteContract;
import com.example.moviesapp.data.FavoriteDbHelper;
import com.example.moviesapp.model.Movie;
import com.example.moviesapp.model.Trailer;
import com.example.moviesapp.model.TrailerResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrailerActivity extends AppCompatActivity {

    private TrailerAdapter adapter;
    private List<Trailer> trailerList;
    private FavoriteDbHelper favoriteDbHelper;
    private Movie favorite;
    String API_KEY_VALUE = "4374ef2c013c2f14cff36be52a3fb3b4";
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer);

        int id = getIntent().getExtras().getInt("mov");
        Toast.makeText(this, "" + id, Toast.LENGTH_SHORT).show();
        recyclerView = findViewById(R.id.recycler_tra);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        try {
            trailerList = new ArrayList<>();

            Client Client = new Client();
            Service apiService = Client.getClient().create(Service.class);
            Call<TrailerResponse> call = apiService.getMovieTrailer(id, API_KEY_VALUE);
            call.enqueue(new Callback<TrailerResponse>() {
                @Override
                public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            List<Trailer> trailer = response.body().getResults();


                            recyclerView.setAdapter(new TrailerAdapter(getApplicationContext(), trailer));
                            recyclerView.smoothScrollToPosition(0);
//                                adapter.notifyDataSetChanged();

                        }
                    }
                }

                @Override
                public void onFailure(Call<TrailerResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(TrailerActivity.this, "Error fetching trailer", Toast.LENGTH_SHORT).show();

                }
            });

        } catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }

    }

}
