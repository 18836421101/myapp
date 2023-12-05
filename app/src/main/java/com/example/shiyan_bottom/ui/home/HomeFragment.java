package com.example.shiyan_bottom.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.shiyan_bottom.R;
import com.example.shiyan_bottom.databinding.FragmentHomeBinding;
import com.example.shiyan_bottom.adapter.BannerAdapter;
import com.google.android.material.navigation.NavigationView;

import java.io.File;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment {
    //music_player
    int[] soundResId = {R.raw.nihaoshengyin,R.raw.duoshaoqianshengyin};	// 音频资源
    SoundPool soundPool;
    int[] soundId;		// soundPool为每个装入的音频资源生成的SoundID

    //fragment
    private FragmentHomeBinding binding;
    private BannerAdapter bannerAdapter;//ViewPager适配器
    public static final int CAROUSEL_TIME = 5000;//banner 滚动间隔

    //public ViewPager vpBanner;//

    private Handler handler = new Handler();
    private int currentItem = 0;//ViewPager当前位置

    private final static String TAG = "VideoRecordActivity";
    private int RECORDER_CODE = 1; // 录制操作的请求码

    private final Runnable mTicker = new Runnable() {
        public void run() {
            long now = SystemClock.uptimeMillis();
            long next = now + (CAROUSEL_TIME - now % CAROUSEL_TIME);

            handler.postAtTime(mTicker, next);//延迟5秒再次执行runnable,就跟计时器一样效果

            currentItem++;
            FragmentHomeBinding Ting = FragmentHomeBinding.inflate(getLayoutInflater());
            ViewPager vpBanner = Ting.drawerlayout.findViewById(R.id.vp_banner);
            vpBanner.setCurrentItem(currentItem);
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //music_player
        soundId = new int[soundResId.length];
        soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 100);	// 创建SoundPool对象
        for(int i=0; i<soundResId.length; i++)
            soundId[i] = soundPool.load(getContext(), soundResId[i], 1);	// 依次载入音频资源，生成的SoundID存入soundId数组，播放时使用

        //fragment
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        DrawerLayout drawerLayout = binding.drawerlayout;
        setDrawerLeftEdgeSizeAndNoLongPressPopup(getActivity(),drawerLayout, 0.7f);
        NavigationView t =binding.navP;;
        View drawview = t.inflateHeaderView(R.layout.nav_header_main);

        ImageView head_iv = drawview.findViewById(R.id.imageView234);
        head_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Log:","头部被点击了！");
            }
        });


        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), com.example.shiyan_bottom.chatGpt.chat_main.class);
                view.getContext().startActivity(intent);
            }
        });

        t.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                                                @Override
                                                public boolean onNavigationItemSelected(MenuItem menuItem) {
                                                    int s=menuItem.getItemId();
                                                    System.out.println(s);
                                                    if (s==R.id.nav_home){
                                                        System.out.println("123456");
                                                    }
                                                    return true;
                                                }
                                            }
        );


        Toolbar mToolbar =binding.appBarMain.toolbar;
        mToolbar.setTitle("学译致用");
        mToolbar.inflateMenu(R.menu.main);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //int t = item.getItemId();
                String p = item.getTitle().toString();
                if (p.equals("扫一扫")){
                    //scan(getActivity());
                    System.out.println("扫一扫");
                    Toast.makeText(getContext(),"扫一扫",Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        ViewPager vpBanner = binding.drawerlayout.findViewById(R.id.vp_banner);
        final Button button1 = binding.drawerlayout.findViewById(R.id.button);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.button) {
                    // 下面准备跳到系统的摄像机，并获得录制完的视频文件
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); // 视频质量。0 低质量；1 高质量
                    intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 10485760L); // 大小限制，单位字节
                    intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10); // 时长限制，单位秒
                    startActivityForResult(intent, RECORDER_CODE); // 打开系统摄像机
                }
            }
        });
        final Button button2 = binding.drawerlayout.findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

                intent.setType("*/*");

                intent.addCategory(Intent.CATEGORY_OPENABLE);

                startActivityForResult(Intent.createChooser(intent, "需要选择文件"), 1);
            }
        });
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        bannerAdapter = new BannerAdapter(getContext());//初始化适配器
        bannerAdapter.setOnBannerClickListener(onBannerClickListener);//图片点击监听
        vpBanner.setOffscreenPageLimit(2);//缓存页数
        vpBanner.setAdapter(bannerAdapter);//设置适配器
        vpBanner.addOnPageChangeListener(onPageChangeListener);
        currentItem = bannerAdapter.getBanners().length * 50;
        vpBanner.setCurrentItem(currentItem);
        handler.postDelayed(mTicker, CAROUSEL_TIME);//开启计时器
        return root;
    }

    //music_player
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



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            currentItem = position;

            //改变点点点图片的选中状态
            //setImageBackground(position %= bannerAdapter.getBanners().length);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };



    private View.OnClickListener onBannerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int position = (Integer) view.getTag();//从tag中取出当前点击的ImageView的位置
            Toast.makeText(getContext(), "当前点击位置:" + position, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(mTicker);//删除计时器
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            Log.i("HomeFragment","data="+data);
            Uri uri = data.getData();

            String pathString = UriUtil.getPath(getContext(), uri);
            Log.i("HomeFragment","pathString="+pathString);
            String url = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/video_cls/dingfeng3";
            /**
             * 动态获取权限，Android 6.0 新特性，一些保护权限，除了要在AndroidManifest中声明权限，还要使用如下代码动态获取
             */
            if (Build.VERSION.SDK_INT >= 23) {
                int REQUEST_CODE_CONTACT = 101;
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                //验证是否许可权限
                for (String str : permissions) {
                    if (getContext().checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                        //申请权限
                        this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                        return;
                    }
                }
            }
            try {
                String sourceVideo = pathString;
                System.out.println(sourceVideo);
                Log.i("HomeFragment","sourceVideo="+sourceVideo);
                sourceVideo = java.net.URLDecoder.decode(sourceVideo, "utf-8");
                /***
                InputStream inputStream = org.apache.commons.io.FileUtils.openInputStream(new File(sourceVideo));
                // encode
                String ss = null;
                ss = new String(Base64.getEncoder().encode(IOUtils.toByteArray(inputStream)), StandardCharsets.ISO_8859_1);
                Map<String, Object> map = new HashMap<>();
                map.put("video", ss);
                map.put("top_num", "1");
                String param = GsonUtils.toJson(map);

                // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
                AuthService authService = new AuthService();
                String accessToken = authService.getAuth("F4uNyQq90G9caArRPAjhm3AO", "svgyoMrGXvV8KGmbMcYGd2DLgYvPYirm");
                String result = HttpUtil.post(url, accessToken, "application/json", param);
                Log.i("HomeFragment","result="+result);
                int a1 = result.indexOf("name\":");
                int a2 = result.indexOf("\",\"sco");
                if (a1 == 6 || a2 == -1) {
                    Log.d("button：【】【】【】", "无");
                } else {
                    String name = result.substring(a1 + 7, a2);
                    Log.i("name=", name);
                    //主页面展示手语翻译含义
                    TextView text=getView().findViewById(R.id.textView2);
                    text.setText("您的手语意思为：\n"+name);
                    Log.i("结果:","您的手语意思为：\n"+name);
                }
                 ***/
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .connectTimeout(120000L, TimeUnit.MILLISECONDS).readTimeout(120000L,TimeUnit.MILLISECONDS)
                        .build();
                MediaType mediaType = MediaType.parse("multipart/form-data; boundary=--------------------------151724770497156709258080");
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("video","0002.mp4",
                                RequestBody.create(MediaType.parse("application/octet-stream"),
                                        new File(sourceVideo)))
                        .build();
                Request request = new Request.Builder()
                        .url("http://192.168.1.104:5000/?video=")
                        .method("POST", body)
                        .build();

                Response response = client.newCall(request).execute();
                //System.out.println(response.body().string());
                String p = response.body().string();
                StringBuffer buffer = new StringBuffer(p);
                //System.out.println(buffer.substring(15, buffer.length()-4));
                String name = buffer.substring(15, buffer.length()-4);
                if(name.equals("\\u4f60\\u597d")){
                    name = "你好";
                    soundPool.play(soundId[0], 1, 1, 1, 0, 1);
                }
//                Button button_bofang = binding.appBarMain.buttonBofang;
//                button_bofang.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (buffer.substring(15, buffer.length()-4).equals("\\u4f60\\u597d")){
//                            soundPool.play(soundId[0], 1, 1, 1, 0, 1);
//                        }
//                        else{
//                            Log.i("提示:","请先进行手语翻译");
//                        }
//                    }
//                });
                System.out.println("name="+name);
                TextView text=getView().findViewById(R.id.textView2);
                text.setText("您的手语意思为：\n"+name);
                Log.i("结果:","您的手语意思为：\n"+name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 抽屉滑动范围控制
     * @param activity
     * @param drawerLayout
     * @param displayWidthPercentage 占全屏的份额0~1
     */
    public void setDrawerLeftEdgeSizeAndNoLongPressPopup(Activity activity,
                                                         DrawerLayout drawerLayout,
                                                         float displayWidthPercentage) {
        if (activity != null) {
            try {
                // displayWidthPercentage传1开启全面屏手势滑动，小于1设定滑动范围
                Field leftDraggerField = this.getClass().getSuperclass().getDeclaredField("mLeftDragger");
                leftDraggerField.setAccessible(true);
                ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(this);
                Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
                edgeSizeField.setAccessible(true);
                int edgeSize = edgeSizeField.getInt(leftDragger);
                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                edgeSizeField.setInt(leftDragger, Math.max(edgeSize, (int) ((float) displaySize.x * displayWidthPercentage)));

                // 禁止长按滑出负一屏
                // 获取Layout的ViewDragCallBack实例mLeftCallback
                // 更改其属性mPeekRunnable
                Field leftCallbackField = drawerLayout.getClass().getSuperclass().getDeclaredField("mLeftCallback");
                leftCallbackField.setAccessible(true);
                // 因为无法直接访问私有内部类，所以该私有内部类实现的接口非常重要，通过多态的方式获取实例
                ViewDragHelper.Callback leftCallback = (ViewDragHelper.Callback) leftCallbackField.get(drawerLayout);
                Field peekRunnableField = leftCallback.getClass().getDeclaredField("mPeekRunnable");
                peekRunnableField.setAccessible(true);
                peekRunnableField.set(leftCallback, (Runnable) () -> {
                });
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


}