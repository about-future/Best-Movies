package com.future.bestmovies.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.future.bestmovies.data.MoviePreferences;

public class ImageUtils {
    private static final String TAG = ImageUtils.class.getSimpleName();
    private static final String IMAGES_BASE_URL = "http://image.tmdb.org/t/p/";
    public static final String IMAGE_SIZE45 = "w45";
    public static final String IMAGE_SIZE92 = "w92";
    public static final String IMAGE_SIZE154 = "w154";
    public static final String IMAGE_SIZE185 = "w185";
    public static final String IMAGE_SIZE342 = "w342";
    public static final String IMAGE_SIZE500 = "w500";
    public static final String IMAGE_SIZE780 = "w780";
    public static final String IMAGE_SIZE1280 = "w1280";
    public static final String IMAGE_SIZE_ORIGINAL = "original";
    public static final String BACKDROP = "backdrop";
    public static final String POSTER = "poster";
    public static final String OPTIMAL = "optimal";
    public static final String MEDIUM = "medium";
    public static final String SMALL = "small";


    // This method will build and return the image URL, based on the imagePath (i.e. "/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg")
    // and on the expected imageType (i.e. backdrop or poster) with the preferred size (optimal, medium or small)
    public static String buildImageUrlWithImageType(Context context, String imagePath, String imageType) {
        // Get image size as a string, so we can build our image URL
        String[] imageSizeAsString = getImageSize(context);

        String imageSize;
        // Depending on what type of image is expected we will select the appropriate result and
        // store it in imageSize
        if (TextUtils.equals(imageType, BACKDROP)) {
            imageSize = imageSizeAsString[0];
        } else {
            // If imageType is POSTER
            imageSize = imageSizeAsString[1];
        }

        // Create the image URL and return it
        String imageUrl = IMAGES_BASE_URL.concat(imageSize).concat(imagePath);
        Log.v(TAG, "Image URL: " + imageUrl);

        return imageUrl;
    }

    // This method will return the width and height of the screen in pixels (i.e. {720, 1280})
    private static int[] getScreenSize(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        //Log.v(TAG, "Width:" + width + ", Height: " + height);

        return new int[]{width, height};
    }

    // This method will return a string array, based on the width of the screen size (i.e. TODO: "w780")
    private static String[] getImageSize(Context context) {
        // Get the preferred image size (optimal, medium or small)
        String preferredImageSize = MoviePreferences.getPreferredImageSize(context);

        // Get the width and height of screen in pixels
        int[] screenSize = getScreenSize(context);

        // Always use the current width of the screen and based on that, generate the string
        // representation in each case and return it
        switch (preferredImageSize) {
            case MEDIUM:
                // Divide the screen size to 2 and generate the string values for poster and backdrop
                return getImageSizeAsString((int) (screenSize[0] / 2));
            case SMALL:
                // Divide the screen size to 4 and generate the string values for poster and backdrop
                return getImageSizeAsString((int) (screenSize[0] / 4));
            default:
                // Generate the string values for poster and backdrop using the full size value of
                // the screen. This option will get best visual results for the user, but will
                // download the most amount of data.
                return getImageSizeAsString(screenSize[0]);
        }
    }


    // This method will use the screen size of the device (getScreenSize), generate the string
    // representation of that image size and store it in a preference.
    public static void createScreenSizePreference(Context context) {
        // We store the width and height of the device in a int array
        int[] screenSize = getScreenSize(context);

        // This string array will store the string representations of the optimal image size
        // for the movie backdrop and movie poster, based on the smallest dimension of the screen.
        String[] imageSizeAsString;

        // Depending on the orientation of the device one size will be smaller than the other.
        // To optimise the use and loading of internet data we must consider the smallest size of
        // the device as the optimal size for our user. In this case we check witch one is smaller
        // and based on this comparison we generate the string representations for this size.
        if (screenSize[0] < screenSize[1]) {
            imageSizeAsString = getImageSizeAsString(screenSize[0]);
        } else {
            imageSizeAsString = getImageSizeAsString(screenSize[1]);
        }

        // This string representation will be part of the URL that is used to fetch each image in
        // our RecyclerView. Because our RecyclerView will load only posters, we will use only the
        // string representation for the poster size, which is stored in imageSizeAsString[1].
        // After we generate the optimal size as a string, we store it in a preference, so it can
        // be used by our RecyclerView.
        MoviePreferences.setImageSizeForRecyclerView(context, imageSizeAsString[1]);
    }


    // This method will compare the given dimension with certain dimension intervals and generate
    // the optimal image size as a string. The intervals are not randomly generated, they are based
    // on the available width dimensions provided by image.tmdb.org. The selectedScreenSize
    // represents the width of the image in pixels and the returned value could be something like
    // this: {"w780", "w342"} or {"w500", "w185"}
    private static String[] getImageSizeAsString(int selectedScreenSize) {
        String backdropWidth;
        String posterWidth;

        if (selectedScreenSize > 1600) {
            backdropWidth = IMAGE_SIZE_ORIGINAL;
            posterWidth = IMAGE_SIZE780;
        } else if (selectedScreenSize > 1000 && selectedScreenSize <= 1600) {
            backdropWidth = IMAGE_SIZE1280;
            posterWidth = IMAGE_SIZE500;
        } else if (selectedScreenSize > 500 && selectedScreenSize <= 1000) {
            backdropWidth = IMAGE_SIZE780;
            posterWidth = IMAGE_SIZE342;
        } else if (selectedScreenSize > 342 && selectedScreenSize <= 500) {
            backdropWidth = IMAGE_SIZE500;
            posterWidth = IMAGE_SIZE185;
        } else if (selectedScreenSize > 185 && selectedScreenSize <= 342) {
            backdropWidth = IMAGE_SIZE342;
            posterWidth = IMAGE_SIZE154;
        } else if (selectedScreenSize > 154 && selectedScreenSize <= 185) {
            backdropWidth = IMAGE_SIZE185;
            posterWidth = IMAGE_SIZE92;
        } else {
            backdropWidth = IMAGE_SIZE154;
            posterWidth = IMAGE_SIZE45;
        }

        //Log.v(TAG, "Backdrop:" + backdropWidth + ", Poster: " + posterWidth);

        return new String[]{backdropWidth, posterWidth};
    }
}
