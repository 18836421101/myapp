package com.example.shiyan_bottom.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.shiyan_bottom.MainActivity;
import com.example.shiyan_bottom.R;
import com.example.shiyan_bottom.video_player;

import java.util.List;

/**
 * Created by  ansen
 * Create Time 2016-12-19
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{
    private List<String> datas;
    private LayoutInflater inflater;
    //private int[] datas_pic;
    private  List<Integer> datas_pic;
    public RecyclerViewAdapter(Context context, List<String> datas,List<Integer> datas_pic){
        inflater=LayoutInflater.from(context);
        this.datas=datas;
        this.datas_pic=datas_pic;
    }

    //创建每一行的View 用RecyclerView.ViewHolder包装
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView=inflater.inflate(R.layout.recycler_item,parent,false);
        return new MyViewHolder(itemView);
    }

    //给每一行View填充数据
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        holder.textview.setText(datas.get(position));
        holder.imageView.setBackgroundResource(datas_pic.get(position));

        //if(position>5 && position == datas.size()-1){
            //holder.progressBar.setVisibility(View.VISIBLE);
        //}else{
            //holder.progressBar.setVisibility(View.GONE);
        //}
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    //数据源的数量
    @Override
    public int getItemCount() {
        return datas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView textview;
        private ImageView imageView;
        //private ProgressBar progressBar;

        public MyViewHolder(View itemView) {
            super(itemView);
            textview= (TextView) itemView.findViewById(R.id.textview);
            imageView=(ImageView) itemView.findViewById(R.id.imageview);
            textview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text =datas.get(getItemViewType());
                    if(text.equals("你好")|text.equals("多少钱")|text.equals("请坐")|text.equals("不用谢")) {
                        Log.i("textview", datas.get(getItemViewType()));
                        Intent intent = new Intent(view.getContext(), video_player.class);
                        //intent.setClass(getContext(), video_player.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("name", datas.get(getItemViewType()));
                        intent.putExtras(bundle);
                        view.getContext().startActivity(intent);
                    }
                }
            });
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text =datas.get(getItemViewType());
                    if(text.equals("你好")|text.equals("多少钱")|text.equals("请坐")|text.equals("不用谢")) {
                        Log.i("textview", datas.get(getItemViewType()));
                        Intent intent = new Intent(view.getContext(), video_player.class);
                        //intent.setClass(getContext(), video_player.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("name", datas.get(getItemViewType()));
                        intent.putExtras(bundle);
                        view.getContext().startActivity(intent);
                    }
                }
            });
            //progressBar= (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }
}
