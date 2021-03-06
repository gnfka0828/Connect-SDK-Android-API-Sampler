//
//  Connect SDK Sample App by LG Electronics
//
//  To the extent possible under law, the person who associated CC0 with
//  this sample app has waived all copyright and related or neighboring rights
//  to the sample app.
//
//  You should have received a copy of the CC0 legalcode along with this
//  work. If not, see http://creativecommons.org/publicdomain/zero/1.0/.
//

package com.connectsdk.sampler.fragments;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.connectsdk.sampler.R;
import com.connectsdk.sampler.util.TestResponseObject;
import com.connectsdk.service.capability.WebAppLauncher;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.command.ServiceSubscription;
import com.connectsdk.service.sessions.LaunchSession;
import com.connectsdk.service.sessions.WebAppSession;
import com.connectsdk.service.sessions.WebAppSession.LaunchListener;
import com.connectsdk.service.sessions.WebAppSession.WebAppPinStatusListener;
import com.connectsdk.service.sessions.WebAppSessionListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class WebAppFragment extends BaseFragment {

    public final static String TAG = "Connect SDK";
    public Button joinWebAppButton;
    public Button sendMessageButton;
    public Button sendJSONButton;
    //public Button sendWebImageButton;
    public Button sendFSImageButton;
    public Button requestGalleryThemeButton;
    public Button stopPlayingThemeButton;
    public Button startPlayingThemeButton;
    public Button startPlayingCustomThemeButton;
    public TestResponseObject testResponse;

    private final static String WEBOSID = "webOS TV";
    private final static String CASTID = "Chromecast";
    private final static String MULTISCREENID = "MultiScreen";

    static boolean isLaunched = false;


    WebAppSession mWebAppSession;
    String webAppId = null;

    public WebAppFragment() {};

    public WebAppFragment(Context context)
    {
        super(context);
        testResponse = new TestResponseObject();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        View rootView = inflater.inflate(
                R.layout.fragment_webapp, container, false);

        joinWebAppButton = (Button) rootView.findViewById(R.id.joinWebAppButton);
        sendMessageButton = (Button) rootView.findViewById(R.id.sendMessageButton);
        sendJSONButton = (Button) rootView.findViewById(R.id.sendJSONButton);

        //sendWebImageButton = (Button) rootView.findViewById(R.id.sendWebImageButton);
        sendFSImageButton = (Button) rootView.findViewById(R.id.sendFSImageButton);

        requestGalleryThemeButton = (Button) rootView.findViewById(R.id.requestGalleryTheme);
        stopPlayingThemeButton = (Button) rootView.findViewById(R.id.stopPlayingThemeButton);
        startPlayingThemeButton = (Button) rootView.findViewById(R.id.startPlayingThemeButton);
        startPlayingCustomThemeButton = (Button) rootView.findViewById(R.id.startPlayingCustomThemeButton);

        buttons = new Button[]{
                joinWebAppButton,
                sendMessageButton,
                sendJSONButton,
                //sendWebImageButton,
                sendFSImageButton,
                requestGalleryThemeButton,
                stopPlayingThemeButton,
                startPlayingThemeButton,
                startPlayingCustomThemeButton
        };

        return rootView;
    }

    @Override
    public void enableButtons() {
        super.enableButtons();

        joinWebAppButton.setEnabled(getTv().hasCapability(WebAppLauncher.Launch));
        joinWebAppButton.setOnClickListener(joinWebApp);

        if (getTv().hasCapability(WebAppLauncher.Message_Send)) {
            sendMessageButton.setOnClickListener(sendMessage);
            sendJSONButton.setOnClickListener(sendJson);
            //sendWebImageButton.setOnClickListener(sendWebImage);
            sendFSImageButton.setOnClickListener(sendFSImage);
            requestGalleryThemeButton.setOnClickListener(getGalleryTheme);
            stopPlayingThemeButton.setOnClickListener(stopPlayingTheme);
            startPlayingThemeButton.setOnClickListener(startPlayingTheme);
            startPlayingCustomThemeButton.setOnClickListener(startPlayingCustomTheme);
        }

        if (!isLaunched) {
            disableButton(sendMessageButton);
            disableButton(sendJSONButton);
            //disableButton(sendWebImageButton);
            disableButton(sendFSImageButton);
            disableButton(requestGalleryThemeButton);
            disableButton(stopPlayingThemeButton);
            disableButton(startPlayingThemeButton);
            disableButton(startPlayingCustomThemeButton);
        }

        if (getTv().getServiceByName(WEBOSID) != null)
            webAppId = "com.webos.app.igallery";
        else if (getTv().getServiceByName(CASTID) != null)
            webAppId = "DDCEDE96";
        else if (getTv().getServiceByName(MULTISCREENID) != null)
            webAppId = "ConnectSDKSampler";
    }

    public View.OnClickListener joinWebApp = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (webAppId == null)
                return;

            getWebAppLauncher().joinWebApp(webAppId, new LaunchListener() {

                @Override
                public void onError(ServiceCommandError error) {
                    Log.d("LG", "Could not join");
                }

                @Override
                public void onSuccess(WebAppSession webAppSession) {
                    testResponse =  new TestResponseObject(true, TestResponseObject.SuccessCode, TestResponseObject.Joined_WebAPP);
                    if (getTv() == null)
                        return;

                    webAppSession.setWebAppSessionListener(webAppListener);
                    mWebAppSession = webAppSession;

                    sendMessageButton.setEnabled(true);
                    //sendWebImageButton.setEnabled(true);
                    sendFSImageButton.setEnabled(true);
                    requestGalleryThemeButton.setEnabled(true);
                    stopPlayingThemeButton.setEnabled(true);
                    startPlayingThemeButton.setEnabled(true);
                    startPlayingCustomThemeButton.setEnabled(true);

                    if (getTv().hasCapabilities(WebAppLauncher.Message_Send_JSON)) sendJSONButton.setEnabled(true);
                    isLaunched = true;
                }
            });
        }
    };

    public View.OnClickListener sendWebImage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            uThread.start(); // ?????? Thread ??????

            try{
                //?????? Thread??? ????????? ????????? ????????? ????????? ????????????!
                //join() ???????????? ????????? ?????? Thread??? ????????? ????????? ?????? Thread??? ?????????
                //join() ???????????? InterruptedException??? ???????????????.
                uThread.join();

                //?????? Thread?????? ???????????? ???????????? ????????? ????????? ???
                //UI ????????? ??? ??? ?????? ?????? Thread?????? ?????????.
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    };

    public View.OnClickListener sendFSImage = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            getImageInPhone();
        }
    };

    public View.OnClickListener getGalleryTheme = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            JSONObject message = null;
            try {
                message = new JSONObject() {{
                    put("type", "request_gallerytheme");
                }};

            } catch (JSONException e) {
                return;
            }

            sendCurWebSession(message);
        }
    };

    public View.OnClickListener stopPlayingTheme = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            JSONObject message = null;
            try {
                message = new JSONObject() {{
                    put("type", "stopplaying_gallerytheme");
                }};
            } catch (JSONException e) {
                return;
            }

            sendCurWebSession(message);
        }
    };

    public View.OnClickListener startPlayingTheme = new View.OnClickListener() {
        final String themename = "Masterpieces";

        @Override
        public void onClick(View v) {
            JSONObject message = null;
            try {
                message = new JSONObject() {{
                    put("type", "startplaying_gallerytheme");
                    put("themename", themename);
                }};
            } catch (JSONException e) {
                return;
            }

            sendCurWebSession(message);
        }
    };

    public View.OnClickListener startPlayingCustomTheme = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            JSONObject message = null;
            try {
                message = new JSONObject() {{
                    put("type", "startplaying_customtheme");
                }};
            } catch (JSONException e) {
                return;
            }

            sendCurWebSession(message);
        }
    };

    public WebAppSessionListener webAppListener = new WebAppSessionListener() {

        @Override
        public void onReceiveMessage(WebAppSession webAppSession, Object message) {
            Log.d(TAG, "Message received from app | " + message);

//            if (message.getClass() == String.class)
//            {
//                responseMessageTextView.append((String) message);
//                responseMessageTextView.append("\n");
//            } else if (message.getClass() == JSONObject.class)
//            {
//                responseMessageTextView.append(((JSONObject) message).toString());
//                responseMessageTextView.append("\n");
//            }
        }

        @Override
        public void onWebAppSessionDisconnect(WebAppSession webAppSession) {
            Log.d("LG", "Device was disconnected");

            if (webAppSession != mWebAppSession) {
                webAppSession.setWebAppSessionListener(null);
                return;
            }

            if (getTv() != null) joinWebAppButton.setEnabled(getTv().hasCapability(WebAppLauncher.Join));
            sendMessageButton.setEnabled(false);
            sendJSONButton.setEnabled(false);
            //sendWebImageButton.setEnabled(false);
            sendFSImageButton.setEnabled(false);
            requestGalleryThemeButton.setEnabled(false);
            stopPlayingThemeButton.setEnabled(false);
            startPlayingThemeButton.setEnabled(false);
            startPlayingCustomThemeButton.setEnabled(false);

            mWebAppSession.setWebAppSessionListener(null);
            mWebAppSession = null;
            isLaunched = false;
        }
    };


    public static String BitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); //????????? ????????? ???????????? ?????? ??????????????? ByteArrayOutputStream????????? ??????
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos);//bitmap??? ?????? (?????? 70??? 70%??? ??????????????? ???)
        byte[] bytes = baos.toByteArray();//?????? bitmap??? byte????????? ????????????.
        String temp = Base64.encodeToString(bytes, Base64.DEFAULT);//Base 64 ????????????byte ????????? String?????? ??????
        return temp;//String??? retrurn
    }

    Thread uThread = new Thread() {
        @Override
        public void run(){
            try{
                //????????? ????????? ????????? URL
                URL url = new URL("http://connectsdk.com/ConnectSDK_Logo.jpg");

                //Web?????? ????????? ????????? ??? ImageView??? ????????? Bitmap ?????????
                /* URLConnection ???????????? protected??? ???????????? ????????????
                     ???????????? ?????? HttpURLConnection ?????? ?????? ?????? */
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                    /* openConnection()???????????? ???????????? urlConnection ?????????
                    HttpURLConnection??? ??????????????? ??? ??? ???????????? ??????????????? ????????????*/
                conn.setDoInput(true); //Server ???????????? ?????? ????????? ????????? ??????
                conn.connect(); //????????? ?????? ????????? ??? (connect() ???????????? ?????? ?????? ?????????)

                InputStream is = conn.getInputStream(); //inputStream ??? ????????????

                sendCurWebSession(BitmapToString(BitmapFactory.decodeStream(is)));
            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    };

    public void getImageInPhone() {    // ????????? ?????? ????????? ????????? ????????? ?????? ????????? ??????
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 101);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // ??????????????? ????????? ?????? ?????? ??????
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {   // ?????? ???????????? ???????????? ?????? ??????
                    Toast.makeText(mContext.getApplicationContext(), "???????????? ???????????? ???????????????.", Toast.LENGTH_LONG).show();
                } else {  // ???????????? ???????????? ????????? ??????
                    ContentResolver resolver = mContext.getApplicationContext().getContentResolver();
                    InputStream is = null;

                    if ( data.getClipData() == null ) { // ????????? 1??? ??????
                        try {
                            is = resolver.openInputStream(data.getData());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        sendCurWebSession("data:image/png;base64," + BitmapToString(BitmapFactory.decodeStream(is)));
                    } else { // ????????? ?????? ??? ?????? ???
                        ClipData clip = data.getClipData();

                        for (int i = 0; i < clip.getItemCount(); i++) {
                            try {
                                is = resolver.openInputStream(clip.getItemAt(i).getUri());
                                sendCurWebSession("data:image/png;base64," +
                                        BitmapToString(BitmapFactory.decodeStream(is)));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (resultCode == Activity.DEFAULT_KEYS_DISABLE) {
                Toast.makeText(mContext.getApplicationContext(), "???????????? ???????????? ???????????????.", Toast.LENGTH_LONG).show();
            }
        }

//        if (requestCode == 101) {
//            if (resultCode == Activity.RESULT_OK) {
//                Uri fileUri = data.getData();
//                ContentResolver resolver = mContext.getApplicationContext().getContentResolver();
//                try {
//                    InputStream is = resolver.openInputStream(fileUri);
//
//                    sendMessageCurWebSession(BitmapToString(BitmapFactory.decodeStream(is)));
//                    is.close();   // ????????? ????????????
//                    Toast.makeText(mContext.getApplicationContext(), "?????? ???????????? ??????", Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    Toast.makeText(mContext.getApplicationContext(), "?????? ???????????? ??????", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
    }

    public View.OnClickListener sendMessage = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            String message = "This is an Android test message.";

            sendCurWebSession(message);
        }
    };

    public void sendCurWebSession(String message) {
        mWebAppSession.sendMessage(message, new ResponseListener<Object>() {
            @Override
            public void onSuccess(Object response) {
                testResponse =  new TestResponseObject(true, TestResponseObject.SuccessCode, TestResponseObject.Sent_Message);
                Log.d(TAG, "Sent : " + response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Log.e(TAG, "Error sending : " + error);
            }
        });
    }

    public void sendCurWebSession(JSONObject message) {
        mWebAppSession.sendMessage(message, new ResponseListener<Object>() {
            @Override
            public void onSuccess(Object response) {
                testResponse =  new TestResponseObject(true, TestResponseObject.SuccessCode, TestResponseObject.Sent_JSON);
                Log.d(TAG, "Sent : " + response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Log.e(TAG, "Error sending : " + error);
            }
        });
    }

    public View.OnClickListener sendJson = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            JSONObject message = null;
            try {
                message = new JSONObject() {{
                    put("type", "message");
                    put("contents", "This is a test message");
                    put("params", new JSONObject() {{
                        put("someParam1", "The content & format of this JSON block can be anything");
                        put("someParam2", "The only limit ... is yourself");
                    }});
                    put("anArray", new JSONArray() {{
                        put("Just");
                        put("to");
                        put("show");
                        put("we");
                        put("can");
                        put("send");
                        put("arrays!");
                    }});
                }};
            } catch (JSONException e) {
                return;
            }

            sendCurWebSession(message);
        }
    };

    @Override
    public void disableButtons() {
        super.disableButtons();
        isLaunched = false;

        webAppId = null;
    }
}