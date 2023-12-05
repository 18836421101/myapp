package com.example.shiyan_bottom.chatGpt;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shiyan_bottom.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class chat_main extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView welcomeText;
    EditText messageEditText;
    ImageButton sendButton;
    List<Message> messageList;
    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();
    MessageAdapter messageAdapter;

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_main);
        messageList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        welcomeText = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);

        //setup recycler view
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);


        sendButton.setOnClickListener((v)->{
            String question = messageEditText.getText().toString().trim();
            addToChat(question,Message.SENT_BY_ME);
            messageEditText.setText("");
            callAPI(question);
            welcomeText.setVisibility(View.GONE);
        });
    }

    void addToChat(String message,String sentBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message,sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });

    }

    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response,Message.SENT_BY_BOT);
    }

    private String utterance(String accessToken,String sc) {
        // 请求URL
        String talkUrl = "https://aip.baidubce.com/rpc/2.0/unit/service/v3/chat";
        try {
            // 请求参数
            String params = "{\"version\":\"3.0\",\"service_id\":\"S95848\",\"session_id\":\"\",\"log_id\":\"7758521\",\"request\":{\"terminal_id\":\"88888\",\"query\":\""+sc+"\"}}";
            String result = HttpUtil.post(talkUrl, accessToken, "application/json", params);
            int a = result.indexOf("ED_HIST");
            int b = result.indexOf("SYS_VARS");
            String c = result.substring(a+14+sc.length(),b-4);
            System.out.println(c);
            addResponse(c);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    void callAPI(String question){
        //okhttp
        messageList.add(new Message("对方正在输入中",Message.SENT_BY_BOT));
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token?client_id=dGUrpkVVIhAaklBjCXCc5VDe&client_secret=z4sCxGykcoP6D7GRVKdb1RvL95wbUkHh&grant_type=client_credentials")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to "+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    try {
                        response = HTTP_CLIENT.newCall(request).execute();
                        String t = response.body().string();
                        //System.out.println(t);
                        int a = t.indexOf("access_token");
                        //System.out.println(a);
                        int b = t.indexOf("scope");
                        //System.out.println(b);
                        String c = t.substring(a+15,b-3);
                        //System.out.println(c);
                        utterance(c,question);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    addResponse("Failed to load response due to "+response.body().toString());
                }
            }
        });
    }
}