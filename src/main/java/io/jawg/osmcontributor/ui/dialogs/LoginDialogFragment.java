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
package io.jawg.osmcontributor.ui.dialogs;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.rest.events.GoogleAuthenticatedEvent;
import io.jawg.osmcontributor.rest.security.GoogleOAuthManager;
import io.jawg.osmcontributor.ui.events.login.AttemptLoginEvent;
import io.jawg.osmcontributor.ui.events.login.ErrorLoginEvent;
import io.jawg.osmcontributor.ui.events.login.UpdateGoogleCredentialsEvent;
import io.jawg.osmcontributor.ui.events.login.ValidLoginEvent;
import io.jawg.osmcontributor.utils.StringUtils;

/**
 * @author Tommy Buonomo on 29/07/16.
 */
public class LoginDialogFragment extends DialogFragment {
    private static final int PICK_ACCOUNT_CODE = 1;

    @BindView(R.id.dialog_connection_google_button)
    Button googleButton;

    @BindView(R.id.dialog_connection_login_button)
    Button loginButton;

    @BindView(R.id.dialog_connection_login_later)
    TextView loginLaterTextView;

    @BindView(R.id.dialog_connection_login_edit_text)
    EditText loginEditText;

    @BindView(R.id.dialog_connection_password_edit_text)
    EditText passwordEditText;

    @Inject
    GoogleOAuthManager googleOAuthManager;

    private EventBus eventBus;

    public static LoginDialogFragment newInstance(EventBus bus) {
        LoginDialogFragment fragment = new LoginDialogFragment();
        fragment.setEventBus(bus);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_connection, container, false);
        ButterKnife.bind(this, rootView);
        eventBus.register(this);

        ((OsmTemplateApplication) getActivity().getApplication()).getOsmTemplateComponent().inject(this);

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = AccountPicker.newChooseAccountIntent(
                        null, null,
                        new String[]{"com.google"},
                        false, null, null, null, null);
                startActivityForResult(intent, PICK_ACCOUNT_CODE);
            }
        });


        loginLaterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!StringUtils.isEmpty(loginEditText.getText()) && !StringUtils.isEmpty(passwordEditText.getText())) {
                    eventBus.post(new AttemptLoginEvent(loginEditText.getText().toString(), passwordEditText.getText().toString()));
                } else {
                    Toast.makeText(getActivity(), R.string.empty_fields, Toast.LENGTH_LONG).show();
                }
            }
        });
        return rootView;
    }


    @Override public void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_ACCOUNT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                googleOAuthManager.authenticate(getActivity(), email);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN) public void onGoogleAuthenticatedEvent(GoogleAuthenticatedEvent event) {
        if (event.isSuccessful()) {
            eventBus.post(new UpdateGoogleCredentialsEvent(event.getToken(), event.getTokenSecret(), event.getConsumer(), event.getConsumerSecret()));
        } else {
            Toast.makeText(getActivity(), R.string.error_login, Toast.LENGTH_SHORT).show();
        }
        dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN) public void onValidLoginEvent(ValidLoginEvent event) {
        Toast.makeText(getActivity(), R.string.valid_login, Toast.LENGTH_SHORT).show();
        dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN) public void onErrorLoginEvent(ErrorLoginEvent event) {
        Toast.makeText(getActivity(), R.string.error_first_login, Toast.LENGTH_SHORT).show();
        resetLoginFields();
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void resetLoginFields() {
        loginEditText.setText("");
        passwordEditText.setText("");
    }
}
