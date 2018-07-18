package com.yellowcong.cas;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
// 解决请求https时出现No subject alternative DNS name matching cas.example.org found的SSL错误
public class CustomizedHostnameVerifier implements HostnameVerifier
{

    @Override
    public boolean verify(String arg0, SSLSession arg1)
    {
        return true;
    }


}

