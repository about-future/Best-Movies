package com.future.bestmovies.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.future.bestmovies.data.MoviePreferences;

import java.net.MalformedURLException;
import java.net.URL;


public class ImageUtils {
    private static final String TAG = ImageUtils.class.getSimpleName();
    private static final String IMAGES_BASE_URL = "http://image.tmdb.org/t/p/w";
    public static final int BACKDROP = 1;
    public static final int POSTER = 2;
    public static final int CAST = 3;
    private static final String OPTIMAL = "optimal";
    private static final String MEDIUM = "medium";

    private static final String VIDEO_THUMBNAIL_BASE_URL = "https://img.youtube.com/vi/";
    private static final String VIDEO_THUMBNAIL_SIZE_M = "/mqdefault.jpg"; // 320x180
    private static final String VIDEO_THUMBNAIL_SIZE_H = "/hqdefault.jpg"; // 480x360
    private static final String VIDEO_THUMBNAIL_SIZE_SD = "/sddefault.jpg"; // 640x480

    /* Return an image URL used in DetailsActivity
     * @param context is used to access getImageWidth method
     * @param imagePath (i.e. "/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg")
     * @pram imageType (i.e. backdrop, poster or cast)
     */
    public static String buildImageUrl(Context context, String imagePath, int imageType) {
        // Get the image width, so we can build our image URL (i.e. 1280 or 500 or 342)
        int imageWidth = getImageWidth(context, imageType);
        // Create the image URL and return it
        return IMAGES_BASE_URL.concat(String.valueOf(imageWidth)).concat(imagePath);
        //Log.v(TAG, "Image URL: " + imageUrl);
        //return imageUrl;
    }

    /* Return an image URL for each poster used in MainActivity's RecyclerView
     * @param context is used to access a movie preference method
     * @param imagePath (i.e. "/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg")
     */
    public static String buildImageUrlForRecyclerView(Context context, String imagePath) {
        // Get the image width, so we can build our image URL (i.e. 500 or 342 or 185)
        int imageWidth = MoviePreferences.getImageWidthForRecyclerView(context);
        // Create the image URL and return it
        return IMAGES_BASE_URL.concat(String.valueOf(imageWidth)).concat(imagePath);
    }

    /* Return the image width for backdrop, poster or cast (i.e. 780, 342 or 185)
     * @param context is used to access a movie preference method
     * @param imageType: BACKDROP, POSTER or CAST
     */
    public static int getImageWidth(Context context, int imageType) {
        // Get the preferred image quality (optimal, medium or small)
        String preferredImageQuality = MoviePreferences.getPreferredImageQuality(context);

        // Get the width of screen in DPs
        int screenWidth = ScreenUtils.getScreenWidthInDps(context);

        // For each preferred quality case, generate the correct image width.
        switch (preferredImageQuality) {
            case OPTIMAL:
                // Generate the corrected value for backdrop, poster or cast, using the full width
                // of the screen. This option will get best visual result for the user, but will
                // download the most amount of data.
                return getCorrectedImageWidth(screenWidth, imageType);
            case MEDIUM:
                // Divide the screen width by 2 and generate the corrected value for backdrop,
                // poster or cast, reducing by 2 the amount of downloaded data.
                return getCorrectedImageWidth(Math.round(screenWidth / 2), imageType);
            default:
                // SMALL: Divide the screen width to 4 and generate the corrected value for backdrop,
                // poster or cast, reducing even more the amount of downloaded data.
                return getCorrectedImageWidth(Math.round(screenWidth / 4), imageType);
        }
    }

    /* Compare the given dimension with certain dimension intervals and return the corrected image
     * width. The intervals are not randomly generated, they are based on the available widths
     * provided by image.tmdb.org.
     * The returned value would be something like: 500, 185 or 154, depending on expected imageType
     *
     * @param selectedScreenWidth represents the width of the screen in dps (i.e. 360 or 800)
     * @param imageType: BACKDROP, POSTER or CAST
     */
    private static int getCorrectedImageWidth(int selectedScreenWidth, int imageType) {
        int backdropWidth;
        int posterWidth;
        int castWidth;

        if (selectedScreenWidth > 1600) {
            backdropWidth = 1280;
            posterWidth = 780;
            castWidth = 500;
        } else if (selectedScreenWidth > 1000 && selectedScreenWidth <= 1600) {
            backdropWidth = 1280;
            posterWidth = 500;
            castWidth = 342;
        } else if (selectedScreenWidth > 500 && selectedScreenWidth <= 1000) {
            backdropWidth = 780;
            posterWidth = 342;
            castWidth = 185;
        } else if (selectedScreenWidth > 342 && selectedScreenWidth <= 500) {
            backdropWidth = 500;
            posterWidth = 185;
            castWidth = 154;
        } else if (selectedScreenWidth > 185 && selectedScreenWidth <= 342) {
            backdropWidth = 342;
            posterWidth = 154;
            castWidth = 92;
        } else { // if (selectedScreenWidth <= 185)
            backdropWidth = 185;
            posterWidth = 92;
            castWidth = 45;
        }

        // Depending on what type of image is expected we will return the appropriate result
        switch (imageType) {
            case BACKDROP:
                return backdropWidth;
            case POSTER:
                return posterWidth;
            default:
                // CAST:
                return castWidth;
        }
    }

    public static String buildVideoThumbnailUrl (String movieKey) {
        return VIDEO_THUMBNAIL_BASE_URL.concat(movieKey).concat(VIDEO_THUMBNAIL_SIZE_SD);
    }
}
