package com.androiditgroup.loclook.tape_pkg;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androiditgroup.loclook.R;

/**
 * Created by OS1 on 29.10.2015.
 */
public class Tape_ViewHolder extends RecyclerView.ViewHolder {

    protected ImageView userAvatar;
    protected TextView  userName;
    protected TextView  publicationDate;
    protected ImageView badgeImage;
    protected TextView  publicationText;
    protected ImageView favorites;
    protected TextView  answersSum;
    protected ImageView answers;
    protected TextView  likedSum;
    protected ImageView likes;
    protected ImageView publicationInfo;

    protected LinearLayout textContainerLL;
    protected LinearLayout photoContainerLL;
    protected LinearLayout quizContainerLL;
    protected LinearLayout favoritesWrapLL;
    protected LinearLayout answersWrapLL;
    protected LinearLayout likedWrapLL;
    protected LinearLayout infoWrapLL;
    protected LinearLayout hLineLL;

    public Tape_ViewHolder(View view) {
        super(view);

        textContainerLL     = (LinearLayout) view.findViewById(R.id.TapeRow_TextContainerLL);
        photoContainerLL    = (LinearLayout) view.findViewById(R.id.TapeRow_PhotoContainerLL);
        quizContainerLL     = (LinearLayout) view.findViewById(R.id.TapeRow_QuizContainerLL);
        favoritesWrapLL     = (LinearLayout) view.findViewById(R.id.TapeRow_FavoritesWrapLL);
        answersWrapLL       = (LinearLayout) view.findViewById(R.id.TapeRow_AnswersWrapLL);
        likedWrapLL         = (LinearLayout) view.findViewById(R.id.TapeRow_LikedWrapLL);
        infoWrapLL          = (LinearLayout) view.findViewById(R.id.TapeRow_InfoWrapLL);
        hLineLL             = (LinearLayout) view.findViewById(R.id.TapeRow_HLine_LL);

        userAvatar          = (ImageView) view.findViewById(R.id.TapeRow_UserAvatarIV);
        userName            = (TextView)  view.findViewById(R.id.TapeRow_UserNameTV);
        publicationDate     = (TextView)  view.findViewById(R.id.TapeRow_PublicationDateTV);
        badgeImage          = (ImageView) view.findViewById(R.id.TapeRow_BadgeImageIV);
        publicationText     = (TextView)  view.findViewById(R.id.TapeRow_PublicationTextTV);
        favorites           = (ImageView) view.findViewById(R.id.TapeRow_FavoritesIV);
        answersSum          = (TextView)  view.findViewById(R.id.TapeRow_AnswersSumTV);
        answers             = (ImageView) view.findViewById(R.id.TapeRow_AnswersIV);
        likedSum            = (TextView)  view.findViewById(R.id.TapeRow_LikedSumTV);
        likes               = (ImageView) view.findViewById(R.id.TapeRow_LikedIV);
        publicationInfo     = (ImageView) view.findViewById(R.id.TapeRow_PublicationInfoIV);
    }
}