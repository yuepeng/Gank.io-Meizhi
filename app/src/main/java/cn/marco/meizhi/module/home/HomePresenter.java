package cn.marco.meizhi.module.home;

import android.text.TextUtils;
import android.view.View;
import java.util.List;

import cn.marco.meizhi.C;
import cn.marco.meizhi.data.entry.Result;
import cn.marco.meizhi.data.source.GankRepository;
import cn.marco.meizhi.module.BaseAbstractPresenter;
import rx.Subscription;

public class HomePresenter extends BaseAbstractPresenter implements HomeContract.Presenter {

    private HomeContract.HomeView mHomeView;
    private GankRepository mRepository;

    public HomePresenter(HomeContract.HomeView homeView, GankRepository repository) {
        this.mHomeView = homeView;
        this.mRepository = repository;
    }

    @Override public void start() {
        this.loadData();
    }



    @Override public void refresh() {
        this.loadData();
    }

    private void loadData() {
        this.mHomeView.startLoading();
        Subscription subscribe = mRepository.getDailyResults().subscribe(this::onSuccess, this::onError);
        this.addSubscription(subscribe);
    }

    @Override public void onItemClick(View view, Result result) {
        if(TextUtils.equals(result.type, C.category.welfare)){
            this.mHomeView.gotoBeauty(view, result);
        }
        else {
            this.mHomeView.gotoGankDetail(result);
        }
    }

    private void onSuccess(List<Result> results) {
        this.mHomeView.finishLoading();
        this.mHomeView.displayResults(results);
    }

    private void onError(Throwable error) {
        this.mHomeView.finishLoading();
        this.mHomeView.displayError(error.getMessage());
    }

    @Override public void destroy() {
        destroyAllSubscription();
    }

}
