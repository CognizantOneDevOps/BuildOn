"""
#-------------------------------------------------------------------------------
# Copyright 2018 Cognizant Technology Solutions
# 
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------
"""
import os
import json
import sys
import time
import requests
import re
import datetime
import subprocess
import pg8000

commit_id = (os.environ['commit_id'])
uid = (os.environ['uid'])
pghost = (os.environ['pghost'])
pgport = (os.environ['pgport'])
pgdatabase = (os.environ['pgdatabase'])
pguser = (os.environ['pguser'])
pgupass = (os.environ['pgupass'])
k8s_namespace = (os.environ['k8s_namespace'])
k8s_serviceaccount = (os.environ['k8s_serviceaccount'])
CustomMavenSettings = (os.environ['CustomMavenSettings'])
SettingsXmlPath = (os.environ['SettingsXmlPath'])
logbasepath = (os.environ['logbasepath'])
print ("Inside dbupdate.py")

pod_name = ''
result = os.popen("curl -k -s `(kubectl config view | grep server | cut -f 2- -d \":\" | tr -d \" \")`/api/v1/namespaces/"+k8s_namespace+"/pods --header \"Authorization: Bearer `(kubectl describe secret $(kubectl get secrets | grep "+k8s_serviceaccount+" | cut -f1 -d ' ') | grep -E '^token' | cut -f2 -d':' | tr -d '\t')`\"").read()
resp_dict = json.loads(result)
for i in resp_dict['items']:
	if commit_id in i['metadata']['name']:
        	pod_name = i['metadata']['name']
podip = ''
statusflag = 0
print ('podName: '+ pod_name)
while statusflag < 1:
        doc_cont = os.popen("curl -k -s `(kubectl config view | grep server | cut -f 2- -d \":\" | tr -d \" \")`/api/v1/namespaces/"+k8s_namespace+"/pods/"+pod_name+" --header \"Authorization: Bearer `(kubectl describe secret $(kubectl get secrets | grep "+k8s_serviceaccount+" | cut -f1 -d ' ') | grep -E '^token' | cut -f2 -d':' | tr -d '\t')`\"").read()
        print (doc_cont)
        cont_json = json.loads(doc_cont)
        podstatus = cont_json['status']['phase']
        print (podstatus)
        if "Running" in podstatus:
                print ("inside Running if dbupdate")
                podip = cont_json['status']['podIP']
                print (podip)
                if(CustomMavenSettings == "true" ):
                       subprocess.Popen(['kubectl cp '+SettingsXmlPath+' '+k8s_namespace+'/'+uid+':/usr/share/maven/conf/settings.xml'], shell=True)
                       print ("after kubectl cp")
                statusflag=1

podport = '8080'
failureflag = 'false'
abortflag = 'false'
buildStatusJson = ''
file = open(logbasepath+commit_id+"/Jenkinsfile").read().splitlines()
i = 0
for index, line in enumerate(file):
	line1 = line.replace(" ","")
	if ("stage('" in line1 or "stage ('" in line1) and 'false' in failureflag:
		bool_flag = 0
		cjob = line.split("'")[1]
		inprogress_flag = 'false'
		while (bool_flag < 1):
			now = datetime.datetime.now()
			try:
				jenkinsStream = os.popen("curl -s http://"+ podip +":"+ podport +"/job/buildon-"+commit_id+"/1/wfapi/describe").read()
				buildStatusJson = json.loads(jenkinsStream)
				if cjob in buildStatusJson["stages"][i]["name"]:
					if "IN_PROGRESS" in buildStatusJson["stages"][i]["status"]:
						if 'false' in inprogress_flag:
                                                        con = pg8000.connect(host=pghost, port=int(pgport), user=pguser, password=pgupass, database=pgdatabase)
                                                        cur = con.cursor()
                                                        cur.execute("UPDATE buildon_reports set STATUS='INPROGRESS', DURATION=0 , ENDDATE='"+now.strftime("%Y-%m-%d")+"'"+" WHERE COMMITID =" +"'" +commit_id+"' AND CI_JOBNAME ="+"'" +cjob+"'")
                                                        con.commit()
                                                        con.close()
                                                        inprogress_flag = 'true'
					else:
                                                con = pg8000.connect(host=pghost, port=int(pgport), user=pguser, password=pgupass, database=pgdatabase)
                                                cur = con.cursor()
                                                if "FAILED" in buildStatusJson["stages"][i]["status"]:
                                                        cur.execute("UPDATE buildon_reports set STATUS='FAILURE', DURATION='"+str(buildStatusJson["stages"][i]["durationMillis"])+"' , ENDDATE='"+now.strftime("%Y-%m-%d")+"', END_TIMESTAMP='"+now.strftime("%Y-%m-%d %H:%M:%S")+"', CI_JOB_TIMESTAMP='"+time.strftime('%Y-%m-%d %H:%M:%S',  time.gmtime(int(buildStatusJson["stages"][i]["startTimeMillis"])/1000.))+"' WHERE COMMITID =" +"'" +commit_id+"' AND CI_JOBNAME ="+"'" +cjob+"'")
                                                        failureflag = 'true'
                                                else:
                                                        cur.execute("UPDATE buildon_reports set STATUS='"+buildStatusJson["stages"][i]["status"]+"', DURATION='"+str(buildStatusJson["stages"][i]["durationMillis"])+"' , ENDDATE='"+now.strftime("%Y-%m-%d")+"', END_TIMESTAMP='"+now.strftime("%Y-%m-%d %H:%M:%S")+"', CI_JOB_TIMESTAMP='"+time.strftime('%Y-%m-%d %H:%M:%S',  time.gmtime(int(buildStatusJson["stages"][i]["startTimeMillis"])/1000.))+"' WHERE COMMITID =" +"'" +commit_id+"' AND CI_JOBNAME ="+"'" +cjob+"'")
                                                bool_flag = 1
                                                i = i + 1
                                                con.commit()
                                                con.close()
				else:
                                        i=i+1
			except Exception as e:
				message='print nothing'
