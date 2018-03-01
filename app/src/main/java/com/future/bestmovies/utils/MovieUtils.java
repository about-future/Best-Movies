package com.future.bestmovies.utils;

import android.content.Context;

import com.future.bestmovies.R;


public class MovieUtils {
    private static final String TAG = MovieUtils.class.getSimpleName();

    public static String getStringMovieGenre(Context context, int genreId) {
        int stringId;
        switch (genreId) {
            case 28:
                stringId = R.string.genre_28;
                break;
            case 12:
                stringId = R.string.genre_12;
                break;
            case 16:
                stringId = R.string.genre_16;
                break;
            case 35:
                stringId = R.string.genre_35;
                break;
            case 80:
                stringId = R.string.genre_80;
                break;
            case 99:
                stringId = R.string.genre_99;
                break;
            case 18:
                stringId = R.string.genre_18;
                break;
            case 10751:
                stringId = R.string.genre_10751;
                break;
            case 14:
                stringId = R.string.genre_14;
                break;
            case 36:
                stringId = R.string.genre_36;
                break;
            case 27:
                stringId = R.string.genre_27;
                break;
            case 10402:
                stringId = R.string.genre_10402;
                break;
            case 9648:
                stringId = R.string.genre_9648;
                break;
            case 10749:
                stringId = R.string.genre_10749;
                break;
            case 878:
                stringId = R.string.genre_878;
                break;
            case 10770:
                stringId = R.string.genre_10770;
                break;
            case 53:
                stringId = R.string.genre_53;
                break;
            case 10752:
                stringId = R.string.genre_10752;
                break;
            case 37:
                stringId = R.string.genre_37;
                break;
            default:
                stringId = R.string.genre_unknown;
        }

        return context.getString(stringId);
    }
}
