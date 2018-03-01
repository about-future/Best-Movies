package com.future.bestmovies.utils;

import android.app.Activity;
import android.content.Context;
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

    // This method will build and return the image URL used by our RecyclerView. The image size
    // will be extracted from our preferences that were created on the first run of the app
    public static String buildImageUrlForRecyclerView(Context context, String imagePath) {
        String imageSizeWidth = MoviePreferences.getImageSizeForRecyclerView(context);
        String imageUrl = IMAGES_BASE_URL.concat(imageSizeWidth).concat(imagePath);

        //Log.v(TAG, "URL: " + imageUrl);
        return imageUrl;
    }

    // This method will build and return the image URL, based on the imagePath (i.e. "/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg")
    // and on the imageType that is expected (i.e. backdrop or poster)
    public static String buildImageUrlWithImageType(Context context, String imagePath, String imageType) {
        // Get optimal image size for the device screen
        String[] optimalImageSize = getOptimalImageSize(context);
        String imageSize;

        // Check what imageType is expected
        if (TextUtils.equals(imageType, BACKDROP)) {
            imageSize = optimalImageSize[0];
        } else {
            imageSize = optimalImageSize[1];
        }

        // Create the image URL and return it
        String imageUrl = IMAGES_BASE_URL.concat(imageSize).concat(imagePath);
        //Log.v(TAG, "Image URL: " + imageUrl);

        return imageUrl;
    }

    // This method will return the width and height of the screen in pixels (i.e. {720, 1280})
    private static int[] getScreenSize(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        //Log.v(TAG, "Width:" + width + ", Height: " + height);

        return new int[]{width, height};
    }

    // This method will return a string array, based on the width of the screen size (i.e. "w780")
    private static String[] getOptimalImageSize (Context context) {
        // Get the width and height of screen and store it in an int array
        int[] screenSize = getScreenSize(context);

        // Use the width of the screen and based on that, generate the string representation of
        // this size and return it.
        return getImageSizeAsString(screenSize[0]);
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
        // After we generate the optimal size as a string, we store it in a preference, so it can be used
        // by our RecyclerView.
        MoviePreferences.setImageSizeForRecyclerView(context, imageSizeAsString[1]);
    }


    // This method will compare the given dimension with certain dimension intervals and generate
    // the optimal image size as a string. The intervals are not randomly generated, they are based
    // on the available width dimensions provided by image.tmdb.org. The selectedScreenSize
    // represents the width of the image in pixels and the returned value could be something like
    // this: {"w780", "w342"} or {"w500", "w185"}
    private static String[] getImageSizeAsString (int selectedScreenSize) {
        String backdropWidth;
        String posterWidth;

        if (selectedScreenSize > 1280 && selectedScreenSize <= 3360) {
            backdropWidth = IMAGE_SIZE_ORIGINAL;
            posterWidth = IMAGE_SIZE780;
        } else if (selectedScreenSize > 780 && selectedScreenSize <= 1280) {
            backdropWidth = IMAGE_SIZE1280;
            posterWidth = IMAGE_SIZE500;
        } else if (selectedScreenSize > 500 && selectedScreenSize <= 780) {
            backdropWidth = IMAGE_SIZE780;
            posterWidth = IMAGE_SIZE342;
        } else if (selectedScreenSize > 342 && selectedScreenSize <= 500) {
            backdropWidth = IMAGE_SIZE500;
            posterWidth = IMAGE_SIZE185;
        } else if (selectedScreenSize > 185 && selectedScreenSize <= 342) {
            backdropWidth = IMAGE_SIZE342;
            posterWidth = IMAGE_SIZE154;
        } else if (selectedScreenSize > 154 && selectedScreenSize <= 185){
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
