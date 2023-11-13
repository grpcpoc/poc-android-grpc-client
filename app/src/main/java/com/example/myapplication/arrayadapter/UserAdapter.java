package com.example.myapplication.arrayadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.drathveloper.grpc.User;
import com.example.myapplication.R;

import java.util.ArrayList;

public class UserAdapter extends ArrayAdapter<User> {
    public UserAdapter(Context context, ArrayList<User> users) {
        super(context, 0, users);
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        User user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_first, parent, false);
                Animation animation = null;
                animation = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.push_left_in);
                animation.setDuration(500);
                convertView.startAnimation(animation);
                animation = null;
        }

        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvNameFirstList);
        // Populate the data into the template view using the data object
        tvName.setText(user.getFirstName() + " " + user.getLastName() + " " + user.getPhone() + " " + user.getEmail());
        // Return the completed view to render on screen
        return convertView;
    }
}