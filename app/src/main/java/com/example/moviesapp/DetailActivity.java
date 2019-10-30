package com.example.moviesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.moviesapp.adapter.ReviewAdapter;
import com.example.moviesapp.adapter.TrailerAdapter;
import com.example.moviesapp.api.Client;
import com.example.moviesapp.api.Service;
import com.example.moviesapp.data.FavoriteDbHelper;
import com.example.moviesapp.database.AppDatabase;
import com.example.moviesapp.database.FavoriteEntry;
import com.example.moviesapp.model.Movie;
import com.example.moviesapp.model.Review;
import com.example.moviesapp.model.ReviewResult;
import com.example.moviesapp.model.Trailer;
import com.example.moviesapp.model.TrailerResponse;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    TextView nameof, plots, userrate, rlease;
    ImageView imageView;

    Movie movie;
    String thumbnail, movieName, synopsis, rating, dateOfRelease;
    int movie_id;
    Toolbar toolbar;
    private RecyclerView recyclerView;
    private TrailerAdapter adapter;
    private List<Trailer> trailerList;
    private FavoriteDbHelper favoriteDbHelper;
    private Movie favorite;
    private final AppCompatActivity activity = DetailActivity.this;
    boolean exists;
    public Boolean like = true;
    private LiveData<List<FavoriteEntry>> mMyFavData;
    private int DbId;
    String API_KEY_VALUE = "4374ef2c013c2f14cff36be52a3fb3b4";
    private String title, date, desc, mImageBackdrop, poster, mApiKey;
    private AppDatabase mDb;
    private double rate;
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.thumbnail_image_header);
        nameof = findViewById(R.id.movietitel);
        userrate = findViewById(R.id.rate);
        plots = findViewById(R.id.plots);
        rlease = findViewById(R.id.releasedate);
        Intent intent = getIntent();
//        initViews();

        movie_id = getIntent().getExtras().getInt("id");
        if (intent.hasExtra("movies")) {
            movie = getIntent().getParcelableExtra("movies");
//
//            movie_id = (movie.getId());
//            Toast.makeText(getBaseContext(), ""+movie_id, Toast.LENGTH_SHORT).show();
            loadJSON();
            loadReview();

            thumbnail = movie.getPosterPath();
            movieName = movie.getOriginalTitle();
            synopsis = movie.getOverview();
            rating = Double.toString(movie.getVoteAverage());
            dateOfRelease = movie.getReleaseDate();
            movie_id = movie.getId();
            String poster = "https://image.tmdb.org/t/p/w500" + thumbnail;

            Glide.with(this)
                    .load(poster)
                    .placeholder(R.drawable.load)
                    .into(imageView);
            nameof.setText(movieName);
            plots.setText(synopsis);
            userrate.setText(rating);
            rlease.setText(dateOfRelease);
            setSupportActionBar(toolbar);

            getSupportActionBar().setTitle(movieName);

        } else {
            Toast.makeText(this, "No API Data", Toast.LENGTH_SHORT).show();
        }

        MaterialFavoriteButton favoriteButton = findViewById(R.id.favb);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        favoriteButton.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
            @Override
            public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                if (favorite) {
                    FavoriteEntry favouriteDB = new FavoriteEntry(movie_id, movieName, rate, thumbnail, synopsis);
                    mDb.favoriteDao().insertFavorite(favouriteDB);
                    Snackbar.make(buttonView, "Add Favorite", Snackbar.LENGTH_LONG).show();

                } else {
                    FavoriteEntry favouriteDB = new FavoriteEntry(movie_id, movieName, rate, thumbnail, synopsis);
                    mDb.favoriteDao().deleteFavorite(favouriteDB);
                    Snackbar.make(buttonView, "Delete Favorite", Snackbar.LENGTH_LONG).show();

                }
            }
        });


//        favoriteButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (like) {
//                    FavoriteEntry favouriteDB = new FavoriteEntry(movie_id,movieName , rate, thumbnail,synopsis);
//                    mDb.favoriteDao().insertFavorite(favouriteDB);
//
//                    like = false;
//                } else {
//                    FavoriteEntry favouriteDB = new FavoriteEntry(movie_id,movieName , rate, thumbnail,synopsis);
//                    mDb.favoriteDao().deleteFavorite(favouriteDB);
//                    like = true;
//
//                }
//            }
//        });
        //        favoriteButton.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
