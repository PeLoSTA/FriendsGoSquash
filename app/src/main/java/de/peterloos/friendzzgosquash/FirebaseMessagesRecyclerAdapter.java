package de.peterloos.friendzzgosquash;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
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


    // TODO:
    // TODO: WAS SOLL DIESE METHODE ???????????????????????????????????????????????????????????????
    // TODO: Wird auf alle Fälle aufgerufen ... und der eine Parameter vom Typ Uri ging NICHT !!!
    // TODO: Nur aus diesem Grund hab ich wieder umgestellt ....................

    /**
     * This method parses the DataSnapshot into the requested type. You can override it in subclasses
     * to do custom parsing.
     *
     * @param snapshot the DataSnapshot to extract the model from
     * @return the model extracted from the DataSnapshot
     */
    @Override
    protected Message parseSnapshot(DataSnapshot snapshot) {

        // use built-in JSON-to-POJO deserializer to convert snapshot to Message object
        Message message = super.parseSnapshot(snapshot);
        return message;
    }
}
