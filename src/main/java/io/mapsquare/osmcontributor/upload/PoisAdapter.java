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
package io.mapsquare.osmcontributor.upload;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.utils.helper.ItemTouchHelperViewHolder;
import io.mapsquare.osmcontributor.utils.helper.SwipeItemTouchHelperAdapter;
import io.mapsquare.osmcontributor.utils.HtmlFontHelper;
import io.mapsquare.osmcontributor.utils.ViewAnimation;

public class PoisAdapter extends RecyclerView.Adapter<PoisAdapter.PoiViewHolder> implements SwipeItemTouchHelperAdapter {

    private List<PoiUpdateWrapper> poisWrapper = null;
    private LayoutInflater inflater;
    private Context context;
    private OnItemRemovedListener OnRemoveListener;

    public PoisAdapter(Context context, List<PoiUpdateWrapper> wrapper) {
        this.poisWrapper = wrapper;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public PoiViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = inflater.inflate(R.layout.single_poi_layout, parent, false);
        return new PoiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PoiViewHolder holder, final int position) {
        final PoiUpdateWrapper poiWrapper = poisWrapper.get(position);

        switch (poiWrapper.getAction()) {
            case CREATE:
                holder.getPoiAction().setText(context.getString(R.string.created));
                break;
            case DELETED:
                holder.getPoiAction().setText(context.getString(R.string.deleted));
                break;
            case UPDATE:
                holder.getPoiAction().setText(context.getString(R.string.updated));
                break;
        }

        final LinearLayout wrapper = holder.getDetailsWrapper();

        if (poiWrapper.getIsPoi()) {
            holder.getPoiType().setText(poiWrapper.getPoiType());
            holder.getPoiName().setText(poiWrapper.getName());
            holder.getExpandBtn().setVisibility(View.VISIBLE);
            populateDiffs(holder, holder.getDetailsWrapper(), poiWrapper);

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
                remove(poisWrapper.indexOf(poiWrapper));
            }
        });

        holder.getCheckbox().setChecked(poiWrapper.isSelected());
        holder.getCheckbox().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                poiWrapper.setSelected(isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return poisWrapper.size();
    }

    @Override
    public void onItemDismiss(int position) {
        remove(position);
    }

    public void insert(int position, PoiUpdateWrapper poiUpdateWrapper) {
        poisWrapper.add(position, poiUpdateWrapper);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        PoiUpdateWrapper wrapper = poisWrapper.remove(position);
        if (OnRemoveListener != null) {
            OnRemoveListener.onItemRemoved(wrapper, position);
        }
        notifyItemRemoved(position);
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

    public List<Long> getPoiToUpload() {
        List<Long> idsToUpload = new ArrayList<>();
        for (PoiUpdateWrapper poiUpdateWrapper : poisWrapper) {
            if (poiUpdateWrapper.isSelected() && poiUpdateWrapper.getIsPoi()) {
                idsToUpload.add(poiUpdateWrapper.getId());
            }
        }
        return idsToUpload;
    }

    public List<Long> getPoiNodeRefToUpload() {
        List<Long> idsToUpload = new ArrayList<>();
        for (PoiUpdateWrapper poiUpdateWrapper : poisWrapper) {
            if (poiUpdateWrapper.isSelected() && !poiUpdateWrapper.getIsPoi()) {
                idsToUpload.add(poiUpdateWrapper.getId());
            }
        }
        return idsToUpload;
    }

    public boolean changedSelected() {
        for (PoiUpdateWrapper poiUpdateWrapper : poisWrapper) {
            if (poiUpdateWrapper.isSelected()) {
                return true;
            }
        }
        return false;
    }

    public void setOnStartSwipeListener(OnItemRemovedListener swipeListener) {
        this.OnRemoveListener = swipeListener;
    }

    public static class PoiViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
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

        @InjectView(R.id.checkbox)
        CheckBox checkbox;

        public PoiViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

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

        public CheckBox getCheckbox() {
            return checkbox;
        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

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

    public interface OnItemRemovedListener {
        /**
         * Called when and item is removed
         *
         * @param poiUpdateWrapper Removed object.
         * @param position         Position of the removed object in the adapter.
         */
        void onItemRemoved(PoiUpdateWrapper poiUpdateWrapper, int position);
    }
}
