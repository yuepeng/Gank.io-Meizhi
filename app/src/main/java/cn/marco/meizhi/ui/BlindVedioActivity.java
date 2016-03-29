package cn.marco.meizhi.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import cn.marco.meizhi.R;
import cn.marco.meizhi.adapter.BlindVedioAdapter;
import cn.marco.meizhi.domain.Data;
import cn.marco.meizhi.domain.Result;
import cn.marco.meizhi.listener.OnRVItemClickListener;
import cn.marco.meizhi.net.GankApi;
import cn.marco.meizhi.net.GankApiService;
import cn.marco.meizhi.util.Utils;
import cn.marco.meizhi.view.LoadMoreRecyclerView;
import rx.Subscription;

public class BlindVedioActivity extends BaseSwipeBackActivity implements OnRVItemClickListener {

    public static final int MAX_PAGE_SIZE = 10;
    private int mPageNumber = 1;
    private boolean mIsLoading = false;

    private LoadMoreRecyclerView mBlindVedioRecyclerView;
    private BlindVedioAdapter mBlindVedioAdapter;
    private String mType;

    public static Intent getStartIntent(Context context, String type) {
        Intent intent = new Intent(context, BlindVedioActivity.class);
        intent.putExtra(CategoryActivity.KEY_CATEGORY, type);
        return intent;
    }

    @Override
    public void initVariable() {
        mType = getIntent().getStringExtra(CategoryActivity.KEY_CATEGORY);
        if (TextUtils.isEmpty(mType)) {
            mType = GankApiService.TYPE_REST_VEDIO;
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_rest_vedio;
    }

    @Override
    public String getToolbarTitle() {
        return TextUtils.equals(GankApiService.TYPE_REST_VEDIO, mType) ? GankApiService.TYPE_REST_VEDIO : GankApiService.TYPE_RECOMMEND;
    }

    @Override
    public void initViews() {
        super.initViews();
        mBlindVedioRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.recyclerView);
        mBlindVedioRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mBlindVedioRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mBlindVedioAdapter = new BlindVedioAdapter();
        mBlindVedioAdapter.setType(mType);
        mBlindVedioAdapter.setOnItemClickListener(this);
        mBlindVedioRecyclerView.setAdapter(mBlindVedioAdapter);

        mBlindVedioRecyclerView.setOnLoadMoreListener(() -> {
            if (!mIsLoading && mPageNumber < MAX_PAGE_SIZE) {
                mIsLoading = true;
                Subscription subscribe = GankApi.getInstance().getData(mType, ++mPageNumber)
                        .compose(GankApi.getInstance().applySchedule())
                        .subscribe(dataResult -> {
                            mIsLoading = false;
                            mBlindVedioAdapter.addDataSource(dataResult.results);
                        }, onError);
                addSubscription(subscribe);
            } else {
                mBlindVedioAdapter.removeFooterView();
                Utils.showToast(getString(R.string.info_no_more_data));
            }
        });

        tryShowRefresh();
        mSwipeRefreshLayout.setOnRefreshListener(() -> loadData());
    }


    @Override
    public void loadData() {
        this.addSubscription(toSubscribe(mType,
                Data.class,
                GankApi.getInstance().getData(mType),
                data -> {
                    tryStopRefresh();
                    mBlindVedioAdapter.setFooterView(R.layout.view_loading);
                    mBlindVedioAdapter.setDataSource(data.results);
                }));
    }

    @Override
    public void onItemClick(View view, Result result) {
        startActivity(WebViewActivity.getStartIntent(this, result.url, result.desc));
    }
}
