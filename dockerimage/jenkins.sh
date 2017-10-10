#! /bin/bash

set -e

##line1 is gitLab code.cognizant.com private token of a user
line1=`echo "echo <<Your Gitlab User Token>>" > /opt/git-askpassword.sh`
line2=`chmod +x /opt/git-askpassword.sh`
##export to avoid password prompt during git checkout for internal/private repo. 
##for public repo this will not be used
export GIT_ASKPASS=/opt/git-askpassword.sh
echo $GIT_ASKPASS
##Parse only the commitID
commitid=`echo ${commitID:${#commitID} - 7}`
#*******************************
echo "Git URL:" 
echo $gitURL
echo "Branch name:"
echo $branchName
echo "CommitID:"
echo $commitID
#*******************************
##Giving permission to jenkinsfileJob folder and Renaming jenkins job folder as commitID
chmod -R 777 /var/jenkins/jobs/jenkinsfileJob
mv /var/jenkins/jobs/jenkinsfileJob /var/jenkins/jobs/$commitID

##Replace GitURL ,commitID and branchName in config.xml

sed -i 's|<gitURL>|'$gitURL'|g' /var/jenkins/jobs/$commitID/config.xml
sed -i 's|<branchName>|'$branchName'|g' /var/jenkins/jobs/$commitID/config.xml
sed -i 's|<commitID>|'$commitID'|g' /var/jenkins/jobs/$commitID/config.xml

##Extract jenkins.war
exec java $JAVA_OPTS -jar /opt/jenkins.war &
#Wait until jenkins is fully up and running
while [[ $(curl -s -w "%{http_code}" http://localhost:8080 -o /dev/null) != "200" ]]; do
	continue
done 	
	
##Check for job to get finished
folder="/var/jenkins/jobs"
##Wait till job is triggered
while [ ! -f $folder/$commitID/builds/1/log ]
do
	continue
done
until  ( grep "Finished: FAILURE"  "$folder/$commitID/builds/1/log"  || grep "Finished: SUCCESS" "$folder/$commitID/builds/1/log" )
do
	continue   #continue is mandate to loop over till jenkins jobs finishes with status SUCCESS OR FAILURE
done
if grep -q "Finished: SUCCESS" $folder/$commitID/builds/1/log
then
	echo "SUCCESS"
        cat "$folder/$commitID/builds/1/log" >> "$folder/jenkins-$commitid.log"
elif grep -q "Finished: FAILURE" $folder/$commitID/builds/1/log
then
	echo "FAILURE"
        cat "$folder/$commitID/builds/1/log" >> "$folder/jenkins-$commitid.log"
        jenkinsLog=`curl --request POST --header "content-type: multipart/form-data" http://$KUBEMASTER:$UPLOADPORT/upload --form file=@$folder/jenkins-$commitid.log`
        kubeLogFetch=`curl -k https://$KUBEMASTER:6443/api/v1/namespaces/default/pods/$commitID/log > $folder/kube-$commitid.log > $folder/kube-$commitid.log`
        kubeLogShip=`curl --request POST --header "content-type: multipart/form-data" http://$KUBEMASTER:$UPLOADPORT/upload --form file=@$folder/kube-$commitid.log`
        echo "Shutting Down"
        exit 1
fi

##Ship Jenkins Job Logs
echo "Shipping jenkins and kube logs to KubeMaster"
jenkinsLog=`curl --request POST --header "content-type: multipart/form-data" http://$KUBEMASTER:$UPLOADPORT/upload --form file=@$folder/jenkins-$commitid.log`
kubeLogFetch=`curl -k https://$KUBEMASTER:6443/api/v1/namespaces/default/pods/$commitID/log > $folder/kube-$commitid.log`
kubeLogShip=`curl --request POST --header "content-type: multipart/form-data" http://$KUBEMASTER:$UPLOADPORT/upload --form file=@$folder/kube-$commitid.log`
echo "Shutting Down"
exec "$@"
