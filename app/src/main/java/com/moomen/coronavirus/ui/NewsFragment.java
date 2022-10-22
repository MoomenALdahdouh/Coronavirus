package com.moomen.coronavirus.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.moomen.coronavirus.R;
import com.moomen.coronavirus.utils.Utils;
import com.moomen.coronavirus.adapters.NewsAdapter;
import com.moomen.coronavirus.adapters.SlidPagerAdapter;
import com.moomen.coronavirus.databinding.FragmentNewsBinding;
import com.moomen.coronavirus.model.News;
import com.moomen.coronavirus.model.SlidPager;
import com.moomen.coronavirus.network.NewsNetworkParser;

import java.util.ArrayList;

public class NewsFragment extends Fragment implements NewsNetworkParser.OnNewsFilledListener, NewsNetworkParser.OnTopNewsFilledListener {
    private FragmentNewsBinding binding;

    private NewsNetworkParser parser;

    private boolean loading = true;
    int pastVisibleItems, visibleItemCount, totalItemCount;

    private static int currentPageNumber = 1;

    private RecyclerView recyclerViewNews;
    private NewsAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private static ArrayList<News> mArticleNews = new ArrayList<>();
    private static ArrayList<News> mTopNews = new ArrayList<>();

    private static int currentScrollPosition;

    private ArrayList<SlidPager> slidPagerList;
    private ViewPager slidPager;
    private SlidPagerAdapter slidPagerAdapter;
    private TabLayout tabLayoutIndicator;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNewsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerViewNews = binding.recyclerViewNewsId;
        adapter = new NewsAdapter(mArticleNews, getContext());
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerViewNews.setLayoutManager(linearLayoutManager);
        recyclerViewNews.setAdapter(adapter);

        showDialogWhenNoInternet(getContext());
        updateWhenUserWantsMoreNews();
        return root;
    }

    private void updateWhenUserWantsMoreNews() {
        recyclerViewNews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            binding.progressBarAddMoreNewsId.setVisibility(View.VISIBLE);
                            currentPageNumber++;
                            parser.parseEverythingJson(currentPageNumber);
                        }
                    }
                }
            }
        });
    }

    private void downloadNews() {
        parser = new NewsNetworkParser(getContext());
        parser.setOnNewsFilledListener(this);
        parser.setOnTopNewsFilledListener(this);
        binding.progressBarNewsId.setVisibility(View.VISIBLE);
        parser.parseEverythingJson(currentPageNumber);
        parser.parseTopHeadlinesJson();
    }

    @Override
    public void startFillingNews(ArrayList<News> articles) {
        loading = true;
        binding.progressBarAddMoreNewsId.setVisibility(View.GONE);
        mArticleNews.clear();
        mArticleNews.addAll(articles);
        adapter.notifyDataSetChanged();
        binding.progressBarNewsId.setVisibility(View.GONE);
    }

    @Override
    public void startFillingTopNews(ArrayList<News> topHeadlines) {
        mTopNews = new ArrayList<>(topHeadlines);
        slidPagerList = new ArrayList<>();
        slidPager = binding.viewPagerId;
        tabLayoutIndicator = binding.indicatorTabSlidPageId;
        int icon = R.drawable.ic_access_time_whit_24dp;
        for (int i = 0; i <5 ; i++)
            slidPagerList.add(new SlidPager(topHeadlines.get(i).getImageUrl(), topHeadlines.get(i).getTitle(), topHeadlines.get(i).getTimeDiff(), icon));
        slidPagerAdapter = new SlidPagerAdapter(slidPagerList, getContext());
        slidPager.setAdapter(slidPagerAdapter);
        tabLayoutIndicator.setupWithViewPager(slidPager, true);
    }

    public void showDialogWhenNoInternet(final Context context) {
        if(!Utils.isNetworkAvailable(context) && mArticleNews.size()==0 && mTopNews.size()==0)  // Don't show the dialog when news are already loaded
            new AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Internet Connection Alert")
                    .setMessage("Please Check Your Internet Connection")
                    .setCancelable(false)
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showDialogWhenNoInternet(context);
                        }
                    }).show();
        else
            downloadNews();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(linearLayoutManager!=null)
            currentScrollPosition = linearLayoutManager.findFirstVisibleItemPosition();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(recyclerViewNews!=null)
            recyclerViewNews.scrollToPosition(currentScrollPosition);
    }
}
