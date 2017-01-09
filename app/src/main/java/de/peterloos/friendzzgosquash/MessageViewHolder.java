package de.peterloos.friendzzgosquash;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.peterloos.models.Message;

/**
 * Created by Peter on 18.12.2016.
 */

public class MessageViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageviewPhoto;
    public TextView textviewDisplayname;
    public TextView textviewText;

    private Context context;

    public MessageViewHolder(View view) {
        super(view);

        this.context = view.getContext();

        this.imageviewPhoto = (ImageView) this.itemView.findViewById(R.id.message_imageview);
        this.textviewDisplayname = (TextView) this.itemView.findViewById(R.id.message_displayname);
        this.textviewText = (TextView) this.itemView.findViewById(R.id.message_text);
    }

    public void bindMessage(Message model) {

        this.textviewDisplayname.setText(model.getDisplayName());
        this.textviewText.setText(model.getText());


        // JUST TESTING
//        if (model.getPhotoUrl() == null || model.getPhotoUrl().equals("")) {
//            Log.d("PeLo", " ########################### No URL ");
//        }
//        else {
//            Log.d("PeLo", "====================================> PhotoUrl = " + model.getPhotoUrl().toString());
//        }

        Uri uri = null;
        String url = model.getPhotoUrl();
        if (! (url == null || url.equals(""))) {
            uri = Uri.parse (model.getPhotoUrl());
        }

        if (uri == null) {

            Drawable drawable = ContextCompat.getDrawable(this.context, R.drawable.ic_account_empty);
            this.imageviewPhoto.setImageDrawable(drawable);
        } else {

            // Log.d("PeLo", "====================================> PhotoUrl = " + model.getPhotoUrl().toString());

            Glide.with(this.context)
                .load(model.getPhotoUrl())
                .into(this.imageviewPhoto);
        }
    }
}
