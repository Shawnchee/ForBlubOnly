    package com.example.mad_forum;

    import android.content.Intent;
    import android.os.Handler;
    import android.os.Looper;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.TextView;

    import com.bumptech.glide.Glide;
    import com.example.mad_forum.Post;



    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import com.google.firebase.Firebase;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    import java.util.List;

    // PostAdapter.java
    public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

        private List<Post> postList;

        public PostAdapter(List<Post> postList) {
            this.postList = postList;
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
            return new PostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
            Post post = postList.get(position);
            holder.getComments(post.getKey(), holder.commentCounts);

            holder.commentIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), CommentsActivity.class);
                    intent.putExtra("postid", post.getKey());
                    intent.putExtra("publisherid", post.getUserId());
                    view.getContext().startActivity(intent);
                }
            });

            holder.commentCounts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), CommentsActivity.class);
                    intent.putExtra("postid", post.getKey());
                    intent.putExtra("publisherid", post.getUserId());
                    view.getContext().startActivity(intent);
                }
            });

            // Bind data to the ViewHolder
            holder.bind(post);

            if (post.isLiked()) {
                holder.likes.setImageResource(R.drawable.likedicon);
            } else {
                holder.likes.setImageResource(R.drawable.likeicon);
            }

            holder.likesCount.setText(String.valueOf(post.getLikesCount()));
        }

        @Override
        public int getItemCount() {
            return postList.size();
        }

        public class PostViewHolder extends RecyclerView.ViewHolder {

            private TextView postTitle, postDescription, likesCount, commentCounts;
            private ImageView likes, imageProfile, commentIcon;

            public PostViewHolder(@NonNull View itemView) {
                super(itemView);
                commentCounts = itemView.findViewById(R.id.commentsCount);
                commentIcon = itemView.findViewById(R.id.commentIcon);
                // imageProfile = itemView.findViewById(R.id.image_profile);
                likes = itemView.findViewById(R.id.likes);
                likesCount = itemView.findViewById(R.id.likesCount);
                postTitle = itemView.findViewById(R.id.postTitle);
                postDescription = itemView.findViewById(R.id.postDescription);

                likes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAbsoluteAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            Post post = postList.get(position);
                            handleLikeClick(post);
                        }
                    }
                });
            }

            private void handleLikeClick(Post post) {
                String postKey = post.getKey();
                if (postKey != null) {
                    int newLikesCount;
                    if (post.isLiked()) {
                        likes.setImageResource(R.drawable.likeicon);
                        newLikesCount = Math.max(0, post.getLikesCount() - 1);
                        post.setLiked(false);
                    } else {
                        likes.setImageResource(R.drawable.likedicon);
                        newLikesCount = post.getLikesCount() + 1;
                        post.setLiked(true);
                    }

                    // Update the likesCount TextView
                    runOnUiThread(() -> likesCount.setText(String.valueOf(newLikesCount)));

                    // Toggle between likepng and likedpng
                    runOnUiThread(() -> {
                        if (post.isLiked()) {
                            likes.setImageResource(R.drawable.likedicon);
                        } else {
                            likes.setImageResource(R.drawable.likeicon);
                        }
                    });

                    // Update the likes count in the database
                    updateLikesInDatabase(postKey, newLikesCount, post);
                    post.setLikesCount(newLikesCount);
                    runOnUiThread(() -> likesCount.setText(String.valueOf(newLikesCount)));
                } else {
                    Log.e("FirebaseData", "Post key is null");
                }
            }

            private void updateLikesInDatabase(String postKey, int newLikesCount, Post post) {
                if (postKey != null) {
                    // Update the likes count in the database using postKey
                    // This assumes you have a "Posts" node in the database
                    DatabaseReference postReference = FirebaseDatabase.getInstance().getReference("Posts").child(postKey);
                    postReference.child("likesCount").setValue(newLikesCount);
                    postReference.child("liked").setValue(post.isLiked()); // Update the liked status
                } else {
                    // Log an error or handle the case where postKey is null
                    Log.e("FirebaseData", "Post key is null");
                }
            }

            private void getComments(String postKey, TextView commentCounts) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postKey).child("Comments");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long commentsCount = snapshot.getChildrenCount();
                        String commentsText = commentsCount + " Comment" + (commentsCount != 1 ? "s" : ""); // Adjust text based on count
                        commentCounts.setText(commentsText);
                        Log.e("FirebaseData", "How many comments:" + commentCounts);

                        updateCommentCountInDatabase(postKey, commentsCount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseData", "Failed to read comments", error.toException());
                    }
                });
            }

            private void runOnUiThread(Runnable action) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(action);
            }

            private void updateCommentCountInDatabase(String postKey, long commentsCount) {
                DatabaseReference postReference = FirebaseDatabase.getInstance().getReference("Posts").child(postKey);
                postReference.child("commentCount").setValue(commentsCount);
            }

            public void bind(Post post) {
                postTitle.setText(post.getTitle());
                postDescription.setText(post.getDescription());
            }
        }
    }