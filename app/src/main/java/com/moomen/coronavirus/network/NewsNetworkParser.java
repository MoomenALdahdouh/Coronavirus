package com.moomen.coronavirus.network;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.moomen.coronavirus.utils.Utils;
import com.moomen.coronavirus.model.News;
import com.moomen.coronavirus.ui.NewsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.RequiresApi;

public class NewsNetworkParser {
    private Context context;
    public static final String NEWS_BASE =
            "https://newsapi.org/v2?apiKey=0e84a24547444efe874ff329ad08c272";
    public static final String NEWS_TOP_HEADLINES =
            "https://newsapi.org/v2/top-headlines?language=en&q=covid%20OR%20coronavirus&apiKey=0e84a24547444efe874ff329ad08c272";
    public static final String NEWS_EVERYTHING =
            "https://newsapi.org/v2/everything?language=en&q=covid%20OR%20coronavirus&apiKey=0e84a24547444efe874ff329ad08c272";

    private RequestQueue queue;
    private StringRequest request;

    public interface OnNewsFilledListener {
        void startFillingNews(ArrayList<News> articles);
    }

    public interface OnTopNewsFilledListener {
        void startFillingTopNews(ArrayList<News> article);
    }

    private OnNewsFilledListener newsListener;

    private OnTopNewsFilledListener topNewsListener;

    public void setOnNewsFilledListener(NewsFragment newsListener){
        this.newsListener = newsListener;
    }

    public void setOnTopNewsFilledListener(NewsFragment topNewsListener){
        this.topNewsListener = topNewsListener;
    }

    private static ArrayList<News> mTopNews;
    private static ArrayList<News> mNews = new ArrayList<>();
    private static int currentPageNumber = 0;

    public NewsNetworkParser(Context context) {
        this.context = context;
    }

    public void parseTopHeadlinesJson() {
        if(mTopNews != null) {
            if(topNewsListener != null)
                topNewsListener.startFillingTopNews(mTopNews);
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(this.context);
        StringRequest request = new StringRequest(Request.Method.GET, NEWS_TOP_HEADLINES, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject root = new JSONObject(response);
                    JSONArray articles = root.getJSONArray("articles");
                    ArrayList<News> listOfTopHeadlines = new ArrayList<>();
                    int counter = 0;
                    for(int i = 0; i<articles.length() && counter < 5; i++) {
                        JSONObject article = articles.getJSONObject(i);
                        String timeDiff = Utils.calculateDateDiff(article.getString("publishedAt"));
                        if(timeDiff == null)
                            continue;
                        String sourceName = article.getJSONObject("source").getString("name");
                        News topNews = new News(article.getString("urlToImage"), article.getString("title"),
                                article.getString("description"), article.getString("publishedAt"),
                                article.getString("url"), sourceName);
                        if(topNews.isValidNews()) {
                            listOfTopHeadlines.add(topNews);
                            counter++;
                        }
                    }
                    mTopNews = new ArrayList<>(listOfTopHeadlines);
                    if(topNewsListener != null)
                        topNewsListener.startFillingTopNews(listOfTopHeadlines);
                }catch (JSONException ex) { }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        parseTopHeadlinesJson();
                    }
                }, 500);
            }
        });
        queue.add(request);
    }

    public void parseEverythingJson(final int currentPageNumber) {
        if(NewsNetworkParser.currentPageNumber == currentPageNumber) {
            if(newsListener != null)
                newsListener.startFillingNews(mNews);
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(context);
        Uri builtUri = Uri.parse(NEWS_EVERYTHING)
                .buildUpon()
                .appendQueryParameter("page", String.valueOf(currentPageNumber))
                .build();
        StringRequest request = new StringRequest(Request.Method.GET, builtUri.toString(), new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject root = new JSONObject(response);
                    JSONArray articles = root.getJSONArray("articles");
                    ArrayList<News> listOfArticles = new ArrayList<>();
                    for(int i = 0; i<articles.length(); i++) {
                        JSONObject article = articles.getJSONObject(i);
                        String timeDiff = Utils.calculateDateDiff(article.getString("publishedAt"));
                        if(timeDiff == null)
                            continue;
                        String sourceName = article.getJSONObject("source").getString("name");
                        News news = new News(article.getString("urlToImage"), article.getString("title"),
                                article.getString("description"), article.getString("publishedAt"),
                                article.getString("url"), sourceName);
                        if(news.isValidNews())
                            listOfArticles.add(news);
                    }
                    mNews.addAll(listOfArticles);
                    NewsNetworkParser.currentPageNumber++;
                    if(newsListener != null)
                        newsListener.startFillingNews(mNews);
                }catch (JSONException ex) { }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        parseEverythingJson(currentPageNumber);
                    }
                }, 500);
            }
        });
        queue.add(request);
    }

}
