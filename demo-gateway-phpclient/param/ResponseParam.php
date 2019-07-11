<?php
namespace param;

/**
 * 响应参数类
 * Class ResponseParam
 * @package param
 */
class ResponseParam{
    private $resp_code;
    private $biz_code;
    private $biz_msg;
    private $mch_no;
    private $data;
    private $rand_str;
    private $sign_type;
    private $sign;
    private $sec_key;

    /**
     * 魔术方法，通用的setter方法，可以给所有属性赋值
     * @param $name
     * @param $value
     */
    public function __set($name, $value) {
        $this->$name = $value;
    }


    /**
     * @return mixed
     */
    public function getRespCode()
    {
        return $this->resp_code;
    }

    /**
     * @param mixed $resp_code
     */
    public function setRespCode($resp_code): void
    {
        $this->resp_code = $resp_code;
    }

    /**
     * @return mixed
     */
    public function getBizCode()
    {
        return $this->biz_code;
    }

    /**
     * @param mixed $biz_code
     */
    public function setBizCode($biz_code): void
    {
        $this->biz_code = $biz_code;
    }

    /**
     * @return mixed
     */
    public function getBizMsg()
    {
        return $this->biz_msg;
    }

    /**
     * @param mixed $biz_msg
     */
    public function setBizMsg($biz_msg): void
    {
        $this->biz_msg = $biz_msg;
    }

    /**
     * @return mixed
     */
    public function getMchNo()
    {
        return $this->mch_no;
    }

    /**
     * @param mixed $mch_no
     */
    public function setMchNo($mch_no): void
    {
        $this->mch_no = $mch_no;
    }

    /**
     * @return mixed
     */
    public function getData()
    {
        return $this->data;
    }

    /**
     * @param mixed $data
     */
    public function setData($data): void
    {
        $this->data = $data;
    }

    /**
     * @return mixed
     */
    public function getRandStr()
    {
        return $this->rand_str;
    }

    /**
     * @param mixed $rand_str
     */
    public function setRandStr($rand_str): void
    {
        $this->rand_str = $rand_str;
    }

    /**
     * @return mixed
     */
    public function getSignType()
    {
        return $this->sign_type;
    }

    /**
     * @param mixed $sign_type
     */
    public function setSignType($sign_type): void
    {
        $this->sign_type = $sign_type;
    }

    /**
     * @return mixed
     */
    public function getSign()
    {
        return $this->sign;
    }

    /**
     * @param mixed $sign
     */
    public function setSign($sign): void
    {
        $this->sign = $sign;
    }

    /**
     * @return mixed
     */
    public function getSecKey()
    {
        return $this->sec_key;
    }

    /**
     * @param mixed $sec_key
     */
    public function setSecKey($sec_key): void
    {
        $this->sec_key = $sec_key;
    }


}