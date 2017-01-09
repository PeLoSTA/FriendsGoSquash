package de.peterloos.friendzzgosquash;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.peterloos.models.Message;

/**
 * Created by Peter on 17.12.2016.
 */

//public class MessagesAdapterAlt extends RecyclerView.Adapter<MessagesAdapterAlt.MessageViewHolder> {
//
//    private List<Message> messagesList;
//
//    public MessagesAdapterAlt(List<Message> messagesList) {
//        this.messagesList = messagesList;
//    }
//
//    public class MessageViewHolder extends RecyclerView.ViewHolder {
//
//        public TextView title;
//        public TextView year;
//        public TextView genre;
//
//        public MessageViewHolder(View view) {
//            super(view);
//
//            this.title = (TextView) view.findViewById(R.id.title);
//            this.genre = (TextView) view.findViewById(R.id.genre);
//            this.year = (TextView) view.findViewById(R.id.year);
//        }
//    }
//
//    @Override
//    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        LayoutInflater inflater =  LayoutInflater.from(parent.getContext());
//        View itemView = inflater.inflate(R.layout.messages_list_row, parent, false);
//        return new MessageViewHolder(itemView);
//    }
//
//    @Override
//    public void onBindViewHolder(MessageViewHolder holder, int position) {
//        Message message = messagesList.get(position);
//        holder.title.setText(message.getTitle());
//        holder.genre.setText(message.getGenre());
//        holder.year.setText(Integer.toString(message.getYear()));
//    }
//
//    @Override
//    public int getItemCount() {
//        return this.messagesList.size();
//    }
//}
