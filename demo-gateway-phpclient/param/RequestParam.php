<?php
namespace param;

class RequestParam{
    public $method;
    public $version;
    public $data;
    public $rand_str;
    public $sign_type;
    public $mch_no;
    public $sign;
    public $sec_key;

    /**
     * @return mixed
     */
    public function getMethod()
    {
        return $this->method;
    }

    /**
     * @param mixed $method
     */
    public function setMethod($method)
    {
        $this->method = $method;
    }

    /**
     * @return mixed
     */
    public function getVersion()
    {
        return $this->version;
    }

    /**
     * @param mixed $version
     */
    public function setVersion($version)
    {
        $this->version = $version;
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
    public function setData($data)
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
    public function setRandStr($rand_str)
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
    public function setSignType($sign_type)
    {
        $this->sign_type = $sign_type;
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
    public function setMchNo($mch_no)
    {
        $this->mch_no = $mch_no;
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
    public function setSign($sign)
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
    public function setSecKey($sec_key)
    {
        $this->sec_key = $sec_key;
    }


}