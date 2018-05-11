/*******************************************************************************
* Copyright 2018 Cognizant Technology Solutions
*  
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not
*  use this file except in compliance with the License.  You may obtain a copy
*  of the License at
*  
*    http://www.apache.org/licenses/LICENSE-2.0
*  
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
*  License for the specific language governing permissions and limitations under
*  the License.
 ******************************************************************************/

package com.cognizant.buildon.domain;

/**
 * @author 338143
 *
 */

public interface Constants {


	String SUCCESS    ="SUCCESS";


	String FAILURE    ="FAILURE";

	String INPROGRESS ="INPROGRESS";

	String NOTSTARTED ="NOTSTARTED";

	String ABORTED="ABORTED";

	String STATUS="status";


	String YOU="you";

	String OTHERS="others";


	String SUCCESSSTATUS="Success";

	String FAILURESTATUS="Failure";

	String ABORTEDSTATUS="Aborted";

	String INPROGRESSSTATUS ="Inprogress";

	String INITIATEDSTATUS ="Initiated";

	String JENKINSFILE="Jenkinsfile";

	String  LOCALPATH="localPath";

	String  PROJECT="project";

	String PROPERTYFILE="buildon.properties";

	String DEFAULTPOD = "default";


	String YOU_SUCCESS = "you_success";

	String YOU_FAILURE = "you_failure";

	String YOU_ABORTED = "you_aborted";


	String OTHERS_SUCCESS = "others_success";

	String OTHERS_FAILURE = "others_failure";

	String OTHERS_ABORTED = "others_aborted";


	String COLOR = "color";

	String BUILD_STATUS = "build_status";

	String VALUE = "value";

	String PROJECTS = "Projects";


	String JOBNAME = "jobname";

	String GIT = "git";

	String LDAP_FILTERS_NAME = "objectClass=user";

	String LDAP_FILTERS_ACC = "sAMAccountName";

	String LDAP_SID = "objectSID";

	String LDAP_SID_BIN = "java.naming.ldap.attributes.binary";

	String SIMPLE = "simple";

	String LDAP_CTX_FAC = "com.sun.jndi.ldap.LdapCtxFactory";

	String LDAP_GN_NAME = "givenname";

	String LDAP_MAIL = "mail";

	String LDAP_NAME = "name";

	String LOG_RESULT = "No Results";

	String NO_JENKINS = "No Jenkinsfile";

	String OAUTH = "oauth2";

	String DOT =".";

	String FWD_SLASH ="/";

	String GIT_UPLOAD ="/usr/bin/git-upload-pack";

	String JENKINS_FILE ="Jenkinsfile";

	String FILE ="file";

	String HEAD ="HEAD";

	String JENKINS_MOD_MSG ="Jenkinsfile modified";

	String STAGE ="stage";

	String LOG_TAB ="/root/buildlog/";

	String LOG =".log";

	String KUBE ="kube-";

	String JENKINS ="jenkins-";

	String JOB ="job";

	String HTTP ="http://";

	String CI_URL ="/lastBuild/consoleText";

	String BUILDON ="buildon-";

	String SERV_URL ="/1/wfapi/describe";

	String INVALID ="invalid";

	String GLB_USER ="globaluser";

	String MINS ="Mins ";

	String SEC ="Sec";

	String LDAP_CONCAT ="@CTS";

	String ACCOUNT_NAME ="sAMAccountName";

	String SEARCH_FILTER_MAIL = "(&(objectClass=user)(mail=";

	String SEARCH_FILTER_ACCOUNT = "(&(objectClass=user)(sAMAccountName=";

	String AES_PADDING = "AES/CBC/PKCS5Padding";

	String UTF = "UTF-8";

	String AES = "AES";

	String VALID_URL ="^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

	String VALID_ALPHA ="^[a-zA-Z]+$";

	String VALID_EMAIL ="^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})";

	String VALID_NUMERIC ="[0-9]+";

	String VALID_ALPHA_NUMERIC ="[A-Za-z0-9]+";

	String NO_USER ="noLDAPUser";

	String  SERACH_ACCOUNT="(&(objectClass=user)(sAMAccountName=";

	String  MEMBER_OF="memberOf";
	
	String FIRST="first";





}
