package com.example.blottermanagementsystem.utils;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LazyListOptimizer {
    
    public static void optimizeRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(android.view.View.DRAWING_CACHE_QUALITY_HIGH);
    }
    
    public static void enablePrefetch(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            layoutManager.setInitialPrefetchItemCount(4);
        }
    }
    
    public static void setRecyclerViewPool(RecyclerView recyclerView, RecyclerView.RecycledViewPool pool) {
        recyclerView.setRecycledViewPool(pool);
    }
    
    public static RecyclerView.RecycledViewPool createSharedPool() {
        RecyclerView.RecycledViewPool pool = new RecyclerView.RecycledViewPool();
        pool.setMaxRecycledViews(0, 20);
        return pool;
    }
}
