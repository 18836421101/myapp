package com.example.shiyan_bottom.ui.dashboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import android.widget.MediaController;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.shiyan_bottom.JsonParser;
import com.example.shiyan_bottom.MyVideoView;
import com.example.shiyan_bottom.R;

import com.example.shiyan_bottom.SpeechApplication;
import com.example.shiyan_bottom.databinding.FragmentDashboardBinding;
import com.example.shiyan_bottom.video_player;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;


public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;


    private static final String TAG = "DashboardFragment";
    private SpeechRecognizer mIat;// 语音听写对象
    private RecognizerDialog mIatDialog;// 语音听写UI

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    //private SharedPreferences pref;//缓存

    private String mEngineType = SpeechConstant.TYPE_CLOUD;// 引擎类型
    private String language = "zh_cn";//识别语言

    private TextView tvResult;//识别结果
    private String resultType = "json";//结果内容数据格式


    int[] soundResId = {R.raw.nihaoshengyin,R.raw.duoshaoqianshengyin};	// 音频资源
    SoundPool soundPool;
    int[] soundId;		// soundPool为每个装入的音频资源生成的SoundID
    int flyStreamId;	// soundPool播放fly音频时使用的Channel，通过Channel控制音效播放
    PlayThread playThread;	// 因为SoundPool的循环播放功能无效，所以用Thread来实现循环播放
    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SpeechUtility.createUtility(getContext(),"appid=67352327");

        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(getContext(), mInitListener);
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(getContext(), mInitListener);



        soundId = new int[soundResId.length];
        soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 100);	// 创建SoundPool对象
        for(int i=0; i<soundResId.length; i++)
            soundId[i] = soundPool.load(getContext(), soundResId[i], 1);	// 依次载入音频资源，生成的SoundID存入soundId数组，播放时使用

        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        ((Activity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        AutoCompleteTextView autoCompleteTextView =binding.autoCompleteTextView;
        autoCompleteTextView.setThreshold(0);

        // 匹配字符串
        String[] strList = new String[]{"你好","多少钱","请坐","芜湖小白","abcd"};

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),R.layout.three,strList);

        // 配置
        autoCompleteTextView.setAdapter(arrayAdapter);

        autoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
