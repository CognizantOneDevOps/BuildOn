import glob
import json
import os
from flask import Flask, render_template, request, redirect, url_for
from buildon import BuildOn
import subprocess
import sys
import string
app = Flask(__name__)
import time
import pg8000
# This is the path to the upload directory
# These are the extension that we are accepting to be uploaded
app.config['ALLOWED_EXTENSIONS'] = set(['log', 'yaml', 'yml', 'json', 'xml', 'txt'])


# For a given file, return whether it's an allowed type or not
def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1] in app.config['ALLOWED_EXTENSIONS']


# This route will show a form to perform an AJAX request
# jQuery is loaded to execute the request and update the
# value of the operation
@app.route('/')
def index():
    return Response('started'),200
# Route that will process the file upload
@app.route('/upload', methods=['POST'])
def upload():
    # Get the name of the uploaded file
    file = request.files['file']
 	
    # Check if the file is one of the allowed types/extensions
    if file and allowed_file(file.filename):
        # Make the filename safe, remove unsupported chars
        filename = file.filename
	file_temp = filename.split(".log")[0]
        commitid = file_temp[-7:]
	print "COMMIT: " +  commitid
	

	##Uplaod folder for Jenkins and Kube logs
	uploadPath="/root/buildlog/"
	if not os.path.exists(uploadPath+commitid):
		os.makedirs(uploadPath+commitid)
        file.save(os.path.join(uploadPath+commitid, filename))
	LOGDIR=uploadPath+commitid
        #resulttemp=subprocess.check_output(['java', '-jar', 'derbyReportsLogUpdate.jar', commitid,LOGDIR])
	con = pg8000.connect(user="postgres", password="postgres") 
	cur = con.cursor()
	cur.execute("UPDATE buildon_reports set LOGDIR='" +LOGDIR+"' WHERE COMMITID ='" +commitid+"'")
	con.commit()
	con.close()
    return 'ok'

@app.route('/setup', methods=['POST'])
def setup():
    print request.data
    build = BuildOn(request.data)
    buildon = build.setup(request.data)
    print buildon
    if (buildon != 0):
	##To forward logs to ElasticSearch. If required enable the below line and copy this script from backup folder
	#pid = subprocess.Popen([sys.executable, "/home/ubuntu/BuildOn/LogsForwarding.py"]) 
	##To update jenkins job's live status in root.reports table
	pid = subprocess.Popen([sys.executable, "/home/ubuntu/BuildOn/dbupdate.py"]) #,stdout=f, stderr=f)
    return "ok"

#app.run(host='0.0.0.0',port=5000, debug='TRUE')
