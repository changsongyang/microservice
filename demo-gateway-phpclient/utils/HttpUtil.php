<?php


namespace utils;


class HttpUtil {
    public static function postJsonSync(string $url, string $jsonData){
        $curl = curl_init();
        //设置抓取的url
        curl_setopt($curl, CURLOPT_URL, $url);
        curl_setopt($curl, CURLOPT_HTTPHEADER, array("Content-type: application/json"));
        //设置需要提交的数据
        curl_setopt($curl, CURLOPT_POSTFIELDS, $jsonData);
        //头文件的信息不当做数据流输出
        curl_setopt($curl, CURLOPT_HEADER, false);
        //设置获取的信息以文件流的形式返回，而不是直接输出。
        curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
        //设置post方式提交
        curl_setopt($curl, CURLOPT_POST, 1);
        //执行命令
        $data = curl_exec($curl);
        curl_close($curl);
        return $data;
    }
}