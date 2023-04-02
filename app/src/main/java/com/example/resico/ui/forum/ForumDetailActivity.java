package com.example.resico.ui.forum;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resico.R;
import com.example.resico.data.LoginRepository;
import com.example.resico.data.model.ForumComment;
import com.example.resico.data.model.User;
import com.example.resico.data.network.ResiCoAPIHandler;
import com.example.resico.databinding.ActivityForumDetailBinding;
import com.example.resico.utils.DateTimeCalc;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

public class ForumDetailActivity extends AppCompatActivity {
	private final String TAG = this.getClass().getSimpleName();

	private ActivityForumDetailBinding binding;

	private RecyclerView recyclerView;
	private ArrayList<ForumComment> forumComments = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityForumDetailBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		setSupportActionBar(binding.toolbar);
		binding.toolbar.setNavigationOnClickListener(view -> finish());

		// Set adapter to recycle view
		recyclerView = binding.commentRecycle;
		ForumCommentAdapter adapter = new ForumCommentAdapter(forumComments);
		recyclerView.setAdapter(adapter);

		// LinearLayoutManager by default has vertical orientation
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(recyclerView.getContext());
		recyclerView.setLayoutManager(linearLayoutManager);

		// Retrieve event data from API
		ResiCoAPIHandler apiHandler = ResiCoAPIHandler.getInstance();
		String postId = getIntent().getStringExtra(getString(R.string.post_id_key));
		if (postId != null) {
			// Attach data to the corresponding component
			apiHandler.getForumPost(postId, post -> {
				if (post == null) return;
				apiHandler.getUser(post.getPosterUserId(), user -> binding.getRoot().post(() -> {
					binding.forumDetailPosterUsername.setText(user.getUsername());
					Picasso.get().load(user.getImageUrl()).error(R.drawable.placeholder_profile).fit().centerCrop().into(binding.forumDetailPosterProfile);
				}));
				binding.getRoot().post(() -> {
					binding.forumDetailTitle.setText(post.getTitle());
					binding.forumDetailBody.setText(post.getContent());
					Picasso.get().load(post.getImageUrl()).error(R.drawable.placeholder_broken_image).fit().centerCrop().into(binding.forumDetailImage);
					binding.forumDetailLikeAmount.setText(String.valueOf(post.getLikeUserId().length));
					binding.forumDetailCommentAmount.setText(String.valueOf(post.getCommentCount()));
					binding.forumDetailPostTime.setText(" ∙ " + DateTimeCalc.getDurationToNow(post.getPostDateTime()) + " ago");
				});
			});
			apiHandler.getForumComments(postId, forumComments -> {
				if (forumComments == null) return;
				this.forumComments.clear();
				this.forumComments.addAll(Arrays.asList(forumComments));
				binding.getRoot().post(() -> adapter.notifyDataSetChanged());
			});
		}


		// Update make comment user image
		User user = LoginRepository.getUser();
		if (user != null) {
			Picasso.get().load(user.getImageUrl()).error(R.drawable.placeholder_profile).fit().centerCrop().into(binding.forumDetailUserProfile);
		}
	}
}