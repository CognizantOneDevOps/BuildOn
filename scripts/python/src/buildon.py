#!/usr/bin/env python
import json, os, wget , glob
import docker
from docker.errors import DockerException
from pprint import pprint
import subprocess
#from scalr.client import ScalrApiClient
#import scalr
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
        data = json.loads(data)
        config = configparser.ConfigParser()
        config.sections()
        config.read('buildon.properties')
        config.sections()
        'server' in config
        self.host_ip=config['server']['kubemaster_ip']
        self.upload_port=config['server']['framework_port']
        self.gitlabtoken=config['server']['serviceaccount']
        print "Printing Payload Data ......"
        print data

        ##Extract Payload data from respective Source Code Repository.Here "head_commit" is the key word
        ##which differs between GitLab and GitHub Payload content.Using this payload extraction is done.
        if  'head_commit' not in data:
                print "GitLab Commit"
                self.commitid = format(data['commits'][0]['id'])[0:7]
                self.unique = format(data['ref'])
                self.tuid = self.unique.split("/")[2]
                self.tagid = self.tuid + self.commitid
                self.uid = "buildon-" + self.commitid
                self.repo_name = format(data['repository']['name'])
                self.buildpath = '/root/buildlog/'+ self.commitid
                self.scmuser = format(data['commits'][0]['author']['email'])
                self.webhookcheck = format(data['repository']['git_http_url'])
                self.httpurl = format(data['project']['http_url'])
                self.repohttpurl = self.httpurl.replace(".git","")
                self.jenkinsfileURL=self.repohttpurl + "/" + "raw" + "/" + self.tuid + "/" + "Jenkinsfile?private_token="+self.gitlabtoken

                print "Printing Paylaod data...***"
                print "CommitID..."+ self.commitid
                print "UID..."+ self.uid
                print "Branch..." + self.tuid
                print "repo_name..."+ self.repo_name
                print "buildpath" + self.buildpath
                print "scmuser..."+ self.scmuser
                print "webhookcheck..." + self.webhookcheck
                print "httpurl ..." + self.httpurl
                print "repohttpurl..." + self.repohttpurl
                print "jenkisnfileURL..." + self.jenkinsfileURL
        else:
                print "GitHub commit"
                print "Printing data"
                print data
                self.commitid = format(data['commits'][0]['id'])[0:7]
                self.unique = format(data['ref'])
                self.tuid = self.unique.split("/")[2]
                self.tagid = self.tuid + self.commitid
                self.uid = "buildon-" + self.commitid
                self.repo_fullName = format(data['repository']['full_name'])
                self.repo_name = format(data['repository']['name'])
                self.buildpath = '/root/buildlog/'+ self.commitid
                self.scmuser = format(data['commits'][0]['author']['email'])
                self.webhookcheck = format(data['repository']['git_url'])
                self.httpurl = format(data['repository']['clone_url'])
                self.repohttpurl = self.httpurl.replace(".git","")
                self.jenkinsfileURL="https://raw.githubusercontent.com/"+self.repo_fullName + "/" + self.tuid + "/" + "Jenkinsfile"

                print "Printing Paylaod data...***"
                print "CommitID..."+ self.commitid
                print "UID..."+ self.uid
                print "Branch..." + self.tuid
                print "repo_name..."+ self.repo_name
                print "repo_fullname..."+ self.repo_fullName
                print "buildpath" + self.buildpath
                print "scmuser..."+ self.scmuser
                print "webhookcheck..." + self.webhookcheck
                print "httpurl ..." + self.httpurl
                print "repohttpurl..." + self.repohttpurl
                print "jenkisnfileURL..." + self.jenkinsfileURL

	os.environ['commit_id'] = self.commitid
        os.environ['repoName'] = self.repo_name
        os.environ['branchName'] = self.tuid

    def dbinsert(self):
	    print "Inside self.dbinsert"
            now = datetime.datetime.now()
            self.start_timestamp = now.strftime("%Y-%m-%d %H:%M:%S")
            self.start_date = now.strftime("%Y-%m-%d")
	    print self.scmuser
            print self.tuid
            print self.repo_name
	    result = ''
            try:
	    	##Check if WebHook is ON/OFF. If OFF exit the execution.Exit only if commit is from GitLab checkin and Webhook is OFF.
	    	con = pg8000.connect(user="postgres", password="postgres")
	    	cur = con.cursor()
	    	cur.execute("select webhook from buildon_preferences where email='"+self.scmuser+"' and REPOSITORY='"+self.repo_name+"'")
	    	for row in cur:
		    result =  row[0]
	    except Exception as e:
		print e
		print 'Preference Error'
	    finally:
	    	con.close()
	    print "self.webhookcheck: " + self.webhookcheck
            if ( result == 0 and self.webhookcheck != "" and 'BOT' not in self.webhookcheck ):  ##Exits without BuildOn flow if webhook is turnedd off when committign code directly to SCM
		print 'WebHook is off'
                return 0

            if ( self.webhookcheck != "" ):
            	if ('BOT' in self.webhookcheck):
			trigger_from="BOT"
	        else:
			trigger_from="FRAMEWORK" 
	    else:
		trigger_from="UI"
	    print trigger_from
	    print 'trigger_from'
            file = open(self.buildpath+"/Jenkinsfile").read().splitlines()
            try:
                for index, line in enumerate(file):
			if "stage ('" in line or "stage('" in line:
				ci_jobname =line.split("'")[1]
                                print ci_jobname
				con = pg8000.connect(user="postgres", password="postgres")
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
		print 'Insert Error Occurs'
                print e

    def setup(self, data):
        if not os.path.exists(self.buildpath):
            os.makedirs(self.buildpath)
            try:
                        wget.download(self.jenkinsfileURL, out=self.buildpath+"/Jenkinsfile")
            except Exception as e:
                        print "Error in downloading files from SCM - Jenkinsfile or fetch image from Jenkinsfile"
	    try:
                 file = open(self.buildpath+"/Jenkinsfile").read().splitlines()
                 for index, line in enumerate(file):
                        if "env.dockerimagename" in line:
                                line_temp = line
                                imagename_temp=line_temp.split("=")[1]
                                imagename=imagename_temp.strip()
                                print "Docker Image Name: " + imagename
	    except Exception as e:
               	print e
            try:
		##DB insert function call 
		hook = self.dbinsert()
                if ( hook == 0 ):
                    return 0
		##uncomment the below line for kubernetes version < = 1.7.x. Please note the change in --env
                ##subprocess.Popen(['kubectl run '+ self.uid +' -i --tty --rm --restart=Never --image='+imagename+ ' --env="commitID='+self.uid+',branchName='+self.tuid+',gitURL='+self.httpurl+',KUBEMASTER='+self.host_ip+',UPLOADPORT='+self.upload_port+'"'], shell=True)		
 		##Below line supports kubernetes v1.8.x. Please note the change in --env
		subprocess.Popen(['kubectl run '+ self.uid +' -i --tty --rm --restart=Never --image='+imagename+ ' --env="commitID='+self.uid+'" --env="branchName='+self.tuid+'" --env="gitURL='+self.httpurl+'" --env="KUBEMASTER='+self.host_ip+'" --env="UPLOADPORT='+self.upload_port+'"'], shell=True) 
		
            except NameError as ne:
		print ne
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
