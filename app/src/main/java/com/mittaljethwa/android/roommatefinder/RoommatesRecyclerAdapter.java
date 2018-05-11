package com.mittaljethwa.android.roommatefinder;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Mittal on 5/9/2018.
 */

public class RoommatesRecyclerAdapter extends RecyclerView.Adapter<RoommatesRecyclerAdapter.ViewHolder> {
        private List<RoommateDetails> roommateDetails;
        private ItemClickedListener listener;


        public RoommatesRecyclerAdapter(List<RoommateDetails> roommateDetailsList, ItemClickedListener listener) {
            this.roommateDetails = roommateDetailsList;
            this.listener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_roommate_details, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bindData(roommateDetails.get(position));
        }

        @Override
        public int getItemCount() {
            return roommateDetails.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtTitle;
            TextView txtProfileCategory;
            TextView txtBio;
            TextView txtGender;
            TextView txtMaxBudget;
            Button btnViewProfile;
            public ViewHolder(View itemView) {
                super(itemView);
                txtTitle = itemView.findViewById(R.id.title);
                txtProfileCategory = itemView.findViewById(R.id.profileCategory);
                txtBio= itemView.findViewById(R.id.bio);
//                txtGender = itemView.findViewById(R.id.resultGender);
                btnViewProfile = itemView.findViewById(R.id.viewProfileButton);
                txtMaxBudget= itemView.findViewById(R.id.maxBudget);
            }

            public void bindData(RoommateDetails roommateDetails) {
                txtTitle.setText(roommateDetails.getFirstname() + " " + roommateDetails.getLastname());
                txtProfileCategory.setText(roommateDetails.getProfileCategory());
                txtBio.setText(roommateDetails.getBio());
                if (roommateDetails.getHousingPreferences() != null)
                    txtMaxBudget.setText(roommateDetails.getHousingPreferences().get("maxBudget").toString());
                else
                    txtMaxBudget.setText(Resources.getSystem().getString(R.string.noMaxBudget));

//                txtGender.setText(roommateDetails.getGender());
                btnViewProfile.setTag(roommateDetails);

                btnViewProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onItemActionButtonClicked((RoommateDetails) view.getTag());
                    }
                });
            }
        }

        interface ItemClickedListener {
            void onItemActionButtonClicked(RoommateDetails roommateDetails);
        }
    }

