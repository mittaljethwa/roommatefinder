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
        private boolean alreadyEnroledClasses;
        private int buttonColor;

        public RoommatesRecyclerAdapter(List<RoommateDetails> roommateDetailsList, boolean enrolledClasses, int buttonColor, ItemClickedListener listener) {
            this.roommateDetails = roommateDetailsList;
            this.listener = listener;
            this.buttonColor = buttonColor;
            this.alreadyEnroledClasses = enrolledClasses;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_roommate_details, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
//            holder.bindData(roommateDetails.get(position));
        }

        @Override
        public int getItemCount() {
            return roommateDetails.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtTitle;
            TextView txtSubject;
            TextView txtDescription;
            TextView txtTime;
            TextView txtOtherDetails;
            Button btnAction;
            public ViewHolder(View itemView) {
                super(itemView);
                txtTitle = itemView.findViewById(R.id.title);
//                txtSubject = itemView.findViewById(R.id.subject);
//                txtDescription = itemView.findViewById(R.id.description);
//                txtTime = itemView.findViewById(R.id.time);
//                btnAction = itemView.findViewById(R.id.btnRemove);
//                btnAction.setBackgroundColor(buttonColor);
//                txtOtherDetails = itemView.findViewById(R.id.otherDetails);
            }

//            public void bindData(RoommateDetails roommateDetails) {
//                txtTitle.setText(roommateDetails.getTitle() + " (" + classDetails.getId() + ")");
//                txtSubject.setText(roommateDetails.getDepartment() + (classDetails.getWaitlist() > 0 ? " (Waitlist : " + classDetails.getWaitlist() + ")" : ""));
//                txtDescription.setText(classDetails.getDescription());
//                txtOtherDetails.setText(classDetails.getSubject() + " - " + classDetails.getInstructor());
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
//                btnAction.setTag(classDetails);
//                if (alreadyEnroledClasses) {
//                    btnAction.setText(R.string.drop_class);
//                }
//                else {
//                    btnAction.setText(R.string.enroll_class);
//                }
//                btnAction.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        listener.onItemActionButtonClicked((ClassDetails)view.getTag());
//                    }
//                });
//            }
        }

        interface ItemClickedListener {
            void onItemActionButtonClicked(RoommateDetails roommateDetails);
        }
    }

