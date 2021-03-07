Steps to configure the automatic execution of the app

1- We have to create a .sh file in `/usr/local/bin/`, we have named it `events_app_service.sh` with the following content, in which we indicate the name of the service `SERVICE_NAME`, the path to the jar `PATH_TO_JAR` and the pid name `PID_PATH_NAME`:

- Type `sudo touch /usr/local/bin/events_app_service.sh` in the shell to generate the file
- Give it run permission by `sudo chmod +x /usr/local/bin/events_app_service.sh`
- Copy the content in the editor with `sudo nano /usr/local/bin/events_app_service.sh`

```shell
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
```

2- Now we have to create the service in `/etc/systemd/system/` which will execute the `.sh`, we have named it `events_app_service.service` with the following content, in which we indicate the command to execute on `start`, `stop` or `reload`:

- Type `sudo touch /etc/systemd/system/events_app_service.service` in the shell to generate the file
- Copy the content in the editor with `sudo nano /etc/systemd/system/events_app_service.service`

```shell
[Unit]
 Description = API Events service
 After network.target = events_app_service.service
[Service]
 Type = forking
 Restart=always
 RestartSec=1
 SuccessExitStatus=143 
 ExecStart = /usr/local/bin/events_app_service.sh start
 ExecStop = /usr/local/bin/events_app_service.sh stop
 ExecReload = /usr/local/bin/events_app_service.sh reload
[Install]
 WantedBy=multi-user.target
```

3- To enable the startup service we have to type the following command in the shell: `sudo systemctl enable events_app_service.service`