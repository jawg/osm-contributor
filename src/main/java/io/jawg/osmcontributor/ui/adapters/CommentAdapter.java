/**
 * Copyright (C) 2019 Takima
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
package io.jawg.osmcontributor.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.Comment;

public class CommentAdapter extends BaseAdapter {
    private List<Comment> comments = new ArrayList<>();
    private Context context;

    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    public void addAll(Collection<Comment> comments) {
        if (comments != null) {
            this.comments.clear();
            this.comments.addAll(comments);
        }
        notifyDataSetChanged();
    }

    public void add(Comment comment) {
        if (comment != null) {
            if (!comments.contains(comment)) {
                comments.add(comment);
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public int getCount() {
        return comments.size();
    }

    @Override
    public Object getItem(int position) {
        return comments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Comment comment = comments.get(position);
        final ViewHolder holder;

        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.single_comment_layout, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }


        holder.getCommentContentTextView().setText(comment.getText());
        holder.getActionTextView().setText(comment.getAction());

        DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yy");
        String date = comment.getCreatedDate() == null ? "" : fmt.print(comment.getCreatedDate());

        if (comment.getUpdated()) {
            date = context.getResources().getString(R.string.synchronizing);
        }

        holder.getDateTextView().setText(date);
        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        Collections.sort(comments);
        super.notifyDataSetChanged();
    }

    static class ViewHolder {
        @BindView(R.id.comment_content_text)
        TextView commentContentTextView;
        @BindView(R.id.date)
        TextView dateTextView;
        @BindView(R.id.action)
        TextView actionTextView;


        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public TextView getCommentContentTextView() {
            return commentContentTextView;
        }

        public TextView getDateTextView() {
            return dateTextView;
        }

        public TextView getActionTextView() {
            return actionTextView;
        }
    }
}