// 手指按下并且文本框为空
                if(event.getAction() == KeyEvent.ACTION_DOWN && TextUtils.isEmpty(autoCompleteTextView.getText().toString())){
                    autoCompleteTextView.showDropDown();                          // 主要是这个方法
                }
                return false;
            }
        });

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String text = adapterView.getItemAtPosition(i).toString();
                if(text.equals("你好")|text.equals("多少钱")|text.equals("请坐")|text.equals("不用谢")) {
                    initNetVideo(text);
                    Toast.makeText(getContext(),"转换成功" , Toast.LENGTH_SHORT).show();
                    //TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                    //v.setTextColor(Color.YELLOW);
                    //toast.show();
                    //Toast.makeText(MainActivity.this, "转换成功!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button button_sousuo=binding.button;
        button_sousuo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text_2 = autoCompleteTextView.getText().toString();
                Log.i("text_2", text_2);
                if (text_2.equals("你好") | text_2.equals("多少钱") | text_2.equals("请坐") | text_2.equals("不用谢")) {
                    initNetVideo(text_2);
                    //Toast toast = Toast.makeText(MainActivity.this,"转换成功" , Toast.LENGTH_SHORT);
                    //TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                    //v.setTextColor(Color.YELLOW);
                    //toast.show();
                    //midToast("转换成功",);
                    Toast.makeText(getContext(), "转换成功!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getContext(),"未查询到对应手语",Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button button_yuyin = binding.buttonYuyin;
        button_yuyin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if( null == mIat ){
                    // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
                    showMsg( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );
                    return;
                }

                mIatResults.clear();//清除数据
                setParam(); // 设置参数
                mIatDialog.setListener(mRecognizerDialogListener);//设置监听
                mIatDialog.show();// 显示对话框
            }
        });


        return root;


    }
    private void initNetVideo(String name) {
        //设置有进度条可以拖动快进
        MyVideoView myVideoView = binding.videoView;
        MediaController localMediaController = new MediaController(getContext());
        myVideoView.setMediaController(localMediaController);
        if(Objects.equals(name, "你好")){
            String uri = "android.resource://"+"com.example.shiyan_bottom"+"/"+R.raw.nihao;
            myVideoView.setVideoURI(Uri.parse(uri));
            localMediaController.setMediaPlayer(myVideoView);
            myVideoView.setMediaController(localMediaController);
            soundPool.play(soundId[0], 1, 1, 1, 0, 1);
        }if(Objects.equals(name, "不用谢")){
            String uri = "android.resource://"+"com.example.shiyan_bottom"+"/"+R.raw.buyongxie;
            myVideoView.setVideoURI(Uri.parse(uri));
            localMediaController.setMediaPlayer(myVideoView);
            myVideoView.setMediaController(localMediaController);
        }if(Objects.equals(name, "请坐")){
            String uri = "android.resource://"+"com.example.shiyan_bottom"+"/"+R.raw.qingzuo;
            myVideoView.setVideoURI(Uri.parse(uri));
            localMediaController.setMediaPlayer(myVideoView);
            myVideoView.setMediaController(localMediaController);
        }if(Objects.equals(name, "多少钱")){
            String uri = "android.resource://"+"com.example.shiyan_bottom"+"/"+R.raw.duoshaoqian;
            myVideoView.setVideoURI(Uri.parse(uri));
            localMediaController.setMediaPlayer(myVideoView);
            myVideoView.setMediaController(localMediaController);
            soundPool.play(soundId[1], 1, 1, 1, 0, 1);
        }
    }


    class PlayThread extends Thread {
        boolean flag;
        int soundId;
        int streamId;
        int duration;
        public PlayThread(int sId, int d){
            soundId = sId;
            duration = d;
        }
        public void run(){
            flag = true;
            while(flag){
                streamId = soundPool.play(soundId, 1, 1, 1, 0, 1f);	// 播放音频soundId
                try {
                    sleep(duration);				// 等待该音频的播放时间
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showMsg("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };


    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {

            printResult(results);//结果数据解析

        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showMsg(error.getPlainDescription(true));
        }

    };

    /**
     * 数据解析
     *
     * @param results
     */
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        AutoCompleteTextView autoTextview = binding.autoCompleteTextView;
        autoTextview.setText(resultBuffer.toString().substring(0,resultBuffer.length()-1));//听写结果显示
        String text_2 = autoTextview.getText().toString();
        if (text_2.equals("你好") | text_2.equals("多少钱") | text_2.equals("请坐") | text_2.equals("不用谢")) {
            initNetVideo(text_2);
            //Toast toast = Toast.makeText(MainActivity.this,"转换成功" , Toast.LENGTH_SHORT);
            //TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            //v.setTextColor(Color.YELLOW);
            //toast.show();
            //midToast("转换成功",);
            Toast.makeText(getContext(), "转换成功!", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getContext(),"未查询到对应手语",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        if (language.equals("zh_cn")) {
            SharedPreferences prefs = getActivity().getSharedPreferences("ARS",
                    Activity.MODE_PRIVATE);
            String lag = prefs.getString("iat_language_preference",
                    "mandarin");
            Log.e(TAG, "language:" + language);// 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        } else {

            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));

        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        SharedPreferences prefs = getActivity().getSharedPreferences("ARS",
                Activity.MODE_PRIVATE);
        mIat.setParameter(SpeechConstant.VAD_BOS, prefs.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, prefs.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, prefs.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    /**
     * 提示消息
     * @param msg
     */
    private void showMsg(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != mIat) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(getContext(), perm)) {
                toApplyList.add(perm);
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(), toApplyList.toArray(tmpList), 123);
        }

    }

    /**
     * 权限申请回调，可以作进一步处理
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}