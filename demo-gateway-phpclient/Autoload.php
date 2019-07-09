<?php
spl_autoload_register('autoload');

function autoload(){
    require "exceptions/BizException.php";
    require "param/RequestParam.php";
    require "param/ResponseParam.php";
    require "param/SecretKey.php";
    require "utils/HttpUtil.php";
    require "utils/RandomUtil.php";
    require "utils/RequestUtil.php";
    require "utils/RSAUtil.php";
    require "utils/SignUtil.php";
    require "vo/BatchVo.php";
    require "vo/DetailVo.php";
}