package com.anjlab.fin33;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anjlab.fin33.model.AppState;
import com.anjlab.fin33.model.Bank;
import com.anjlab.fin33.model.BanksUpdatedListener;
import com.anjlab.fin33.model.ExchangeRate;

import java.util.List;


public class MainFragment extends Fragment implements BanksUpdatedListener {

    List<Bank> banks;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public MainFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            ParseFin33Task mt = new ParseFin33Task(null);
                            mt.execute();
                        }
                    });
        }
        AppState.getInstance().subscribe(this);
        onParseDone(AppState.getInstance().getBanks());
        return view;
    }

    @Override
    public void onDestroyView() {
        AppState.getInstance().unsubscribe(this);
        super.onDestroyView();
    }

    @Override
    public void onParseDone(List<Bank> banks) {
        MainFragment.this.banks = banks;
        mAdapter = new MainFragmentAdapter(AppState.getInstance().getBanks(), new ExchangeRate.Currency[]{
                ExchangeRate.Currency.USD, ExchangeRate.Currency.EUR
        });
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onParseError(Throwable error) {
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
