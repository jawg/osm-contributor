/**
 * Copyright (C) 2016 eBusiness Information
 *
 * This file is part of OSM Contributor.
 *
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.mapsquare.osmcontributor.edition;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;

public class PickValueActivity extends AppCompatActivity {
    public static final int PICK_VALUE_ACTIVITY_CODE = 1;
    public static final String KEY = "KEY";
    public static final String VALUE = "VALUE";
    public static final String AUTOCOMPLETE = "AUTOCOMPLETE";
    private String key;
    private List<String> autocompleteValues = new ArrayList<>();
    private SearchableAdapter adapter;


    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.value)
    EditText editTextValue;

    @BindView(R.id.autocomplete_list)
    ListView autocompleteListView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_value);
        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();

        key = intent.getStringExtra(KEY);
        String value = intent.getStringExtra(VALUE);
        Collections.addAll(autocompleteValues, intent.getStringArrayExtra(AUTOCOMPLETE));

        setTitle(key);
        String text = String.format(getResources().getString(R.string.addValueDialogTitle), key);
        adapter = new SearchableAdapter(this, autocompleteValues, editTextValue);

        title.setText(text);
        editTextValue.setText(value);
        editTextValue.setSelectAllOnFocus(true);
        autocompleteListView.setAdapter(adapter);

        // Add Text Change Listener to EditText
        editTextValue.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call back the Adapter with current character to Filter
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pick_value, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED, null);
            finish();
            return true;
        }

        if (id == R.id.action_confirm_edit) {

            Intent returnIntent = new Intent();
            returnIntent.putExtra(KEY, key);
            returnIntent.putExtra(VALUE, editTextValue.getText().toString());
            setResult(Activity.RESULT_OK, returnIntent);

            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
