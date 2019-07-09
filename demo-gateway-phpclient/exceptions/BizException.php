<?php


namespace exceptions;


class BizException extends \Exception {
    public static $PARAM_ERROR = "10000";
    public static $BIZ_ERROR = "20000";

    private $bizCode;
    private $msg;

    function __construct(int $bizCode, string $msg) {
        parent::__construct();
        $this->bizCode = $bizCode;
        $this->msg = $msg;
    }

    /**
     * @return int
     */
    public function getBizCode()
    {
        return $this->bizCode;
    }

    /**
     * @param int $bizCode
     */
    public function setBizCode($bizCode)
    {
        $this->bizCode = $bizCode;
    }

    /**
     * @return string
     */
    public function getMsg()
    {
        return $this->msg;
    }

    /**
     * @param mixed $msg
     */
    public function setMsg($msg)
    {
        $this->msg = $msg;
    }


}