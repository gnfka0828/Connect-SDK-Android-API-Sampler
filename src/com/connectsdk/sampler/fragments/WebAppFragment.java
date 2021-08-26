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
    //public Button launchWebAppButton;
    public Button joinWebAppButton;
    //public Button leaveWebAppButton;
    //public Button closeWebAppButton;
    public Button sendMessageButton;
    public Button sendJSONButton;
    //public Button pinWebAppButton;
    //public Button unPinWebAppButton;
    public Button sendWebImageButton;
    public Button sendFSImageButton;
    public Button requestGalleryThemeButton;
    public Button stopPlayingThemeButton;
    public Button startPlayingThemeButton;
    public TestResponseObject testResponse;

    private final static String WEBOSID = "webOS TV";
    private final static String CASTID = "Chromecast";
    private final static String MULTISCREENID = "MultiScreen";

    static boolean isLaunched = false;

    //TextView responseMessageTextView;
    LaunchSession runningAppSession;

    WebAppSession mWebAppSession;
    ServiceSubscription<WebAppPinStatusListener> isWebAppPinnedSubscription;
    String webAppId = null;

    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> arrayList;

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

        //launchWebAppButton = (Button) rootView.findViewById(R.id.launchWebAppButton);
        joinWebAppButton = (Button) rootView.findViewById(R.id.joinWebAppButton);
        //leaveWebAppButton = (Button) rootView.findViewById(R.id.leaveWebAppButton);
        //closeWebAppButton = (Button) rootView.findViewById(R.id.closeWebAppButton);
        sendMessageButton = (Button) rootView.findViewById(R.id.sendMessageButton);
        sendJSONButton = (Button) rootView.findViewById(R.id.sendJSONButton);
        //responseMessageTextView = (TextView) rootView.findViewById(R.id.responseMessageTextView);

        //pinWebAppButton = (Button) rootView.findViewById(R.id.pinWebAppButton);
        //unPinWebAppButton = (Button) rootView.findViewById(R.id.unPinWebAppButton);

        sendWebImageButton = (Button) rootView.findViewById(R.id.sendWebImageButton);
        sendFSImageButton = (Button) rootView.findViewById(R.id.sendFSImageButton);

        requestGalleryThemeButton = (Button) rootView.findViewById(R.id.requestGalleryTheme);
        stopPlayingThemeButton = (Button) rootView.findViewById(R.id.stopPlayingThemeButton);
        startPlayingThemeButton = (Button) rootView.findViewById(R.id.startPlayingThemeButton);

        buttons = new Button[]{
                //launchWebAppButton,
                joinWebAppButton,
                //leaveWebAppButton,
                //closeWebAppButton,
                sendMessageButton,
                sendJSONButton,
                //pinWebAppButton,
                //unPinWebAppButton,
                sendWebImageButton,
                sendFSImageButton,
                requestGalleryThemeButton,
                stopPlayingThemeButton,
                startPlayingThemeButton
        };

        return rootView;
    }

    @Override
    public void enableButtons() {
        super.enableButtons();

//        if (getTv().hasCapability(WebAppLauncher.Launch)) {
//            launchWebAppButton.setOnClickListener(launchWebApp);
//        }
//        else {
//            disableButton(launchWebAppButton);
//        }

        joinWebAppButton.setEnabled(getTv().hasCapability(WebAppLauncher.Launch));
        joinWebAppButton.setOnClickListener(joinWebApp);

        //leaveWebAppButton.setEnabled(getTv().hasCapability(WebAppLauncher.Disconnect));
        //leaveWebAppButton.setOnClickListener(leaveWebApp);

        if (getTv().hasCapability(WebAppLauncher.Close)) {
            //closeWebAppButton.setOnClickListener(closeWebApp);
        }

        if (getTv().hasCapability(WebAppLauncher.Message_Send)) {
            sendMessageButton.setOnClickListener(sendMessage);
            sendJSONButton.setOnClickListener(sendJson);
            sendWebImageButton.setOnClickListener(sendWebImage);
            sendFSImageButton.setOnClickListener(sendFSImage);
            requestGalleryThemeButton.setOnClickListener(getGalleryTheme);
            stopPlayingThemeButton.setOnClickListener(stopPlayingTheme);
            startPlayingThemeButton.setOnClickListener(startPlayingTheme);
        }

//        if (getTv().hasCapability(WebAppLauncher.Pin)) {
//            pinWebAppButton.setOnClickListener(pinWebApp);
//            unPinWebAppButton.setOnClickListener(unPinWebApp);
//        }

        //responseMessageTextView.setText("");

        if (!isLaunched) {
            //disableButton(closeWebAppButton);
            //disableButton(leaveWebAppButton);
            disableButton(sendMessageButton);
            disableButton(sendJSONButton);
            disableButton(sendWebImageButton);
            disableButton(sendFSImageButton);
            disableButton(requestGalleryThemeButton);
            disableButton(stopPlayingThemeButton);
            disableButton(startPlayingThemeButton);
        }
//        else {
//            disableButton(launchWebAppButton);
//        }

        if (getTv().getServiceByName(WEBOSID) != null)
            webAppId = "com.webos.app.igallery";
        else if (getTv().getServiceByName(CASTID) != null)
            webAppId = "DDCEDE96";
        else if (getTv().getServiceByName(MULTISCREENID) != null)
            webAppId = "ConnectSDKSampler";


//        if (getTv().hasCapability(WebAppLauncher.Pin)) {
//            subscribeIfWebAppIsPinned();
//        }
//        else {
//            disableButton(pinWebAppButton);
//            disableButton(unPinWebAppButton);
//        }
    }

    public View.OnClickListener launchWebApp = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (webAppId == null)
                return;

            //launchWebAppButton.setEnabled(false);

            getWebAppLauncher().launchWebApp(webAppId, new LaunchListener() {

                @Override
                public void onError(ServiceCommandError error) {
                    Log.e("LG", "Error connecting to web app | error = " + error);
                    //launchWebAppButton.setEnabled(true);
                }

                @Override
                public void onSuccess(WebAppSession webAppSession) {
                    testResponse =  new TestResponseObject(true, TestResponseObject.SuccessCode, TestResponseObject.Launched_WebAPP);
                    webAppSession.setWebAppSessionListener(webAppListener);
                    isLaunched = true;

                    if (getTv().hasAnyCapability(WebAppLauncher.Message_Send, WebAppLauncher.Message_Receive, WebAppLauncher.Message_Receive_JSON, WebAppLauncher.Message_Send_JSON))
                        webAppSession.connect(connectionListener);
                    else
                        connectionListener.onSuccess(webAppSession.launchSession);

                    mWebAppSession = webAppSession;
                }
            });
        }
    };

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
                    sendWebImageButton.setEnabled(true);
                    sendFSImageButton.setEnabled(true);
                    requestGalleryThemeButton.setEnabled(true);
                    stopPlayingThemeButton.setEnabled(true);
                    startPlayingThemeButton.setEnabled(true);

                    //launchWebAppButton.setEnabled(false);
                    //leaveWebAppButton.setEnabled(getTv().hasCapability(WebAppLauncher.Disconnect));
                    if (getTv().hasCapabilities(WebAppLauncher.Message_Send_JSON)) sendJSONButton.setEnabled(true);
                    //if (getTv().hasCapabilities(WebAppLauncher.Close)) closeWebAppButton.setEnabled(true);
                    isLaunched = true;
                }
            });
        }
    };

    public View.OnClickListener leaveWebApp = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mWebAppSession != null) {
                mWebAppSession.setWebAppSessionListener(null);
                mWebAppSession.disconnectFromWebApp();
                mWebAppSession = null;

                //launchWebAppButton.setEnabled(true);
                joinWebAppButton.setEnabled(getTv().hasCapability(WebAppLauncher.Join));
                sendMessageButton.setEnabled(false);
                sendJSONButton.setEnabled(false);
                //leaveWebAppButton.setEnabled(false);
                //closeWebAppButton.setEnabled(false);

                sendWebImageButton.setEnabled(false);
                sendFSImageButton.setEnabled(false);
                requestGalleryThemeButton.setEnabled(false);
                stopPlayingThemeButton.setEnabled(false);
                startPlayingThemeButton.setEnabled(false);

                isLaunched = false;
            }
        }
    };

    public View.OnClickListener sendWebImage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            uThread.start(); // 작업 Thread 실행

            try{
                //메인 Thread는 별도의 작업을 완료할 때까지 대기한다!
                //join() 호출하여 별도의 작업 Thread가 종료될 때까지 메인 Thread가 기다림
                //join() 메서드는 InterruptedException을 발생시킨다.
                uThread.join();

                //작업 Thread에서 이미지를 불러오는 작업을 완료한 뒤
                //UI 작업을 할 수 있는 메인 Thread에서 작업함.
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

    public View.OnClickListener pinWebApp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getTv() != null) {
                getWebAppLauncher().pinWebApp(webAppId, new ResponseListener<Object>() {

                    @Override
                    public void onError(ServiceCommandError error) {
                        Log.w(TAG, "pin web app failure, " + error.getLocalizedMessage());
                    }

                    @Override
                    public void onSuccess(Object object) {
                        testResponse =  new TestResponseObject(true, TestResponseObject.SuccessCode, TestResponseObject.Pinned_WebAPP);
                        Log.d(TAG, "pin web app success");
                    }
                });
            }
        }
    };

    public View.OnClickListener unPinWebApp = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (webAppId == null)
                return;

            if (getTv() != null) {
                getWebAppLauncher().unPinWebApp(webAppId, new ResponseListener<Object>() {

                    @Override
                    public void onError(ServiceCommandError error) {
                        Log.w(TAG, "unpin web app failture, " + error.getLocalizedMessage());
                    }

                    @Override
                    public void onSuccess(Object object) {
                        testResponse =  new TestResponseObject(true, TestResponseObject.SuccessCode, TestResponseObject.UnPinned_WebAPP);
                        Log.d(TAG, "unpin web app success");
                    }
                });
            }
        }
    };

