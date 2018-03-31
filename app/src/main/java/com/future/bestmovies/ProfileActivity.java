package com.future.bestmovies;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.future.bestmovies.data.Actor;
import com.future.bestmovies.data.ActorLoader;
import com.future.bestmovies.data.Cast;
import com.future.bestmovies.data.CastLoader;

import java.util.ArrayList;

import static com.future.bestmovies.DetailsActivity.*;

public class ProfileActivity extends AppCompatActivity {

    private static final int ACTOR_LOADER_ID = 136;
    private static final int CREDITS_LOADER_ID = 354;

    private int mActorId;
    private Toast mToast;
    private TextView testTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // If we have an instance saved and contains our movie object, we use it to populate our UI
        if (savedInstanceState != null && savedInstanceState.containsKey(ACTOR_ID)) {
            mActorId = savedInstanceState.getInt(ACTOR_ID);
        } else {
            // Otherwise, we check our intent and see if there is a Movie object or a movieId passed
            // from DetailsActivity, so we can populate our UI. If there isn't we close this activity
            // and display a toast message.
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(ACTOR_ID)) {
                // If DetailsActivity passed an actor id
                mActorId = intent.getIntExtra(ACTOR_ID, 10297);
            } else {
                closeOnError();
            }
        }

        testTextView = findViewById(R.id.testText);

        getSupportLoaderManager().restartLoader(ACTOR_LOADER_ID, null, actorResultLoaderListener);
        //start loader with actorId

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
                @Override
                public Loader<Actor> onCreateLoader(int loaderId, Bundle bundle) {
                    switch (loaderId) {
                        case ACTOR_LOADER_ID:
                            // If the loaded id matches ours, return a new cast movie loader
                            return new ActorLoader(getApplicationContext(), String.valueOf(mActorId));
                        default:
                            throw new RuntimeException("Loader Not Implemented: " + loaderId);
                    }
                }

                @Override
                public void onLoadFinished(Loader<Actor> loader, Actor actorDetails) {
                    testTextView.setText("");
                    testTextView.append("ID: " + actorDetails.getActorId() + "\n");
                    testTextView.append("NAME: " + actorDetails.getActorName() + "\n");
                    testTextView.append("BIRTHDATE: " + actorDetails.getBirthday() + "\n");
                    testTextView.append("BIOGRAPHY: " + actorDetails.getBiography() + "\n");

                    setTitle(actorDetails.getActorName());
                    // Populate actor details section
                    //populateActorDetails(actorDetails);
                }

                @Override
                public void onLoaderReset(Loader<Actor> loader) {
                    //mCastAdapter.swapCast(new ArrayList<Cast>() {
                    //});
                    testTextView.setText("");
                }
            };
}
