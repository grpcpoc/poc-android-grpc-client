package com.example.myapplication.arrayadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.drathveloper.grpc.CreatedUser;
import com.example.myapplication.R;

import java.util.ArrayList;

public class CreatedUserAdapter extends ArrayAdapter<CreatedUser> {
    public CreatedUserAdapter(Context context, ArrayList<CreatedUser> users) {
        super(context, 0, users);
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        CreatedUser user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_second, parent, false);
            Animation animation = null;
            animation = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.push_left_in);
            animation.setDuration(500);
            convertView.startAnimation(animation);
            animation = null;
        }

        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvNameSecondList);
        // Populate the data into the template view using the data object
        tvName.setText(user.getFirstName() + " " + user.getLastName() + " " + user.getPhone() + " " + user.getEmail());
        // Return the completed view to render on screen
        return convertView;
    }
}