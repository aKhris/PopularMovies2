package com.example.anatoly.popularmovies.Utils;

import com.example.anatoly.popularmovies.TMDBObjects.Movie;
import com.example.anatoly.popularmovies.TMDBObjects.Review;
import com.example.anatoly.popularmovies.TMDBObjects.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JSONUtils {


    private final static String JSON_KEY_RESULTS = "results";

    //Movie keys:
    private final static String JSON_KEY_POSTER_PATH = "poster_path";
    private final static String JSON_KEY_ADULT = "adult";
    private final static String JSON_KEY_OVERVIEW = "overview";
    private final static String JSON_KEY_RELEASE_DATE = "release_date";
    private final static String JSON_KEY_GENRE_IDS = "genre_ids";
    private final static String JSON_KEY_ID = "id";
    private final static String JSON_KEY_ORIGINAL_TITLE = "original_title";
    private final static String JSON_KEY_ORIGINAL_LANG = "original_language";
    private final static String JSON_KEY_TITLE = "title";
    private final static String JSON_KEY_BACKDROP_PATH = "backdrop_path";
    private final static String JSON_KEY_POPULARITY = "popularity";
    private final static String JSON_KEY_VOTE_COUNT= "vote_count";
    private final static String JSON_KEY_VIDEO= "video";
    private final static String JSON_KEY_VOTE_AVR= "vote_average";

    //Video keys:
    private final static String JSON_KEY_ISO_639_1 = "iso_639_1";
    private final static String JSON_KEY_ISO_3166_1 = "iso_3166_1";
    private final static String JSON_KEY_KEY = "key";
    private final static String JSON_KEY_NAME = "name";
    private final static String JSON_KEY_SITE= "site";
    private final static String JSON_KEY_SIZE = "size";
    private final static String JSON_KEY_TYPE = "type";

    //Review keys:
    private final static String JSON_KEY_AUTHOR = "author";
    private final static String JSON_KEY_CONTENT = "content";
    private final static String JSON_KEY_URL = "url";


    /**
     * Function for parsing the whole string that came as HTTP response
     * @return JSONArray of movies-JSONObject's
     */
    public static JSONArray parseResults (String responseString){
        JSONArray array = null;
        try {
            JSONObject resultJSON = new JSONObject(responseString);
            array = resultJSON.getJSONArray(JSON_KEY_RESULTS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return array;
    }


    /**
     * Function for getting Movie object from JSONObject
     * @return Movie object with all fields from jsonMovie
     */
    public static Movie parseMovie (JSONObject jsonMovie){
        Movie movie = new Movie();

        String posterPath = jsonMovie.optString(JSON_KEY_POSTER_PATH);
        boolean adult = jsonMovie.optBoolean(JSON_KEY_ADULT);
        String overview = jsonMovie.optString(JSON_KEY_OVERVIEW);
        String releaseDate = jsonMovie.optString(JSON_KEY_RELEASE_DATE);
        List<Integer> genreIDs = new ArrayList<>();
        try {
            genreIDs = getListFromJSONArray(jsonMovie.getJSONArray(JSON_KEY_GENRE_IDS));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int id = jsonMovie.optInt(JSON_KEY_ID);
        String originalTitle = jsonMovie.optString(JSON_KEY_ORIGINAL_TITLE);
        String originalLang = jsonMovie.optString(JSON_KEY_ORIGINAL_LANG);
        String title = jsonMovie.optString(JSON_KEY_TITLE);
        String backdropPath = jsonMovie.optString(JSON_KEY_BACKDROP_PATH);
        double popularity = jsonMovie.optDouble(JSON_KEY_POPULARITY);
        int voteCount = jsonMovie.optInt(JSON_KEY_VOTE_COUNT);
        boolean video = jsonMovie.optBoolean(JSON_KEY_VIDEO);
        double voteAvr = jsonMovie.optDouble(JSON_KEY_VOTE_AVR);

        movie.setPosterPath(posterPath);
        movie.setAdult(adult);
        movie.setOverview(overview);
        movie.setReleaseDate(releaseDate);
        movie.setGenreIDs(genreIDs);
        movie.setId(id);
        movie.setOriginalTitle(originalTitle);
        movie.setOriginalLang(originalLang);
        movie.setTitle(title);
        movie.setBackdropPath(backdropPath);
        movie.setPopularity(popularity);
        movie.setVoteCount(voteCount);
        movie.setVideo(video);
        movie.setVoteAvr(voteAvr);

        return movie;
    }

    private static Video parseVideo(JSONObject jsonVideo){
        Video video = new Video();

        String id = jsonVideo.optString(JSON_KEY_ID);
        String iso_639_1 = jsonVideo.optString(JSON_KEY_ISO_639_1);
        String iso_3166_1 = jsonVideo.optString(JSON_KEY_ISO_3166_1);
        String key = jsonVideo.optString(JSON_KEY_KEY);
        String name = jsonVideo.optString(JSON_KEY_NAME);
        String site = jsonVideo.optString(JSON_KEY_SITE);
        int size = jsonVideo.optInt(JSON_KEY_SIZE);
        String type = jsonVideo.optString(JSON_KEY_TYPE);

        video.setId(id);
        video.setIso_639_1(iso_639_1);
        video.setIso_3166_1(iso_3166_1);
        video.setKey(key);
        video.setName(name);
        video.setSite(site);
        video.setSize(size);
        video.setType(type);

        return video;
    }

    private static Review parseReview(JSONObject jsonReview){
        Review review = new Review();

        String id = jsonReview.optString(JSON_KEY_ID);
        String author = jsonReview.optString(JSON_KEY_AUTHOR);
        String content = jsonReview.optString(JSON_KEY_CONTENT);
        String url = jsonReview.optString(JSON_KEY_URL);

        review.setId(id);
        review.setAuthor(author);
        review.setContent(content);
        review.setUrl(url);

        return review;
    }


    /**
     * Function that makes a List of Integers from JSONArray
     * @param array - JSONArray that looks like: [1, 2, 3]
     * @return List of Integers or just empty list with size=0
     */
    private static List<Integer> getListFromJSONArray(JSONArray array){
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                list.add(array.getInt(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }


    /**
     * Making a list of movies from HTTP response
     * @param array - result of "parseResults" method
     * @return ArrayList of Video objects
     */
    public static List<Video> getVideoList(JSONArray array){
        List<Video> videos = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject jsonObject = array.getJSONObject(i);
                videos.add(parseVideo(jsonObject));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return videos;
    }

    /**
     * Making a list of reviews from HTTP response
     * @param array - result of "parseResults" method
     * @return ArrayList of Review objects
     */

    public static List<Review> getReviewsList(JSONArray array){
        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject jsonObject = array.getJSONObject(i);
                reviews.add(parseReview(jsonObject));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return reviews;
    }



}
