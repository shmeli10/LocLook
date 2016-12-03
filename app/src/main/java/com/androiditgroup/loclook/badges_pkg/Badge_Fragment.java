package com.androiditgroup.loclook.badges_pkg;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.androiditgroup.loclook.R;

/**
 * Created by OS1 on 21.09.2015.
 */
public class Badge_Fragment     extends     Fragment
                                implements  CompoundButton.OnCheckedChangeListener {

    public int badgeIdValue = 0;
    public boolean switchStateOnValue = true;

    public static String badgeMarginTopParam = "badge_margin_top";
    public static int badgeMarginTopValue = 0;

    public static String badgeMarginBottomParam = "badge_margin_bottom";
    public static int badgeMarginBottomValue = 0;

    public static String badgeImgParam = "badge_img";
    public static String badgeImgValue = "badge_1";

    public static String badgeNameParam = "badge_name";
    public static String badgeNameValue = "Undefined";


    Bundle args;

    Context context;

    View badgeView;

    RelativeLayout badgeRowRL;

    LinearLayout.LayoutParams layoutParams;

    Switch switchStatusBtn;
//    ToggleButton switchStatusBtn;

    float density;

    private OnSwitchStateChangedListener badgeActivityListener;

    //
    public interface OnSwitchStateChangedListener {
      public void onSwitchStateChanged(int badgeId,boolean switchIsOff);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnSwitchStateChangedListener) {
            badgeActivityListener = (OnSwitchStateChangedListener) activity;
        }
        else {
            throw new ClassCastException(activity.toString() + " must implement OnSwitchStateChangedListener");
        }
    }

    //
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        args    = getArguments();
        context = getActivity().getApplicationContext();
        density = context.getResources().getDisplayMetrics().density;

        badgeMarginTopValue     = args.getInt(badgeMarginTopParam);
        badgeMarginBottomValue  = args.getInt(badgeMarginBottomParam);

        badgeImgValue           = args.getString(badgeImgParam);
        badgeNameValue          = args.getString(badgeNameParam);

        badgeView = inflater.inflate(R.layout.fragment_badge, null);

        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        return badgeView;
    }

    //
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        badgeRowRL = (RelativeLayout) badgeView.findViewById(R.id.Badges_BadgeRowRL);

        setMargins(layoutParams, 0, badgeMarginTopValue, 0, badgeMarginBottomValue);
        badgeRowRL.setLayoutParams(layoutParams);

        if(badgeIdValue > 0) {

            String uri="@drawable/" + badgeImgValue;
            ((ImageView) badgeView.findViewById(R.id.Badges_BadgeImageIV)).setImageResource(getResources().getIdentifier(uri, null, getActivity().getPackageName()));
        }

        if(badgeNameValue != null)
            ((TextView) badgeView.findViewById(R.id.Badges_BadgeTextTV)).setText(badgeNameValue);


         switchStatusBtn = (Switch) badgeView.findViewById(R.id.Badges_SwitchStatusBTN);
//        switchStatusBtn = (ToggleButton) badgeView.findViewById(R.id.Badges_SwitchStatusBTN);
        switchStatusBtn.setOnCheckedChangeListener(this);

        if(!switchStateOnValue)
            switchStatusBtn.setChecked(false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        badgeActivityListener.onSwitchStateChanged(badgeIdValue, switchStatusBtn.isChecked());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void setMargins(LinearLayout.LayoutParams layout,int left, int top, int right, int bottom) {

        int marginLeft     = (int)(left * density);
        int marginTop      = (int)(top * density);
        int marginRight    = (int)(right * density);
        int marginBottom   = (int)(bottom * density);

        layout.setMargins(marginLeft, marginTop, marginRight, marginBottom);
    }

    //
    public void setBadgeId(int badgeId) {
        this.badgeIdValue = badgeId;
    }

    //
    public void setSwitchState(String switchIsOn) {

        if(switchIsOn.equals("N"))
          this.switchStateOnValue = false;
    }
}