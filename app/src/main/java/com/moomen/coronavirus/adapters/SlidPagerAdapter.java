package com.moomen.coronavirus.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.moomen.coronavirus.R;
import com.moomen.coronavirus.model.SlidPager;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class SlidPagerAdapter extends PagerAdapter {

    ArrayList<SlidPager> slidPagerArrayList;
    Context context;

    public SlidPagerAdapter(ArrayList<SlidPager> slidPagerArrayList, Context context) {
        this.slidPagerArrayList = slidPagerArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        @SuppressLint("InflateParams") View slidLayout = inflater.inflate(R.layout.slid_item, null);
        SlidPager currentPage = slidPagerArrayList.get(position);
        ImageView imageNews = slidLayout.findViewById(R.id.image_view_main_news_id);
        TextView titleNews = slidLayout.findViewById(R.id.text_view_title_main_news__id);
        TextView publishDate = slidLayout.findViewById(R.id.text_view_published_at_main_news_id);
        ImageView iconTime = slidLayout.findViewById(R.id.icon_time);
        final ProgressBar progressBar = slidLayout.findViewById(R.id.progress_bar_main_news_id);
        Picasso.get()
                .load(currentPage.getImageNews())
                .into(imageNews, new Callback() {
                    @Override
                    public void onSuccess() { progressBar.setVisibility(View.GONE); }

                    @Override
                    public void onError(Exception e) { }
                });
        titleNews.setText(currentPage.getTitleNews());
        publishDate.setText("Published " + currentPage.getPublishDate() + " ago.");
        iconTime.setImageResource(currentPage.getIconTime());
        container.addView(slidLayout);
        return slidLayout;
    }

    @Override
    public int getCount() {
        return slidPagerArrayList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

}
