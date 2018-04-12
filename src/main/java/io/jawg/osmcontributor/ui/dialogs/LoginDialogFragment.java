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
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.database.preferences.LoginPreferences;
import io.jawg.osmcontributor.rest.events.GoogleAuthenticatedEvent;
import io.jawg.osmcontributor.rest.security.GoogleOAuthManager;
import io.jawg.osmcontributor.ui.events.login.UpdateGoogleCredentialsEvent;
import io.jawg.osmcontributor.ui.managers.executor.GenericSubscriber;
import io.jawg.osmcontributor.ui.managers.login.UpdateCredentialsIfValid;
import io.jawg.osmcontributor.utils.StringUtils;

/**
 * @author Tommy Buonomo on 29/07/16.
 */
public class LoginDialogFragment extends DialogFragment {
    private static final int PICK_ACCOUNT_CODE = 1;

    @BindView(R.id.dialog_connection_login_edit_text)
    EditText loginEditText;

    @BindView(R.id.dialog_connection_password_edit_text)
    EditText passwordEditText;

    @Inject
    LoginPreferences loginPreferences;

    @Inject
    GoogleOAuthManager googleOAuthManager;

    @Inject
    EventBus eventBus;

    @Inject
    UpdateCredentialsIfValid updateCredentialsIfValid;

    private OnLoginSuccessfulListener onLoginSuccessfulListener;

    public static LoginDialogFragment newInstance(OnLoginSuccessfulListener onLoginSuccessfulListener) {
        LoginDialogFragment fragment = new LoginDialogFragment();
        fragment.onLoginSuccessfulListener = onLoginSuccessfulListener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_connection, container, false);
        ButterKnife.bind(this, rootView);

        ((OsmTemplateApplication) getActivity().getApplication()).getOsmTemplateComponent().inject(this);

        eventBus.register(this);

        loginEditText.setText(loginPreferences.retrieveLogin());
        passwordEditText.setText(loginPreferences.retrievePassword());

        return rootView;
    }


    @Override
    public void onDestroy() {
        eventBus.unregister(this);
        updateCredentialsIfValid.unsubscribe();
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

    @OnClick(R.id.dialog_connection_google_button)
    void onGoogleButtonClicked() {
        Intent intent = AccountPicker.newChooseAccountIntent(
                null, null,
                new String[]{"com.google"},
                false, null, null, null, null);
        startActivityForResult(intent, PICK_ACCOUNT_CODE);
    }

    @OnClick(R.id.dialog_connection_login_button)
    void onConnectionButtonClicked() {
        if (!StringUtils.isEmpty(loginEditText.getText()) && !StringUtils.isEmpty(passwordEditText.getText())) {
            updateCredentialsIfValid.init(loginEditText.getText().toString(), passwordEditText.getText().toString()).execute(new UpdateCredentialsIfValidObservable());
        } else {
            Toast.makeText(getActivity(), R.string.empty_fields, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.dialog_connection_login_later)
    void onLoginLaterClicked() {
        dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGoogleAuthenticatedEvent(GoogleAuthenticatedEvent event) {
        if (event.isSuccessful()) {
            eventBus.post(new UpdateGoogleCredentialsEvent(event.getToken(), event.getTokenSecret(), event.getConsumer(), event.getConsumerSecret()));
        } else {
            Toast.makeText(getActivity(), R.string.error_login, Toast.LENGTH_SHORT).show();
        }
        dismiss();
    }

    private void resetLoginFields() {
        loginEditText.setText("");
        passwordEditText.setText("");
    }

    public interface OnLoginSuccessfulListener {

        void onLoginSuccessful();
    }

    private class UpdateCredentialsIfValidObservable extends GenericSubscriber<Boolean> {
        @Override
        public void onNext(Boolean isValidCredentials) {
            if (isValidCredentials) {
                Toast.makeText(getActivity(), R.string.valid_login, Toast.LENGTH_SHORT).show();

                if (onLoginSuccessfulListener != null) {
                    onLoginSuccessfulListener.onLoginSuccessful();
                }

                dismiss();
            } else {
                Toast.makeText(getActivity(), R.string.error_first_login, Toast.LENGTH_SHORT).show();
                resetLoginFields();
            }
            super.onNext(isValidCredentials);
        }
    }
}
