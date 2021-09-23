package com.lentera.silaq.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lentera.silaq.Callback.IRecyclerClickListener;
import com.lentera.silaq.EventBus.PlusClick;
import com.lentera.silaq.EventBus.PopularCategoryClick;
import com.lentera.silaq.Model.PlusModel;
import com.lentera.silaq.Model.PopularCategoryModel;
import com.lentera.silaq.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyPlusAdapter extends RecyclerView.Adapter<MyPlusAdapter.MyViewHolder> {

    Context context;
    List<PlusModel>plusModellList;


    public MyPlusAdapter(Context context, List<PlusModel> plusModellList) {
        this.context = context;
        this.plusModellList = plusModellList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_chill,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(plusModellList.get(position).getImage())
                .into(holder.category_image);
        holder.txt_plus_name.setText(plusModellList.get(position).getName());

        holder.setListener((view, pos) ->
                EventBus.getDefault().postSticky(new PlusClick(plusModellList.get(pos)))
        );
    }

    @Override
    public int getItemCount() {
        return plusModellList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;

        @BindView(R.id.txt_plus_name)
        TextView txt_plus_name;
        @BindView(R.id.category_image)
        CircleImageView category_image;

        IRecyclerClickListener listener;
        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v,getAdapterPosition());
        }
    }
}
