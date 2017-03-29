package com.vitaliyhtc.lcbo.presenter;

import com.vitaliyhtc.lcbo.interfaces.BaseView;

interface BasePresenter {

    void onAttachView(BaseView baseView);
    void onDetachView();
}