//    public void checkIfWebAppIsPinned() {
//        if (webAppId == null)
//            return;
//
//        getWebAppLauncher().isWebAppPinned( webAppId, new WebAppPinStatusListener() {
//
//            @Override
//            public void onError(ServiceCommandError error) {
//                Log.w(TAG, "isWebAppPinned failture, " + error.getLocalizedMessage());
//            }
//
//            @Override
//            public void onSuccess(Boolean status) {
//                updatePinButton(status);
//            }
//        });
//    }

//    public void subscribeIfWebAppIsPinned() {
//        if (webAppId == null)
//            return;
//
//        isWebAppPinnedSubscription = getWebAppLauncher().subscribeIsWebAppPinned(webAppId, new WebAppPinStatusListener() {
//
//            @Override
//            public void onError(ServiceCommandError error) {
//                Log.w(TAG, "isWebAppPinned failure, " + error.getLocalizedMessage());
//            }
//
//            @Override
//            public void onSuccess(Boolean status) {
//                updatePinButton(status);
//            }
//        });
//    }

//    public void updatePinButton(boolean status) {
//        if (status) {
//            pinWebAppButton.setEnabled(false);
//            unPinWebAppButton.setEnabled(true);
//        }
//        else {
//            pinWebAppButton.setEnabled(true);
//            unPinWebAppButton.setEnabled(false);
//        }
//    }

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

            //launchWebAppButton.setEnabled(true);
            if (getTv() != null) joinWebAppButton.setEnabled(getTv().hasCapability(WebAppLauncher.Join));
            sendMessageButton.setEnabled(false);
            sendJSONButton.setEnabled(false);
            sendWebImageButton.setEnabled(false);
            sendFSImageButton.setEnabled(false);
            requestGalleryThemeButton.setEnabled(false);
            stopPlayingThemeButton.setEnabled(false);
            startPlayingThemeButton.setEnabled(false);
            //leaveWebAppButton.setEnabled(false);
            //closeWebAppButton.setEnabled(false);

            mWebAppSession.setWebAppSessionListener(null);
            mWebAppSession = null;
            isLaunched = false;
        }
    };

    public ResponseListener<Object> connectionListener = new ResponseListener<Object>() {

        @Override
        public void onSuccess(Object response) {
            if (getTv() == null)
                return;

            if (getTv().hasCapability(WebAppLauncher.Message_Send_JSON))
                sendJSONButton.setEnabled(true);

            if (getTv().hasCapability(WebAppLauncher.Message_Send))
                sendMessageButton.setEnabled(true);

            //leaveWebAppButton.setEnabled(getTv().hasCapability(WebAppLauncher.Disconnect));

            //closeWebAppButton.setEnabled(true);
            //launchWebAppButton.setEnabled(false);
            isLaunched = true;
        }

        @Override
        public void onError(ServiceCommandError error) {
            sendJSONButton.setEnabled(false);
            sendMessageButton.setEnabled(false);
            //closeWebAppButton.setEnabled(false);
            //launchWebAppButton.setEnabled(true);
            isLaunched = false;

            if (mWebAppSession != null) {
                mWebAppSession.setWebAppSessionListener(null);
                mWebAppSession.close(null);
            }
        }
    };

    public static String BitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); //바이트 배열을 차례대로 읽어 들이기위한 ByteArrayOutputStream클래스 선언
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos);//bitmap을 압축 (숫자 70은 70%로 압축한다는 뜻)
        byte[] bytes = baos.toByteArray();//해당 bitmap을 byte배열로 바꿔준다.
        String temp = Base64.encodeToString(bytes, Base64.DEFAULT);//Base 64 방식으로byte 배열을 String으로 변환
        return temp;//String을 retrurn
    }

    Thread uThread = new Thread() {
        @Override
        public void run(){
            try{
                //서버에 올려둔 이미지 URL
                URL url = new URL("http://connectsdk.com/ConnectSDK_Logo.jpg");

                //Web에서 이미지 가져온 후 ImageView에 지정할 Bitmap 만들기
                /* URLConnection 생성자가 protected로 선언되어 있으므로
                     개발자가 직접 HttpURLConnection 객체 생성 불가 */
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                    /* openConnection()메서드가 리턴하는 urlConnection 객체는
                    HttpURLConnection의 인스턴스가 될 수 있으므로 캐스팅해서 사용한다*/
                conn.setDoInput(true); //Server 통신에서 입력 가능한 상태로 만듦
                conn.connect(); //연결된 곳에 접속할 때 (connect() 호출해야 실제 통신 가능함)

                InputStream is = conn.getInputStream(); //inputStream 값 가져오기

                sendCurWebSession(BitmapToString(BitmapFactory.decodeStream(is)));
            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    };

    public void getImageInPhone() {    // 이미지 선택 누르면 실행됨 이미지 고를 갤러리 오픈
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 101);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // 갤러리에서 사진을 고른 후의 동작
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {   // 어떤 이미지도 선택하지 않은 경우
                    Toast.makeText(mContext.getApplicationContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
                } else {  // 이미지를 하나라도 선택한 경우
                    ContentResolver resolver = mContext.getApplicationContext().getContentResolver();
                    InputStream is = null;

                    if ( data.getClipData() == null ) { // 이미지 1개 선택
                        try {
                            is = resolver.openInputStream(data.getData());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        sendCurWebSession(BitmapToString(BitmapFactory.decodeStream(is)));
                    } else { // 이미지 여러 개 선택 시
                        ClipData clip = data.getClipData();

                        for (int i = 0; i < clip.getItemCount(); i++) {
                            try {
                                is = resolver.openInputStream(clip.getItemAt(i).getUri());
                                sendCurWebSession(BitmapToString(BitmapFactory.decodeStream(is)));
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
                Toast.makeText(mContext.getApplicationContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
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
//                    is.close();   // 스트림 닫아주기
//                    Toast.makeText(mContext.getApplicationContext(), "파일 불러오기 성공", Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    Toast.makeText(mContext.getApplicationContext(), "파일 불러오기 실패", Toast.LENGTH_SHORT).show();
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

    public View.OnClickListener closeWebApp = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            //responseMessageTextView.setText("");

            //closeWebAppButton.setEnabled(false);
            sendMessageButton.setEnabled(false);
            sendJSONButton.setEnabled(false);
            //leaveWebAppButton.setEnabled(false);
            isLaunched = false;

            mWebAppSession.setWebAppSessionListener(null);
            mWebAppSession.close(new ResponseListener<Object>() {

                @Override
                public void onSuccess(Object response) {
                	testResponse =  new TestResponseObject(true, TestResponseObject.SuccessCode, TestResponseObject.Close_WebAPP);
                    //launchWebAppButton.setEnabled(true);
                }

                @Override
                public void onError(ServiceCommandError error) {
                    Log.e(TAG, "Error closing web app | error = " + error);

                    //launchWebAppButton.setEnabled(true);
                }
            });
        }
    };

    @Override
    public void disableButtons() {
        super.disableButtons();
        isLaunched = false;

        //responseMessageTextView.setText("");
        webAppId = null;
    }

    public void setRunningAppInfo(LaunchSession session) {
        runningAppSession = session;
    }

}