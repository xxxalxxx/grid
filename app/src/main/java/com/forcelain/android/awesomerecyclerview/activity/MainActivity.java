package com.forcelain.android.awesomerecyclerview.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.forcelain.android.awesomerecyclerview.R;
import com.forcelain.android.awesomerecyclerview.view.ArticleAdapter;
import com.forcelain.android.awesomerecyclerview.view.AwesomeLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private AwesomeLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        layoutManager = new AwesomeLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        ArticleAdapter adapter = new ArticleAdapter();
        recyclerView.setAdapter(adapter);

        List<String> a = new ArrayList<>();
        a.add("a");
        a.add("b");
        a.add("c");
        a.add("d");
        a.add("e");
        a.add("f");
        a.add("g");
        a.add("h");

        adapter.setArticles(a);

    }
}
