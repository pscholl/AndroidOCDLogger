package unifr.sensorrecorder.Evaluation;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import unifr.sensorrecorder.DataContainer.StaticDataProvider;
import unifr.sensorrecorder.NotificationSpawner;
import unifr.sensorrecorder.R;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.datatype.Duration;

public class HandwashEvaluation extends WearableActivity {

    private TextView questionText;
    private Button evalButtonYes;
    private Button evalButtonNo;
    private Button rateButton;
    private RatingBar ratingBar;
    private Button rateButton2;
    private RatingBar ratingBar2;
    private View answerYN;
    private View answerRating;
    private View answerRating2;
    private TextView ratingBarDescL;
    private TextView ratingBarDescR;

    private long timestamp;

    private HashMap<Integer, Integer> questions;
    private HashMap<Integer, Integer> answers;
    private HashMap<Integer, View> answerViews;
    private int currentQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handwash_evaluation);

        Intent intent = getIntent();
        timestamp = intent.getLongExtra("timestamp", -1);

        questionText = (TextView) findViewById(R.id.textQuestionText);
        evalButtonYes = (Button) findViewById(R.id.buttonEvalYes);
        evalButtonNo = (Button) findViewById(R.id.buttonEvalNo);
        rateButton = (Button) findViewById(R.id.answerRateButton);
        ratingBar = (RatingBar) findViewById(R.id.answerRatingBar);
        rateButton2 = (Button) findViewById(R.id.answerRateButton2);
        ratingBar2 = (RatingBar) findViewById(R.id.answerRatingBar2);


        answerYN = (View) findViewById(R.id.answerYesNo);
        answerRating = (View) findViewById(R.id.answerRating);
        answerRating2 = (View) findViewById(R.id.answerRating2);
        answerYN.setVisibility(View.GONE);
        answerRating.setVisibility(View.GONE);
        answerRating2.setVisibility(View.GONE);

        ratingBarDescL = (TextView) findViewById(R.id.rtb_desc_l);
        ratingBarDescR = (TextView) findViewById(R.id.rtb_desc_r);


        questions = new HashMap<>();
        answerViews = new HashMap<>();
        questions.put(0, R.string.not_just_washed_hands);
        questions.put(1, R.string.str_eval_question_1);
        questions.put(2, R.string.str_eval_question_2);
        questions.put(3, R.string.str_eval_question_3);
        answerViews.put(0, answerYN);
        answerViews.put(1, answerYN);
        answerViews.put(2, answerRating2);   // use 5 star rating
        answerViews.put(3, answerRating2);   // use 5 star rating

        answers = new HashMap<>();
        answers.put(0, 1);
        currentQuestion = 1;
        initUi();
        answerYN.setVisibility(View.VISIBLE);

        // Enables Always-on
        setAmbientEnabled();
    }

    private void initUi(){
        questionText.setText(questions.get(currentQuestion));
        answerViews.get(currentQuestion).setVisibility(View.VISIBLE);
        evalButtonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAnswer(1);
            }
        });
        evalButtonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAnswer(0);
            }
        });
        rateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAnswer(Math.round(ratingBar.getRating()));
            }
        });
        rateButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAnswer(Math.round(ratingBar2.getRating()));
            }
        });
        ratingBarDescL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingBar.setRating(0);
            }
        });
        ratingBarDescR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingBar.setRating(10);
            }
        });
    }

    private void setAnswer(int value){
        answers.put(currentQuestion, value);
        currentQuestion++;
        if(currentQuestion >= questions.size()) {
            saveEvaluation();
        } else {
            questionText.setText(questions.get(currentQuestion));
            answerViews.get(currentQuestion-1).setVisibility(View.GONE);
            answerViews.get(currentQuestion).setVisibility(View.VISIBLE);
        }

    }

    private void saveEvaluation(){
        StringBuilder line = new StringBuilder(String.valueOf(timestamp));
        for(int i = 0; i < answers.size(); i++){
            line.append("\t").append(answers.get(i));
        }
        line.append("\n");
        try {
            StaticDataProvider.getProcessor().writeEvaluation(line.toString(), timestamp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(this.getApplicationContext(), getString(R.string.toast_eval_finished), Toast.LENGTH_LONG).show();

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NotificationSpawner.EVALUATION_REQUEST_CODE);
        moveTaskToBack(true);
        finish();
    }
}