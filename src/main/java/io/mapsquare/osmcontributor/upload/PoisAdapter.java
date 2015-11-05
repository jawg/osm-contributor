/**
 * Copyright (C) 2015 eBusiness Information
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
package io.mapsquare.osmcontributor.upload;


import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.upload.events.PleaseConfirmRevertEvent;
import io.mapsquare.osmcontributor.utils.HtmlFontHelper;
import io.mapsquare.osmcontributor.utils.ViewAnimation;

public class PoisAdapter extends BaseAdapter {

    private List<PoiUpdateWrapper> poisWrapper = null;
    private PoiUpdateWrapper lastReverted = null;
    private int lastRevertedPosition;
    private LayoutInflater inflater;
    private Context context;
    private EventBus eventBus;

    public PoisAdapter(Context context, List<PoiUpdateWrapper> wrapper, EventBus eventBus) {
        this.poisWrapper = wrapper;
        this.context = context;
        this.eventBus = eventBus;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return poisWrapper.size();
    }

    @Override
    public Object getItem(int position) {
        return poisWrapper.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        final PoiViewHolder holder;

        if (view != null) {
            holder = (PoiViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.single_poi_layout, parent, false);
            holder = new PoiViewHolder(view);
            view.setTag(holder);
        }

        final PoiUpdateWrapper poiWrapper = poisWrapper.get(position);


        switch (poiWrapper.getAction()) {
            case CREATE:
                holder.getPoiAction().setText(view.getContext().getString(R.string.created));
                break;
            case DELETED:
                holder.getPoiAction().setText(view.getContext().getString(R.string.deleted));
                break;
            case UPDATE:
                holder.getPoiAction().setText(view.getContext().getString(R.string.updated));
                break;
        }

        final LinearLayout wrapper = holder.getDetailsWrapper();

        if (poiWrapper.getIsPoi()) {
            holder.getPoiType().setText(poiWrapper.getPoiType());
            holder.getPoiName().setText(poiWrapper.getName());
            holder.getExpandBtn().setVisibility(View.VISIBLE);
            populateDiffs(holder, parent, poiWrapper);

            View.OnClickListener expendCardnew = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    poiWrapper.setOpen(!poiWrapper.isOpen());

                    ViewAnimation.animate(wrapper, poiWrapper.isOpen());
                    if (poiWrapper.isOpen()) {
                        holder.getExpandBtn().setImageResource(R.drawable.chevron_up);
                    } else {
                        holder.getExpandBtn().setImageResource(R.drawable.chevron_down);
                    }
                }
            };

            holder.getExpandBtn().setOnClickListener(expendCardnew);
            holder.getHeader().setOnClickListener(expendCardnew);

            if (poiWrapper.isOpen()) {
                wrapper.setVisibility(View.VISIBLE);
                holder.getExpandBtn().setImageResource(R.drawable.chevron_up);
            } else {
                wrapper.setVisibility(View.GONE);
                holder.getExpandBtn().setImageResource(R.drawable.chevron_down);
            }
        } else {
            holder.getExpandBtn().setVisibility(View.GONE);
            wrapper.setVisibility(View.GONE);
            holder.getPoiType().setText(context.getString(R.string.node_ref_title));
            holder.getPoiName().setText("");
        }

        holder.getRevertBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastReverted = poisWrapper.remove(position);
                lastRevertedPosition = position;
                eventBus.post(new PleaseConfirmRevertEvent(poiWrapper.getId(), poiWrapper.getIsPoi()));
                notifyDataSetChanged();
            }
        });

        return view;
    }

    static class PoiViewHolder {
        @InjectView(R.id.poi_action)
        TextView poiAction;

        @InjectView(R.id.poi_name)
        TextView poiName;

        @InjectView(R.id.poi_type)
        TextView poiType;

        @InjectView(R.id.changes_details)
        LinearLayout detailsWrapper;

        @InjectView(R.id.header)
        RelativeLayout header;

        @InjectView(R.id.expend_button)
        ImageButton expandBtn;

        @InjectView(R.id.revert)
        Button revertBtn;


        public TextView getPoiAction() {
            return poiAction;
        }

        public TextView getPoiName() {
            return poiName;
        }

        public LinearLayout getDetailsWrapper() {
            return detailsWrapper;
        }

        public ImageButton getExpandBtn() {
            return expandBtn;
        }

        public TextView getPoiType() {
            return poiType;
        }

        public RelativeLayout getHeader() {
            return header;
        }

        public Button getRevertBtn() {
            return revertBtn;
        }

        public PoiViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    static class TagChangeViewHolder {
        @InjectView(R.id.old_tag)
        TextView oldTag;

        @InjectView(R.id.new_tag)
        TextView newTag;


        public TextView getOldTag() {
            return oldTag;
        }

        public void setOldTag(TextView oldTag) {
            this.oldTag = oldTag;
        }

        public TextView getNewTag() {
            return newTag;
        }

        public void setNewTag(TextView newTag) {
            this.newTag = newTag;
        }

        public TagChangeViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    private void populateDiffs(PoiViewHolder holder, ViewGroup parent, PoiUpdateWrapper poiWrapper) {
        holder.getDetailsWrapper().removeAllViews();
        TagChangeViewHolder tagChangeViewHolder;

        if (poiWrapper.isPositionChanged()) {
            View positionChanged = inflater.inflate(R.layout.single_changes_line_layout, parent, false);
            tagChangeViewHolder = new TagChangeViewHolder(positionChanged);
            String positionChangedStr = HtmlFontHelper.getBold(context.getString(R.string.position)) + HtmlFontHelper.addColor(context.getString(R.string.changed), HtmlFontHelper.ORANGE);
            tagChangeViewHolder.getNewTag().setText(Html.fromHtml(positionChangedStr), TextView.BufferType.SPANNABLE);
            holder.getDetailsWrapper().addView(positionChanged);
        }

        for (PoiDiffWrapper poiDiffWrapper : poiWrapper.getPoiDiff()) {
            View singleLine = inflater.inflate(R.layout.single_changes_line_layout, parent, false);

            tagChangeViewHolder = new TagChangeViewHolder(singleLine);
            tagChangeViewHolder.getNewTag().setText(Html.fromHtml(poiDiffWrapper.getColoredDetail(true)), TextView.BufferType.SPANNABLE);
            tagChangeViewHolder.getOldTag().setText(Html.fromHtml(poiDiffWrapper.getColoredDetail(false)), TextView.BufferType.SPANNABLE);

            holder.getDetailsWrapper().addView(singleLine);
        }
    }

    public void retriveLastReverted() {
        if (lastReverted != null) {
            poisWrapper.add(lastRevertedPosition, lastReverted);
            notifyDataSetChanged();
        }
        lastReverted = null;
    }
}

