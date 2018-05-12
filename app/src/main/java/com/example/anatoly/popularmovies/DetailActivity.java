package com.example.anatoly.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.anatoly.popularmovies.Adapters.OnItemClickListener;
import com.example.anatoly.popularmovies.Adapters.ReviewsRecyclerAdapter;
import com.example.anatoly.popularmovies.Adapters.VideosRecyclerAdapter;
import com.example.anatoly.popularmovies.TMDBObjects.Movie;
import com.example.anatoly.popularmovies.TMDBObjects.Review;
import com.example.anatoly.popularmovies.TMDBObjects.Video;
import com.example.anatoly.popularmovies.Utils.JSONUtils;
import com.example.anatoly.popularmovies.Utils.NetworkUtils;
import com.example.anatoly.popularmovies.db.FavoriteContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.net.URL;
import java.util.List;

import static com.example.anatoly.popularmovies.MainActivity.BUNDLE_ARG_URL;

public class DetailActivity extends AppCompatActivity
        implements View.OnClickListener,
        OnItemClickListener
{

    private final static int FAVORITE_LOADER_ID = 12314;
    private final static int HTTP_VIDEO_LOADER_ID = 12315;
    private final static int HTTP_REVIEWS_LOADER_ID = 12316;

    private Movie movie;
    private ImageView favoriteImageView;

    //Videos block
    private TextView videosLabelTextView;
    private RecyclerView videosRecyclerView;
    private View videosDivider;
    //Reviews block
    private TextView reviewsLabelTextView;
    private RecyclerView reviewsRecyclerView;
    private View reviewsDivider;


    private Uri movieUri;
    private boolean isFavorite = false;

    //Loaders Callbacks /////////////////////////////////////////////////////////////////////
    LoaderManager.LoaderCallbacks<Cursor> favoriteLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
            if(id!=FAVORITE_LOADER_ID){
                throw new UnsupportedOperationException("Wrong loader id: "+id);
            }
            return new CursorLoader(
                    getApplicationContext(),
                    movieUri,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
            if(loader.getId()!=FAVORITE_LOADER_ID){
                throw new UnsupportedOperationException("Wrong loader id: "+loader.getId());
            }
            isFavorite = data.getCount()>0;
            setFavoriteStar();
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        }
    };

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
            if(result==null || result.equals("")){return;}
