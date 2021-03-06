package ca.team2.crapmap.controller;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ca.team2.crapmap.R;
import ca.team2.crapmap.model.Bathroom;
import ca.team2.crapmap.model.Review;
import ca.team2.crapmap.service.BathroomService;
import ca.team2.crapmap.service.RequestHandler;

public class PreviewBathroomActivity extends AppCompatActivity {

    static final int NEW_COMMENT_REQUEST = 1;  // The request code

    public Bathroom bathroom;
    private LatLng currentLocation;
    private List<Review> reviewList = new ArrayList<Review>();

    private RatingBar avg_accessibility, avg_availability, avg_cleanliness;
    private TextView bathroom_name, text_travel_time, text_is_open, text_requires_purchase;
    private ListView review_listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_bathroom);

        LayoutInflater inflater = getLayoutInflater();

        review_listView = (ListView) findViewById(R.id.review_listView);
        View header = inflater.inflate(R.layout.preview_bathroom_header, null);
        review_listView.addHeaderView(header);

        Intent intent = this.getIntent();
        bathroom = (Bathroom)intent.getSerializableExtra("bathroom");
        currentLocation = new LatLng(intent.getDoubleExtra("userLat", 0), intent.getDoubleExtra("userLng", 0));
        bathroom.setLocation(new LatLng(intent.getDoubleExtra("bathroomLat", 0), intent.getDoubleExtra("bathroomLng", 0)));
        Log.i("bundle", bathroom.toString());
        // cleanliness = bathroom.getReviews().get(0).getCleanliness();
        avg_accessibility = (RatingBar) findViewById(R.id.rating_average_accessibility);
        avg_availability = (RatingBar) findViewById(R.id.rating_average_availability);
        avg_cleanliness = (RatingBar) findViewById(R.id.rating_average_cleanliness);
        bathroom_name = (TextView) findViewById(R.id.text_bathroom);
        text_travel_time = (TextView) findViewById(R.id.text_travel_time);
        text_requires_purchase = (TextView) findViewById(R.id.text_requires_purchase);
        text_is_open = (TextView) findViewById(R.id.text_is_open);

        FloatingActionButton fab_newComment = (FloatingActionButton) findViewById(R.id.fab_newComment);
        fab_newComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadNewCommentView();
            }
        });

        reviewList = bathroom.getReviews();
        loadBaseStats(); // Load initial data
        generateReviewModule(); // Generate listview contents
        getTravelTime(); // Get walking time estimate from API
    }


    // Main method to handle the loading of comments/ratings
    public void loadBaseStats() {

        bathroom_name.setText(bathroom.getName());
        float[] avgRatings = bathroom.getAvgRatings();
        avg_cleanliness.setRating(avgRatings[0]);
        avg_availability.setRating(avgRatings[1]);
        avg_accessibility.setRating(avgRatings[2]);
        if (bathroom.getRequiresPurchase()) {
            text_requires_purchase.setText(getResources().getString(R.string.label_requires_purchase));
        } else {
            text_requires_purchase.setText("");
            text_requires_purchase.setHeight(0);
            text_requires_purchase.setPadding(0,0,0,0);
        }
        if (bathroom.hasAnyHours() && bathroom.isOpen()) {
            text_is_open.setText(getResources().getString(R.string.bathroom_open));
            text_is_open.setTextColor(getResources().getColor(R.color.colorGreen));
        } else if (bathroom.hasAnyHours()) {
            text_is_open.setText(getResources().getString(R.string.bathroom_closed));
            text_is_open.setTextColor(getResources().getColor(R.color.colorRed));
        } else {
            text_is_open.setText("");
            text_requires_purchase.setHeight(0);
            text_requires_purchase.setPadding(0,0,0,0);
        }

    }

    // Sets adapter for listview, calls custom ReviewAdapter class
    public void generateReviewModule() {
        //ArrayList<Review> br = bathroom.getReviews();
        ArrayAdapter<Review> arrayAdapter = new ReviewAdapter();//ArrayAdapter<Review>(this, R.layout.review_row, br);
        review_listView.setAdapter(arrayAdapter);

    }

    public void getTravelTime() {

        BathroomService.getBathroomTravelTime(currentLocation, bathroom.getLocation(), new RequestHandler<String>() {
            @Override
            public void callback(String result) {
                try {
                    JSONObject response = new JSONObject(result);
                    Log.i("DISTANCE MATRIX RESP", result);
                    String duration = response.getJSONArray("rows")
                            .getJSONObject(0)
                            .getJSONArray("elements")
                            .getJSONObject(0)
                            .getJSONObject("duration")
                            .getString("text");
                    text_travel_time.setText(duration + " away on foot");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private class ReviewAdapter extends ArrayAdapter<Review> {
        public ReviewAdapter(){
            super(PreviewBathroomActivity.this, R.layout.review_row, bathroom.getReviews());
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Check for null & create if needed
            View itemView = convertView;
            if (itemView == null){
                itemView = getLayoutInflater().inflate(R.layout.review_row,parent,false);
            }

            // Get current review
            Review currentReview = reviewList.get(position);

            // Fill row
            TextView user = (TextView) itemView.findViewById(R.id.text_username);
            RatingBar access = (RatingBar) itemView.findViewById(R.id.rb_access);
            RatingBar avail = (RatingBar) itemView.findViewById(R.id.rb_avail);
            RatingBar clean = (RatingBar) itemView.findViewById(R.id.rb_clean);
            TextView comment = (TextView) itemView.findViewById(R.id.text_comment);
            // user.setText("Anon");//TODO temp solution
            if(currentReview.getUser() != null) {
                if (currentReview.getUser().getUsername() == "") {
                    user.setText("Anon");//If no name given, print anon
                } else {
                    user.setText(currentReview.getUser().getUsername());
                }
            }

            access.setRating(currentReview.getAccessibility());
            avail.setRating(currentReview.getAvailability());
            clean.setRating(currentReview.getCleanliness());
            comment.setText(currentReview.getReview());
            return itemView;
            // return super.getView(position, convertView, parent);
        }
    }

    public void loadNewCommentView() {
        Intent intent = new Intent(PreviewBathroomActivity.this, NewCommentActivity.class);
        if (bathroom != null) {
            String bathroomID = bathroom.getId();
            String url = ""; // getIntent().getStringExtra("baseApiUrl") + getIntent().getStringExtra("id") + "/review";
            intent.putExtra("bathroomID", bathroomID);
            intent.putExtra("reviewURL", url);
            startActivityForResult(intent, NEW_COMMENT_REQUEST);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_COMMENT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                bathroom = (Bathroom)data.getSerializableExtra("responseBathroom");
                reviewList = bathroom.getReviews();
                loadBaseStats();//load initial data
                generateReviewModule();//generate listview contents
            }
        }
    }


}
