    package com.example.mad_forum;

    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.Comparator;
    import java.util.List;

    public class PostActivity extends AppCompatActivity {

        private DatabaseReference databaseReference;
        private RecyclerView recyclerView;
        private PostAdapter postAdapter;
        private List<Post> postList = new ArrayList<>();
        private TextView titleTextView;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_post);

            Bundle intent = getIntent().getExtras();
            if (intent != null) {
                String publisher = intent.getString("publisherid");

                SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();

                editor.putString("profileid", publisher);
                editor.apply();


            }

            titleTextView = findViewById(R.id.titleTextView);

            recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Create and set the adapter
            postAdapter = new PostAdapter(postList);
            recyclerView.setAdapter(postAdapter);

            // Initialize Firebase Realtime Database reference
            databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

            // Fetch data based on the default button (Most Recent)


            // Set onClick listeners for your buttons
            Button mostRecentButton = findViewById(R.id.mostRecent);
            Button mostLikedButton = findViewById(R.id.mostLiked);
            Button mostCommentedButton = findViewById(R.id.mostCommented);

            fetchPostsFromFirebase("timestamp");

            mostRecentButton.setOnClickListener(v -> {
                fetchPostsFromFirebase("timestamp");
            });

            mostCommentedButton.setOnClickListener(v -> {
                sortPostsByCommentCount();
                fetchPostsFromFirebase("commentCount");
            });


            mostLikedButton.setOnClickListener(v -> {
                sortPostsByLikesCount();
                fetchPostsFromFirebase("likesCount");

            });

//            mostRecentButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    fetchPostsFromFirebase("timestamp");
//                }
//            });

    //        mostLikedButton.setOnClickListener(new View.OnClickListener() {
    //            @Override
    //            public void onClick(View v) {
    //                fetchPostsFromFirebase("likesCount");
    //            }
    //        });

            // Add onClick listeners for other buttons as needed

            findViewById(R.id.createPostButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Create and show the dialog
                    showCreatePostDialog();
                }
            });
        }

        private void showCreatePostDialog() {
            // Launch CreatePostActivity
            Intent intent = new Intent(this, CreatePostActivity.class);
            startActivity(intent);
        }

        private void sortPostsByLikesCount() {
            Collections.sort(postList, new Comparator<Post>() {
                @Override
                public int compare(Post post1, Post post2) {
                    return Integer.compare(post2.getLikesCount(), post1.getLikesCount());
                }
            });

            postAdapter.notifyDataSetChanged();
        }

        private void sortPostsByCommentCount() {
            Collections.sort(postList, new Comparator<Post>() {
                @Override
                public int compare(Post post1, Post post2) {
                    return Integer.compare(post2.getCommentCount(), post1.getCommentCount());
                }
            });

            postAdapter.notifyDataSetChanged();
        }


            private void fetchPostsFromFirebase (String orderBy){
                databaseReference.orderByChild(orderBy).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Post post = dataSnapshot.getValue(Post.class);
                            if (post != null) {
                                post.setKey(dataSnapshot.getKey());
                                postList.add(0, post);
                            }
                        }

                        Log.d("FirebaseData", "Number of posts retrieved: " + postList.size());
                        postAdapter.notifyDataSetChanged();

                        if (orderBy.equals("timestamp")) {
                            titleTextView.setText("Most Recent");
                        } else if (orderBy.equals("likesCount")) {
                            titleTextView.setText("Most Liked");
                        } else if (orderBy.equals("commentCount")) {
                            titleTextView.setText("Most Commented");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MostRecent", "Failed to read value.", error.toException());
                    }
                });
            }
        }


    //    private void updateTitle(String orderBy) {
    //        switch (orderBy) {
    //            case "timestamp":
    //                titleTextView.setText("MOST RECENT");
    //                break;
    //            case "likesCount":
    //                titleTextView.setText("MOST LIKED");
    //                break;
    //            // Add cases for other buttons as needed
    //        }
    //    }
    //}
