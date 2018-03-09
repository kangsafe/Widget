package com.ks.plugin.widget.launcher.changba;

import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ks.plugin.widget.launcher.AvatarHelper;
import com.ks.plugin.widget.launcher.R;

/**
 * Created by Monkey on 2015/6/29.
 */
public class HomeViewHolder extends RecyclerView.ViewHolder {
    public ImageView vimgpic;
    public ImageView vimgbg;
    public TextView vtitle;

    public HomeViewHolder(View itemView) {
        super(itemView);
        vimgpic = itemView.findViewById(R.id.vIconPic);
        vimgbg = itemView.findViewById(R.id.vIconBg);
        vtitle = itemView.findViewById(R.id.vName);
    }

    public void bindHolder(TitlePhoto m) {
        AvatarHelper.setAvatar(vimgpic, m.getHeadphoto());
        AvatarHelper.setAvatar(vimgbg, m.getImage());
//        Glide.with(vimgpic).load(m.getImage()).into(new SimpleTarget<Drawable>() {
//            @Override
//            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    vimgpic.setBackground(resource);
//                } else {
//                    vimgpic.setBackgroundDrawable(resource);
//                }
//            }
//        });

        vtitle.setText(m.getName());
    }
}
