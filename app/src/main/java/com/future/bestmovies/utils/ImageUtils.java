package com.future.bestmovies.utils;

import android.content.Context;

import com.future.bestmovies.data.MoviePreferences;

public class ImageUtils {
    private static final String TAG = ImageUtils.class.getSimpleName();
    private static final String IMAGES_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String IMAGE_SIZE45 = "w45";
    private static final String IMAGE_SIZE92 = "w92";
    private static final String IMAGE_SIZE154 = "w154";
    public static final String IMAGE_SIZE185 = "w185";
    private static final String IMAGE_SIZE342 = "w342";
    private static final String IMAGE_SIZE500 = "w500";
    private static final String IMAGE_SIZE780 = "w780";
    private static final String IMAGE_SIZE1280 = "w1280";
    private static final String IMAGE_SIZE_ORIGINAL = "original";
    public static final String BACKDROP = "backdrop";
    public static final String POSTER = "poster";
    public static final String CAST = "cast";
    private static final String OPTIMAL = "optimal";
    private static final String MEDIUM = "medium";


    // This method will build and return the image URL, based on the imagePath (i.e. "/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg")
    // and on the expected imageType (i.e. backdrop, poster or cast) with the preferred size (optimal, medium or small)
    public static String buildImageUrlWithImageType(Context context, String imagePath, String imageType) {
        // Get image size as a string, so we can build our image URL (i.e. "w185", "w342", "w500" and so on)
        String[] imageSizeAsString = getImageSize(context);

        String imageSize;
        // Depending on what type of image is expected we will select the appropriate result and
        // store it in imageSize
        switch (imageType) {
            case BACKDROP:
                imageSize = imageSizeAsString[0];
                break;
            case POSTER:
                imageSize = imageSizeAsString[1];
                break;
            default:
                // CAST:
                imageSize = imageSizeAsString[2];
                break;
        }

        // Create the image URL and return it
        return IMAGES_BASE_URL.concat(imageSize).concat(imagePath);
    }


    // Return a string array for backdrop, poster and cast, based on the width of the screen in DPs
    // and quality selected. (i.e. "w780", "w342", "w185")
    private static String[] getImageSize(Context context) {
        // Get the preferred image quality (optimal, medium or small)
        String preferredImageQuality = MoviePreferences.getPreferredImageQuality(context);

        // Get the width of screen in DPs
        int screenSize = ScreenUtils.getScreenWidthInDps(context);

        // Use the current width of the screen and based on that, generate the string representation
        // for the preferred quality case and return it.
        switch (preferredImageQuality) {
            case OPTIMAL:
                // Generate the string values for backdrop, poster and cast, using the full width
                // of the screen. This option will get best visual results for the user, but will
                // download the most amount of data. (i.e. "
                return getImageSizeAsString(screenSize);
            case MEDIUM:
                // Divide the screen width by 2 and generate the string values for backdrop,
                // poster and cast, reducing by 2 the amount of downloaded data.
                return getImageSizeAsString(Math.round(screenSize / 2));
            default:
                // SMALL: Divide the screen width size to 4 and generate the string values for
                // backdrop, poster and cast, reducing even more the amount of downloaded data.
                return getImageSizeAsString(Math.round(screenSize / 4));
        }
    }


    // This method will compare the given dimension with certain dimension intervals and generate
    // the optimal image size as a string. The intervals are not randomly generated, they are based
    // on the available width dimensions provided by image.tmdb.org. The selectedScreenSize
    // represents the width of the image in pixels and the returned value could be something like
    // this: {"w780", "w342"} or {"w500", "w185"}
    private static String[] getImageSizeAsString(int selectedScreenSize) {
        String backdropWidth;
        String posterWidth;
        String castWidth;

        if (selectedScreenSize > 1600) {
            backdropWidth = IMAGE_SIZE_ORIGINAL;
            posterWidth = IMAGE_SIZE780;
            castWidth = IMAGE_SIZE500;
        } else if (selectedScreenSize > 1000 && selectedScreenSize <= 1600) {
            backdropWidth = IMAGE_SIZE1280;
            posterWidth = IMAGE_SIZE500;
            castWidth = IMAGE_SIZE342;
        } else if (selectedScreenSize > 500 && selectedScreenSize <= 1000) {
            backdropWidth = IMAGE_SIZE780;
            posterWidth = IMAGE_SIZE342;
            castWidth = IMAGE_SIZE185;
        } else if (selectedScreenSize > 342 && selectedScreenSize <= 500) {
            backdropWidth = IMAGE_SIZE500;
            posterWidth = IMAGE_SIZE185;
            castWidth = IMAGE_SIZE154;
        } else if (selectedScreenSize > 185 && selectedScreenSize <= 342) {
            backdropWidth = IMAGE_SIZE342;
            posterWidth = IMAGE_SIZE154;
            castWidth = IMAGE_SIZE92;
        } else if (selectedScreenSize > 154 && selectedScreenSize <= 185) {
            backdropWidth = IMAGE_SIZE185;
            posterWidth = IMAGE_SIZE92;
            castWidth = IMAGE_SIZE45;
        } else {
            backdropWidth = IMAGE_SIZE154;
            posterWidth = IMAGE_SIZE45;
            castWidth = IMAGE_SIZE45;
        }

        return new String[]{backdropWidth, posterWidth, castWidth};
    }
}
