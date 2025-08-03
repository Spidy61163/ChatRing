package pk.edu.itu.bsai23023.chatring.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;
import pk.edu.itu.bsai23023.chatring.Activities.MainActivity;
import pk.edu.itu.bsai23023.chatring.Models.Status;
import pk.edu.itu.bsai23023.chatring.Models.UserStatus;
import pk.edu.itu.bsai23023.chatring.R;
import pk.edu.itu.bsai23023.chatring.databinding.ItemStatusBinding;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {

    Context context;
    ArrayList<UserStatus> userStatuses;

    public StatusAdapter(Context context, ArrayList<UserStatus> userStatuses) {
        this.context = context;
        this.userStatuses = userStatuses;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        UserStatus userStatus = userStatuses.get(position);

        if (!userStatus.getStatuses().isEmpty()) {
            Status lastStatus = userStatus.getStatuses().get(userStatus.getStatuses().size() - 1);
            Glide.with(context).load(lastStatus.getImageUrl()).into(holder.binding.image);
            holder.binding.circularStatusView.setPortionsCount(userStatus.getStatuses().size());
        }

        holder.binding.circularStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<MyStory> myStories = new ArrayList<>();

                for (Status status : userStatus.getStatuses()) {
                    myStories.add(new MyStory(status.getImageUrl()));
                }

                if (!myStories.isEmpty()) {
                    new StoryView.Builder(((MainActivity) context).getSupportFragmentManager())
                            .setStoriesList(myStories)
                            .setStoryDuration(5000)
                            .setTitleText(userStatus.getName())
                            .setSubtitleText("")
                            .setTitleLogoUrl(userStatus.getProfileImage())
                            .setStoryClickListeners(new StoryClickListeners() {
                                @Override
                                public void onDescriptionClickListener(int position) {

                                }

                                @Override
                                public void onTitleIconClickListener(int position) {

                                }
                            })
                            .build()
                            .show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userStatuses.size();
    }

    public class StatusViewHolder extends RecyclerView.ViewHolder {
        ItemStatusBinding binding;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemStatusBinding.bind(itemView);
        }
    }
}