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
package io.mapsquare.osmcontributor.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.model.entities.Comment;
import io.mapsquare.osmcontributor.model.entities.Note;
import io.mapsquare.osmcontributor.ui.events.map.PleaseChangeValuesDetailNoteFragmentEvent;
import io.mapsquare.osmcontributor.ui.activities.NoteActivity;


public class NoteDetailFragment extends Fragment {
    private Note note;

    @Inject
    EventBus eventBus;

    @BindView(R.id.status)
    TextView textViewNoteStatus;

    @BindView(R.id.comment_text)
    TextView textViewCommentText;

    @BindView(R.id.osm_copyright)
    TextView osmCopyrightTextView;

    @BindView(R.id.edit_note_detail)
    FloatingActionButton floatingActionButtonEditNote;

    @OnClick(R.id.edit_note_detail)
    public void editNoteOnClick() {
        if (note != null) {
            Intent intent = new Intent(getActivity(), NoteActivity.class);
            intent.putExtra(NoteActivity.NOTE_ID, note.getId());
            startActivity(intent);
        }
    }


    public NoteDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_note_detail, container, false);

        ((OsmTemplateApplication) getActivity().getApplication()).getOsmTemplateComponent().inject(this);
        ButterKnife.bind(this, rootView);

        osmCopyrightTextView.setText(Html.fromHtml(getString(R.string.osm_copyright)));

        return rootView;
    }

    private void setNote(Note note) {
        this.note = note;

        List<Comment> comments = new ArrayList<>();
        comments.addAll(note.getComments());

        if (!comments.isEmpty()) {
            for (Comment c : comments) {
                if (c.getAction().equals("opened")) {
                    textViewCommentText.setText(c.getText());
                }
            }
        }

        textViewNoteStatus.setText("Note (" + note.getStatus() + ")");
    }


    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseChangeValuesDetailNoteFragmentEvent(PleaseChangeValuesDetailNoteFragmentEvent event) {
        setNote(event.getNote());
    }
}
