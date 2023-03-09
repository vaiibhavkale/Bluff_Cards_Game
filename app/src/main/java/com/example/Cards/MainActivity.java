package com.example.Cards;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Cards.Utils.CardDetail;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    RecyclerView cardRecycler;
    ArrayList<CardDetail> cards, ourCards, drawPile, playedCards;
    CardsRecyclerAdapter adapter;
    String ip;
    TextView result, drawNum, playedNum, playedCard, recyclerNum;
    Button startButton, fakePrev, fakeNext, innocent, bluff;
    ImageView draw, aboutToPlay;
    RecyclerView.LayoutManager lm;
    RelativeLayout parent;
    int clickedPosition;
    boolean newSelection = false, callBluff = false, turn = false, first = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            ip = getIntent().getExtras().get("ip").toString();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        Toast.makeText(this, ip, Toast.LENGTH_SHORT).show();

        cardRecycler = findViewById(R.id.cardRecycler);
        playedCard = findViewById(R.id.playedCard);
        aboutToPlay = findViewById(R.id.aboutToPlay);
        startButton = findViewById(R.id.startButton);
        draw = findViewById(R.id.draw);
        result = findViewById(R.id.result);
        drawNum = findViewById(R.id.drawNum);
        playedNum = findViewById(R.id.playedNum);
        fakePrev = findViewById(R.id.fakePrev);
        fakeNext = findViewById(R.id.fakeNext);
        innocent = findViewById(R.id.innocent);
        bluff = findViewById(R.id.bluff);
        parent = findViewById(R.id.parent);
        recyclerNum = findViewById(R.id.recyclerNum);

        cards = new ArrayList<>();
        ourCards = new ArrayList<>();
        drawPile = new ArrayList<>();
        playedCards = new ArrayList<>();

        adapter = new CardsRecyclerAdapter(ourCards, this);
        lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        cardRecycler.setLayoutManager(lm);
        cardRecycler.setAdapter(adapter);

        adapter.setOnItemClickListener(new CardsRecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                if (turn) {
                    newSelection = true;
                    Log.d("Turn", "OnItemClick: My Turn");
                    Log.d("Return", "OnItemClick: "+position);
                    aboutToPlay.setVisibility(View.VISIBLE);
                    clickedPosition = position;
                    CardDetail cd = ourCards.get(clickedPosition);
                    Log.d("Turn", "OnItemClick: imageResource "+cd.getCardName());
                    aboutToPlay.setImageResource(cd.getCardImage());
                    if (first) {
                        Log.d("Turn", "OnItemClick: Inside");
                        playedCards.add(cd);
                        Log.d("inside", "OnItemClick: "+cd.getCardName());
                        if (cd.getCardName().equals("c10") || cd.getCardName().equals("d10")
                        || cd.getCardName().equals("h10") || cd.getCardName().equals("s10")) {
                            playedCard.setText("10");
                        }
                        playedCard.setText(cd.getCardName().toUpperCase().charAt(1)+"");
                        int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                        playedNum.setText(temp + "");
                        ourCards.remove(clickedPosition);
                        dataSetChanged();
                        BackConnect b = new BackConnect();
                        char playedChar[] = cd.getCardName().toUpperCase().toCharArray();
                        b.execute(ip, cd.getCardName() + playedChar[1]);
                        aboutToPlay.setImageDrawable(null);
                        turn = false;
                        first = false;
                        parent.setBackgroundColor(0);

                    }
                }
            }
        });

        Thread myThread = new Thread(new MyServer());
        Thread listThread = new Thread(new ReceiveList());
        listThread.start();
        myThread.start();

    }

    public void drawCard(View v){
        if (turn && !first && !callBluff){
            CardDetail cd = drawPile.get(0);
            drawPile.remove(0);
            ourCards.add(cd);
            dataSetChanged();
            fakePrev.setVisibility(View.INVISIBLE);
            fakeNext.setVisibility(View.INVISIBLE);
            BackConnect b = new BackConnect();
            drawNum.setText(drawPile.size()+"");
            b.execute(ip, "dr");
            turn = false;
        }
    }

    public void dataSetChanged(){
        adapter.notifyDataSetChanged();
        recyclerNum.setText(ourCards.size()+"");
    }

    public void drawCard(){
        drawPile.remove(0);
        drawNum.setText(drawPile.size()+"");
    }

    public void playCard(View v){
        Log.d("newSelection", "playCard: "+newSelection);

        if (turn && newSelection){
            CardDetail cd = ourCards.get(clickedPosition);
            playedCards.add(cd);
            //int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
            int temp = playedCards.size();
            playedNum.setText(temp+"");
            ourCards.remove(clickedPosition);
            dataSetChanged();

            switch(v.getId()){
                case R.id.fakePrev:
                    playedCard.setText(fakePrev.getText().toString());
                    BackConnect b = new BackConnect();
                    b.execute(ip, cd.getCardName() + fakePrev.getText().toString());
                    break;
                case R.id.fakeNext:
                    playedCard.setText(fakeNext.getText().toString());
                    BackConnect b1 = new BackConnect();
                    b1.execute(ip, cd.getCardName() + fakeNext.getText().toString());
                    break;
            }

            fakeNext.setVisibility(View.INVISIBLE);
            fakePrev.setVisibility(View.INVISIBLE);
            aboutToPlay.setImageDrawable(null);
            aboutToPlay.setVisibility(View.INVISIBLE);

            if (ourCards.size() == 0){
                won();
            }
            turn = false;
            first = false;
            newSelection = false;
            parent.setBackgroundColor(0);
        }
    }

    public void won() {
        result.setVisibility(View.VISIBLE);
        result.setText("YOU WON!");
        BackConnect b1 = new BackConnect();
        b1.execute(ip, "wo");
    }

    public void wonR() {
        result.setVisibility(View.VISIBLE);
        result.setText("YOU WON!");
    }

    public void lose() {
        result.setVisibility(View.VISIBLE);
        result.setText("YOU LOSE!");
        BackConnect b1 = new BackConnect();
        b1.execute(ip, "lo");
    }

    public void loseR() {
        result.setVisibility(View.VISIBLE);
        result.setText("YOU LOSE!");
    }

    public void start(View v){
        cardRecycler.setVisibility(View.VISIBLE);
        aboutToPlay.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.INVISIBLE);
        draw.setVisibility(View.VISIBLE);
        drawNum.setVisibility(View.VISIBLE);
        playedCard.setVisibility(View.VISIBLE);
        playedNum.setVisibility(View.VISIBLE);
        recyclerNum.setVisibility(View.VISIBLE);
        recyclerNum.setText(ourCards.size()+"");

        turn = true;
        BackConnect b = new BackConnect();
        populateList();
        SendList l = new SendList();
        l.execute(cards);
        b.execute(ip, "st");

    }

    public void updatePlayedCardText(){
        playedCard.setText("");
        playedNum.setText(playedCards.size()+"");
    }

    public void start(){
        cardRecycler.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.INVISIBLE);
        draw.setVisibility(View.VISIBLE);
        drawNum.setVisibility(View.VISIBLE);
        playedNum.setVisibility(View.VISIBLE);
        playedCard.setVisibility(View.VISIBLE);
        playedNum.setVisibility(View.VISIBLE);
        recyclerNum.setText(ourCards.size()+"");
    }

    public void catchBluff(View v){
        innocent.setVisibility(View.INVISIBLE);
        bluff.setVisibility(View.INVISIBLE);
        Log.d("inside", "catchBluff: "+v.getId());
        switch (v.getId()){

            case R.id.bluff:
                if (Character.toUpperCase(playedCards.get(playedCards.size()-1).getCardName().charAt(1))
                        == playedCard.getText().toString().charAt(0)){
                    Toast.makeText(this, "The opponent was innocent", Toast.LENGTH_SHORT).show();

                    Log.d("inside", "catchBluff: playedCards size "+playedCards.size());
                    int n = playedCards.size();
                    for(int i=0; i<n; i++){
                        ourCards.add(playedCards.get(0));
                        Log.d("updation", "catchBluff: size "+playedCards.size());
                        playedCards.remove(0);
                    }
                    Log.d("inside", "catchBluff: playedCards size "+playedCards.size());
                    dataSetChanged();
                    updatePlayedCardText();
                    BackConnect b1 = new BackConnect();
                    b1.execute(ip, "ic");
                    if (ourCards.size() > 30){
                        lose();
                    }
                    first = true;
                }else{
                    Toast.makeText(this, "The opponent bluffed", Toast.LENGTH_SHORT).show();
                    Log.d("inside", "catchBluff: both cards playedCards top"
                            +playedCards.get(playedCards.size()-1).getCardName().charAt(1)
                            +" TextView "+playedCard.getText().toString().charAt(0));
                    int n = playedCards.size();
                    for(int i=0; i<n; i++){
                        playedCards.remove(0);
                    }
                    updatePlayedCardText();
                    BackConnect b1 = new BackConnect();
                    b1.execute(ip, "bc");
                    turn = false;
                }
                break;
            case R.id.innocent:
                setPlayButtons();
                break;
        }
    }

    public void bluffCaught(){
        Toast.makeText(this, "Bluff caugth", Toast.LENGTH_SHORT).show();
        int n = playedCards.size();
        for(int i=0; i<n; i++){
            ourCards.add(playedCards.get(0));
            playedCards.remove(0);
            Log.d("updation", "Bluff caugth: size "+playedCards.size());
        }
        dataSetChanged();
        if (ourCards.size() > 30){
            lose();
        }
        updatePlayedCardText();
        first = true;
        turn = true;
    }

    public void setCatchBluff(){
        callBluff = true;
        parent.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        innocent.setVisibility(View.VISIBLE);
        bluff.setVisibility(View.VISIBLE);
    }

    public void setPlayButtons(){
        callBluff = false;
        String playedString = playedCard.getText().toString();
        if (playedString.equals("0"))
            playedString = "10";
        int played;
        fakePrev.setVisibility(View.VISIBLE);
        fakeNext.setVisibility(View.VISIBLE);
        aboutToPlay.setVisibility(View.VISIBLE);

        if (playedString.equals("K")){
            fakePrev.setText("Q");
            fakeNext.setText("A");
        }
        else if (playedString.equals("Q")){
            fakePrev.setText("J");
            fakeNext.setText("K");
        }
        else if (playedString.equals("J")){
            fakePrev.setText("10");
            fakeNext.setText("Q");
        }
        else if (playedString.equals("10")){
            fakePrev.setText("9");
            fakeNext.setText("J");
        }
        else if (playedString.equals("2")){
            fakePrev.setText("A");
            fakeNext.setText("3");
        }
        else if (playedString.equals("A")){
            fakePrev.setText("K");
            fakeNext.setText("2");
        }
        else{
            played = Integer.parseInt(playedString);
            fakePrev.setText(played-1 + "");
            fakeNext.setText(played+1 + "");
        }
    }

    public void innocentCaught(){
        Toast.makeText(this, "Innocent Caught", Toast.LENGTH_SHORT).show();
        int n = playedCards.size();
        for(int i=0; i<n; i++){
            playedCards.remove(0);
            Log.d("updation", "innocentCaught: size "+playedCards.size());
        }
        updatePlayedCardText();
    }

    class MyServer implements Runnable{

        ServerSocket ss;
        Socket mySocket;
        DataInputStream dis;
        String message, originalCard;
        Handler handler = new Handler();
        char playedChar[];

        @Override
        public void run() {
            try {
                Log.d("check", "run: normal server");
                ss = new ServerSocket(9700);
                while (true){
                    mySocket = ss.accept();
                    dis = new DataInputStream(mySocket.getInputStream());
                    message = dis.readUTF();
                    turn = true;
                    first = false;
                    parent.setBackgroundColor(0);
                    playedChar = message.toUpperCase().toCharArray();
                    originalCard = playedChar[0]+""+playedChar[1];
                    Log.d("originalCard", "run: "+originalCard);
                    originalCard = originalCard.toLowerCase();
                    switch (originalCard){
                        case "ic":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    innocentCaught();
                                }
                            });
                            break;
                        case "bc":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    bluffCaught();
                                }
                            });
                            break;
                        case "wo":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    loseR();
                                    turn = false;
                                }
                            });
                            break;
                        case "lo":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    wonR();
                                }
                            });
                        case "dr":
                            drawCard();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    setPlayButtons();
                                }
                            });
                            break;
                        case "st":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("post", "run: populating");
                                    turn = false;
                                    start();
                                }
                            });
                            break;
                        case "ca":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.ca));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "c2":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.c2));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "c3":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.c3));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "c4":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.c4));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "c5":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.c5));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "c6":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.c6));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "c7":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.c7));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "c8":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.c8));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "c9":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.c9));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "c1":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText("10");
                                    playedCards.add(new CardDetail(message, R.drawable.c10));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "cj":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.cj));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "cq":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.cq));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "ck":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.ck));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "da":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.da));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "d2":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.d2));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "d3":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.d3));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "d4":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.d4));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "d5":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.d5));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "d6":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.d6));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "d7":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.d7));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "d8":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.d8));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "d9":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.d9));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "d1":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText("10");
                                    playedCards.add(new CardDetail(message, R.drawable.d10));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "dj":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.dj));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "dq":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.dq));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "dk":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.dk));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "ha":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.ha));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "h2":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.h2));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "h3":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.h3));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "h4":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.h4));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "h5":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.h5));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "h6":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.h6));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "h7":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.h7));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "h8":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.h8));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "h9":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.h9));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "h1":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText("10");
                                    playedCards.add(new CardDetail(message, R.drawable.h10));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "hj":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.hj));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "hq":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.hq));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "hk":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.hk));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "sa":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.sa));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "s2":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.s2));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "s3":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.s3));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "s4":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.s4));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "s5":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.s5));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "s6":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.s6));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "s7":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.s7));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "s8":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.s8));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "s9":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.s9));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "s1":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText("10");
                                    playedCards.add(new CardDetail(message, R.drawable.s10));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "sj":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.sj));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "sq":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.sq));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                        case "sk":
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playedCard.setText(playedChar[2]+"");
                                    playedCards.add(new CardDetail(message, R.drawable.sk));
                                    int temp = Integer.parseInt(playedNum.getText().toString()) + 1;
                                    playedNum.setText(temp+"");
                                    setCatchBluff();
                                    //setPlayButtons();
                                }
                            });
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("start", "run: server exception");
            }
        }
    }

    class ReceiveList implements Runnable{

        ServerSocket ss;
        Socket mySocket;
        ObjectInputStream ois;
        Handler handler = new Handler();

        @Override
        public void run() {
            try{
                ss = new ServerSocket(9800);
                Log.d("check", "run: ServerSocket");
                while (true){
                    mySocket = ss.accept();
                    ois = new ObjectInputStream(mySocket.getInputStream());
                    try{
                        cards = (ArrayList<CardDetail>) ois.readObject();
                        Log.d("recieve", "run: got list");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                populateList(1);
                            }
                        });
                    }catch (ClassNotFoundException e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Exception", Toast.LENGTH_SHORT).show();
                    }

                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class BackConnect extends AsyncTask<String, Void, String>{

        Socket s;
        DataOutputStream dos;
        String ip, card;

        @Override
        protected String doInBackground(String... strings) {

            ip = strings[0];
            card = strings[1];

            try{
                Log.d("start", "doInBackground: msg sent");
                s = new Socket(ip, 9700);
                dos = new DataOutputStream(s.getOutputStream());
                dos.writeUTF(card);
                dos.close();
                s.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }
    }

    class SendList extends AsyncTask<ArrayList<CardDetail>, Void, String>{

        ArrayList<CardDetail> send;
        Socket s;
        ObjectOutputStream oos;

        @Override
        protected String doInBackground(ArrayList<CardDetail>... arrayLists) {
            send = arrayLists[0];
            Log.d("send", "doInBackground: inside");
            try{
                s = new Socket(ip, 9800);
                oos = new ObjectOutputStream(s.getOutputStream());
                oos.writeObject(send);
                Log.d("send", "doInBackground: sending");
                oos.close();
                s.close();
            }catch (IOException e){
                e.printStackTrace();
                Log.d("send", "doInBackground: exception"+e);
            }
            return null;
        }
    }

    public void populateList(int a){
        for(int i=15; i<30; i++){
            ourCards.add(cards.get(i));
            Log.i("Opp cards", "populateList: "+i+" "+cards.get(i).getCardName());
        }
        for(int i=30; i<52; i++){
            drawPile.add(cards.get(i));
        }
        drawNum.setText(drawPile.size()+"");
        dataSetChanged();
    }

    public void populateList(){

        cards.add(new CardDetail("ca", R.drawable.ca));
        cards.add(new CardDetail("c2", R.drawable.c2));
        cards.add(new CardDetail("c3", R.drawable.c3));
        cards.add(new CardDetail("c4", R.drawable.c4));
        cards.add(new CardDetail("c5", R.drawable.c5));
        cards.add(new CardDetail("c6", R.drawable.c6));
        cards.add(new CardDetail("c7", R.drawable.c7));
        cards.add(new CardDetail("c8", R.drawable.c8));
        cards.add(new CardDetail("c9", R.drawable.c9));
        cards.add(new CardDetail("c10", R.drawable.c10));
        cards.add(new CardDetail("cj", R.drawable.cj));
        cards.add(new CardDetail("cq", R.drawable.cq));
        cards.add(new CardDetail("ck", R.drawable.ck));

        cards.add(new CardDetail("da", R.drawable.da));
        cards.add(new CardDetail("d2", R.drawable.d2));
        cards.add(new CardDetail("d3", R.drawable.d3));
        cards.add(new CardDetail("d4", R.drawable.d4));
        cards.add(new CardDetail("d5", R.drawable.d5));
        cards.add(new CardDetail("d6", R.drawable.d6));
        cards.add(new CardDetail("d7", R.drawable.d7));
        cards.add(new CardDetail("d8", R.drawable.d8));
        cards.add(new CardDetail("d9", R.drawable.d9));
        cards.add(new CardDetail("d10", R.drawable.d10));
        cards.add(new CardDetail("dj", R.drawable.dj));
        cards.add(new CardDetail("dq", R.drawable.dq));
        cards.add(new CardDetail("dk", R.drawable.dk));

        cards.add(new CardDetail("ha", R.drawable.ha));
        cards.add(new CardDetail("h2", R.drawable.h2));
        cards.add(new CardDetail("h3", R.drawable.h3));
        cards.add(new CardDetail("h4", R.drawable.h4));
        cards.add(new CardDetail("h5", R.drawable.h5));
        cards.add(new CardDetail("h6", R.drawable.h6));
        cards.add(new CardDetail("h7", R.drawable.h7));
        cards.add(new CardDetail("h8", R.drawable.h8));
        cards.add(new CardDetail("h9", R.drawable.h9));
        cards.add(new CardDetail("h10", R.drawable.h10));
        cards.add(new CardDetail("hj", R.drawable.hj));
        cards.add(new CardDetail("hq", R.drawable.hq));
        cards.add(new CardDetail("hk", R.drawable.hk));

        cards.add(new CardDetail("sa", R.drawable.sa));
        cards.add(new CardDetail("s2", R.drawable.s2));
        cards.add(new CardDetail("s3", R.drawable.s3));
        cards.add(new CardDetail("s4", R.drawable.s4));
        cards.add(new CardDetail("s5", R.drawable.s5));
        cards.add(new CardDetail("s6", R.drawable.s6));
        cards.add(new CardDetail("s7", R.drawable.s7));
        cards.add(new CardDetail("s8", R.drawable.s8));
        cards.add(new CardDetail("s9", R.drawable.s9));
        cards.add(new CardDetail("s10", R.drawable.s10));
        cards.add(new CardDetail("sj", R.drawable.sj));
        cards.add(new CardDetail("sq", R.drawable.sq));
        cards.add(new CardDetail("sk", R.drawable.sk));


        Collections.shuffle(cards);
        for(int i=0; i<52; i++){
            Log.i("Shuffled ->", "populateList: "+i+" "+cards.get(i).getCardName());
        }

        for(int i=0; i<15; i++){
            ourCards.add(cards.get(i));
        }

        for(int i=30; i<52; i++){
            drawPile.add(cards.get(i));
        }
        drawNum.setText(drawPile.size()+"");
        dataSetChanged();

    }
}
