<?php
/**
 * 自动加载，把需要使用的相关类自动加载到php环境中
 */

spl_autoload_register('autoload');

function autoload(){
    require "exceptions/BizException.php";
    require "param/RequestParam.php";
    require "param/ResponseParam.php";
    require "param/SecretKey.php";
    require "utils/AESUtil.php";
    require "utils/HttpUtil.php";
    require "utils/MD5Util.php";
    require "utils/RandomUtil.php";
    require "utils/RequestUtil.php";
    require "utils/RSAUtil.php";
    require "utils/SignUtil.php";
    require "vo/BatchVo.php";
    require "vo/DetailVo.php";
}