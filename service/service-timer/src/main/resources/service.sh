#!/bin/sh
#linux下启动jar应用的通用脚本，

###################################
#引入环境变量，使SSH登陆执行脚本的用户也可以使用到环境变量，否则会出现command not found，class not found等异常，导致无法正常关闭应用
###################################
source /etc/profile
source ~/.bash_profile

###################################
#初始化全局变量
###################################
#执行程序启动所使用的系统用户，考虑到安全，推荐不使用root帐号
RUNNING_USER=study
#当前日期
DATE=$(date +%Y%m%d)
#当前目录
APP_HOME=$(cd "$(dirname "$0")"; pwd)
#应用名称，在SpringBoot的maven插件打包时会被替换成pom中设置的值，如果不是放在maven项目中一起打包就需要手动修改此值
APP_NAME=@appName@
APP_FULL_NAME=$APP_HOME/$APP_NAME.jar
#日志文件路径
APP_LOG_PATH=$APP_HOME/log
#java虚拟机启动参数
JAVA_OPTS="-Xms256m -Xmx512m -Xmn128m -Dspath=$APP_FULL_NAME -XX:ErrorFile=./java_err_pid.log"
#当前应用的进程id
PS_ID=0
#当前停止次数
CUR_STOP_TIMES=0
#最大停止次数
MAX_STOP_TIMES=5
#停止时的休眠时间(秒)
STOP_SLEEP_SECOND=3

###################################
#(函数)判断程序是否已启动
#
#说明：
#使用JDK自带的JPS命令及grep命令组合，准确查找pid
#jps 加 l 参数，表示显示java的完整包路径
#使用awk，分割出pid ($1部分)，及Java程序名称($2部分)
###################################
checkPid() {
   javaps=`jps -lv | grep spath=$APP_FULL_NAME`

   # -n 表示检测字符串长度是否为0，不为0返回 true
   if [ -n "$javaps" ]; then
      PS_ID=`echo $javaps | awk '{print $1}'`
   else
      PS_ID=0
   fi
}

###################################
#(函数)启动程序
#
#说明：
#1. 首先调用checkPid函数，刷新$PS_ID全局变量
#2. 如果程序已经启动（$PS_ID不等于0），则提示程序已启动
#3. 如果程序没有被启动，则执行启动命令行
#4. 启动命令执行后休眠4秒，然后再次调用checkPid函数
#5. 如果步骤4的结果能够确认程序的pid,则打印[OK]，否则打印[Failed]
#注意: "nohup 某命令 > /dev/null 2>&1 &" 的用法
###################################
start() {
   checkPid

   if [ $PS_ID -ne 0 ]; then
      echo "================================"
      echo "Warn: $APP_NAME Already Started! (pid=$PS_ID)"
      echo "================================"
   else
      echo -n "Starting $APP_NAME ..."
      nohup java $JAVA_OPTS -jar $APP_FULL_NAME > /dev/null 2>&1 &
      checkPid
      if [ $PS_ID -ne 0 ]; then
         echo "(pid=$PS_ID) [OK]"
      else
         sleep 4
         checkPid
         if [ $PS_ID -ne 0 ]; then
            echo "(pid=$PS_ID) [OK]"
         else
             echo "[Failed]"
             exit 1
         fi
      fi
   fi
}

