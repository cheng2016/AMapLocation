package com.wecare.app.data.source.remote;


import com.wecare.app.data.entity.GetImageResp;
import com.wecare.app.data.entity.QueryBusinessResp;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by chengzj on 2017/6/18.
 */

public interface HttpApi {
    //http://gank.io/api/day/2016/10/12
    public static final String base_url = "http://sit.wecarelove.com/api/";

    @GET("open/qcode/query")
    Observable<GetImageResp> getImageUrl(@Query("content") String json);

    @GET("open/business/query")
    Observable<QueryBusinessResp> queryBusiness(@Query("content") String json);
}
