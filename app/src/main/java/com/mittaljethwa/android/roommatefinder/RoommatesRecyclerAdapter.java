package com.mittaljethwa.android.roommatefinder;

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
            TextView txtAge;
            TextView txtMaxBudget;
            Button btnViewProfile;
            public ViewHolder(View itemView) {
                super(itemView);
                txtTitle = itemView.findViewById(R.id.title);
                txtProfileCategory = itemView.findViewById(R.id.profileCategory);
                txtBio= itemView.findViewById(R.id.bio);
                txtAge = itemView.findViewById(R.id.age);
                btnViewProfile = itemView.findViewById(R.id.viewProfileButton);
                txtMaxBudget= itemView.findViewById(R.id.maxBudget);
            }

            public void bindData(RoommateDetails roommateDetails) {
                txtTitle.setText(roommateDetails.getFirstname() + " " + roommateDetails.getLastname());
                txtProfileCategory.setText(roommateDetails.getProfileCategory());
                txtBio.setText(roommateDetails.getBio());
                if (roommateDetails.getHousingPreferences() != null)
                    txtMaxBudget.setText("$"+roommateDetails.getHousingPreferences().get("maxBudget").toString());
                else
                    txtMaxBudget.setText("$ NULL");
//                if (classDetails.getStartTime() != null && !classDetails.getStartTime().trim().isEmpty()) {
//                    String timeString = classDetails.getStartTime().substring(0, 2);
//                    timeString += ":";
//                    timeString += classDetails.getStartTime().substring(2, 4);
//                    if (classDetails.getEndTime() != null && !classDetails.getEndTime().trim().isEmpty()) {
//                        timeString += " - ";
//                        timeString += classDetails.getEndTime().substring(0, 2);
//                        timeString += ":";
//                        timeString += classDetails.getEndTime().substring(2, 4);
//                    }
//                    txtTime.setText(timeString);
//                }
                txtAge.setText(String.valueOf(25));
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

