package com.example.shiyan_bottom.ui.notifications;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.shiyan_bottom.R;
import com.example.shiyan_bottom.RecycleViewDivider;
import com.example.shiyan_bottom.adapter.RecyclerViewAdapter;
import com.example.shiyan_bottom.databinding.FragmentNotificationsBinding;
import com.example.shiyan_bottom.video_player;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private FragmentNotificationsBinding binding;
    public static final int PULL_TO_REFRESH=1;//下拉刷新
    public static final int UP_TO_REFRESH=2;//上拉加载更多

    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private List<String> datas;

    private boolean isLoadMore = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Integer>  datas_pic;
    //private int [] datas_pic=new int[10];
    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        AutoCompleteTextView autoCompleteTextView = binding.autoCompleteTextView;
        // 设置必须写入1个及以上的字符数才可以自动提示备选
        autoCompleteTextView.setThreshold(0);

        // 匹配字符串
        String[] strList = new String[]{"你好","多少钱","请坐","芜湖小白","abcd"};

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),R.layout.two,strList);

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
                    Log.i("textview", text);
                    Intent intent = new Intent(view.getContext(), video_player.class);
                    //intent.setClass(getContext(), video_player.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("name", text);
                    intent.putExtras(bundle);
                    view.getContext().startActivity(intent);
                }
            }
        });

        Button button_sousuo= binding.button2;
        button_sousuo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text_2 = autoCompleteTextView.getText().toString();
                Log.i("text_2", text_2);
                if (text_2.equals("你好") | text_2.equals("多少钱") | text_2.equals("请坐") | text_2.equals("不用谢")) {
                    Intent intent = new Intent(view.getContext(), video_player.class);
                    //intent.setClass(getContext(), video_player.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("name", text_2);
                    intent.putExtras(bundle);
                    view.getContext().startActivity(intent);
                }
                else {
                    Toast.makeText(getContext(),"未查询到对应手语",Toast.LENGTH_SHORT).show();
                }
            }
        });

        swipeRefreshLayout= (SwipeRefreshLayout) binding.swipeRefreshLayout;
        //监听刷新状态
        swipeRefreshLayout.setOnRefreshListener(this);
        //设置下拉刷新的箭头颜色(可以设置多个颜色)
        swipeRefreshLayout.setColorSchemeResources(R.color.black,
                android.R.color.holo_green_light,R.color.purple_200);

        initData();

        initData_pic();
        recyclerView= (RecyclerView) binding.recyclerview;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));//设置布局管理器
        recyclerView.setAdapter(adapter=new RecyclerViewAdapter(getContext(),datas,datas_pic));

        recyclerView.addOnScrollListener(onScrollListener);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2,GridLayoutManager.VERTICAL,false));
        recyclerView.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL));
        //final TextView textView = binding.textNotifications;
        //notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    private RecyclerView.OnScrollListener onScrollListener=new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            RecyclerView.LayoutManager mLayoutManager = recyclerView.getLayoutManager();
            int lastVisibleItem = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
            int totalItemCount = mLayoutManager.getItemCount();
            //最后一项显示&&下滑状态的时候 加载更多
            if (lastVisibleItem >= totalItemCount-1 && dy > 0) {
                if(!isLoadMore){
                    loadMore();//加载更多
                    isLoadMore=true;
                }
            }
        }
    };

    @Override
    public void onRefresh() {
        //延迟3000毫秒,发送空消息跟handle，handle的handleMessage方法会接收到
        handler.sendEmptyMessageDelayed(PULL_TO_REFRESH,1000);
    }

    public void loadMore() {
        handler.sendEmptyMessageDelayed(UP_TO_REFRESH,1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case PULL_TO_REFRESH://下拉刷新
                    if(datas.size()>0){
                        //int t = datas.size();
                        int p = datas_pic.get(0);
                        String x = datas.get(0);
                        datas.remove(0);//删除第一条
                        //datas_pic = datas_pic_refrensh(datas_pic);
                        datas_pic.remove(0);
                        adapter.notifyDataSetChanged();//更新第一条记录
                        swipeRefreshLayout.setRefreshing(false);//false:刷新完成  true:正在刷新
                        datas.add(x);
                        datas_pic.add(p);
                    }
                    break;
                //case UP_TO_REFRESH://上拉加载更多
                //for(int i=0;i<3;i++){
                //datas.add("load more item:"+i);
                //}
                //adapter.notifyDataSetChanged();//更新列表
                //isLoadMore=false;//加载更多完成
                //break;
            }
        }
    };

    private void initData(){
        datas=new ArrayList<>();
        datas.add("你好");
        datas.add("多少钱");
        datas.add("请坐");
        datas.add("不用谢");
        for(int i=4;i<10;i++){
            datas.add("item:"+i);
        }
    }

    private void initData_pic() {
        datas_pic=new ArrayList<>();
        datas_pic.add(R.mipmap.pic);
        datas_pic.add(R.mipmap.pic2);
        datas_pic.add(R.mipmap.pic3);
        datas_pic.add(R.mipmap.pic);
        datas_pic.add(R.mipmap.pic2);
        datas_pic.add(R.mipmap.pic3);
        datas_pic.add(R.mipmap.pic);
        datas_pic.add(R.mipmap.pic2);
        datas_pic.add(R.mipmap.pic3);
        datas_pic.add(R.mipmap.pic);
    }

}