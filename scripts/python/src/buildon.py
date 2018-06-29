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
#!/usr/bin/env python
import json, os, wget , glob
import docker
from docker.errors import DockerException
from pprint import pprint
import subprocess
import pg8000
from io import BytesIO
from docker import APIClient
import datetime
import sys
import re
import string
import shutil
import configparser

class BuildOn(object):
    def __init__(self, data):
        ##Fetch server details and ports from properties file
        print (data)
        data = json.loads(data.decode('utf-8'))
        config = configparser.ConfigParser()
        config.sections()
        config.read('buildon.properties')
        config.sections()
        'server' in config
        self.host_ip=config['server']['kubemaster_ip']
        self.k8s_namespace=config['server']['k8s_namespace']
        self.k8s_serviceaccount=config['server']['k8s_serviceaccount']
        self.upload_port=config['server']['framework_port']
        self.gitlabtoken=config['server']['gitlab_token']
        self.pghost=config['server']['pghost']
        self.pgport=config['server']['pgport']
        self.pgdatabase=config['server']['pgdatabase']
        self.pguser=config['server']['pguser']
        self.pgupass=config['server']['pgupass']
        self.githubtoken=config['server']['githubtoken']        
        self.logbasepath=config['server']['logbasepath']
        self.bitbucket_ip=config['server']['bitbucket_ip']
        self.bitbucket_port=config['server']['bitbucket_port']
        self.bitbucket_token=config['server']['bitbucket_token']
        self.bitbucket_serviceaccount=config['server']['bitbucket_serviceaccount']
        self.CustomMavenSettings=config['server']['CustomMavenSettings']
        self.SettingsXmlPath=config['server']['SettingsXmlPath']

        print ("Printing Payload Data ......")
        print (data)

        ##Extract Payload data from respective Source Code Repository.Here "head_commit" is the key word
        #which differs between GitLab and GitHub and eventKey is to identify BitBucket's Payload content.Using this payload extraction is done.
        if 'eventKey' in data:
                print ("BitBucket Commit")
                self.commitid = format(data['changes'][0]['toHash'])[0:7]
                self.unique = format(data['changes'][0]['ref']['id'])
                self.scmuser = format(data['actor']['emailAddress'])
                self.tuid = self.unique.split("/")[2]
                self.tagid = self.tuid + self.commitid
                self.uid = "buildon-" + self.commitid
                self.repo_name = format(data['repository']['name'])
                self.buildpath = self.logbasepath + self.commitid
                self.webhookcheck = format(data['repository']['scmId'])
                self.projectKey = format(data['repository']['project']['key'])
                self.jenkinsfileURL="http://"+self.bitbucket_ip+":"+self.bitbucket_port+"/projects/"+self.projectKey+"/repos/"+self.repo_name+"/raw/Jenkinsfile"
                self.httpurl='http://'+self.bitbucket_serviceaccount+'@'+self.bitbucket_ip+':'+self.bitbucket_port+'/scm/'+self.projectKey+'/'+self.repo_name+'.git'
                self.SCM = 'bitbucket'
                print ("Printing Paylaod data...***")
                print ("CommitID..."+ self.commitid)
                print ("UID..."+ self.uid)
                print ("Branch..." + self.tuid)
                print ("repo_name..."+ self.repo_name)
                print ("buildpath" + self.buildpath)
                print ("scmuser..."+ self.scmuser)
                print ("webhookcheck..." + self.webhookcheck)
                print ("jenkisnfileURL..." + self.jenkinsfileURL)

        elif  'head_commit' not in data:
                print ("GitLab Commit")
                self.commitid = format(data['commits'][0]['id'])[0:7]
                self.unique = format(data['ref'])
                self.tuid = self.unique.split("/")[2]
                self.tagid = self.tuid + self.commitid
                self.uid = "buildon-" + self.commitid
                self.repo_name = format(data['repository']['name'])
                self.buildpath = self.logbasepath + self.commitid
                self.scmuser = format(data['commits'][0]['author']['email'])
                self.webhookcheck = format(data['repository']['git_http_url'])
                self.httpurl = format(data['project']['http_url'])
                self.repohttpurl = self.httpurl.replace(".git","")
                self.jenkinsfileURL=self.repohttpurl + "/" + "raw" + "/" + self.tuid + "/" + "Jenkinsfile?private_token="+self.gitlabtoken
                self.SCM = 'gitlab'

                print ("Printing Paylaod data...***")
                print ("CommitID..."+ self.commitid)
                print ("UID..."+ self.uid)
                print ("Branch..." + self.tuid)
                print ("repo_name..."+ self.repo_name)
                print ("buildpath" + self.buildpath)
                print ("scmuser..."+ self.scmuser)
                print ("webhookcheck..." + self.webhookcheck)
                print ("httpurl ..." + self.httpurl)
                print ("repohttpurl..." + self.repohttpurl)
                print ("jenkisnfileURL..." + self.jenkinsfileURL)
        else:
                print ("GitHub commit")
                print ("Printing data")
                print (data)
                self.commitid = format(data['commits'][0]['id'])[0:7]
                self.unique = format(data['ref'])
                self.tuid = self.unique.split("/")[2]
                self.tagid = self.tuid + self.commitid
                self.uid = "buildon-" + self.commitid
                self.repo_fullName = format(data['repository']['full_name'])
                self.repo_name = format(data['repository']['name'])
                self.buildpath = self.logbasepath + self.commitid
                self.scmuser = format(data['commits'][0]['author']['email'])
                self.webhookcheck = format(data['repository']['git_url'])
                self.httpurl = format(data['repository']['clone_url'])
                self.repohttpurl = self.httpurl.replace(".git","")
                self.jenkinsfileURL="https://raw.githubusercontent.com/"+self.repo_fullName + "/" + self.tuid + "/" + "Jenkinsfile"
                self.SCM = 'github'
                print ("Printing Paylaod data...***")
                print ("CommitID..."+ self.commitid)
                print ("UID..."+ self.uid)
                print ("Branch..." + self.tuid)
                print ("repo_name..."+ self.repo_name)
                print ("repo_fullname..."+ self.repo_fullName)
                print ("buildpath" + self.buildpath)
                print ("scmuser..."+ self.scmuser)
                print ("webhookcheck..." + self.webhookcheck)
                print ("httpurl ..." + self.httpurl)
                print ("repohttpurl..." + self.repohttpurl)
                print ("jenkisnfileURL..." + self.jenkinsfileURL)
        if (self.SCM == "gitlab"):
                self.scmServiceAccount = self.gitlabtoken
        elif (self.SCM == "bitbucket"):
                self.scmServiceAccount = self.bitbucket_token
        elif (self.SCM == "github"):
                self.scmServiceAccount = self.githubtoken

        os.environ['commit_id'] = self.commitid
        os.environ['uid'] = self.uid
        os.environ['pghost'] = self.pghost
        os.environ['pgport'] = self.pgport
        os.environ['pgdatabase'] = self.pgdatabase
        os.environ['pguser'] = self.pguser
        os.environ['pgupass'] = self.pgupass
        print (self.logbasepath)
        os.environ['logbasepath'] = self.logbasepath
        os.environ['k8s_namespace'] = self.k8s_namespace
        os.environ['k8s_serviceaccount'] = self.k8s_serviceaccount
        os.environ['CustomMavenSettings'] = self.CustomMavenSettings
        os.environ['SettingsXmlPath'] = self.SettingsXmlPath
        print ("environment variable")
        print  (os.environ['pgupass'])
        print (os.environ['logbasepath'])
    def dbinsert(self):
            print ("Inside self.dbinsert")
            now = datetime.datetime.now()
            self.start_timestamp = now.strftime("%Y-%m-%d %H:%M:%S")
            self.start_date = now.strftime("%Y-%m-%d")
            print (self.scmuser)
            print (self.tuid)
            print (self.repo_name)
            result = ''
            try:
                ##Check if WebHook is ON/OFF. If OFF exit the execution.Exit only if commit is from GitLab checkin and Webhook is OFF.
                con = pg8000.connect(host=self.pghost, port=int(self.pgport), user=self.pguser, password=self.pgupass, database=self.pgdatabase)
                cur = con.cursor()
                cur.execute("select webhook from buildon_preferences where email='"+self.scmuser+"' and repository='"+self.repo_name+"'")
                for row in cur:
                    result =  row[0]
            except Exception as e:
                print (e)
                print ('Preference Error')
            finally:
                con.close()
            print ("self.webhookcheck: " + self.webhookcheck)
            if ( result == 0 and self.webhookcheck != "" and 'BOT' not in self.webhookcheck ):  ##Exits without BuildOn flow if webhook is turnedd off when committign code directly to SCM
                print ('WebHook is off')
                return 0

            if ( self.webhookcheck != "" ):
                if ('BOT' in self.webhookcheck):
                        trigger_from="BOT"
                else:
                        trigger_from="FRAMEWORK"
            else:
                trigger_from="UI"
            print (trigger_from)
            print ('trigger_from')
            file = open(self.buildpath+"/Jenkinsfile").read().splitlines()
            try:
                for index, line in enumerate(file):
                        if "stage ('" in line or "stage('" in line:
                                ci_jobname =line.split("'")[1]
                                print (ci_jobname)
                                con = pg8000.connect(host=self.pghost, port=int(self.pgport), user=self.pguser, password=self.pgupass, database=self.pgdatabase)
                                cur = con.cursor()
                                est_dur = ''
                                cur.execute("select jobname from buildon_reports where STATUS='SUCCESS' and BRANCH='"+self.tuid+"' group by jobname;")
                                if len(list(cur)) > 0:
                                        cur.execute("select avg(d) as DURATION from (select sum(DURATION) as d from buildon_reports where STATUS='SUCCESS' and BRANCH='"+self.tuid+"' group by jobname) as tmp;")
                                        for row in cur:
                                                minutes = int(row[0]/ 60000)
                                                minutes1 = row[0] % 60000
                                                seconds = int(minutes1 / 1000)
                                                est_dur = str(minutes) + "Mins " + str(seconds) + "Sec"
                                cur.execute("insert into buildon_reports (BRANCH,COMMITID,JOBNAME,PROJECT,SCMUSER,STARTDATE,START_TIMESTAMP,CI_JOBNAME,STATUS,ESTIMATED_DURATION,TRIGGER_FROM,LOGDIR)  values ('"+self.tuid+"','"+self.commitid+"','"+self.uid+"','"+self.repo_name+"','"+self.scmuser+"','"+self.start_date+"','"+self.start_timestamp+"','"+ci_jobname+"','NOTSTARTED','"+est_dur+"','"+trigger_from+"','"+self.buildpath+"')")
                                con.commit()
                                con.close()
            except Exception as e:
                print ('Insert Error Occurs')
                print (e)

    def setup(self, data):
        if not os.path.exists(self.buildpath):
            os.makedirs(self.buildpath)
            try:

                if ('github' in self.SCM or 'gitlab' in self.SCM):
                        print ("BitBucket Jenkinsfile")
                        wget.download(self.jenkinsfileURL, out=self.buildpath+"/Jenkinsfile")
                else:
                        print ("BitBucket Jenkinsfile")
                        os.system('cd  '+self.buildpath +'; curl -O -G -H "Authorization: Bearer MTM1MzMwNzI5NjIyOkG16lNX7T2F/ihcShuLIISkKc2P" '+ self.jenkinsfileURL +' -d at='+self.unique)
            except Exception as e:
                        print ("Error in downloading files from SCM - Jenkinsfile or fetch image from Jenkinsfile")
            try:
                 file = open(self.buildpath+"/Jenkinsfile").read().splitlines()
                 for index, line in enumerate(file):
                        if "env.dockerimagename" in line:
                                line_temp = line
                                imagename_temp=line_temp.split("=")[1]
                                imagename=imagename_temp.strip()
                                print ("Docker Image Name: " + imagename)
            except Exception as e:
                print (e)
            try:
                ##DB insert function call
                hook = self.dbinsert()
                if ( hook == 0 ):
                    return 0
                #subprocess.Popen(['kubectl run '+ self.uid +' -i --tty --rm --restart=Never --image='+imagename+ ' --env="commitID='+self.uid+',branchName='+self.tuid+',gitURL='+self.httpurl+',KUBEMASTER='+self.host_ip+',UPLOADPORT='+self.upload_port+'"'], shell=True)
                subprocess.Popen(['kubectl run '+ self.uid +' -i --tty --rm --restart=Never --image='+imagename+ ' --env="commitID='+self.uid+'" --env="branchName='+self.tuid+'" --env="gitURL='+self.httpurl+'" --env="KUBEMASTER='+self.host_ip+'" --env="UPLOADPORT='+self.upload_port+'" --env="scmServiceAccount='+self.scmServiceAccount+'"'], shell=True)
            except NameError as ne:
                print (ne)
                raise BuildOn(
                    "Error in replacing <appndid> in Jenkinsfile"
                )

    @staticmethod
    def replacestring (filepath, orgstring, replacestring):
        if os.path.isfile(filepath):
            filehandle = open(filepath, "r+")
            lines = filehandle.read()
            lines = lines.replace(orgstring, replacestring)
            filehandle.close()
            filehandle = open(filepath, "w")
            filehandle.write(lines)
            filehandle.close()
        else:
            print('File not found{}'.filepath)