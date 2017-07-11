package com.simple.demotest;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.simple.demotest.databinding.ActivityMainBinding;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mMainBinding;

    private static final String CONNECT = "conn";

    private static final String REGISTER = "register";

    private static final String MESSAGE = "message";

    private static final String CONTENT = "content";

    private ArrayAdapter<String> mAdapter;

    private ArrayAdapter<String> mMessageAdapter;

    private List<String> mUsers;

    private List<String> mMessages;

    private String mCurrent;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.6.94:3000/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener onRegister = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String content;
                    try {
                        content = data.getString(CONTENT);
                        if (!TextUtils.isEmpty(content) && !content.contains(" is exist")) {
                            ArrayList<String> listdata = new ArrayList<>();
                            JSONArray jArray = (JSONArray) data.get(CONTENT);
                            if (jArray != null) {
                                for (int i=0;i<jArray.length();i++){
                                    listdata.add(jArray.getString(i));
                                }
                            }

                            if (!listdata.isEmpty()) {
                                mUsers.clear();
                                mUsers.addAll(listdata);
                                mAdapter.notifyDataSetChanged();
                            }
                        } else {
                            mCurrent = "";
                            Toast.makeText(getBaseContext(), content, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (mCurrent.equals(data.getString("current"))) {
                            mMainBinding.buttonRegister.setVisibility(View.GONE);
                            mMainBinding.buttonSend.setVisibility(View.VISIBLE);
                            mMainBinding.editTextName.setText("");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String content;
                    try {
                        content = data.getString(CONTENT);
                        if (!TextUtils.isEmpty(content)) {
                            Toast.makeText(getBaseContext(), content, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String noidung;
                    try {
                        noidung = data.getString(CONTENT);
                        if (!TextUtils.isEmpty(noidung)) {
                            mMessages.add(noidung);
                            mMessageAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSocket.on(CONNECT, onConnect);
        mSocket.on(REGISTER, onRegister);
        mSocket.on(MESSAGE, onMessage);
        mSocket.connect();
        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mMainBinding.setActivity(this);
        mUsers = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mUsers);
        mMainBinding.listViewUser.setAdapter(mAdapter);
        mMessages = new ArrayList<>();
        mMessageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mMessages);
        mMainBinding.listViewChat.setAdapter(mMessageAdapter);
    }

    public void sendUsername(String username) {
        if (username == null || TextUtils.isEmpty(username)) {
            return;
        }
        mCurrent = username;
        mSocket.emit(REGISTER, username);
    }

    public void sendMessage(String message) {
        if (message == null || TextUtils.isEmpty(message)) {
            return;
        }
        mSocket.emit(MESSAGE, mCurrent +  ": " + message);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSocket.emit("disconn", mCurrent);
        mSocket.disconnect();
    }
}
