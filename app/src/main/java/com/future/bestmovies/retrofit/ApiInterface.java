package com.future.bestmovies.retrofit;

import com.future.bestmovies.cast.CastResponse;
import com.future.bestmovies.credits.Actor;
import com.future.bestmovies.credits.CreditsResponse;
import com.future.bestmovies.movie_details.Details;
import com.future.bestmovies.movie.MovieResponse;
import com.future.bestmovies.reviews.ReviewResponse;
import com.future.bestmovies.videos.VideoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("movie/{category}")
    Call<MovieResponse> getMovies(@Path("category") String category, @Query("page") int page, @Query("api_key") String apiKey);

    @GET("movie/{id}")
    Call<Details> getMovieDetails(@Path("id") int movieId, @Query("api_key") String apiKey);

    @GET("movie/{id}/credits")
    Call<CastResponse> getMovieCast(@Path("id") int movieId, @Query("api_key") String apiKey);

    @GET("movie/{id}/videos")
    Call<VideoResponse> getMovieVideos(@Path("id") int movieId, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<ReviewResponse> getMovieReviews(@Path("id") int movieId, @Query("api_key") String apiKey);

    @GET("person/{id}")
    Call<Actor> getActorProfile(@Path("id") int personId, @Query("api_key") String apiKey);

    @GET("person/{id}/movie_credits")
    Call<CreditsResponse> getActorCredits(@Path("id") int personId, @Query("api_key") String apiKey);
}