###################################
#(函数)停止程序(支持优雅停机)
#
#说明：
#1. 首先调用checkPid函数，刷新$PS_ID全局变量
#2. 如果程序还未启动（$PS_ID等于0），则提示程序未运行
#3. 如果程序已启动，则执行 kill $PS_ID 命令来杀死进程(这样就可支持java应用的优雅宕机)
#4. 执行kill命令之后，休眠一段时间
#5. 再次调用checkPid函数，如果$PS_ID为0，则提示关闭成功，如果$PS_ID还不为0，则递归调用本方法，直到到达最大次数为止
###################################
stop() {
   checkPid

   if [ $PS_ID -ne 0 ]; then
      let CUR_STOP_TIMES+=1
      echo "Stopping $APP_NAME ...(pid=$PS_ID) $CUR_STOP_TIMES Times"
      kill $PS_ID

      #休眠一定时间，让服务器有时间来停止
      echo "Killing Process(pid=$PS_ID), Please Wait......"
      sleep $STOP_SLEEP_SECOND

       checkPid
       if [ $PS_ID -ne 0 ]; then
           if [ $CUR_STOP_TIMES -ge $MAX_STOP_TIMES ]; then
               echo "Fail To Kill Application $CUR_STOP_TIMES Times, [Kill Failed]"
               exit 1
           else
               stop
           fi
       else
           echo "[Kill Success]"
       fi
   else
      echo "================================"
      echo "Warn: $APP_NAME Is Not Running"
      echo "================================"
   fi
}

###################################
#(函数)强制停止程序(优雅停机优先，后再强制中止)
#
#说明：
#1. 首先调用checkPid函数，刷新$PS_ID全局变量
#2. 如果程序还未启动（$PS_ID等于0），则提示程序未运行
#3. 如果程序已启动，则执行 kill $PS_ID 命令来杀死进程(这样就可支持java应用的优雅宕机)
#4. 执行kill命令之后，休眠一段时间
#5. 再次调用checkPid函数，如果$PS_ID为0，则提示关闭成功，如果$PS_ID还不为0，则递归调用本方法，当到达最大重试次数时，使用 kill -9 命令强制中止进程
###################################
stopF() {
   checkPid

   if [ $PS_ID -ne 0 ]; then
      let CUR_STOP_TIMES+=1
      echo "Stopping $APP_NAME ...(pid=$PS_ID) $CUR_STOP_TIMES Times"
      if [ $CUR_STOP_TIMES -ge $MAX_STOP_TIMES ]; then
          kill -9 $PS_ID
      else
          kill $PS_ID
      fi

      #休眠一定时间，让服务器有时间来停止进程
      echo "Killing Process(pid=$PS_ID), Please Wait......"
      sleep $STOP_SLEEP_SECOND

       checkPid
       if [ $PS_ID -ne 0 ]; then
           if [ $CUR_STOP_TIMES -gt $MAX_STOP_TIMES ]; then
               echo "Fail To Force Kill Application $CUR_STOP_TIMES Times, [Force Kill Failed]"
               exit 1
           else
               stopF
           fi
       else
           echo "[Kill Success]"
       fi
   else
      echo "================================"
      echo "Warn: $APP_NAME Is Not Running"
      echo "================================"
   fi
}

###################################
#(函数)检查程序运行状态
#
#说明：
#1. 首先调用checkPid函数，刷新$PS_ID全局变量
#2. 如果程序已经启动（$PS_ID不等于0），则提示正在运行并打印pid
#3. 否则，提示程序未运行
###################################
status() {
   checkPid

   if [ $PS_ID -ne 0 ];  then
      echo "$APP_NAME Is Running! (pid=$PS_ID)"
   else
      echo "$APP_NAME Is Not Running"
   fi
}

###################################
#(函数)打印系统环境参数
###################################
info() {
   echo "System Information:"
   echo "****************************"
   echo `head -n 1 /etc/issue`
   echo `uname -a`
   echo
   echo `java -version`
   echo
   echo "APP_HOME=$APP_HOME"
   echo "APP_NAME=$APP_NAME"
   echo "****************************"
}


###################################
#读取脚本的第一个参数($1)，进行判断
#参数取值范围：{start|stop|stopF|restart|restartF|status|info}
#如参数不在指定范围之内，则打印帮助信息
###################################
case "$1" in
   'start')
      start
      ;;
   'stop')
     stop
     ;;
   'stopF')
     stopF
     ;;
   'restart')
     stop
     start
     ;;
   'restartF')
     stopF
     start
     ;;
   'status')
     status
     ;;
   'info')
     info
     ;;
  *)
     echo "Usage: $0 {start|stop|stopF|restart|restartF|status|info}"
     exit 1
esac
exit 0
