package pk.edu.itu.bsai23023.chatring.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import pk.edu.itu.bsai23023.chatring.Models.Message;
import pk.edu.itu.bsai23023.chatring.Models.User;
import pk.edu.itu.bsai23023.chatring.R;
import pk.edu.itu.bsai23023.chatring.databinding.ItemRecievedGroupBinding;
import pk.edu.itu.bsai23023.chatring.databinding.ItemSendGroupBinding;

public class GroupMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<Message> messages;

    private static final int ITEM_SEND = 1;
    private static final int ITEM_RECEIVE = 2;

    public GroupMessagesAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_send_group, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_recieved_group, parent, false);
            return new RecieverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (FirebaseAuth.getInstance().getUid().equals(message.getSenderId())) {
            return ITEM_SEND;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        int[] reactions = new int[]{
                R.drawable.like,
                R.drawable.love,
                R.drawable.laugh,
                R.drawable.wow,
                R.drawable.sad,
                R.drawable.angry,
                R.drawable.jojoref
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (reactionIndex) -> {
            if (reactionIndex >= 0 && reactionIndex < reactions.length) {
                // Update the reaction image based on the selected reaction
                if (holder instanceof SentViewHolder) {
                    SentViewHolder sentViewHolder = (SentViewHolder) holder;
                    sentViewHolder.binding.reaction.setImageResource(reactions[reactionIndex]);
                    sentViewHolder.binding.reaction.setVisibility(View.VISIBLE);
                } else {
                    RecieverViewHolder receiverViewHolder = (RecieverViewHolder) holder;
                    receiverViewHolder.binding.reaction.setImageResource(reactions[reactionIndex]);
                    receiverViewHolder.binding.reaction.setVisibility(View.VISIBLE);
                }

                message.setReaction(reactionIndex);

                // Update message in Firebase
                if (message.getMessageId() != null) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("public")
                            .child(message.getMessageId())
                            .setValue(message);
                } else {
                    Log.e("GroupMessagesAdapter", "Message ID is null");
                }
            }

            return true; // close popup
        });

        // Bind message content to the view
        if (holder instanceof SentViewHolder) {
            SentViewHolder viewHolder = (SentViewHolder) holder;
            bindSentMessage(viewHolder, message, reactions, popup);
        } else {
            RecieverViewHolder viewHolder = (RecieverViewHolder) holder;
            bindReceivedMessage(viewHolder, message, reactions, popup);
        }
    }

    private void bindSentMessage(SentViewHolder viewHolder, Message message, int[] reactions, ReactionPopup popup) {
        if ("photo".equals(message.getMessage())) {
            viewHolder.binding.image.setVisibility(View.VISIBLE);
            viewHolder.binding.message.setVisibility(View.GONE);
            Glide.with(context)
                    .load(message.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image);
        } else {
            viewHolder.binding.message.setText(message.getMessage());
        }

        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(message.getSenderId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                viewHolder.binding.name.setText("@" + user.getName());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        // Set the reaction if already set
        if (message.getReaction() >= 0) {
            viewHolder.binding.reaction.setImageResource(reactions[message.getReaction()]);
            viewHolder.binding.reaction.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.reaction.setVisibility(View.GONE);
        }

        viewHolder.binding.message.setOnTouchListener((view, motionEvent) -> {
            popup.onTouch(view, motionEvent);
            return false;
        });

        viewHolder.binding.image.setOnTouchListener((view, motionEvent) -> {
            popup.onTouch(view, motionEvent);
            return false;
        });
    }

    private void bindReceivedMessage(RecieverViewHolder viewHolder, Message message, int[] reactions, ReactionPopup popup) {
        if ("photo".equals(message.getMessage())) {
            viewHolder.binding.image.setVisibility(View.VISIBLE);
            viewHolder.binding.message.setVisibility(View.GONE);
            Glide.with(context)
                    .load(message.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image);
        } else {
            viewHolder.binding.message.setText(message.getMessage());
        }

        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(message.getSenderId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                viewHolder.binding.name.setText("@" + user.getName());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        // Set the reaction if already set
        if (message.getReaction() >= 0) {
            viewHolder.binding.reaction.setImageResource(reactions[message.getReaction()]);
            viewHolder.binding.reaction.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.reaction.setVisibility(View.GONE);
        }

        viewHolder.binding.message.setOnTouchListener((view, motionEvent) -> {
            popup.onTouch(view, motionEvent);
            return false;
        });

        viewHolder.binding.image.setOnTouchListener((view, motionEvent) -> {
            popup.onTouch(view, motionEvent);
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {
        ItemSendGroupBinding binding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSendGroupBinding.bind(itemView);
        }
    }

    public class RecieverViewHolder extends RecyclerView.ViewHolder {
        ItemRecievedGroupBinding binding;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemRecievedGroupBinding.bind(itemView);
        }
    }
}