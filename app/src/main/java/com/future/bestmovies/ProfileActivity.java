package com.future.bestmovies;

import android.content.Intent;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.TimeUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.future.bestmovies.data.Actor;
import com.future.bestmovies.data.ActorLoader;
import com.future.bestmovies.data.Cast;
import com.future.bestmovies.data.CastLoader;
import com.future.bestmovies.data.Credits;
import com.future.bestmovies.data.CreditsAdapter;
import com.future.bestmovies.data.CreditsLoader;
import com.future.bestmovies.data.MovieCategoryAdapter;
import com.future.bestmovies.utils.ImageUtils;
import com.future.bestmovies.utils.ScreenUtils;
import com.squareup.picasso.Picasso;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

import static com.future.bestmovies.DetailsActivity.*;

public class ProfileActivity extends AppCompatActivity implements CreditsAdapter.ListItemClickListener {

    private static final int ACTOR_LOADER_ID = 136;
    private static final int CREDITS_LOADER_ID = 354;
    private static final String IS_FAVOURITE_ACTOR_KEY = "is_favourite_actor";

    private int mActorId;
    private String mActorName;
    private String mBackdropPath;
    private boolean mIsFavouriteActor;
    private Toast mToast;
    private ImageView profileBackdropImageView;
    private TextView ageTextView;
    private ImageView profilePictureImageView;
    private TextView genderTextView;
    private TextView birthdayTextView;
    private TextView birthPlaceTextView;
    private TextView biographyTextView;
    private CreditsAdapter mCreditsAdapter;
    private RecyclerView mCreditsRecyclerView;
    private GridLayoutManager mCreditsLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        profileBackdropImageView = findViewById(R.id.profile_backdrop_iv);
        ageTextView = findViewById(R.id.actor_age_tv);
        profilePictureImageView = findViewById(R.id.credit_actor_iv);
        genderTextView = findViewById(R.id.credit_gender_tv);
        birthdayTextView = findViewById(R.id.credit_birthday_tv);
        birthPlaceTextView = findViewById(R.id.credit_place_of_birth_tv);
        biographyTextView = findViewById(R.id.credit_biography_tv);

        mCreditsRecyclerView = findViewById(R.id.credits_rv);
        mCreditsLayoutManager = new GridLayoutManager(this, 3);
        mCreditsRecyclerView.setLayoutManager(mCreditsLayoutManager);
        mCreditsRecyclerView.setHasFixedSize(true);
        mCreditsAdapter = new CreditsAdapter(this, this);
        mCreditsRecyclerView.setAdapter(mCreditsAdapter);

        //mIsFavouriteActor = false;

        // If we have an instance saved and contains our movie object, we use it to populate our UI
        if (savedInstanceState != null && savedInstanceState.containsKey(ACTOR_ID_KEY)) {
            mActorId = savedInstanceState.getInt(ACTOR_ID_KEY);
        } else {
            // Otherwise, we check our intent and see if there is a Movie object or a movieId passed
            // from DetailsActivity, so we can populate our UI. If there isn't we close this activity
            // and display a toast message.
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(ACTOR_ID_KEY)) {
                // If DetailsActivity passed an actor id
                mActorId = intent.getIntExtra(ACTOR_ID_KEY, 10297);
                mActorName = intent.getStringExtra(ACTOR_NAME_KEY);
                mBackdropPath = intent.getStringExtra(MOVIE_BACKDROP_KEY);

            } else {
                closeOnError();
            }
        }

        setTitle(mActorName);

        Picasso.with(this)
                .load(ImageUtils.buildImageUrl(
                        this,
                        mBackdropPath,
                        ImageUtils.BACKDROP))
                .error(R.drawable.ic_landscape)
                .into(profileBackdropImageView);

        getSupportLoaderManager().restartLoader(ACTOR_LOADER_ID, null, actorResultLoaderListener);
        getSupportLoaderManager().restartLoader(CREDITS_LOADER_ID, null, actorCreditsResultLoaderListener);

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putString(MOVIE_BACKDROP_KEY, mBackdropPath);
        outState.putInt(ACTOR_ID_KEY, mActorId);
        outState.putString(ACTOR_NAME_KEY, mActorName);
        outState.putBoolean(IS_FAVOURITE_ACTOR_KEY, mIsFavouriteActor);

        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mBackdropPath = savedInstanceState.getString(MOVIE_BACKDROP_KEY);
        mActorId = savedInstanceState.getInt(ACTOR_ID_KEY);
        mActorName = savedInstanceState.getString(ACTOR_NAME_KEY);
        mIsFavouriteActor = savedInstanceState.getBoolean(IS_FAVOURITE_ACTOR_KEY);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);

        MenuItem favouritesMenuItem = menu.findItem(R.id.action_favourite_actor);
        if (mIsFavouriteActor) {
            DrawableCompat.setTint(favouritesMenuItem.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        } else {
            DrawableCompat.setTint(favouritesMenuItem.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
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
            } else {
                DrawableCompat.setTint(item.getIcon(), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
            }

            mIsFavouriteActor = !mIsFavouriteActor;
            Log.v("FAVOURITE", "ACTOR " + mIsFavouriteActor);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void closeOnError() {
        finish();
        toastThis(getString(R.string.details_error_message));
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
                    Picasso.with(getApplicationContext())
                            .load(ImageUtils.buildImageUrl(
                                    getApplicationContext(),
                                    actorDetails.getProfilePath(),
                                    ImageUtils.POSTER))
                            .error(R.drawable.ic_person)
                            .into(profilePictureImageView);

                    genderTextView.setText(actorDetails.getGender());
                    birthdayTextView.setText(actorDetails.getBirthday());
                    if (actorDetails.getBirthday().length() > 4) {
                        int birthYear = Integer.valueOf(TextUtils.substring(actorDetails.getBirthday(), 0, 4));
                        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                        ageTextView.setText(getString(R.string.credit_age).concat(Integer.toString(currentYear - birthYear)));
                    } else {
                        ageTextView.setText(getString(R.string.credit_age).concat("unknown"));
                    }

                    birthPlaceTextView.setText(actorDetails.getPlaceOfBirth());
                    biographyTextView.setText(actorDetails.getBiography());

                    setTitle(actorDetails.getActorName());
                    // Populate actor details section
                    //populateActorDetails(actorDetails);
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
                public void onLoadFinished(@NonNull Loader<ArrayList<Credits>> loader, ArrayList<Credits> credits) {
                    mCreditsAdapter.swapCredits(credits);
                }

                @Override
                public void onLoaderReset(@NonNull Loader<ArrayList<Credits>> loader) {

                }
            };

    @Override
    public void onListItemClick(Credits creditsClicked) {
        Intent movieDetailsIntent = new Intent(ProfileActivity.this, DetailsActivity.class);
        movieDetailsIntent.putExtra(DetailsActivity.MOVIE_ID_KEY, creditsClicked.getMovieId());
        movieDetailsIntent.putExtra(DetailsActivity.MOVIE_TITLE_KEY, creditsClicked.getTitle());
        startActivity(movieDetailsIntent);
    }
}
