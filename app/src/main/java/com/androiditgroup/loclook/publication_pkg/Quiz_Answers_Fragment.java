package com.androiditgroup.loclook.publication_pkg;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androiditgroup.loclook.R;

/**
 * Created by OS1 on 25.09.2015.
 */
public class Quiz_Answers_Fragment extends Fragment {

    Context context;

    private TextView addFieldTV;

    float density;

    int quizAnswerSum = 2;

    int answerNum = 10;

    LinearLayout.LayoutParams layoutParams;
    LinearLayout.LayoutParams editTextLayoutParams;
    LinearLayout.LayoutParams buttonDeleteLayoutParams;
    LinearLayout.LayoutParams addFieldLayoutParams;

    private OnQuizAnswerSumChangedListener publicationActivityListener;

    // интерфейс для работы с Publication_Activity
    public interface OnQuizAnswerSumChangedListener {
        public void onQuizAnswerSumChanged(String operationName, String answerLLTagName, EditText answerET);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // если Publication_Activity выполняем интерфейс
        if (activity instanceof OnQuizAnswerSumChangedListener) {
            // получаем ссылку на Publication_Activity
            publicationActivityListener = (OnQuizAnswerSumChangedListener) activity;
        }
        else {
            throw new ClassCastException(activity.toString() + " must implement OnQuizAnswerSumChangedListener");
        }
    }

    //
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        context = getActivity().getApplicationContext();

        density = context.getResources().getDisplayMetrics().density;

        // определяем параметры компоновки для элементов фрагмента
        layoutParams                = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        editTextLayoutParams        = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,.70f);
        buttonDeleteLayoutParams    = new LinearLayout.LayoutParams(85, 75,.30f);
        addFieldLayoutParams        = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        ///////////////////////////////////////////////////////////////////////////////

        // главный контейнер фрагмента
        final LinearLayout answersContainerLL = new LinearLayout(context);
        answersContainerLL.setLayoutParams(layoutParams);
        answersContainerLL.setOrientation(LinearLayout.VERTICAL);

        ////////////////////////////////////////////////////////

        TextView answersTV = new TextView(context);
        answersTV.setText(context.getString(R.string.answers_text) + ":");
        answersTV.setTextColor(Color.BLACK);
        answersTV.setTextSize(12);

        answersContainerLL.addView(answersTV);

        ////////////////////////////////////////////////////////

        // контейнер всех ответов во фрагменте
        final LinearLayout lastAnswersLL = new LinearLayout(context);
        lastAnswersLL.setLayoutParams(layoutParams);
        lastAnswersLL.setOrientation(LinearLayout.VERTICAL);

        // добавляем первые 2 статических поля с ответами, их нельзя удалять
        lastAnswersLL.addView(getAnswerLL(true));
        lastAnswersLL.addView(getAnswerLL(true));

        // добавляем контейнер с ответами, в главный контейнер фрагмента
        answersContainerLL.addView(lastAnswersLL);

        ////////////////////////////////////////////////////////

        // текстовое поле "Добавить поле", выполняющее роль кнопки добавления полей
        // final TextView addFieldTV = new TextView(context);
        addFieldTV = new TextView(context);
        setMargins(addFieldLayoutParams, 0, 10, 0, 10);
        addFieldTV.setLayoutParams(addFieldLayoutParams);
        addFieldTV.setBackgroundResource(R.color.orange);
        addFieldTV.setTextColor(Color.WHITE);
        addFieldTV.setTextSize(12);
        addFieldTV.setText(context.getString(R.string.add_answer_text));
        addFieldTV.setGravity(Gravity.CENTER_HORIZONTAL);

        setPaddings(addFieldTV, 10, 10, 10, 10);

        // назначаем обработчик клика по текстовому полю
        addFieldTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // если кол-во ответов не достигло 10
                if (quizAnswerSum < 10) {

                    // добавляем в контейнер с ответами очередную строку с ответом
                    lastAnswersLL.addView(getAnswerLL(false));

                    // учитываем что ответ добавлен
                    quizAnswerSum++;

                    if(quizAnswerSum == 10) {
                        addFieldTV.setBackgroundResource(R.color.h_light_grey);
                        addFieldTV.setClickable(false);
                    }
                }
            }
        });

        // добавляем текстовое поле в главный контейнер фрагмента
        answersContainerLL.addView(addFieldTV);

        ///////////////////////////////////////////////////////////

        // возвращаем заполненный главный контейнер фрагмента
        return answersContainerLL;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private LinearLayout getAnswerLL(boolean isStatic) {

        // номер поля ответа
        answerNum++;

        ////////////////////////////////////////////////////////////////////////////////

        // контейнер для поля с ответом и кнопки удаления ответа
        final LinearLayout answerLL = new LinearLayout(context);
        answerLL.setLayoutParams(layoutParams);
        answerLL.setOrientation(LinearLayout.HORIZONTAL);

        ////////////////////////////////////////////////////////////////////////////////

        // поле для ответа
        final EditText answerET = getAnswerField(editTextLayoutParams, 6, "");

        // задаем тег для уникальности поля
        answerET.setTag("answerET_" + answerNum);
        answerET.setMaxLines(4);

        // устанавливаем ограничение по кол-ву символов в ответе
        int maxLength = 100;
        answerET.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});

        // отправляем запрос в Publication_Activity, на добавление ответа в коллекцию ответов
        publicationActivityListener.onQuizAnswerSumChanged("add",answerET.getTag().toString(), answerET);

        ////////////////////////////////////////////////////////////////////////////////

        // изображение выполняющее роль кнопки удаления ответа
        ImageView deleteQuizAnswerIV = getAnswerButton(buttonDeleteLayoutParams, -5);

        // если этот ответ можно удалить
        if(!isStatic) {
            // устанавливаем обработчик клика по изображению
            deleteQuizAnswerIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // отправляем запрос в Publication_Activity, на удаление ответа из коллекции ответов
                    publicationActivityListener.onQuizAnswerSumChanged("delete",answerET.getTag().toString(), null);

                    // удаляем из контейнера поле с ответом и само изображение
                    answerLL.removeAllViews();

                    // учитываем что ответ удален
                    quizAnswerSum--;

                    if(quizAnswerSum < 10) {
                        addFieldTV.setBackgroundResource(R.color.orange);

                        addFieldTV.setClickable(true);
                    }
                }
            });
        }

        // добавляем поле для ответа и кнопку в контейнер
        answerLL.addView(answerET);
        answerLL.addView(deleteQuizAnswerIV);

        // вернуть заполненный контейнер
        return answerLL;
    }

    //
    private EditText getAnswerField(LinearLayout.LayoutParams layout,int margintop,String text) {

        EditText field = new EditText(context);

        setMargins(layout, 0, margintop, 0, 0);
        field.setLayoutParams(layout);
        field.setBackground(getResources().getDrawable(R.drawable.rounded_rect_quiz_field_white));
        field.setText(text);
        field.setTextColor(Color.BLACK);
        field.setTextSize(12);

        return field;
    }

    //
    private ImageView getAnswerButton(LinearLayout.LayoutParams layout,int marginLeft) {

        ImageView btn = new ImageView(context);

        setMargins(layout, marginLeft, 0, 0, 0);
        btn.setLayoutParams(layout);
        btn.setImageResource(R.drawable._delete_quiz_answer_btn);

        return btn;
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
    private void setPaddings(View view, int left, int top, int right, int bottom) {

        int paddingLeft     = (int)(left * density);
        int paddingTop      = (int)(top * density);
        int paddingRight    = (int)(right * density);
        int paddingBottom   = (int)(bottom * density);

        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }
}