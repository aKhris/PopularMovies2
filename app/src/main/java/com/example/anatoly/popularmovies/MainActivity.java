package com.example.anatoly.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.anatoly.popularmovies.Adapters.MoviesRecyclerAdapter;
import com.example.anatoly.popularmovies.Adapters.OnItemClickListener;
import com.example.anatoly.popularmovies.TMDBObjects.Movie;
import com.example.anatoly.popularmovies.Utils.JSONUtils;
import com.example.anatoly.popularmovies.Utils.NetworkUtils;
import com.example.anatoly.popularmovies.db.FavoriteContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener,
        OnItemClickListener
        {


    RecyclerView moviesPicsRV;
    ProgressBar loadingMoviesPB;
    List<Movie> movies;

    //Variable to store String value of the image size (which is in strings.xml file)
    //to make different image sizes on different screen sizes
    String imageSize;

    //Key value for passing url in a HTTPTaskLoader bundle
    //Also used in DetailActivity
    public final static String BUNDLE_ARG_URL = "arg_url";

    //Key value for storing selected menu item (popular/top/favorites)
    //and load it in onCreate;
    private final static String BUNDLE_ARG_MENU_POSITION= "arg_menu_position";

    private int menuPosition = 0;

    //Loaders ids
    private final static int FAVORITES_LOADER_ID = 30942;
    private final static int HTTPS_LOADER_ID = 30943;




    //Callbacks for CursorLoader
    //Are used to load list of favorite movies from database
    private LoaderManager.LoaderCallbacks<Cursor> favoritesCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
            if(id!=FAVORITES_LOADER_ID){
                throw new UnsupportedOperationException("Wrong loader id: "+id);
            }
            return new CursorLoader(
                    getApplicationContext(),
                    FavoriteContract.FavoriteEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
            movies = new ArrayList<>();
            while(data.moveToNext()){
                Movie movie = new Movie();
                int idIndex = data.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID);
                int titleIndex = data.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_TITLE);
                int posterPathIndex = data.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_POSTER_PATH);
                int voteavrIndex = data.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_VOTE_AVR);
                int overviewIndex = data.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_OVERVIEW);
                int releaseIndex = data.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_RELEASE_DATE);


                movie.setPosterPath(data.getString(posterPathIndex));
                movie.setId(data.getInt(idIndex));
                movie.setOriginalTitle(data.getString(titleIndex));
                movie.setVoteAvr(data.getDouble(voteavrIndex));
                movie.setOverview(data.getString(overviewIndex));
                movie.setReleaseDate(data.getString(releaseIndex));

                movies.add(movie);
            }
            MoviesRecyclerAdapter adapter = new MoviesRecyclerAdapter(movies, MainActivity.this, imageSize);
            moviesPicsRV.setAdapter(adapter);
            hideProgress();
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        }
    };

    //Callbacks for HTTPTaskLoader
    //Are used to load data from Internet
    private LoaderManager.LoaderCallbacks<String> httpCallbacks = new LoaderManager.LoaderCallbacks<String>() {
        @NonNull
        @Override
        public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
            if(args==null || !args.containsKey(BUNDLE_ARG_URL)){
                throw new UnsupportedOperationException("Must pass URL to HTTPTaskLoader!");
            }
            return new HTTPTaskLoader(getApplicationContext(), (URL) args.getSerializable(BUNDLE_ARG_URL));
        }

        @Override
        public void onLoadFinished(@NonNull Loader<String> loader, String result) {
            hideProgress();
            if(result==null || result.equals("")){return;}
            movies = new ArrayList<>();
            JSONArray array = JSONUtils.parseResults(result);
            if(array == null || array.length()==0) {return;}
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject jsonMovie = array.getJSONObject(i);
                    movies.add(JSONUtils.parseMovie(jsonMovie));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            MoviesRecyclerAdapter adapter = new MoviesRecyclerAdapter(movies, MainActivity.this, imageSize);
            moviesPicsRV.setAdapter(adapter);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<String> loader) {

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageSize = getString(R.string.image_size_main);
        if(savedInstanceState!=null){
            menuPosition = savedInstanceState.getInt(BUNDLE_ARG_MENU_POSITION,0);
        }

        setContentView(R.layout.activity_main);

        moviesPicsRV = findViewById(R.id.rv_movies_pics);
        loadingMoviesPB = findViewById(R.id.pb_movies_loading);

        moviesPicsRV.setHasFixedSize(true);

        //Spancount is stored in integers.xml file
        //to make 2 columns in portrait mode and 3 columns in landscape mode
        int spanCount = getResources().getInteger(R.integer.columnsCount);
        GridLayoutManager gridManager = new GridLayoutManager(this, spanCount);
        moviesPicsRV.setLayoutManager(gridManager);
    }

    /**
     * Solution of adding Spinner to the menu got here:
     * https://stackoverflow.com/questions/37250397/how-to-add-a-spinner-next-to-a-menu-in-the-toolbar
     * Adapter uses custom layout for items - to make the background color using one of theme colors
     * (not the standard gray one) and white color for the text
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.menu_spinner);
        Spinner spinner = (Spinner) item.getActionView();
        String[] orderTypes = getResources().getStringArray(R.array.order_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.menu_spinner_item_layout,orderTypes);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(menuPosition);
        return super.onCreateOptionsMenu(menu);
    }


    //Saving selected menu item in a bundle
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(BUNDLE_ARG_MENU_POSITION, menuPosition);
        super.onSaveInstanceState(outState);
    }

    //Processing clicks on menu items
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        menuPosition = position;
        //Internet connections is not important if Favorites (position==2) are to be shown
        if(!NetworkUtils.isOnline(this) && ((position == 0)||(position == 1))){
            Toast.makeText(this, R.string.check_internet, Toast.LENGTH_SHORT).show();
            return;
        }

        //Calling loaders depending of what we are going to load
        switch (position){
            case 0: //TOP RATED
                Bundle args = new Bundle();
                args.putSerializable(BUNDLE_ARG_URL, NetworkUtils.buildMoviesUrl(NetworkUtils.SORT_ORDER.top_rated));
                getSupportLoaderManager().restartLoader(HTTPS_LOADER_ID, args, httpCallbacks);
                showProgress();
                break;
            case 1: //POPULAR
                Bundle args2 = new Bundle();
                args2.putSerializable(BUNDLE_ARG_URL, NetworkUtils.buildMoviesUrl(NetworkUtils.SORT_ORDER.popular));
                getSupportLoaderManager().restartLoader(HTTPS_LOADER_ID, args2, httpCallbacks);
                showProgress();
                break;
            case 2: //FAVORITES
                getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null,favoritesCallbacks);
                showProgress();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Do nothing
    }

    private void showProgress(){
        loadingMoviesPB.setVisibility(View.VISIBLE);
        moviesPicsRV.setVisibility(View.INVISIBLE);
    }

    private void hideProgress(){
        loadingMoviesPB.setVisibility(View.INVISIBLE);
        moviesPicsRV.setVisibility(View.VISIBLE);
    }


    //Processing clicking on movies images
    @Override
    public void itemClicked(int position, View view) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Movie.TAG, movies.get(position));
        startActivity(intent);
    }
}
