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

package com.cognizant.buildon.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognizant.buildon.dao.BuildOnDAO;
import com.cognizant.buildon.domain.Constants;
import com.cognizant.buildon.domain.LDAPAuthentication;
import com.cognizant.buildon.domain.Reports;
import com.cognizant.buildon.domain.ScmDetails;
import com.cognizant.buildon.domain.Service;
import com.cognizant.buildon.domain.Users;

/**
 * @author 338143
 *
 */

public class BuildOnServiceImpl implements BuildOnService {

	private static final Logger logger=LoggerFactory.getLogger(BuildOnServiceImpl.class);	
	private BuildOnDAO buildOnDao=BuildOnFactory.getDAOInstance();

	@Override
	public List<Users> getAuth(String username, String password) {
		return buildOnDao.getAuth(username, password);	
	}

	@Override
	public List<Reports> getresults(Date startdate, Date enddate, String project, String branch,String intiatedBy,String userId) {
		return buildOnDao.getresults(startdate,enddate,project,branch,intiatedBy,userId);	
	}

	@Override
	public boolean saveScmDet(String userId ,String switchval ,String type,String url,String username,String id) {
		return buildOnDao.saveScmDet(userId,switchval,type,url,username,id);
	}

	@Override
	public List<ScmDetails> getScmDetails(String userId ,String type) {
		return buildOnDao.getScmDetails(userId,type);
	}

	@Override
	public boolean removeRecord(String id,String userid) {
		return buildOnDao.removeRecord(id,userid);
	}

	@Override
	public List<String> getPeferenceDetails(String userid, String type) {
		return  buildOnDao.getPeferenceDetails(userid,type);
	}

	@Override
	public boolean savePeferenceDetails(String userid, String switchmode, String repo,String branch) {
		return  buildOnDao.savePeferenceDetails(userid,switchmode,repo,branch);
	}

	@Override
	public JSONObject getHistoricalReports(String userid) {
		return  buildOnDao.getHistoricalReports(userid);
	}

	@Override
	public JSONObject getIndividualReports(String scmuser) {
		Reports report=buildOnDao.getIndividualReports(scmuser);
		JSONObject json=new JSONObject();
		try {	if(null !=report){
			json.put("Project",report.getProject());
			json.put("Branch",report.getBranch());
			json.put("jenkinsfile","Jenkinsfile");
			json.put("Initiatedby",report.getScmuser().toLowerCase());
			json.put("CommitID",report.getCommitid());
			json.put("Beginon",report.getStart_timestamp()+" UTC");
			json.put("JobId",report.getJobname());
			json.put("Estimatedtime",report.getEstimated_duration());
			json.put("Logdir",report.getLogdir());
			json.put("TRIGGER_FROM",report.getTRIGGER_FROM());
			if(null!=report.getStatus() && report.getStatus().equals("NOTSTARTED")){
				json.put(Constants.STATUS,"Initiated");	
			}else if( report.getStatus().equals("INPROGRESS")){
				json.put(Constants.STATUS,"Inprogress");	
			}else if( report.getStatus().equals("SUCCESS")){
				json.put(Constants.STATUS,"Success");	
			}else if( report.getStatus().equals("FAILURE")){
				json.put(Constants.STATUS,"Failure");	
			}else if( report.getStatus().equals("ABORTED")){
				json.put(Constants.STATUS,"Aborted");	
			}

		}
		} catch (JSONException e) {
			logger.debug(e.toString());
		}

		return json;
	}


	@Override
	public JSONObject getIndividualstatusReports(String scmuser, String commitid) {
		JSONObject json=new JSONObject();
		Reports report=buildOnDao.getIndividualstatusReports(scmuser,commitid);
		try {	
			if(null !=report){
				json.put("Project",report.getProject());
				json.put("Branch",report.getBranch());
				json.put("jenkinsfile","Jenkinsfile");
				json.put("Initiatedby",report.getScmuser().toLowerCase());
				json.put("CommitID",report.getCommitid());
				json.put("Beginon",report.getStart_timestamp()+" UTC");
				json.put("JobId",report.getJobname());
				json.put("Estimatedtime",report.getEstimated_duration());
				json.put("status",report.getStatus());
				json.put("Logdir",report.getLogdir());
				json.put("TRIGGER_FROM",report.getTRIGGER_FROM());
			}
		} catch (JSONException e) {
			logger.debug(e.toString());
		}
		return  json;
	}


	@Override
	public String getJsonData(String commitid) {
		return  buildOnDao.getJsonData(commitid);
	}

	@Override
	public String getReportTriggerData(String commitid) {
		return  buildOnDao.getReportTriggerData(commitid);
	}

