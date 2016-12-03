package com.androiditgroup.loclook.tape_pkg;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by OS1 on 29.10.2015.
 */
public abstract class Tape_EndlessOnScrollListener extends RecyclerView.OnScrollListener {
//    public static String TAG = Tape_EndlessOnScrollListener.class.getSimpleName();

    private int previousTotal = 0;      // The total number of items in the dataset after the last load
    private boolean loading = true;     // True if we are still waiting for the last set of data to load.
    private int visibleThreshold =  5;  // The minimum amount of items to have below your current scroll position before loading more.
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private int current_page = 1;

    private LinearLayoutManager mLinearLayoutManager;

    public Tape_EndlessOnScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }

        // Log.d("scrollListener", "!" +loading+ " && (" +totalItemCount+ " - " +visibleItemCount+ ") <= (" +firstVisibleItem+ " + " +visibleThreshold+ "): " +(!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)));

        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // End has been reached

            // Log.d("scrollListener", "0_current_page: " +current_page+ " loading: " +loading);

            // Do something
            current_page++;

            onLoadMore(current_page);

            loading = true;

            // Log.d("scrollListener", "1_current_page: " +current_page+ " loading: " +loading);
        }
    }

    public abstract void onLoadMore(int current_page);
}