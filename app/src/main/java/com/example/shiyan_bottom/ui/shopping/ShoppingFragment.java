package com.example.shiyan_bottom.ui.shopping;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.example.shiyan_bottom.R;
import com.example.shiyan_bottom.RecycleViewDivider;
import com.example.shiyan_bottom.adapter.BannerAdapter;
import com.example.shiyan_bottom.adapter.RecyclerViewAdapter;
import com.example.shiyan_bottom.databinding.FragementShoppingBinding;

import java.util.ArrayList;
import java.util.List;

public class ShoppingFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    private FragementShoppingBinding binding;
    private BannerAdapter bannerAdapter;//ViewPager适配器
    public static final int CAROUSEL_TIME = 5000;//banner 滚动间隔

    //public ViewPager vpBanner;//

    private Handler handler1 = new Handler();
    private int currentItem = 0;//ViewPager当前位置
    public static final int PULL_TO_REFRESH = 1;//下拉刷新
    public static final int UP_TO_REFRESH = 2;//上拉加载更多

    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private List<String> datas;
    //private int [] datas_pic=new int[8];
    private List<Integer> datas_pic;
    private boolean isLoadMore = false;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final Runnable mTicker = new Runnable() {
        public void run() {
            long now = SystemClock.uptimeMillis();
            long next = now + (CAROUSEL_TIME - now % CAROUSEL_TIME);

            handler1.postAtTime(mTicker, next);//延迟5秒再次执行runnable,就跟计时器一样效果

            currentItem++;
            binding = FragementShoppingBinding.inflate(getLayoutInflater());

            ViewPager vpBanner = binding.vpBannerShopping;
            vpBanner.setCurrentItem(currentItem);
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ShoppingViewModel shoppingViewModel =
                new ViewModelProvider(this).get(ShoppingViewModel.class);

        binding = FragementShoppingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        ViewPager vpBanner = binding.vpBannerShopping;
        bannerAdapter = new BannerAdapter(getContext());//初始化适配器
        bannerAdapter.setOnBannerClickListener(onBannerClickListener);//图片点击监听
        vpBanner.setOffscreenPageLimit(2);//缓存页数
        vpBanner.setAdapter(bannerAdapter);//设置适配器
        vpBanner.addOnPageChangeListener(onPageChangeListener);
        currentItem = bannerAdapter.getBanners().length * 50;
        vpBanner.setCurrentItem(currentItem);
        handler1.postDelayed(mTicker, CAROUSEL_TIME);//开启计时器

        final TextView textView = binding.textShopping;
        shoppingViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        swipeRefreshLayout = (SwipeRefreshLayout) binding.swipeRefreshLayout;
        //监听刷新状态
        swipeRefreshLayout.setOnRefreshListener(this);
        //设置下拉刷新的箭头颜色(可以设置多个颜色)
        swipeRefreshLayout.setColorSchemeResources(R.color.black,
                android.R.color.holo_green_light, R.color.purple_200);

        initData();
        initData_pic();

        recyclerView = (RecyclerView) binding.recyclerview;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));//设置布局管理器
        recyclerView.setAdapter(adapter = new RecyclerViewAdapter(getContext(), datas,datas_pic));
        recyclerView.addOnScrollListener(onScrollListener);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL));
        return root;
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
        handler1.removeCallbacks(mTicker);//删除计时器
    }

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            RecyclerView.LayoutManager mLayoutManager = recyclerView.getLayoutManager();
            int lastVisibleItem = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
            int totalItemCount = mLayoutManager.getItemCount();
            //最后一项显示&&下滑状态的时候 加载更多
            if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
                if (!isLoadMore) {
                    loadMore();//加载更多
                    isLoadMore = true;
                }
            }
        }
    };

    public void onRefresh() {
        //延迟3000毫秒,发送空消息跟handle，handle的handleMessage方法会接收到
        handler.sendEmptyMessageDelayed(PULL_TO_REFRESH, 1000);
    }

    public void loadMore() {
        handler.sendEmptyMessageDelayed(UP_TO_REFRESH, 1000);
    }


    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PULL_TO_REFRESH://下拉刷新
                    if (datas.size() > 0) {
                        //int t = datas.size();
                        int p = datas_pic.get(0);
                        String x= datas.get(0);
                        datas_pic.remove(0);
                        datas.remove(0);//删除第一条
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

    private void initData() {
        datas = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            datas.add("item:" + i);
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
        }
    }

