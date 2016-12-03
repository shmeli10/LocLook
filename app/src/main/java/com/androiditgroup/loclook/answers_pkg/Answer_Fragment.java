package com.androiditgroup.loclook.answers_pkg;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androiditgroup.loclook.R;

/**
 * Created by OS1 on 20.11.2015.
 */
public class Answer_Fragment    extends     Fragment
                                implements  View.OnClickListener {

    final private int userAvatarLLResId         = R.id.AnswerRow_UserAvatar_LL;
    final private int userNameLLResId           = R.id.AnswerRow_UserName_LL;
    final private int userNameTVResId           = R.id.AnswerRow_UserNameTV;
    final private int answerTextTVResId         = R.id.AnswerRow_AnswerTextTV;
    final private int answerTimeAgoTextTVResId  = R.id.AnswerRow_AnswerTimeAgoTextTV;
    final private int selectRecipientTVResId    = R.id.AnswerRow_SelectRecipientTV;

    private int userId;

    private String userName;
    private String answerText;
    private String answerTimeAgoText;

    private boolean isRecipientSelectable;

    private OnRecipientDataClickListener recipientDataClickListener;
    private OnSelectRecipientClickListener selectRecipientClickListener;

    //
    public interface OnRecipientDataClickListener {
        public void onRecipientDataClick(int selectedRecipientId, String selectedRecipientName);
    }

    //
    public interface OnSelectRecipientClickListener {
        public void onSelectRecipientClick(int selectedRecipientId, String selectedRecipientName);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnRecipientDataClickListener) {
            recipientDataClickListener = (OnRecipientDataClickListener) activity;
        }
        else {
            throw new ClassCastException(activity.toString() + " must implement OnRecipientDataClickListener");
        }

        if (activity instanceof OnSelectRecipientClickListener) {
            selectRecipientClickListener = (OnSelectRecipientClickListener) activity;
        }
        else {
            throw new ClassCastException(activity.toString() + " must implement OnSelectRecipientClickListener");
        }
    }

    //
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View answerView = inflater.inflate(R.layout.row_answer, null);

        (answerView.findViewById(userAvatarLLResId)).setOnClickListener(this);
        (answerView.findViewById(userNameLLResId)).setOnClickListener(this);

        // TextView userNameTV = (TextView) answerView.findViewById(R.id.AnswerRow_UserNameTV);
        ((TextView) answerView.findViewById(userNameTVResId)).setText(userName);

        // TextView answerTextTV = (TextView) answerView.findViewById(R.id.AnswerRow_AnswerTextTV);
        ((TextView) answerView.findViewById(answerTextTVResId)).setText(answerText);

        // TextView answerTimeAgoTextTV    = (TextView) answerView.findViewById(R.id.AnswerRow_AnswerTimeAgoTextTV);
        ((TextView) answerView.findViewById(answerTimeAgoTextTVResId)).setText(answerTimeAgoText);

        TextView selectRecipientTV  = (TextView) answerView.findViewById(selectRecipientTVResId);
        selectRecipientTV.setOnClickListener(this);

        selectRecipientTV.setClickable(isRecipientSelectable);

        if(!isRecipientSelectable)
            selectRecipientTV.setVisibility(View.INVISIBLE);

        return answerView;
    }

    //
    public void setUserId(int userId) {
        this.userId = userId;
    }

    //
    public void setUserName(String userName) {
        this.userName = userName;
    }

    //
    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    //
    public void setAnswerTimeAgoText(String answerTimeAgoText) {
        this.answerTimeAgoText = answerTimeAgoText;
    }

    //
    public void setIsRecipientSelectable(boolean isRecipientSelectable) {
        this.isRecipientSelectable = isRecipientSelectable;
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClick(View v) {

        switch(v.getId()) {

            case selectRecipientTVResId:
                                        selectRecipientClickListener.onSelectRecipientClick(userId, userName);
                                        break;
            case userAvatarLLResId:
            case userNameLLResId:
                                        recipientDataClickListener.onRecipientDataClick(userId, userName);
                                        break;
        }
    }
}