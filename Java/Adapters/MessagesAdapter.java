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
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import pk.edu.itu.bsai23023.chatring.Models.Message;
import pk.edu.itu.bsai23023.chatring.R;
import pk.edu.itu.bsai23023.chatring.databinding.ItemRecievedBinding;
import pk.edu.itu.bsai23023.chatring.databinding.ItemSendBinding;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final ArrayList<Message> messages;

    private final int ITEM_SEND = 1;
    private final int ITEM_RECEIVE = 2;

    private final String senderRoom;
    private final String receiverRoom;

    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_send, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_recieved, parent, false);
            return new ReceiverViewHolder(view);
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

        ReactionPopup popup = new ReactionPopup(context, config, pos -> {
            if (pos >= 0) {
                if (holder instanceof SentViewHolder) {
                    SentViewHolder viewHolder = (SentViewHolder) holder;
                    viewHolder.binding.reaction.setImageResource(reactions[pos]);
                    viewHolder.binding.reaction.setVisibility(View.VISIBLE);
                } else if (holder instanceof ReceiverViewHolder) {
                    ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                    viewHolder.binding.reaction.setImageResource(reactions[pos]);
                    viewHolder.binding.reaction.setVisibility(View.VISIBLE);
                }

                message.setReaction(pos);
                updateReactionInDatabase(message, pos);
            }
            return true;
        });

        if (holder instanceof SentViewHolder) {
            SentViewHolder viewHolder = (SentViewHolder) holder;
            bindMessage(viewHolder.binding, message, popup, reactions);
        } else if (holder instanceof ReceiverViewHolder) {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            bindMessage(viewHolder.binding, message, popup, reactions);
        }
    }

    private void bindMessage(Object binding, Message message, ReactionPopup popup, int[] reactions) {
        if (binding instanceof ItemSendBinding) {
            ItemSendBinding sendBinding = (ItemSendBinding) binding;

            if ("photo".equals(message.getMessage())) {
                sendBinding.image.setVisibility(View.VISIBLE);
                sendBinding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(sendBinding.image);
            } else {
                sendBinding.image.setVisibility(View.GONE);
                sendBinding.message.setVisibility(View.VISIBLE);
                sendBinding.message.setText(message.getMessage());
            }

            handleReactionVisibility(sendBinding.reaction, message, reactions);

            sendBinding.message.setOnTouchListener((view, motionEvent) -> popup.onTouch(view, motionEvent));
            sendBinding.image.setOnTouchListener((view, motionEvent) -> popup.onTouch(view, motionEvent));

        } else if (binding instanceof ItemRecievedBinding) {
            ItemRecievedBinding receiveBinding = (ItemRecievedBinding) binding;

            if ("photo".equals(message.getMessage())) {
                receiveBinding.image.setVisibility(View.VISIBLE);
                receiveBinding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(receiveBinding.image);
            } else {
                receiveBinding.image.setVisibility(View.GONE);
                receiveBinding.message.setVisibility(View.VISIBLE);
                receiveBinding.message.setText(message.getMessage());
            }

            handleReactionVisibility(receiveBinding.reaction, message, reactions);

            receiveBinding.message.setOnTouchListener((view, motionEvent) -> popup.onTouch(view, motionEvent));
            receiveBinding.image.setOnTouchListener((view, motionEvent) -> popup.onTouch(view, motionEvent));
        }
    }

    private void handleReactionVisibility(View reactionView, Message message, int[] reactions) {
        if (message.getReaction() >= 0) {
            reactionView.setVisibility(View.VISIBLE);
            ((android.widget.ImageView) reactionView).setImageResource(reactions[message.getReaction()]);
        } else {
            reactionView.setVisibility(View.GONE);
        }
    }

    private void updateReactionInDatabase(Message message, int reaction) {
        if (senderRoom != null && receiverRoom != null && message.getMessageId() != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(message.getMessageId())
                    .child("reaction")
                    .setValue(reaction);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child("messages")
                    .child(message.getMessageId())
                    .child("reaction")
                    .setValue(reaction);
        } else {
            Log.e("MessagesAdapter", "Error: Null values for senderRoom, receiverRoom, or messageId");
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class SentViewHolder extends RecyclerView.ViewHolder {
        ItemSendBinding binding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSendBinding.bind(itemView);
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        ItemRecievedBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemRecievedBinding.bind(itemView);
        }
    }
}