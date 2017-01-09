package de.peterloos.friendzzgosquash;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import de.peterloos.models.Message;

/**
 * Created by Peter on 18.12.2016.
 */

public class FirebaseMessagesRecyclerAdapter extends FirebaseRecyclerAdapter<Message, MessageViewHolder> {

    public FirebaseMessagesRecyclerAdapter (
            Class<Message> modelClass,
            int modelLayout,
            Class<MessageViewHolder> viewHolderClass,
            DatabaseReference ref) {

        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(MessageViewHolder viewHolder, Message model, int position) {

        /* mProgressBar.setVisibility(ProgressBar.INVISIBLE);   */    /* TODO    WIE MACHEN WIR DAS MIT DER PROGRESSBAR ?!?!?!?!  */

        viewHolder.bindMessage(model);
    }

    @Override
    protected Message parseSnapshot(DataSnapshot snapshot) {

        // use built-in JSON-to-POJO deserializer to convert snapshot into Message object
        Message message = super.parseSnapshot(snapshot);
        return message;
    }
}