//            @Override
//            public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
//                if (favorite) {
//
//                    SharedPreferences.Editor editor = getSharedPreferences("com.example.moviesapp.DetailActivity", MODE_PRIVATE).edit();
//                    editor.commit();
////                    SaveG
//                    saveFavorite();
//                    editor.putBoolean("Favorite movie Added", true);
//                    Snackbar.make(buttonView, "Add Favorite", Snackbar.LENGTH_LONG).show();
//
//                } else {
//                    SharedPreferences.Editor editor = getSharedPreferences("com.example.moviesapp.DetailActivity", MODE_PRIVATE).edit();
//                    favoriteDbHelper = new FavoriteDbHelper(getBaseContext());
//                    favoriteDbHelper.deleteFavorite(movie_id);
//                    editor.commit();
//                    editor.putBoolean("Favorite movie Remove", true);
//                    Snackbar.make(buttonView, "Delete Favorite", Snackbar.LENGTH_LONG).show();
//
//
//                }
//            }
//        });

    }

    private void initViews() {
        trailerList = new ArrayList<>();
        adapter = new TrailerAdapter(this, trailerList);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view1);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        loadJSON();
        loadReview();
    }

    private void loadJSON() {
        try {
            trailerList = new ArrayList<>();

            Client Client = new Client();
            Service apiService = Client.getClient().create(Service.class);
            Call<TrailerResponse> call = apiService.getMovieTrailer(movie_id, API_KEY_VALUE);
            call.enqueue(new Callback<TrailerResponse>() {
                @Override
                public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            List<Trailer> trailer = response.body().getResults();
                            RecyclerView recyclerView = findViewById(R.id.recycler_view1);
                            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                            recyclerView.setLayoutManager(mLayoutManager);

                            recyclerView.setAdapter(new TrailerAdapter(getApplicationContext(), trailer));
                            recyclerView.smoothScrollToPosition(0);
//                                adapter.notifyDataSetChanged();

                        }
                    }
                }

                @Override
                public void onFailure(Call<TrailerResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(DetailActivity.this, "Error fetching trailer", Toast.LENGTH_SHORT).show();

                }
            });

        } catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void saveFavorite() {

        movie = getIntent().getParcelableExtra("movies");
        favoriteDbHelper = new FavoriteDbHelper(this);
        favorite = new Movie();
        int ids = getIntent().getExtras().getInt("id");
//        int id=movie.getId();
        Double rate = movie.getVoteAverage();
        String posterph = movie.getPosterPath();
        favorite.setId(ids);
        favorite.setOriginalTitle(nameof.getText().toString());
        favorite.setPosterPath(posterph);
        favorite.setVoteAverage(rate);
        favorite.setOverview(plots.getText().toString());
        favoriteDbHelper.addFavorite(favorite);
//        final FavoriteContract.FavoriteEntry favoriteEntry = new FavoriteContract.FavoriteEntry(movie_id, movieName, rate, thumbnail, synopsis);
//        AppExecutors.getInstance().diskIO().execute(new Runnable() {
//            @Override
//            public void run() {
//                mDb.favoriteDao().insertFavorite(favoriteEntry);
//            }
//        });
    }
//

    //
//    private void deleteFavorite(final int movie_id){
//        AppExecutors.getInstance().diskIO().execute(new Runnable() {
//            @Override
//            public void run() {
//                mDb.favoriteDao().deleteFavoriteWithId(movie_id);
//            }
//        });
//    }
    //TODO
    private void loadReview() {
        try {

            Client Client = new Client();
            Service apiService = Client.getClient().create(Service.class);
            Call<Review> call = apiService.getReview(movie_id, API_KEY_VALUE);

            call.enqueue(new Callback<Review>() {
                @Override
                public void onResponse(Call<Review> call, Response<Review> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            List<ReviewResult> reviewResults = response.body().getResults();
                            RecyclerView recyclerView2 = findViewById(R.id.review_recyclerview);
                            recyclerView2.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                            recyclerView2.setAdapter(new ReviewAdapter(getApplicationContext(), reviewResults));
                            recyclerView2.smoothScrollToPosition(0);


                        }
                    }
                }

                @Override
                public void onFailure(Call<Review> call, Throwable t) {

                }
            });


        } catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, "unable to fetch data", Toast.LENGTH_SHORT).show();
        }

    }

    private void shareContent() {

        Bitmap bitmap = getBitmapFromView(imageView);
        try {
            File file = new File(this.getExternalCacheDir(), "logicchip.png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_TEXT, movieName);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent, "Share image via"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.share:
                shareContent();
                return true;
            case R.id.fav:
                loadReview();
        }

        return super.onOptionsItemSelected(item);
    }

}
