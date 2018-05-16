package com.example.anatoly.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener,
        LoaderManager.LoaderCallbacks,
        OnItemClickListener
        {


    RecyclerView moviesPicsRV;
    ProgressBar loadingMoviesPB;
    List<Movie> movies;
    MoviesRecyclerAdapter adapter;

    //Variable to store String value of the image size (which is in strings.xml file)
    //to make different image sizes on different screen sizes
    String imageSize;

    //Key value for passing url in a HTTPTaskLoader bundle
    //Also used in DetailActivity
    public final static String BUNDLE_ARG_URL = "url";

    //Key value for storing selected menu item (popular/top/favorites)
    //and load it in onCreate;
    private final static String BUNDLE_ARG_MENU_POSITION= "menu_position";
    private final static String BUNDLE_ARG_FIRST_VISIBLE= "first_visible";
    private final static String BUNDLE_ARG_WHAT_TO_LOAD= "what_to_load";

    private int menuPosition = 0;

    // What to load in a single loader:
    private final static int LOAD_TOPRATED = 10;
    private final static int LOAD_POPULAR = 11;
    private final static int LOAD_FAVORITES = 12;

    //Loader id
    private final static int LOADER_ALL_IN_1_ID = 30945;

            //Using a single loader's callbacks
            //it comes to work much better than using two different callbacks for http and cursor loaders
            @NonNull
            @Override
            public Loader onCreateLoader(int id, @Nullable Bundle args) {
                if(args==null){throw new UnsupportedOperationException("args must not be null");}
                int whatToLoad = args.getInt(BUNDLE_ARG_WHAT_TO_LOAD);
                switch (whatToLoad){
                    case LOAD_FAVORITES:
                        return new CursorLoader(
                                getApplicationContext(),
                                FavoriteContract.FavoriteEntry.CONTENT_URI,
                                null,
                                null,
                                null,
                                null);
                    case LOAD_TOPRATED:
                            return new HTTPTaskLoader(getApplicationContext(), NetworkUtils.buildMoviesUrl(NetworkUtils.SORT_ORDER.top_rated));
                    case LOAD_POPULAR:
                            return new HTTPTaskLoader(getApplicationContext(), NetworkUtils.buildMoviesUrl(NetworkUtils.SORT_ORDER.popular));
                        default:
                            throw new UnsupportedOperationException("Wrong loader id: "+id);
                }
            }


            @Override
            public void onLoadFinished(@NonNull Loader loader, Object loadedObject) {
                hideProgress();
                movies = new ArrayList<>();
                if(loadedObject instanceof Cursor){
                    Cursor data = (Cursor) loadedObject;

                    //Resetting the position of a cursor
                    //otherwise if it returns previously used cursor, data.moveToNext() will return false
                    //because it was set to the last position previously.
                    data.moveToPosition(-1);
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
                } else if (loadedObject instanceof String){
                    String result = (String) loadedObject;
                    if(result.equals("")){return;}
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
                }

                if(adapter == null){
                    adapter = new MoviesRecyclerAdapter(movies, MainActivity.this, imageSize);
                    moviesPicsRV.setAdapter(adapter);
                } else {
                    adapter.swapMovies(movies);
                }

            }

            @Override
            public void onLoaderReset(@NonNull Loader loader) {

            }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageSize = getString(R.string.image_size_main);


        setContentView(R.layout.activity_main);

        moviesPicsRV = findViewById(R.id.rv_movies_pics);
        loadingMoviesPB = findViewById(R.id.pb_movies_loading);

        moviesPicsRV.setHasFixedSize(true);

        //Spancount is stored in integers.xml file
        //to make 2 columns in portrait mode and 3 columns in landscape mode
        int spanCount = getResources().getInteger(R.integer.columnsCount);
        moviesPicsRV.setLayoutManager(new GridLayoutManager(this, spanCount));
        getSupportLoaderManager().initLoader(LOADER_ALL_IN_1_ID, getBundle(LOAD_TOPRATED), this);
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
        Spinner menuSpinner = (Spinner) item.getActionView();
        String[] orderTypes = getResources().getStringArray(R.array.order_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.menu_spinner_item_layout,orderTypes);
        menuSpinner.setAdapter(adapter);
        menuSpinner.setOnItemSelectedListener(this);
//        menuSpinner.setSelection(menuPosition);
        return super.onCreateOptionsMenu(menu);
    }


    //Saving selected menu item in a bundle
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(BUNDLE_ARG_MENU_POSITION, menuPosition);
        outState.putInt(BUNDLE_ARG_FIRST_VISIBLE,((GridLayoutManager) moviesPicsRV.getLayoutManager()).findFirstCompletelyVisibleItemPosition());
        super.onSaveInstanceState(outState);
    }

            @Override
            protected void onRestoreInstanceState(Bundle savedInstanceState) {
                super.onRestoreInstanceState(savedInstanceState);
                if(savedInstanceState!=null){
                    menuPosition = savedInstanceState.getInt(BUNDLE_ARG_MENU_POSITION,0);
                    int firstItemPosition = savedInstanceState.getInt(BUNDLE_ARG_FIRST_VISIBLE);
                    moviesPicsRV.getLayoutManager().scrollToPosition(firstItemPosition);
                }
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
        int whatToLoad=0;
        switch (position){
            case 0: //TOP RATED
                whatToLoad = LOAD_TOPRATED;
                break;
            case 1: //POPULAR
                whatToLoad = LOAD_POPULAR;
                break;
            case 2: //FAVORITES
                whatToLoad = LOAD_FAVORITES;
                break;
        }
        getSupportLoaderManager().restartLoader(LOADER_ALL_IN_1_ID, getBundle(whatToLoad), this);
        showProgress();
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

    private Bundle getBundle (int whatToLoad){
        Bundle args = new Bundle();
        args.putInt(BUNDLE_ARG_WHAT_TO_LOAD, whatToLoad);
        return args;
    }


    //Processing clicking on movies images
    @Override
    public void itemClicked(int position, View view) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Movie.TAG, movies.get(position));
        startActivity(intent);
    }
    }
