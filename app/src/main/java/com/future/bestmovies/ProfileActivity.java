package com.future.bestmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.future.bestmovies.credits.Actor;
import com.future.bestmovies.credits.ActorLoader;
import com.future.bestmovies.credits.Credits;
import com.future.bestmovies.credits.CreditsAdapter;
import com.future.bestmovies.credits.CreditsLoader;
import com.future.bestmovies.data.FavouritesContract;
import com.future.bestmovies.utils.ImageUtils;
import com.future.bestmovies.utils.NetworkUtils;
import com.future.bestmovies.utils.ScreenUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.future.bestmovies.DetailsActivity.*;
import static com.future.bestmovies.data.FavouritesContract.*;

public class ProfileActivity extends AppCompatActivity implements CreditsAdapter.ListItemClickListener {

    private static final int ACTOR_LOADER_ID = 136;
    private static final int CREDITS_LOADER_ID = 435;
    private static final int FAVOURITE_ACTOR_LOADER_ID = 516136;
    private static final int FAVOURITE_CREDITS_LOADER_ID = 516435;
    private static final int CHECK_IF_FAVOURITE_ACTOR_LOADER_ID = 473136;

    private static final String IS_FAVOURITE_ACTOR_KEY = "is_favourite_actor";
    private static final String ACTOR_DETAILS_KEY = "actor";
    private static final String MOVIE_CREDITS_KEY = "movie_credits";
    private static final String CREDITS_POSITION_KEY = "credits_position";

    // Query projection used to check if the actor is a favourite or not
    private static final String[] ACTOR_CHECK_PROJECTION = {ActorsEntry.COLUMN_ACTOR_ID};

    // Query projection used to retrieve actor details
    private static final String[] ACTOR_DETAILED_PROJECTION = {
            ActorsEntry.COLUMN_ACTOR_ID,
            ActorsEntry.COLUMN_BIRTHDAY,
            ActorsEntry.COLUMN_BIOGRAPHY,
            ActorsEntry.COLUMN_DEATH_DAY,
            ActorsEntry.COLUMN_GENDER,
            ActorsEntry.COLUMN_NAME,
            ActorsEntry.COLUMN_PROFILE_PATH,
            ActorsEntry.COLUMN_PLACE_OF_BIRTH
    };

    // Query projection used to retrieve movie credits
    private static final String[] CREDITS_DETAILED_PROJECTION = {
            CreditsEntry.COLUMN_ACTOR_ID,
            CreditsEntry.COLUMN_CHARACTER,
            CreditsEntry.COLUMN_MOVIE_ID,
            CreditsEntry.COLUMN_POSTER_PATH,
            CreditsEntry.COLUMN_RELEASE_DATE,
            CreditsEntry.COLUMN_TITLE
    };

    @BindView(R.id.profile_toolbar)
    Toolbar toolbar;

    // Profile detail variables
    @BindView(R.id.profile_backdrop_iv)
    ImageView profileBackdropImageView;
    @BindView(R.id.actor_age_tv)
    TextView ageTextView;
    @BindView(R.id.credit_actor_iv)
    ImageView profilePictureImageView;
    @BindView(R.id.credit_gender_tv)
    TextView genderTextView;
    @BindView(R.id.credit_birthday_tv)
    TextView birthdayTextView;
    @BindView(R.id.credit_place_of_birth_tv)
    TextView birthPlaceTextView;
    @BindView(R.id.credit_biography_tv)
    TextView biographyTextView;

    @BindView(R.id.credits_rv)
    RecyclerView mCreditsRecyclerView;
    private Actor mActor;
    private ArrayList<Credits> mCredits;
    private int mActorId;
    private String mActorName;
    private String mBackdropPath;
    private boolean mIsFavouriteActor;
    private Toast mToast;
    private CreditsAdapter mCreditsAdapter;
    private GridLayoutManager mCreditsLayoutManager;
    private int mCreditsPosition = RecyclerView.NO_POSITION;

    private Bundle mBundleState;

