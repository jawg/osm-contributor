/**
 * Copyright (C) 2016 eBusiness Information
 * <p>
 * This file is part of OSM Contributor.
 * <p>
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.psdev.licensesdialog.LicensesDialog;
import io.jawg.osmcontributor.BuildConfig;
import io.jawg.osmcontributor.R;

import static io.jawg.osmcontributor.utils.FlavorUtils.isBus;

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.edited_by)
    TextView editBy;

    @BindView(R.id.powered_by)
    TextView poweredBy;

    @BindView(R.id.contribute_to)
    TextView contributeTo;

    @BindView(R.id.mapsquare)
    TextView mapsquare;

    @BindView(R.id.version_text)
    TextView version;

    @BindView(R.id.thanks)
    TextView thanks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mapsquare.setText(Html.fromHtml(getString(R.string.mapsquare)));
        contributeTo.setText(Html.fromHtml(getString(R.string.contribute_to)));
        editBy.setText(Html.fromHtml(getString(R.string.splash_screen_edited_by)));
        poweredBy.setText(Html.fromHtml(getString(R.string.splash_screen_powered_by)));
        version.setText(getString(R.string.version_format, BuildConfig.VERSION_NAME));

        if (isBus()) {
            thanks.setText(R.string.thanks);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.mapsquare)
    public void mapsquareClick() {
        openUrlInBrowser(getString(R.string.jawg_url));
    }

    @OnClick(R.id.splash_image)
    public void splashClick() {
        openUrlInBrowser(getString(R.string.osm_dma_url));
    }

    @OnClick(R.id.contribute_to)
    public void contributeClick() {
        openUrlInBrowser(getString(R.string.osm_url));
    }

    @OnClick(R.id.edited_by)
    public void editedClick() {
        openUrlInBrowser(getString(R.string.ebiz_url));
    }

    @OnClick(R.id.powered_by)
    public void poweredClick() {
        openUrlInBrowser(getString(R.string.dma_url));
    }

    @OnClick(R.id.licenses_button)
    public void displayLicenses() {
        new LicensesDialog.Builder(this)
                .setNotices(R.raw.notices)
                .build()
                .show();
    }

    private void openUrlInBrowser(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}
