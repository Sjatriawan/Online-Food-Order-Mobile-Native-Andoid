package com.lentera.silaq.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asksira.loopingviewpager.LoopingViewPager;
import com.lentera.silaq.Adapter.MyBestDealAdapter;
import com.lentera.silaq.Adapter.MyPlusAdapter;
import com.lentera.silaq.Adapter.MyPopularCategoriesAdapter;
import com.lentera.silaq.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    Unbinder unbinder;

    @BindView(R.id.recycler_popular)
    RecyclerView recycler_popular;
    @BindView(R.id.recycler_chill)
    RecyclerView recycler_chill;
    @BindView(R.id.viewpager)
    LoopingViewPager viewPager;

    LayoutAnimationController layoutAnimationController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this,root);
        init();
        homeViewModel.getPopularList().observe(this, popularCategoryModels ->{
            MyPopularCategoriesAdapter adapter = new MyPopularCategoriesAdapter(getContext(),popularCategoryModels);
            recycler_popular.setAdapter(adapter);
            recycler_popular.setLayoutAnimation(layoutAnimationController);
        });

        homeViewModel.getBestDealList().observe(this,bestDealModels ->{
            MyBestDealAdapter adapter = new MyBestDealAdapter(getContext(),bestDealModels,true);
            viewPager.setAdapter(adapter);

            homeViewModel.getPlusList().observe(this, plusModels ->{
                MyPlusAdapter adapter1 = new MyPlusAdapter(getContext(),plusModels);
                recycler_chill.setAdapter(adapter1);
                recycler_chill.setLayoutAnimation(layoutAnimationController);
            });

        });
        return root;
    }
    private  void init(){
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
        recycler_popular.setHasFixedSize(true);
        recycler_popular.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL, false));
        recycler_chill.setHasFixedSize(true);
        recycler_chill.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL, false));
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager.resumeAutoScroll();
    }

    @Override
    public void onPause() {
        viewPager.pauseAutoScroll();
        super.onPause();

    }
}
