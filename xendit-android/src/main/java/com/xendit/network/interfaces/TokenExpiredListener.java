package com.xendit.network.interfaces;


import com.xendit.network.BaseRequest;

public interface TokenExpiredListener {
    void onTokenExpired(BaseRequest request);
}