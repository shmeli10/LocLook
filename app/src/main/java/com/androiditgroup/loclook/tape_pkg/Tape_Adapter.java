package com.androiditgroup.loclook.tape_pkg;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.androiditgroup.loclook.R;
import com.androiditgroup.loclook.answers_pkg.Answers_Activity;

import java.util.List;


/**
 * Created by OS1 on 29.10.2015.
 */
public class Tape_Adapter extends RecyclerView.Adapter<Tape_ViewHolder> {

    Context context;

    float density;

    private List<Tape_ListItems>            publications;

    private OnBadgeClickListener            badgeListener;
    private OnFavoritesClickListener        favoritesListener;
    private OnAnswersClickListener          answersListener;
    private OnLikedClickListener            likesListener;
    private OnPublicationInfoClickListener  infoListener;

    // интерфейс для работы с Tape_Activity
    public interface OnBadgeClickListener {
        void onBadgeClicked(int badgeId, int badgeDrawable);
    }

    // интерфейс для работы с Tape_Activity
    public interface OnFavoritesClickListener {
        void onFavoritesClicked(String operationName, Tape_ListItems tapeListItems, int favoritesPublicationRowId, int publicationId);
    }

    // интерфейс для работы с Tape_Activity
    public interface OnAnswersClickListener {
        void onAnswersClicked();
    }

    // интерфейс для работы с Tape_Activity
    public interface OnLikedClickListener {
        void onLikedClicked(String operationName, Tape_ListItems tapeListItems, int likesPublicationRowId, int publicationId);
    }

    // интерфейс для работы с Tape_Activity
    public interface OnPublicationInfoClickListener {
        void onPublicationInfoClicked(int publicationId, int authorId, float latitude, float longitude, String regionName, String streetName, String publicationText);
    }

    public Tape_Adapter(Context context, List<Tape_ListItems> publications) {
        this.publications = publications;
        this.context = context;
    }

