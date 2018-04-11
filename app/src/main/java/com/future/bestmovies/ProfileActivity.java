package com.future.bestmovies;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
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
import com.future.bestmovies.utils.ImageUtils;
import com.future.bestmovies.utils.NetworkUtils;
import com.future.bestmovies.utils.ScreenUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.future.bestmovies.DetailsActivity.*;

public class ProfileActivity extends AppCompatActivity implements CreditsAdapter.ListItemClickListener {

    private static final int ACTOR_LOADER_ID = 136;
    private static final int CREDITS_LOADER_ID = 354;
    private static final String IS_FAVOURITE_ACTOR_KEY = "is_favourite_actor";
    public static final String ACTOR_DETAILS_KEY = "actor";
    private static final String MOVIE_CREDITS_KEY = "movie_credits";

    @BindView(R.id.profile_toolbar) Toolbar toolbar;

    // Profile detail variables
    @BindView(R.id.profile_backdrop_iv) ImageView profileBackdropImageView;
    @BindView(R.id.actor_age_tv) TextView ageTextView;
    @BindView(R.id.credit_actor_iv) ImageView profilePictureImageView;
    @BindView(R.id.credit_gender_tv) TextView genderTextView;
    @BindView(R.id.credit_birthday_tv) TextView birthdayTextView;
    @BindView(R.id.credit_place_of_birth_tv) TextView birthPlaceTextView;
    @BindView(R.id.credit_biography_tv) TextView biographyTextView;

    @BindView(R.id.credits_rv) RecyclerView mCreditsRecyclerView;
    private Actor mActor;
    private ArrayList<Credits> mCredits;
    private int mActorId;
    private String mActorName;
    private String mBackdropPath;
    private boolean mIsFavouriteActor;
    private Toast mToast;
    private CreditsAdapter mCreditsAdapter;
    private GridLayoutManager mCreditsLayoutManager;

    // Movie credits variables
    @BindView(R.id.credits_messages_tv) TextView mCreditsMessagesTextView;
    @BindView(R.id.loading_credits_pb) ProgressBar mCreditsProgressBar;
    @BindView(R.id.no_credits_iv) ImageView mNoCreditsImageView;
    @BindView(R.id.no_credits_connection_iv) ImageView mNoCreditsConnectionImageView;

    private MenuItem mFavouriteActorMenuItem;

    // Resources
    @BindString(R.string.credit_date_unknown) String dateUnknown;
    @BindString(R.string.credit_age) String age;

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
                Picasso.get()
                        .load(ImageUtils.buildImageUrl(
                                this,
                                mBackdropPath,
                                ImageUtils.BACKDROP))
                        .error(R.drawable.ic_landscape)
                        .into(profileBackdropImageView);

                if (NetworkUtils.isConnected(this)) {
                    getSupportLoaderManager().restartLoader(ACTOR_LOADER_ID, null, actorResultLoaderListener);
                    hideCredits();
                    getSupportLoaderManager().restartLoader(CREDITS_LOADER_ID, null, actorCreditsResultLoaderListener);
                } else {
                    closeOnError(getString(R.string.no_connection));
                }
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
            Picasso.get()
                    .load(ImageUtils.buildImageUrl(
                            this,
                            mBackdropPath,
                            ImageUtils.BACKDROP))
                    .error(R.drawable.ic_landscape)
                    .into(profileBackdropImageView);
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
        if (savedInstanceState.containsKey(IS_FAVOURITE_ACTOR_KEY))
            mIsFavouriteActor = savedInstanceState.getBoolean(IS_FAVOURITE_ACTOR_KEY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);

        MenuItem favouriteActorMenuItem = menu.findItem(R.id.action_favourite_actor);
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
            if (!mIsFavouriteActor) {
                DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                toastThis(getString(R.string.favourite_actor_insert_successful));
            } else {
                DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
                toastThis(getString(R.string.favourite_actor_delete_successful));
            }

            mIsFavouriteActor = !mIsFavouriteActor;

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

    public void toastThis(String toastMessage) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        mToast.show();
    }

    private LoaderManager.LoaderCallbacks<Actor> actorResultLoaderListener =
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

    private LoaderManager.LoaderCallbacks<ArrayList<Credits>> actorCreditsResultLoaderListener =
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

    private void populateActorDetails(Actor actorDetails) {
        // Profile picture
        Picasso.get()
                .load(ImageUtils.buildImageUrl(
                        getApplicationContext(),
                        actorDetails.getProfilePath(),
                        ImageUtils.POSTER))
                .placeholder(R.drawable.no_picture)
                .error(R.drawable.no_picture)
                .into(profilePictureImageView);

        // Gender
        genderTextView.setText(actorDetails.getGender());

        // Birthday
        birthdayTextView.setText(actorDetails.getBirthday());

        // Age
        int birthYear;
        int endYear;
        if (actorDetails.getBirthday().length() > 4 && !actorDetails.getBirthday().equals(dateUnknown)) {
            birthYear = Integer.valueOf(actorDetails.getBirthday().substring(0, 4));
            if (actorDetails.getDeathday().length() > 4 && !actorDetails.getDeathday().equals(dateUnknown)) {
                endYear = Integer.valueOf(actorDetails.getDeathday().substring(0, 4));
            } else {
                endYear = Calendar.getInstance().get(Calendar.YEAR);
            }
            ageTextView.setText(age.concat(Integer.toString(endYear - birthYear)));
        } else {
            ageTextView.setText(age.concat("unknown"));
        }

        // Birthplace
        birthPlaceTextView.setText(actorDetails.getPlaceOfBirth());

        // Biography
        if (!actorDetails.getBiography().isEmpty()) {
            biographyTextView.setText(actorDetails.getBiography());
        } else {
            biographyTextView.setVisibility(View.INVISIBLE);
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

    @Override
    public void onListItemClick(Credits creditsClicked) {
        Intent movieDetailsIntent = new Intent(ProfileActivity.this, DetailsActivity.class);
        movieDetailsIntent.putExtra(DetailsActivity.MOVIE_ID_KEY, creditsClicked.getMovieId());
        movieDetailsIntent.putExtra(DetailsActivity.MOVIE_TITLE_KEY, creditsClicked.getTitle());
        startActivity(movieDetailsIntent);
    }
}
