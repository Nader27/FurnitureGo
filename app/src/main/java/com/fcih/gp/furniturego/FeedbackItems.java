package com.fcih.gp.furniturego;

/**
 * Created by abobakr sokarno on 6/25/2017.
 */

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

public class FeedbackItems extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<String> feedback;
    private final ArrayList<String> imgid;
    private final ArrayList<String> feedbackers ;
    private final ArrayList<String> dateOfFeedback ;


    public FeedbackItems(Activity context, ArrayList<String>feedbackItself, ArrayList<String> feedback, ArrayList<String> imgid,ArrayList<String> feedbackDate) {
        super(context, R.layout.feedback_item, feedback);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.feedback=feedback;
        this.imgid=imgid;
        this.feedbackers = feedbackItself ;
        this.dateOfFeedback = feedbackDate ;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.feedback_item, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.feed_user_name);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.feed_user_image);
        TextView extratxt = (TextView) rowView.findViewById(R.id.feed_text);
        RatingBar ratingBar = (RatingBar)rowView.findViewById(R.id.feed_rate);
        TextView date = (TextView)rowView.findViewById(R.id.feed_date);


        txtTitle.setText(feedbackers.get(position));
        //imageView.setImageResource(Integer.parseInt(imgid.get(position)));
        extratxt.setText(feedback.get(position));
        date.setText(dateOfFeedback.get(position));


        return rowView;

    };
}