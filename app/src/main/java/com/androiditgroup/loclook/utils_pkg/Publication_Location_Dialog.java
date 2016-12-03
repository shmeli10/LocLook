package com.androiditgroup.loclook.utils_pkg;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androiditgroup.loclook.R;
import com.androiditgroup.loclook.answers_pkg.Answers_Activity;
import com.androiditgroup.loclook.tape_pkg.Tape_Activity;
import com.androiditgroup.loclook.user_profile_pkg.User_Profile_Activity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by OS1 on 24.11.2015.
 */
public class Publication_Location_Dialog    extends    DialogFragment
                                            implements View.OnClickListener,
                                                       OnMapReadyCallback {

    private GoogleMap   googleMap;
    private UiSettings  UISettings;
    private Marker      marker;

    private LatLng      location;
    private String      regionName;
    private String      streetName;

    FragmentActivity    activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.PublicationLocationInfoDialog_Theme);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        View view = inflater.inflate(R.layout.dialog_publication_location, null);
        view.findViewById(R.id.PublicationLocationDialog_CloseLL).setOnClickListener(this);

        if(getActivity() instanceof Answers_Activity)
            activity = (Answers_Activity) getActivity();
        else if(getActivity() instanceof Tape_Activity)
            activity = (Tape_Activity) getActivity();
        else if(getActivity() instanceof User_Profile_Activity)
            activity = (User_Profile_Activity) getActivity();

        SupportMapFragment mapFragment = (SupportMapFragment) activity.getSupportFragmentManager().findFragmentById(R.id.PublicationLocationDialog_Map);
        mapFragment.getMapAsync(this);

        return view;
    }

    public void onClick(View view) {
        getDialog().hide();
    }

    public void onDismiss(DialogInterface dialog) {
        if(getDialog() != null)
            getDialog().hide();
    }

    public void onCancel(DialogInterface dialog) {
        getDialog().hide();
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onMapReady(GoogleMap google_map) {
        googleMap = google_map;

        // получаем ссылку к пользовательским настройкам карты
        UISettings = this.googleMap.getUiSettings();

        // зум включить
        UISettings.setZoomControlsEnabled(false);

        UISettings.setAllGesturesEnabled(false);

        //
        UISettings.setMapToolbarEnabled(false);

        // вращение карты выключить
        UISettings.setRotateGesturesEnabled(false);

        // отобразить маркер с окружностью и отцентрировать карту
        showLocation(location, regionName, streetName);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void showLocation(LatLng point, String region_name, String street_name) {
        CameraPosition newPoint = new CameraPosition.Builder().target(point).zoom(15f).bearing(0).tilt(0).build();

        // центрируем карту на заданной точке
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(newPoint));

        // формируем маркер в заданной точке
        marker = googleMap.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(false));

        // сформируем адрес точки, где была сделана публикация
        StringBuilder publicationAddress = new StringBuilder();

        // если город/область/страна определены, выводим на экран
        if((region_name != null) && (!region_name.equals(""))) {

            // прикрепляем название города/области/страны
            publicationAddress.append(region_name);

            // если улица уже прикреплена
            if(publicationAddress.length() > 0)
                publicationAddress.append(", ");
        }

        // если улица определена, выводим на экран
        if((street_name != null) && (!street_name.equals("")))
            publicationAddress.append(street_name);

        // если адрес содержит значение
        if(publicationAddress.length() > 0)
            marker.setTitle(publicationAddress.toString());
        // подставляем текст "Неизвестная область"
        else
            marker.setTitle(getResources().getString(R.string.undefined_area) + "...");

        // сразу отобразить заголовок маркера
        marker.showInfoWindow();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    public void setLocation(float latitude, float longitude) {
        this.location = new LatLng(latitude, longitude);
    }

    //
    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    //
    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    //
    public void resetLocation() {

        // отобразить маркер с окружностью и отцентрировать карту
        showLocation(location, regionName, streetName);
    }
}