//            movies = new ArrayList<>();
            JSONArray array = JSONUtils.parseResults(result);
            if(array == null) {return;}
            switch (loader.getId()){
                case HTTP_REVIEWS_LOADER_ID:
                    final List<Review> reviews = JSONUtils.getReviewsList(array);
                    reviewsRecyclerView.setAdapter(new ReviewsRecyclerAdapter(reviews, DetailActivity.this));
                    showReviewsBlock(reviews.size()>0);
                    break;
                case HTTP_VIDEO_LOADER_ID:
                    final List<Video> videos = JSONUtils.getVideoList(array);
                    videosRecyclerView.setAdapter(new VideosRecyclerAdapter(videos, DetailActivity.this));
                    showVideosBlock(videos.size()>0);
                    break;
            }

        }

        @Override
        public void onLoaderReset(@NonNull Loader<String> loader) {

        }
    };

    private void openVideoUrl(String videoKey){
        Intent intent = new Intent (Intent.ACTION_VIEW);
        intent.setData(NetworkUtils.buildYoutubeUri(videoKey));
        startActivity(intent);
    }

    private void openReviewUrl(String url){
        Intent intent = new Intent (Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private Intent createShareVideoIntent(String key) {
        return ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(NetworkUtils.buildYoutubeUri(key).toString())
                .getIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String imageSize = getString(R.string.image_size_detail);
        setContentView(R.layout.activity_detail);

        if(getSupportActionBar()!=null) {
            getSupportActionBar().setTitle(R.string.detail_activity_title);
        }

        //Views init:
        TextView originalTitleTextView = findViewById(R.id.tv_details_original_title);
        ImageView moviePosterImageView = findViewById(R.id.iv_details_movie_poster);
        TextView overviewTextView = findViewById(R.id.tv_details_overview);
        TextView voteAverageTextView = findViewById(R.id.tv_details_vote_average_text);
        TextView releaseDateTextView = findViewById(R.id.tv_details_release_date_text);

        favoriteImageView = findViewById(R.id.iv_favorite_star);
        videosLabelTextView = findViewById(R.id.tv_label_videos);
        reviewsLabelTextView = findViewById(R.id.tv_label_reviews);
        videosDivider = findViewById(R.id.divider1);
        reviewsDivider = findViewById(R.id.divider2);
        videosRecyclerView = findViewById(R.id.rv_movie_videos);
        reviewsRecyclerView = findViewById(R.id.rv_movie_reviews);

        //Setting up recyclerViews
        videosRecyclerView.setHasFixedSize(true);
        reviewsRecyclerView.setHasFixedSize(true);
        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        favoriteImageView.setOnClickListener(this);

        //Getting movie information from Intent
        if(!getIntent().hasExtra(Movie.TAG)){return;}
        this.movie = (Movie) getIntent().getSerializableExtra(Movie.TAG);
        this.movieUri = FavoriteContract.FavoriteEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(movie.getId())).build();

        //Setting up TextViews and ImageView
        originalTitleTextView.setText(movie.getOriginalTitle());
        Picasso.get()
                .load(NetworkUtils.buildImageUri(movie.getPosterPath(), imageSize))
                .into(moviePosterImageView);
        overviewTextView.setText(movie.getOverview());
        String voteString = String.valueOf(movie.getVoteAvr())+"/10";
        voteAverageTextView.setText(voteString);
        releaseDateTextView.setText(movie.getReleaseDate());

        //Actualize the state of Star-image, determining the presense of the movie in DB
        //note that we're not loading movie-data from DB, because the Movie object containing all the stuff
        //is passed in the Intent while starting this activity.
        getSupportLoaderManager().initLoader(FAVORITE_LOADER_ID, null, favoriteLoader);

        //Initialize HTTP loaders if there is a connection
        if(NetworkUtils.isOnline(this)) {

            URL videosURL = NetworkUtils.buildSubFolderURL(movie.getId(), NetworkUtils.SUB_PATH.videos);
            URL reviewsURL = NetworkUtils.buildSubFolderURL(movie.getId(), NetworkUtils.SUB_PATH.reviews);

            Bundle videosBundle = new Bundle();
            Bundle reviewsBundle = new Bundle();

            videosBundle.putSerializable(BUNDLE_ARG_URL, videosURL);
            reviewsBundle.putSerializable(BUNDLE_ARG_URL, reviewsURL);

            getSupportLoaderManager().initLoader(HTTP_REVIEWS_LOADER_ID, reviewsBundle, httpCallbacks);
            getSupportLoaderManager().initLoader(HTTP_VIDEO_LOADER_ID, videosBundle, httpCallbacks);
        }
    }




    private void setFavoriteStar(){
        int imageID = isFavorite?
                android.R.drawable.btn_star_big_on:
                android.R.drawable.btn_star_big_off;
            favoriteImageView.setImageResource(imageID);
    }

    private void showVideosBlock(boolean show){
        if(show){
            videosLabelTextView.setVisibility(View.VISIBLE);
            videosRecyclerView.setVisibility(View.VISIBLE);
            videosDivider.setVisibility(View.VISIBLE);
        } else {
            videosLabelTextView.setVisibility(View.GONE);
            videosRecyclerView.setVisibility(View.GONE);
            videosDivider.setVisibility(View.GONE);
        }
    }

    private void showReviewsBlock(boolean show){
        if(show){
            reviewsLabelTextView.setVisibility(View.VISIBLE);
            reviewsRecyclerView.setVisibility(View.VISIBLE);
            reviewsDivider.setVisibility(View.VISIBLE);
        } else {
            reviewsLabelTextView.setVisibility(View.GONE);
            reviewsRecyclerView.setVisibility(View.GONE);
            reviewsDivider.setVisibility(View.GONE);
        }
    }



    //Processing clicks on Star icon
    //if the movie in DB - remove it from there
    //if not - insert using ContentResolver
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_favorite_star:
                if(isFavorite) {
                    getContentResolver().delete(movieUri,null, null);
                }
                else {
                    ContentValues cv = new ContentValues();
                    cv.put(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID, movie.getId());
                    cv.put(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_TITLE, movie.getOriginalTitle());
                    cv.put(FavoriteContract.FavoriteEntry.COLUMN_OVERVIEW, movie.getOverview());
                    cv.put(FavoriteContract.FavoriteEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
                    cv.put(FavoriteContract.FavoriteEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
                    cv.put(FavoriteContract.FavoriteEntry.COLUMN_VOTE_AVR, movie.getVoteAvr());
                    getContentResolver().insert(movieUri, cv);
                }
                isFavorite = !isFavorite;
                setFavoriteStar();
                break;
        }
    }


    //Processing clicks from recyclerviews items:
    //-review icon
    //-video play icon
    //-video share icon
    @Override
    public void itemClicked(int position, View view) {
        String tag = (String) view.getTag();
        if(tag==null || tag.isEmpty()){return;}
        switch (view.getId()){
            case R.id.iv_review:
                openReviewUrl(tag);
                break;
            case R.id.iv_video_play:
                openVideoUrl(tag);
                break;
            case R.id.iv_video_share:
                startActivity(createShareVideoIntent(tag));
                break;
        }
    }
}
