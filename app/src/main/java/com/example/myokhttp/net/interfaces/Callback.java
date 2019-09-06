package com.example.myokhttp.net.interfaces;

import com.example.myokhttp.net.Call;
import com.example.myokhttp.net.Response;

/**
 * Created by luoling on 2019/9/5.
 * description:
 */
public interface Callback {

    void onFailure(Call call, Throwable throwable);

    void onResponse(Call call, Response response);

}