	@Override
	public String decrypt(String password) {
		String psk =getEncryptkey();
		String iv = getEncryptkey();
		byte[] cipherText= Base64.getDecoder().decode(password);
		String encryptionKey = psk;
		Cipher cipher;
		String decrypted=null;
		try {
			cipher = Cipher.getInstance(Constants.AES_PADDING);
			final SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(Constants.UTF),Constants.AES);
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv.getBytes(Constants.UTF)));
			decrypted = new String(cipher.doFinal(cipherText),Constants.UTF);

		} catch (NoSuchAlgorithmException e) {
			logger.debug(e.toString());
		} catch (NoSuchPaddingException e) {
			logger.debug(e.toString());
		} catch (InvalidKeyException e) {
			logger.debug(e.toString());
		} catch (InvalidAlgorithmParameterException e) {
			logger.debug(e.toString());
		} catch (UnsupportedEncodingException e) {
			logger.debug(e.toString());
		} catch (IllegalBlockSizeException e) {
			logger.debug(e.toString());
		} catch (BadPaddingException e) {
			logger.debug(e.toString());
		}
		return decrypted;
	}

	@Override
	public String encrypt(String value) {
		try {
			String key =getEncryptkey();
			String initVector = getEncryptkey();

			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(Constants.UTF));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(Constants.UTF),Constants.AES);

			Cipher cipher = Cipher.getInstance(Constants.AES_PADDING);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

			byte[] encrypted = cipher.doFinal(value.getBytes());

			String encryptedValue = Base64.getEncoder().encodeToString(encrypted);
			return encryptedValue;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	@Override
	public String getEncryptkey(){
		InetAddress ip = null;
		byte[] mac = null;
		NetworkInterface network = null;
		StringBuilder sb = new StringBuilder();
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			logger.debug(e1.toString());
		}
		try {
			network = NetworkInterface.getByInetAddress(ip);
			mac = network.getHardwareAddress();
		} catch (SocketException e) {
			logger.debug(e.toString());
		}
		for (int i = 0; i < mac.length; i++) {
			sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "" : ""));
		}
		sb.append("0000");
		return sb.toString();
	}

	@Override
	public boolean isNumeric(String value) {
		return value != null && value.matches(Constants.VALID_NUMERIC);
	}

	@Override
	public boolean isAlphaNumeric(String value) {
		return value != null && value.matches(Constants.VALID_ALPHA_NUMERIC);
	}

	@Override
	public boolean isValidUrl(String value) {
		Pattern pattern = Pattern.compile(Constants.VALID_URL);
		Matcher matcher;
		matcher=pattern.matcher(value);
		boolean matches = matcher.matches();
		return matches;
	}

	@Override
	public boolean isValidEmail(String value) {
		Pattern pattern = Pattern.compile(Constants.VALID_EMAIL);
		Matcher matcher;
		matcher=pattern.matcher(value);
		boolean matches = matcher.matches();
		return matches;
	}

	@Override
	public boolean isValidAlpha(String value) {
		Pattern pattern = Pattern.compile(Constants.VALID_ALPHA);
		Matcher matcher;
		matcher=pattern.matcher(value);
		boolean matches = matcher.matches();
		return matches;
	}

	@Override
	public String getCookiesDecrytpedvalue(String globalCookie) {
		String user =decrypt(globalCookie);
		return user;
	}
	
	@Override
	public JSONArray getCompareBuild(String userId) {
		return buildOnDao.getCompareBuild(userId);
	}

	@Override
	public JSONArray getLatestbuild(String userId) {
		return buildOnDao.getLatestbuild(userId);
	}

	@Override
	public JSONArray getProjectwiseBuild(String userId) {
		return buildOnDao.getProjectwiseBuild(userId);
	}

	@Override
	public JSONArray getBuildtrends(String userId) {
		return buildOnDao.getBuildtrends(userId);
	}
	
	
	@Override
	public Users getEmailForUser(String id) {
		Properties props = readPropertyFile();
		boolean isLDAP = Boolean.parseBoolean(props.getProperty("ldap.isLDAP"));
		if(!isLDAP){
		return  buildOnDao.getEmailForUser(id);
		}else{
			Users users=LDAPAuthentication.getEmailForLDAPUser(id);
		return  users;
		}
	}

	/**
	 * @return
	 */
	@Override
	public Properties readPropertyFile() {
		Properties props = new Properties();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream(Constants.PROPERTYFILE);
		try {
			props.load(is);
			is.close();
		} catch (FileNotFoundException e1) {
			logger.debug(e1.toString());
		} catch (IOException e) {
			logger.debug(e.toString());
		}
		return props;
	}
	
	
	/**
	 * @param response
	 * @param cookie
	 */
	@Override
	public void deleteCookies(HttpServletResponse response, Cookie[] cookie) {
		if (cookie != null) {
			for (Cookie cookiedel : cookie) {
				cookiedel.setValue(null);
				cookiedel.setMaxAge(0);
		        response.addCookie(cookiedel);
		  
		    }
		}
	}

	@Override
	public List<ScmDetails> getUserScmDetails(String email, String type) {
		return buildOnDao.getUserScmDetails(email, type);
	}

	@Override
	public boolean getDBServiceInsert(String podIP, String podPort, String podNameValue, String resultJSON,
			String commitid) {
		return buildOnDao.getDBServiceInsert(podIP, podPort,podNameValue, resultJSON,commitid);
	}

	@Override
	public boolean getDBServiceUpdate(String podIP, String podPort, String podNameValue, String resultJSON,
			String commitid) {
		// TODO Auto-generated method stub
		return buildOnDao.getDBServiceUpdate(podIP, podPort,podNameValue, resultJSON,commitid);
	}

	@Override
	public String getServiceCommitId(String commitId) {
		return buildOnDao.getServiceCommitId(commitId);
	}

	@Override
	public String getReportsStatus(String commitId, String cijobname) {
		return buildOnDao.getReportsStatus(commitId,cijobname);
	}

	@Override
	public boolean getHistoricDBServiceInsert(String commitid, String resultJSON) {
		return buildOnDao.getHistoricDBServiceInsert(commitid,resultJSON);
	}

	@Override
	public String getPodname(String commitId) {
		return buildOnDao.getPodname(commitId);
	}

	@Override
	public Service getServiceData(String commitId) {
		return buildOnDao.getServiceData(commitId);
	}

	@Override
	public List<ScmDetails> getHistoricalURL(String email, String repo) {
		return buildOnDao.getHistoricalURL(email,repo);
	}

	

	
}
