package com.example.guesscelebrity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    //data for pupulating names to buttons
    String[] responses = new String[4];
    int correctResponse = 0;
    Button button0, button1, button2, button3;
    //create Array to keep list of celebrities
    ArrayList<String> nameList = new ArrayList<String>();
    ArrayList<String> linkList = new ArrayList<String>();
    //keep selected name
    int selectedName = 0;
    //keep image
    ImageView pictureHolder;

    //function for clicking buttons to answer who is on the photo
    public void selectedNameFunction(View view){
        Toast toast = null;
        Context context = getApplicationContext();
        if(view.getTag().toString().equals(Integer.toString(correctResponse))){
            String info = "Yes, you're genius";
            toast.makeText(context, Html.fromHtml("<font color='#008000' ><b>" + info + "</b></font>"),
                     Toast.LENGTH_LONG).show();
        }
        else{
            String info = "Nope, it was " + nameList.get(selectedName);
            toast.makeText(context, Html.fromHtml("<font color='#8B0000' ><b>" + info
                            + "</b></font>"),
                            Toast.LENGTH_LONG).show();
                }

        //make sure toast is shown before jumping to another question
        if(toast == null || !toast.getView().isShown()) {
            //generateNewQuestion();
            delay(2000);
        }

    }
    //image download task
    public class PictureDownload extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... links) {
            try {
                URL link = new URL(links[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) link.openConnection();
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                Bitmap imageOfPerson = BitmapFactory.decodeStream(inputStream);
                return imageOfPerson;

            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }
    //retrive name and image link from the website
    public class DownloadClass extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            //convert string to url
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream io = urlConnection.getInputStream();
                InputStreamReader isr = new InputStreamReader(io);
                int  data =isr.read();
                while (data != -1){
                    char curChar = (char) data;
                    result += curChar;
                    data = isr.read();
                }
                return result;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }
    //create new question after answering
    public void generateNewQuestion(){
        try {
            //create random name to pupulate image box
            Random random = new Random();
            //pull random from links
            selectedName = random.nextInt(linkList.size());
            //download image
            PictureDownload pictureDownload = new PictureDownload();
            Bitmap nameImage = pictureDownload.execute(linkList.get(selectedName)).get();
            //pull downloaded image to image box
            pictureHolder.setImageBitmap(nameImage);
            //puttign names to buttons
            correctResponse = random.nextInt(4);
            int wrongResponse;
            //loop through correct responses
            for (int i = 0; i < 4; i++) {
                if (i == correctResponse) {
                    responses[i] = nameList.get(selectedName);
                } else {
                    wrongResponse = random.nextInt(linkList.size());
                    //check if wrong answer matches correct name
                    while (wrongResponse == selectedName) {
                        wrongResponse = random.nextInt(linkList.size());
                    }
                    responses[i] = nameList.get(wrongResponse);
                }
            }
            button0.setText(responses[0]);
            button1.setText(responses[1]);
            button2.setText(responses[2]);
            button3.setText(responses[3]);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //delay function generateNewQuestion() to allow toast to disappear
    public void delay(final int c){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(c);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                generateNewQuestion();           //send
            }
        }, c);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //assign image to imageholder
        pictureHolder = findViewById(R.id.pictureHolder);
        //assign buttons
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        //create download class to retrieve names and links for image
        DownloadClass dClass = new DownloadClass();
        String output = null;
        String link = "http://www.posh24.se/kandisar";
        try{
            output = dClass.execute(link).get();
            //cut off unnecessary part of the website
            String[] cutOffOutput = output.split("<div class=\"listedArticles\">");
            Log.i("contents of URL: ", output);
            //parse images from the website
            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m = p.matcher(cutOffOutput[0]);
            while (m.find()){
                linkList.add(m.group(1));
            }
            //parse names from the website
            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(cutOffOutput[0]);
            while (m.find()){
                nameList.add(m.group(1));
            }
            generateNewQuestion();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
