package com.future.bestmovies.retrofit;

import com.future.bestmovies.cast.Cast;
import com.future.bestmovies.credits.Actor;
import com.future.bestmovies.credits.Credits;
import com.future.bestmovies.movie.MovieDetails;
import com.future.bestmovies.movie.MovieResponse;
import com.future.bestmovies.reviews.Review;
import com.future.bestmovies.videos.Video;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("movie/{category}")
    Call<MovieResponse> getMovies(@Path("category") String category, @Query("page") int page, @Query("api_key") String apiKey);

    @GET("movie/{id}")
    Call<MovieDetails> getMovieDetails(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/credits")
    Call<ArrayList<Cast>> getMovieCast(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/videos")
    Call<ArrayList<Video>> getVideos(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<ArrayList<Review>> getMovieReviews(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("person/{id}")
    Call<Actor> getActorProfile(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("person/{id}/movie_credits")
    Call<ArrayList<Credits>> getActorCredits(@Path("id") int id, @Query("api_key") String apiKey);
}
