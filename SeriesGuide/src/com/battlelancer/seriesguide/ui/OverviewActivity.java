/*
 * Copyright 2011 Uwe Trottmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.battlelancer.seriesguide.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.battlelancer.seriesguide.items.Series;
import com.battlelancer.seriesguide.util.DBUtils;
import com.battlelancer.seriesguide.util.TaskManager;
import com.battlelancer.seriesguide.util.UpdateTask;
import com.battlelancer.seriesguide.util.Utils;
import com.battlelancer.thetvdbapi.TheTVDB;
import com.google.analytics.tracking.android.EasyTracker;
import com.uwetrottmann.androidutils.AndroidUtils;
import com.uwetrottmann.seriesguide.R;

/**
 * Hosts an {@link OverviewFragment}.
 */
public class OverviewActivity extends BaseActivity {

    private Fragment mFragment;

    protected int mShowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mShowId = getIntent().getIntExtra(OverviewFragment.InitBundle.SHOW_TVDBID, -1);
        if (mShowId == -1) {
            finish();
            return;
        }
        
        if (!(this instanceof OverviewActivityICS) && AndroidUtils.isICSOrHigher()) {
            Intent intent = new Intent(this, OverviewActivityICS.class);
            intent.putExtra(OverviewFragment.InitBundle.SHOW_TVDBID, mShowId);
            if (savedInstanceState != null) {
                intent.putExtras(savedInstanceState);
            }
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.overview);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.description_overview));
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            mFragment = new OverviewFragment();
            mFragment.setArguments(getIntent().getExtras());

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            ft.replace(R.id.fragment_overview, mFragment).commit();
        }

        // try to update this show
        onUpdate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, ShowsActivity.class);
                upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(upIntent);
                overridePendingTransition(R.anim.fragment_slide_right_enter,
                        R.anim.fragment_slide_right_exit);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onUpdate() {
        // only update this show if no global update is running and we have a
        // connection
        if (!TaskManager.getInstance(this).isUpdateTaskRunning(false)
                && Utils.isAllowedConnection(this)) {
            final SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());

            // check if auto-update is enabled
            final boolean isAutoUpdateEnabled = prefs.getBoolean(
                    SeriesGuidePreferences.KEY_AUTOUPDATE, true);
            if (isAutoUpdateEnabled) {
                String showId = String.valueOf(mShowId);
                boolean isTime = TheTVDB.isUpdateShow(showId, System.currentTimeMillis(), this);

                // look if we need to update
                if (isTime) {
                    UpdateTask updateTask = new UpdateTask(showId, this);
                    TaskManager.getInstance(this).tryUpdateTask(updateTask, false, -1);
                }
            }

        }
    }

    @Override
    public boolean onSearchRequested() {
        // refine search with the show's title
        final Series show = DBUtils.getShow(this, String.valueOf(mShowId));
        final String showTitle = show.getTitle();

        Bundle args = new Bundle();
        args.putString(SearchFragment.InitBundle.SHOW_TITLE, showTitle);
        startSearch(null, false, args, false);
        return true;
    }

}