    // Movie credits variables
    @BindView(R.id.credits_messages_tv)
    TextView mCreditsMessagesTextView;
    @BindView(R.id.loading_credits_pb)
    ProgressBar mCreditsProgressBar;
    @BindView(R.id.no_credits_iv)
    ImageView mNoCreditsImageView;
    @BindView(R.id.no_credits_connection_iv)
    ImageView mNoCreditsConnectionImageView;
    @BindString(R.string.no_connection)
    String noConnection;

    private MenuItem mFavouriteActorMenuItem;

    // Resources
    @BindString(R.string.credit_date_unknown)
    String dateUnknown;
    @BindString(R.string.credit_age)
    String age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mCreditsLayoutManager = new GridLayoutManager(
                this,
                ScreenUtils.getNumberOfColumns(this, 120, 3));
        mCreditsRecyclerView.setLayoutManager(mCreditsLayoutManager);
        mCreditsRecyclerView.setHasFixedSize(false);
        mCreditsAdapter = new CreditsAdapter(this, this);
        mCreditsRecyclerView.setAdapter(mCreditsAdapter);
        mCreditsRecyclerView.setNestedScrollingEnabled(false);

        mCreditsMessagesTextView.setText(R.string.loading);

        if (savedInstanceState == null) {
            // Check our intent and see if there is an actor ID passed from DetailsActivity, so we
            // can populate our UI. If there isn't we close this activity and display a toast message.
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(ACTOR_ID_KEY)) {
                // If DetailsActivity passed an actor id
                mActorId = intent.getIntExtra(ACTOR_ID_KEY, 10297);
                mActorName = intent.getStringExtra(ACTOR_NAME_KEY);
                setTitle(mActorName);

                mBackdropPath = intent.getStringExtra(MOVIE_BACKDROP_KEY);
                // Try loading backdrop image from memory
                Picasso.get()
                        .load(ImageUtils.buildImageUrl(
                                this,
                                mBackdropPath,
                                ImageUtils.BACKDROP))
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(profileBackdropImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                // Yay! We have it!
                            }

                            @Override
                            public void onError(Exception e) {
                                // Try again online, if cache loading failed
                                Picasso.get()
                                        .load(ImageUtils.buildImageUrl(
                                                getApplicationContext(),
                                                mBackdropPath,
                                                ImageUtils.BACKDROP))
                                        .error(R.drawable.ic_landscape)
                                        .into(profileBackdropImageView);
                            }
                        });

                // Check if this actor is a favourite or not
                getSupportLoaderManager().restartLoader(CHECK_IF_FAVOURITE_ACTOR_LOADER_ID, null, favouriteActorResultLoaderListener);
            } else {
                closeOnError(getString(R.string.details_error_message));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(MOVIE_BACKDROP_KEY, mBackdropPath);
        outState.putInt(ACTOR_ID_KEY, mActorId);
        outState.putString(ACTOR_NAME_KEY, mActorName);
        outState.putParcelable(ACTOR_DETAILS_KEY, mActor);
        outState.putParcelableArrayList(MOVIE_CREDITS_KEY, mCredits);
        outState.putBoolean(IS_FAVOURITE_ACTOR_KEY, mIsFavouriteActor);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(MOVIE_BACKDROP_KEY)) {
            mBackdropPath = savedInstanceState.getString(MOVIE_BACKDROP_KEY);
            // Try loading backdrop image from memory
            Picasso.get()
                    .load(ImageUtils.buildImageUrl(
                            this,
                            mBackdropPath,
                            ImageUtils.BACKDROP))
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(profileBackdropImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            // Yay! We have it!
                        }

                        @Override
                        public void onError(Exception e) {
                            // Try again online, if cache loading failed
                            Picasso.get()
                                    .load(ImageUtils.buildImageUrl(
                                            getApplicationContext(),
                                            mBackdropPath,
                                            ImageUtils.BACKDROP))
                                    .error(R.drawable.ic_landscape)
                                    .into(profileBackdropImageView);
                        }
                    });
        }
        if (savedInstanceState.containsKey(ACTOR_ID_KEY))
            mActorId = savedInstanceState.getInt(ACTOR_ID_KEY);
        if (savedInstanceState.containsKey(ACTOR_NAME_KEY))
            mActorName = savedInstanceState.getString(ACTOR_NAME_KEY);
        if (savedInstanceState.containsKey(ACTOR_DETAILS_KEY)) {
            mActor = savedInstanceState.getParcelable(ACTOR_DETAILS_KEY);
            if (mActor != null)
                populateActorDetails(mActor);
        }
        if (savedInstanceState.containsKey(MOVIE_CREDITS_KEY)) {
            mCredits = savedInstanceState.getParcelableArrayList(MOVIE_CREDITS_KEY);
            if (mCredits != null)
                populateCredits(mCredits);
        }

        // Favourite Actor
        if (savedInstanceState.containsKey(IS_FAVOURITE_ACTOR_KEY))
            mIsFavouriteActor = savedInstanceState.getBoolean(IS_FAVOURITE_ACTOR_KEY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBundleState = new Bundle();

        // Save Credits position
        mCreditsPosition = mCreditsLayoutManager.findFirstCompletelyVisibleItemPosition();
        mBundleState.putInt(CREDITS_POSITION_KEY, mCreditsPosition);

        mBundleState.putBoolean(IS_FAVOURITE_ACTOR_KEY, mIsFavouriteActor);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // restore RecyclerView state
        if (mBundleState != null) {
            // Restore Credits position
            mCreditsPosition = mBundleState.getInt(CREDITS_POSITION_KEY);
            if (mCreditsPosition == RecyclerView.NO_POSITION) mCreditsPosition = 0;
            // Scroll the RecyclerView to mCreditsPosition
            mCreditsRecyclerView.smoothScrollToPosition(mCreditsPosition);

            mIsFavouriteActor = mBundleState.getBoolean(IS_FAVOURITE_ACTOR_KEY);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);

        MenuItem favouriteActorMenuItem = menu.findItem(R.id.action_favourite_actor);
        // This item will be used in the near future
        mFavouriteActorMenuItem = favouriteActorMenuItem;
        if (mIsFavouriteActor) {
            DrawableCompat.setTint(favouriteActorMenuItem.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        } else {
            DrawableCompat.setTint(favouriteActorMenuItem.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (id == R.id.action_favourite_actor) {
            if (mIsFavouriteActor)
                deleteFavouriteActor(mActor, item);
            else {
                insertFavouriteActor(mActor, item);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void closeOnError(String message) {
        finish();
        toastThis(message);
    }

    // Hide the progress bar and show credits
    private void showCredits() {
        mCreditsRecyclerView.setVisibility(View.VISIBLE);
        mCreditsProgressBar.setVisibility(View.INVISIBLE);
        mCreditsMessagesTextView.setVisibility(View.INVISIBLE);
        mNoCreditsImageView.setVisibility(View.INVISIBLE);
        mNoCreditsConnectionImageView.setVisibility(View.INVISIBLE);
    }

    // Show progress bar and hide credits
    private void hideCredits() {
        mCreditsRecyclerView.setVisibility(View.GONE);
        mCreditsProgressBar.setVisibility(View.VISIBLE);
        mCreditsMessagesTextView.setVisibility(View.VISIBLE);
        mNoCreditsImageView.setVisibility(View.INVISIBLE);
        mNoCreditsConnectionImageView.setVisibility(View.INVISIBLE);
    }

    // Hide progress bar and show no reviews and message
    private void noCredits() {
        mCreditsRecyclerView.setVisibility(View.GONE);
        mCreditsMessagesTextView.setVisibility(View.VISIBLE);
        mCreditsMessagesTextView.setText(R.string.no_credits);
        mCreditsProgressBar.setVisibility(View.INVISIBLE);
        mNoCreditsImageView.setVisibility(View.VISIBLE);
    }

    private void toastThis(String toastMessage) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        mToast.show();
    }

    private final LoaderManager.LoaderCallbacks<Actor> actorResultLoaderListener =
            new LoaderManager.LoaderCallbacks<Actor>() {
                @NonNull
                @Override
                public Loader<Actor> onCreateLoader(int loaderId, @Nullable Bundle bundle) {
                    switch (loaderId) {
                        case ACTOR_LOADER_ID:
                            // If the loaded id matches ours, return a new cast movie loader
                            return new ActorLoader(getApplicationContext(), mActorId);
                        default:
                            throw new RuntimeException("Loader Not Implemented: " + loaderId);
                    }
                }

                @Override
                public void onLoadFinished(@NonNull Loader<Actor> loader, Actor actorDetails) {
                    mActor = actorDetails;
                    populateActorDetails(actorDetails);
                }

                @Override
                public void onLoaderReset(@NonNull Loader<Actor> loader) {

                }
            };

    private final LoaderManager.LoaderCallbacks<ArrayList<Credits>> actorCreditsResultLoaderListener =
            new LoaderManager.LoaderCallbacks<ArrayList<Credits>>() {
                @NonNull
                @Override
                public Loader<ArrayList<Credits>> onCreateLoader(int loaderId, @Nullable Bundle args) {
                    switch (loaderId) {
                        case CREDITS_LOADER_ID:
                            // If the loaded id matches ours, return a new cast movie loader
                            return new CreditsLoader(getApplicationContext(), mActorId);
                        default:
                            throw new RuntimeException("Loader Not Implemented: " + loaderId);
                    }
                }

                @Override
                public void onLoadFinished(@NonNull Loader<ArrayList<Credits>> loader, ArrayList<Credits> movieCredits) {
                    mCredits = movieCredits;
                    populateCredits(movieCredits);
                }

                @Override
                public void onLoaderReset(@NonNull Loader<ArrayList<Credits>> loader) {

                }
            };

    private final LoaderManager.LoaderCallbacks<Cursor> favouriteActorResultLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @NonNull
                @Override
                public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle args) {
                    switch (loaderId) {
                        case FAVOURITE_ACTOR_LOADER_ID:
                            return new CursorLoader(getApplicationContext(),
                                    FavouritesContract.buildUriWithId(ActorsEntry.CONTENT_URI, mActorId),
                                    ACTOR_DETAILED_PROJECTION,
                                    null,
                                    null,
                                    null);
                        case CHECK_IF_FAVOURITE_ACTOR_LOADER_ID:
                            return new CursorLoader(getApplicationContext(),
                                    FavouritesContract.buildUriWithId(ActorsEntry.CONTENT_URI, mActorId),
                                    ACTOR_CHECK_PROJECTION,
                                    null,
                                    null,
                                    null);
                        default:
                            throw new RuntimeException("Loader Not Implemented: " + loaderId);
                    }
                }

                @Override
                public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
                    switch (loader.getId()) {
                        case FAVOURITE_ACTOR_LOADER_ID:
                            if (cursor != null && cursor.moveToFirst()) {
                                // Find the columns of actor attributes that we're interested in
                                int actorIdColumnIndex = cursor.getColumnIndex(ActorsEntry.COLUMN_ACTOR_ID);
                                int biographyColumnIndex = cursor.getColumnIndex(ActorsEntry.COLUMN_BIOGRAPHY);
                                int birthdayColumnIndex = cursor.getColumnIndex(ActorsEntry.COLUMN_BIRTHDAY);
                                int deathDayColumnIndex = cursor.getColumnIndex(ActorsEntry.COLUMN_DEATH_DAY);
                                int genderColumnIndex = cursor.getColumnIndex(ActorsEntry.COLUMN_GENDER);
                                int nameColumnIndex = cursor.getColumnIndex(ActorsEntry.COLUMN_NAME);
                                int placeColumnIndex = cursor.getColumnIndex(ActorsEntry.COLUMN_PLACE_OF_BIRTH);
                                int profilePathColumnIndex = cursor.getColumnIndex(ActorsEntry.COLUMN_PROFILE_PATH);

                                // Set the extracted value from the Cursor for the given column index and use each
                                // value to create an Actor object
                                mActor = new Actor(
                                        cursor.getInt(actorIdColumnIndex),
                                        cursor.getString(biographyColumnIndex),
                                        cursor.getString(birthdayColumnIndex),
                                        cursor.getString(deathDayColumnIndex),
                                        cursor.getInt(genderColumnIndex),
                                        cursor.getString(nameColumnIndex),
                                        cursor.getString(placeColumnIndex),
                                        cursor.getString(profilePathColumnIndex)
                                );

                                cursor.close();
                                // Populate actor details section
                                populateActorDetails(mActor);
                            }
                            break;

                        case CHECK_IF_FAVOURITE_ACTOR_LOADER_ID:
                            if (cursor != null && cursor.moveToFirst()) {
                                mIsFavouriteActor = true;

                                // As soon as we know the movie is a favourite, color the star, so the user will know it too
                                if (mFavouriteActorMenuItem != null)
                                    DrawableCompat.setTint(mFavouriteActorMenuItem.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                cursor.close();
                                // If it's a favourite actor, load data using a cursor for each section
                                getSupportLoaderManager().initLoader(FAVOURITE_ACTOR_LOADER_ID, null, favouriteActorResultLoaderListener);
                                getSupportLoaderManager().initLoader(FAVOURITE_CREDITS_LOADER_ID, null, favouriteCreditsResultLoaderListener);
                            } else {
                                mIsFavouriteActor = false;
                                if (NetworkUtils.isConnected(getApplicationContext())) {
                                    // Otherwise, use am actor details loader and download the actor details and credits
                                    getSupportLoaderManager().restartLoader(ACTOR_LOADER_ID, null, actorResultLoaderListener);
                                    hideCredits();
                                    getSupportLoaderManager().restartLoader(CREDITS_LOADER_ID, null, actorCreditsResultLoaderListener);
                                } else {
                                    closeOnError(noConnection);
                                }
                            }
                            break;
                    }
                }

                @Override
                public void onLoaderReset(@NonNull Loader<Cursor> loader) {

                }
            };

    private final LoaderManager.LoaderCallbacks<Cursor> favouriteCreditsResultLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @NonNull
                @Override
                public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle args) {
                    switch (loaderId) {
                        case FAVOURITE_CREDITS_LOADER_ID:
                            return new CursorLoader(getApplicationContext(),
                                    FavouritesContract.buildUriWithId(CreditsEntry.CONTENT_URI, mActorId),
                                    CREDITS_DETAILED_PROJECTION,
                                    null,
                                    null,
                                    null);
                        default:
                            throw new RuntimeException("Loader Not Implemented: " + loaderId);
                    }
                }

                @Override
                public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
                    if (cursor != null && cursor.moveToFirst()) {
                        // Find the columns of movie credit attributes that we're interested in
                        int characterColumnIndex = cursor.getColumnIndex(CreditsEntry.COLUMN_CHARACTER);
                        int movieIdColumnIndex = cursor.getColumnIndex(CreditsEntry.COLUMN_MOVIE_ID);
                        int posterPathColumnIndex = cursor.getColumnIndex(CreditsEntry.COLUMN_POSTER_PATH);
                        int releaseDateColumnIndex = cursor.getColumnIndex(CreditsEntry.COLUMN_RELEASE_DATE);
                        int titleColumnIndex = cursor.getColumnIndex(CreditsEntry.COLUMN_TITLE);

                        mCredits = new ArrayList<>();
                        for (int i = 0; i < cursor.getCount(); i++) {
                            cursor.moveToPosition(i);
                            // Set the extracted value from the Cursor for the given column index and use each
                            // value to create a Credit object
                            mCredits.add(new Credits(
                                    cursor.getString(characterColumnIndex),
                                    cursor.getInt(movieIdColumnIndex),
                                    cursor.getString(posterPathColumnIndex),
                                    cursor.getString(releaseDateColumnIndex),
                                    cursor.getString(titleColumnIndex)));
                        }
                        cursor.close();

                        // Populate movie cast section
                        populateCredits(mCredits);
                    } else {
                        noCredits();
                    }
                }

                @Override
                public void onLoaderReset(@NonNull Loader<Cursor> loader) {

                }
            };

    private void populateActorDetails(final Actor actorDetails) {
        // Try loading profile picture from memory
        Picasso.get()
                .load(ImageUtils.buildImageUrl(
                        getApplicationContext(),
                        actorDetails.getProfilePath(),
                        ImageUtils.POSTER))
                .placeholder(R.drawable.no_picture)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(profilePictureImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Yay! We got the picture already!
                    }

                    @Override
                    public void onError(Exception e) {
                        // Try again online, if cache loading failed
                        Picasso.get()
                                .load(ImageUtils.buildImageUrl(
                                        getApplicationContext(),
                                        actorDetails.getProfilePath(),
                                        ImageUtils.POSTER))
                                .placeholder(R.drawable.no_picture)
                                .error(R.drawable.no_picture)
                                .into(profilePictureImageView);
                    }
                });

        // Gender
        switch (actorDetails.getGender()) {
            case 1:
                genderTextView.setText(getText(R.string.gender_female));
                break;
            case 2:
                genderTextView.setText(getString(R.string.gender_male));
                break;
            default:
                genderTextView.setText(getString(R.string.gender_unknown));
        }

        // Birthday
        if (actorDetails.getBirthday() != null)
            birthdayTextView.setText(actorDetails.getBirthday());
        else
            birthdayTextView.setText(getString(R.string.credit_date_unknown));

        // Age
        int birthYear;
        int endYear;
        if (actorDetails.getBirthday() != null) {
            birthYear = Integer.valueOf(actorDetails.getBirthday().substring(0, 4));
            if (actorDetails.getDeathDay() != null) {
                endYear = Integer.valueOf(actorDetails.getDeathDay().substring(0, 4));
            } else {
                endYear = Calendar.getInstance().get(Calendar.YEAR);
            }
            ageTextView.setText(age.concat(Integer.toString(endYear - birthYear)));
        } else {
            ageTextView.setText(age.concat("unknown"));
        }

        // Birthplace
        if (actorDetails.getPlaceOfBirth() != null)
            birthPlaceTextView.setText(actorDetails.getPlaceOfBirth());
        else
            birthPlaceTextView.setText(getString(R.string.credit_date_unknown));

        // Biography
        if (actorDetails.getBiography() != null && actorDetails.getBiography().length() > 0) {
            biographyTextView.setText(actorDetails.getBiography());
        } else {
            biographyTextView.setVisibility(View.GONE);
        }

        // Set title as the name of the actor
        setTitle(actorDetails.getActorName());
    }

    private void populateCredits(ArrayList<Credits> movieCredits) {
        mCreditsAdapter.swapCredits(movieCredits);

        // If movieCredits has data
        if (movieCredits.size() != 0) {
            // Show movie credits
            showCredits();
        } else {
            // Otherwise, hide progress bar and show "No credits available" message
            mCreditsMessagesTextView.setVisibility(View.VISIBLE);
            mCreditsMessagesTextView.setText(R.string.no_credits);
            mCreditsProgressBar.setVisibility(View.INVISIBLE);
            mNoCreditsImageView.setVisibility(View.VISIBLE);
            mNoCreditsConnectionImageView.setVisibility(View.INVISIBLE);
        }
    }

    private void insertFavouriteActor(Actor selectedActor, MenuItem item) {
        int INITIAL_VALUE = -1;

        // Actor details insertion
        ContentValues actorValues = new ContentValues();
        actorValues.put(ActorsEntry.COLUMN_ACTOR_ID, selectedActor.getId());
        actorValues.put(ActorsEntry.COLUMN_BIOGRAPHY, selectedActor.getBiography());
        actorValues.put(ActorsEntry.COLUMN_BIRTHDAY, selectedActor.getBirthday());
        actorValues.put(ActorsEntry.COLUMN_DEATH_DAY, selectedActor.getDeathDay());
        actorValues.put(ActorsEntry.COLUMN_GENDER, selectedActor.getGender());
        actorValues.put(ActorsEntry.COLUMN_NAME, selectedActor.getActorName());
        actorValues.put(ActorsEntry.COLUMN_PLACE_OF_BIRTH, selectedActor.getPlaceOfBirth());
        actorValues.put(ActorsEntry.COLUMN_PROFILE_PATH, selectedActor.getProfilePath());

        Uri actorResponseUri = getContentResolver().insert(ActorsEntry.CONTENT_URI, actorValues);

        // Credits insertion
        ContentValues[] allCreditsValues = new ContentValues[mCredits.size()];
        // For each movie credit, get the data and put it in creditValue
        for (int i = 0; i < mCredits.size(); i++) {
            ContentValues creditValues = new ContentValues();
            creditValues.put(CreditsEntry.COLUMN_ACTOR_ID, selectedActor.getId());
            creditValues.put(CreditsEntry.COLUMN_CHARACTER, mCredits.get(i).getCharacter());
            creditValues.put(CreditsEntry.COLUMN_MOVIE_ID, mCredits.get(i).getMovieId());
            creditValues.put(CreditsEntry.COLUMN_POSTER_PATH, mCredits.get(i).getPosterPath());
            creditValues.put(CreditsEntry.COLUMN_RELEASE_DATE, mCredits.get(i).getReleaseDate());
            creditValues.put(CreditsEntry.COLUMN_TITLE, mCredits.get(i).getTitle());

            // Add each castValues to the array
            allCreditsValues[i] = creditValues;
        }

        // Initialize the value of castResponse
        int creditsResponse = INITIAL_VALUE;
        // If we have credits values to insert, insert them and update the value of creditsResponse
        if (allCreditsValues.length != 0) {
            creditsResponse = getContentResolver().bulkInsert(CreditsEntry.CONTENT_URI, allCreditsValues);
        }

        // Show a toast message depending on whether or not the insertion was successful
        if (actorResponseUri != null && creditsResponse > 0) {
            // The insertion was successful and we can display a toast.
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            toastThis(getString(R.string.favourite_actor_insert_successful));
            mIsFavouriteActor = true;
        } else {
            // Otherwise, if the new content URI is null, then there was an error with insertion.
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
            toastThis(getString(R.string.favourite_actor_insert_failed));
        }
    }

    private void deleteFavouriteActor(Actor selectedActor, MenuItem item) {
        int rowsDeleted = getContentResolver().delete(ActorsEntry.CONTENT_URI,
                ActorsEntry.COLUMN_ACTOR_ID + " =?",
                new String[]{String.valueOf(selectedActor.getId())});

        getContentResolver().delete(CreditsEntry.CONTENT_URI,
                CreditsEntry.COLUMN_ACTOR_ID + " =?",
                new String[]{String.valueOf(selectedActor.getId())});

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted != 0) {
            // Otherwise, the delete was successful and we can display a toast.
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
            toastThis(getString(R.string.favourite_actor_delete_successful));
            mIsFavouriteActor = false;
        } else {
            // Otherwise, if no rows were affected, then there was an error with the delete.
            DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            toastThis(getString(R.string.favourite_actor_delete_failed));
        }
    }

    @Override
    public void onListItemClick(Credits creditsClicked) {
        Intent movieDetailsIntent = new Intent(ProfileActivity.this, DetailsActivity.class);
        movieDetailsIntent.putExtra(DetailsActivity.MOVIE_ID_KEY, creditsClicked.getMovieId());
        movieDetailsIntent.putExtra(DetailsActivity.MOVIE_TITLE_KEY, creditsClicked.getTitle());
        startActivity(movieDetailsIntent);
    }
}
