package com.example.Cards;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connection extends AppCompatActivity {

    EditText editIp;
    String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        editIp = findViewById(R.id.editIp);

        //Thread myThread = new Thread(new MyServer());
        //myThread.start();
    }

    public void connect(View v){
        //BackgroundTask b = new BackgroundTask();
        ip = editIp.getText().toString();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("ip", ip);
        startActivity(intent);
        finish();
        //b.execute(ip);
    }

    class MyServer implements Runnable{

        ServerSocket ss;
        Socket mySocket;
        DataInputStream dis;
        String message;
        Handler handler = new Handler();

        @Override
        public void run() {
            try {
                ss = new ServerSocket(9700);
                while (true){
                    mySocket = ss.accept();
                    dis = new DataInputStream(mySocket.getInputStream());
                    message = dis.readUTF();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.putExtra("ip", ip);
                                    startActivity(intent);
                                }
                            }, 2000);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class BackgroundTask extends AsyncTask<String, Void, String>{

        Socket s;
        DataOutputStream dos;
        String ip;

        @Override
        protected String doInBackground(String... strings) {

            ip = strings[0];

            try{
                s = new Socket(ip, 9700);
                dos = new DataOutputStream(s.getOutputStream());
                dos.writeUTF(ip);
                dos.close();
                s.close();
            }catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }
    }
}