    @Override
    public Tape_ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int position) {

        // dbHelper    = new DB_Helper(context);
        density     = context.getResources().getDisplayMetrics().density;

        //////////////////////////////////////////////////////////////////////////////////

        View v = LayoutInflater.from(context).inflate(R.layout.row_tape, null);
        Tape_ViewHolder holder = new Tape_ViewHolder(v);

        holder.getLayoutPosition();

        // если Tape_Activity выполняет интерфейс
        if (context instanceof OnBadgeClickListener)
            // получаем ссылку на Tape_Activity
            badgeListener = (OnBadgeClickListener) context;
        else
            throw new ClassCastException(context.toString() + " must implement OnBadgeClickListener");

        // если Tape_Activity выполняет интерфейс
        if (context instanceof OnFavoritesClickListener)
            // получаем ссылку на Tape_Activity
            favoritesListener = (OnFavoritesClickListener) context;
        else
            throw new ClassCastException(context.toString() + " must implement OnFavoritesClickListener");

        // если Tape_Activity выполняет интерфейс
        if (context instanceof OnAnswersClickListener)
            // получаем ссылку на Tape_Activity
            answersListener = (OnAnswersClickListener) context;
        else
            throw new ClassCastException(context.toString() + " must implement OnAnswersClickListener");

        // если Tape_Activity выполняет интерфейс
        if (context instanceof OnLikedClickListener)
            // получаем ссылку на Tape_Activity
            likesListener = (OnLikedClickListener) context;
        else
            throw new ClassCastException(context.toString() + " must implement OnLikesClickListener");

        // если Tape_Activity выполняет интерфейс
        if (context instanceof OnPublicationInfoClickListener)
            // получаем ссылку на Tape_Activity
            infoListener = (OnPublicationInfoClickListener) context;
        else
            throw new ClassCastException(context.toString() + " must implement OnPublicationInfoClickListener");

        return holder;
    }

    @Override
    public void onBindViewHolder(final Tape_ViewHolder tapeViewHolder, final int position) {
        final Tape_ListItems publication = publications.get(position);

        /////////////////////////////////////////////////////////////////////////////

        final int publicationId = publication.getPublicationId();
        final int authorId      = publication.getAuthorId();

        int userAvatar          = publication.getUserAvatar();
        final int badgeId       = publication.getBadgeId();
        final int badgeImg      = publication.getBadgeImage();
        int favoritesImg        = publication.getFavorites();
        int answersImg          = publication.getAnswers();
        int likesImg            = publication.getLikes();
        int infoImg             = publication.getPublicationInfo();

        String userName         = publication.getUserName();
        String date             = publication.getPublicationDate();


        final float latitude    = publication.getLatitude();
        final float longitude   = publication.getLongitude();

        final String text       = publication.getPublicationText();
        final String regionName = publication.getRegionName();
        final String streetName = publication.getStreetName();

        /////////////////////////////////////////////////////////////////////////////

        final Intent intent = new Intent(context,Answers_Activity.class);

        intent.putExtra("authorId",         authorId);
        intent.putExtra("userName",         userName);
        intent.putExtra("publicationDate",  date);
        intent.putExtra("badgeImg",         badgeImg);
        intent.putExtra("publicationText",  text);
        intent.putExtra("itemPosition",     position);
        intent.putExtra("latitude",         latitude);
        intent.putExtra("longitude",        longitude);
        intent.putExtra("regionName",       regionName);
        intent.putExtra("streetName",       streetName);

        /////////////////////////////////////////////////////////////////////////////

        tapeViewHolder.userAvatar.setImageResource(userAvatar);
        tapeViewHolder.userName.setText(userName);
        tapeViewHolder.publicationDate.setText(date);

        tapeViewHolder.badgeImage.setImageResource(badgeImg);
        tapeViewHolder.badgeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log.d("Tape_Adapter", "badge where date: " + tapeViewHolder.publicationDate.getText().toString());

                badgeListener.onBadgeClicked(badgeId, badgeImg);
            }
        });

        tapeViewHolder.textContainerLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // удаляем диалоговое окно, если оно существует и находится в скрытом состоянии
                answersListener.onAnswersClicked();

                // динамически изменяемые данные
                intent.putExtra("publicationId",            publicationId);
                intent.putExtra("isFavorite",               publication.isPublicationFavorite());
                intent.putExtra("favoritePublicationRowId", publication.getFavoritePublicationRowId());
                intent.putExtra("answersSum",               publication.getAnswersSum());
                intent.putExtra("likedSum",                 publication.getLikedSum());
                intent.putExtra("isLiked",                  publication.isPublicationLiked());
                intent.putExtra("likedPublicationRowId",    publication.getLikedPublicationRowId());

                ((Tape_Activity) context).startActivityForResult(intent, 0);
            }
        });
        tapeViewHolder.publicationText.setText(text);

        // чистим контейнер изображений в публикации
        tapeViewHolder.photoContainerLL.removeAllViews();

        // добавляем опрос в публикацию
        ((Tape_Activity) context).addImagesToPublication(tapeViewHolder.photoContainerLL, publicationId);


        // чистим контейнер опроса в публикации
        tapeViewHolder.quizContainerLL.removeAllViews();

        // добавляем опрос в публикацию
        ((Tape_Activity) context).addQuizToPublication(tapeViewHolder.quizContainerLL, publicationId);

        tapeViewHolder.favoritesWrapLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // если favorite надо убрать
                if (publication.isPublicationFavorite()) {
                    favoritesListener.onFavoritesClicked("delete", publication, publication.getFavoritePublicationRowId(), publicationId);
                    tapeViewHolder.favorites.setImageResource(R.drawable.star_icon);
                }
                // если favorite надо добавить
                else {
                    favoritesListener.onFavoritesClicked("add", publication, 0, publicationId);
                    tapeViewHolder.favorites.setImageResource(R.drawable.star_icon_active);
                }
            }
        });
        tapeViewHolder.favorites.setImageResource(favoritesImg);

        tapeViewHolder.answersSum.setText(publication.getAnswersSum());
        tapeViewHolder.answersWrapLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // удаляем диалоговое окно, если оно существует и находится в скрытом состоянии
                answersListener.onAnswersClicked();

                // динамически изменяемые данные
                intent.putExtra("publicationId",            publicationId);
                intent.putExtra("isFavorite",               publication.isPublicationFavorite());
                intent.putExtra("favoritePublicationRowId", publication.getFavoritePublicationRowId());
                intent.putExtra("answersSum",               publication.getAnswersSum());
                intent.putExtra("likedSum",                 publication.getLikedSum());
                intent.putExtra("isLiked",                  publication.isPublicationLiked());
                intent.putExtra("likedPublicationRowId",    publication.getLikedPublicationRowId());

                ((Tape_Activity) context).startActivityForResult(intent, 0);
            }
        });
        tapeViewHolder.answers.setImageResource(answersImg);


        tapeViewHolder.likedSum.setText(publication.getLikedSum());
        tapeViewHolder.likedWrapLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // если like надо убрать
                if (publication.isPublicationLiked()) {
                    likesListener.onLikedClicked("delete", publication, publication.getLikedPublicationRowId(), publicationId);
                    tapeViewHolder.likes.setImageResource(R.drawable.like_icon);
                }
                // если like надо добавить
                else {
                    likesListener.onLikedClicked("add", publication, 0, publicationId);
                    tapeViewHolder.likes.setImageResource(R.drawable.like_icon_active);
                }

                tapeViewHolder.likedSum.setText("" + publication.getLikedSum());
            }
        });
        tapeViewHolder.likes.setImageResource(likesImg);

        tapeViewHolder.infoWrapLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // вызываем метод-обработчик нажатия на "пункт информации" (горизонтальное троеточие)
                infoListener.onPublicationInfoClicked(publicationId, authorId, latitude, longitude, regionName, streetName, text);
            }
        });
        tapeViewHolder.publicationInfo.setImageResource(infoImg);

        // если это не последний элемент в списке
        if((publications.size() - 1) != position) {

            // создаем горизонтальную линию
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ((int) (1 * density)));

            View hLine = new View(context);
            hLine.setLayoutParams(layoutParams);
            hLine.setBackgroundResource(R.color.h_line_grey);

            // добавить линию в контейнер
            tapeViewHolder.hLineLL.addView(hLine);
        }
        // последний элемент в списке
        else
            // очистить контейнер
            tapeViewHolder.hLineLL.removeAllViews();
    }

    public void clearAdapter () {
        publications.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (null != publications ? publications.size() : 0);
    }
}