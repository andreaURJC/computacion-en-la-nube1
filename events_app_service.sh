#!/bin/sh 
SERVICE_NAME=events_app_service
PATH_TO_JAR=/home/ubuntu/app.jar
PID_PATH_NAME=/tmp/events_app_service-pid 
case $1 in 
start)
       echo "Starting $SERVICE_NAME ..."
  if [ ! -f $PID_PATH_NAME ]; then 
       nohup java -DBUCKET_NAME=andrea-juanma-urjc -DRDS_ENDPOINT=database-events.cjyvoxj9hemh.us-east-1.rds.amazonaws.com -DRDS_DATABASE=events -DRDS_USER=admin -DRDS_PASS=password -Dspring.profiles.active=production -jar $PATH_TO_JAR /tmp 2>> /dev/null >>/dev/null &      
                   echo $! > $PID_PATH_NAME  
       echo "$SERVICE_NAME started ..."         
  else 
       echo "$SERVICE_NAME is already running ..."
  fi
;;
stop)
  if [ -f $PID_PATH_NAME ]; then
         PID=$(cat $PID_PATH_NAME);
         echo "$SERVICE_NAME stoping ..." 
         kill $PID;         
         echo "$SERVICE_NAME stopped ..." 
         rm $PID_PATH_NAME       
  else          
         echo "$SERVICE_NAME is not running ..."   
  fi    
;;    
restart)  
  if [ -f $PID_PATH_NAME ]; then 
      PID=$(cat $PID_PATH_NAME);    
      echo "$SERVICE_NAME stopping ..."; 
      kill $PID;           
      echo "$SERVICE_NAME stopped ...";  
      rm $PID_PATH_NAME     
      echo "$SERVICE_NAME starting ..."  
      nohup java -DBUCKET_NAME=andrea-juanma-urjc -DRDS_ENDPOINT=database-events.cjyvoxj9hemh.us-east-1.rds.amazonaws.com -DRDS_DATABASE=events -DRDS_USER=admin -DRDS_PASS=password -Dspring.profiles.active=production -jar $PATH_TO_JAR /tmp 2>> /dev/null >> /dev/null &            
      echo $! > $PID_PATH_NAME  
      echo "$SERVICE_NAME started ..."    
  else           
      echo "$SERVICE_NAME is not running ..."    
     fi     ;;
 esac