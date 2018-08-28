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

package com.cognizant.buildon.dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.commons.dbcp.BasicDataSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognizant.buildon.domain.Constants;
import com.cognizant.buildon.domain.Preferences;
import com.cognizant.buildon.domain.Reports;
import com.cognizant.buildon.domain.ScmDetails;
import com.cognizant.buildon.domain.Service;
import com.cognizant.buildon.domain.Users;

/**
 * @author 338143
 *
 */

public class BuildOnDAOImpl  implements BuildOnDAO  {

	private static final String PERSISTENCE_UNIT_NAME = "persistence-unit";
	private static EntityManagerFactory factory=Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
	private static final Logger logger=LoggerFactory.getLogger(BuildOnDAOImpl.class);


	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getAuth(java.lang.String, java.lang.String)
	 */
	@Override
	public List<Users>  getAuth(String email, String password) {
		logger.debug("Authentication=>"+email+password);
		String username=email.toLowerCase();
		EntityManager em = factory.createEntityManager();
		Query query = em.createQuery("select usr from  Users  usr where usr.email=:uname and usr.upass=:upass");
		query.setParameter("uname", username);
		query.setParameter("upass", password);
		List<Users> userinfo = query.getResultList();
		em.close();
		return userinfo;
	}

	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getEmailForUser(java.lang.String)
	 */
	@Override
	public Users  getEmailForUser(String sid) {
		logger.debug("getEmailForUser: "+sid);
		int id=Integer.parseInt(sid);
		EntityManager em = factory.createEntityManager();
		Query query = em.createQuery("select usr from Users usr where usr.id= :id");
		query.setParameter("id",id);
		List<Users> userinfo = query.getResultList();
		em.close();
		if(!userinfo.isEmpty()){
			return userinfo.get(0);
		}else{
			return null;	
		}
	}


	/* (non-Javadoc)
	 * @see com.dao.BuildOnDAO#getresults(java.util.Date, java.util.Date, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<Reports> getresults(Date startdate, Date enddate, String project, String branch,String intiatedBy,String email) {
		List<Reports> reports =new ArrayList<>();
		String commitidChk="";
		String userId=email.toLowerCase();
		ArrayList<String> list=new ArrayList<>();
		if(intiatedBy.equalsIgnoreCase("self")){
			String sql=" (select   JOBNAME,STATUS,BRANCH,PROJECT,COMMITID,SUM(DURATION) as DURATION,TRIGGER_FROM,start_timestamp,SCMUSER  from buildon_reports  "
					+ " where  SCMUSER=?   and ( startDate>=?  and startDate<=? ) or endDate=? or  PROJECT=? or  BRANCH=? "
					+ " group by JOBNAME,STATUS,BRANCH,PROJECT,COMMITID,TRIGGER_FROM,start_timestamp,SCMUSER ) order by start_timestamp,COMMITID  desc";

			try(Connection con=createConnection();PreparedStatement statement=con.prepareStatement(sql)) {
				statement.setString(1,userId);
				statement.setDate(2,new java.sql.Date(startdate.getTime()));
				statement.setDate(3,new java.sql.Date(enddate.getTime()));
				statement.setDate(4,new java.sql.Date(enddate.getTime()));
				if (project!= null) {
					statement.setString(5,project);
				} else {
					statement.setNull(5, java.sql.Types.VARCHAR);
				}
				if (branch!= null) {
					statement.setString(6,branch);
				} else {
					statement.setNull(6, java.sql.Types.VARCHAR);
				}
				logger.info("sql"+sql);
				Reports reporttemp=new Reports();
				try (ResultSet rs = statement.executeQuery()) {
					while (rs.next()) {
						Reports report=new Reports();
						if(!commitidChk.equalsIgnoreCase(rs.getString("COMMITID"))){
							list.clear();
							report.setJobname(rs.getString("JOBNAME"));
							report.setBranch(rs.getString("BRANCH"));
							report.setProject(rs.getString("PROJECT"));
							report.setCommitid(rs.getString("COMMITID"));
							report.setDuration(rs.getInt("DURATION"));
							report.setTRIGGER_FROM(rs.getString("TRIGGER_FROM"));
							report.setScmuser(rs.getString("SCMUSER"));
							list.add(rs.getString("STATUS"));
							if( rs.getString("STATUS").equalsIgnoreCase(Constants.NOTSTARTED)){
								report.setStatus(Constants.INITIATEDSTATUS);
							}else if(rs.getString("STATUS").equalsIgnoreCase(Constants.INPROGRESS)){
								report.setStatus(Constants.INPROGRESSSTATUS);
							}else if(rs.getString("STATUS").equalsIgnoreCase(Constants.SUCCESS)){
								report.setStatus(Constants.SUCCESSSTATUS);
							}else if(rs.getString("STATUS").equalsIgnoreCase(Constants.FAILURE)){
								report.setStatus(Constants.FAILURESTATUS);
							}else if(rs.getString("STATUS").equalsIgnoreCase(Constants.ABORTED)){
								report.setStatus(Constants.ABORTEDSTATUS);
							}
							reports.add(report);
							commitidChk=rs.getString("COMMITID");

						}else{
							int j=reports.indexOf(reporttemp);
							reports.remove(j);
							report.setJobname(rs.getString("JOBNAME"));
							report.setBranch(rs.getString("BRANCH"));
							report.setProject(rs.getString("PROJECT"));
							report.setCommitid(rs.getString("COMMITID"));
							report.setDuration(rs.getInt("DURATION"));
							report.setTRIGGER_FROM(rs.getString("TRIGGER_FROM"));
							report.setScmuser(rs.getString("SCMUSER"));
							list.add(rs.getString("STATUS"));
							if(list.contains(Constants.FAILURE)){								
								report.setStatus(Constants.FAILURESTATUS);
							}else if(list.contains(Constants.ABORTED)){						
								report.setStatus(Constants.ABORTEDSTATUS);
							}else if(list.contains(Constants.INPROGRESS)){
								report.setStatus(Constants.INPROGRESSSTATUS);
							}else if(list.contains(Constants.NOTSTARTED)){
								if( list.isEmpty() || list.stream().allMatch(list.get(0)::equals)){
									report.setStatus(Constants.INITIATEDSTATUS);
								}else{
									report.setStatus(Constants.INPROGRESSSTATUS);
								}
							}else if(list.contains(Constants.SUCCESS)){
								report.setStatus(Constants.SUCCESSSTATUS);
							}
							reports.add(report);
						}
						reporttemp=report;
					}
				}
			} catch (SQLException e) {
				logger.debug(e.toString());
			}
		}else{
			String  sql =("(SELECT start_timestamp,JOBNAME,STATUS,PROJECT,BRANCH,SUM(DURATION) as DURATION,COMMITID,TRIGGER_FROM,scmuser  FROM  buildon_reports r WHERE r.scmuser=?  "
					+ "    and (  r.project =?   or r.branch= ?  or  (r.startDate>=?  and r.startDate<=?) or r.endDate=? ) group by start_timestamp,JOBNAME,STATUS,BRANCH,PROJECT,COMMITID,TRIGGER_FROM,scmuser) "
					+ "  UNION "
					+ " (SELECT  T.start_timestamp,T.JOBNAME,T.STATUS,T.PROJECT,T.BRANCH,SUM(T.DURATION)  as DURATION,T.COMMITID,T.TRIGGER_FROM,T.scmuser   FROM  buildon_reports  T WHERE T.scmuser <> ?  "
					+ "  AND (T.project = ?   or  T.branch= ?   or  (T.startDate>=?    and  T.startDate<=? ) or T.endDate=? )"
					+ " group by T.start_timestamp,T.JOBNAME,T.STATUS,T.BRANCH,T.PROJECT,T.COMMITID,T.TRIGGER_FROM,T.scmuser) ORDER BY start_timestamp,COMMITID desc");

			try(Connection con=createConnection();PreparedStatement statement=con.prepareStatement(sql)) {
				statement.setString(1,userId);
				statement.setString(2,project);
				statement.setString(3,branch);
				statement.setDate(4,new java.sql.Date(startdate.getTime()));
				statement.setDate(5,new java.sql.Date(enddate.getTime()));
				statement.setDate(6,new java.sql.Date(enddate.getTime()));
				statement.setString(7,userId);
				statement.setString(8,project);
				statement.setString(9,branch);
				statement.setDate(10,new java.sql.Date(startdate.getTime()));
				statement.setDate(11,new java.sql.Date(enddate.getTime()));
				statement.setDate(12,new java.sql.Date(enddate.getTime()));
				Reports reporttemp=new Reports();
				try (ResultSet rs = statement.executeQuery()) {
					while (rs.next()) {
						Reports report=new Reports();
						if(!commitidChk.equalsIgnoreCase(rs.getString("COMMITID"))){
							list.clear();
							report.setJobname(rs.getString(2));
							report.setProject(rs.getString(4));
							report.setBranch(rs.getString(5));
							report.setDuration(rs.getInt(6));
							report.setCommitid(rs.getString(7));
							report.setTRIGGER_FROM(rs.getString(8));
							report.setScmuser(rs.getString(9));
							list.add(rs.getString(3));
							if(rs.getString(3).equalsIgnoreCase(Constants.NOTSTARTED)){
								report.setStatus(Constants.INITIATEDSTATUS);
							}else if(rs.getString(3).equalsIgnoreCase(Constants.INPROGRESS)){
								report.setStatus(Constants.INPROGRESSSTATUS);	
							}else if(rs.getString(3).equalsIgnoreCase(Constants.SUCCESS)){
								report.setStatus(Constants.SUCCESSSTATUS);
							}else if(rs.getString(3).equalsIgnoreCase(Constants.FAILURE)){
								report.setStatus(Constants.FAILURESTATUS);
							}else if(rs.getString(3).equalsIgnoreCase(Constants.ABORTED)){
								report.setStatus(Constants.ABORTEDSTATUS);
							}

							reports.add(report);
							commitidChk=rs.getString(7);
						}else{

							int j=reports.indexOf(reporttemp);
							reports.remove(j);
							report.setJobname(rs.getString(2));
							report.setProject(rs.getString(4));
							report.setBranch(rs.getString(5));
							report.setDuration(rs.getInt(6));
							report.setCommitid(rs.getString(7));
							report.setTRIGGER_FROM(rs.getString(8));
							report.setScmuser(rs.getString(9));
							list.add(rs.getString(3));
							if(list.contains(Constants.FAILURE)){
								report.setStatus(Constants.FAILURESTATUS);
							}else if(list.contains(Constants.ABORTED)){
								report.setStatus(Constants.ABORTEDSTATUS);
							}else if(list.contains(Constants.INPROGRESS)){
								report.setStatus(Constants.INPROGRESSSTATUS);
							}else if(list.contains(Constants.NOTSTARTED)){
								if( list.isEmpty() || list.stream().allMatch(list.get(0)::equals)){
									report.setStatus(Constants.INITIATEDSTATUS);
								}else{
									report.setStatus(Constants.INPROGRESSSTATUS);
								}
							}else if(list.contains(Constants.SUCCESS)){
								report.setStatus(Constants.SUCCESSSTATUS);
							}
							reports.add(report);
						}
						reporttemp=report;
					}
				}
			} catch (SQLException e) {
				logger.debug(e.toString());
			}
		}

		return reports ;
	}


	/* (non-Javadoc)
	 * @see com.dao.BuildOnDAO#saveScmDet(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Transactional
	@Override
	public boolean  saveScmDet(String email,String switchval,String type,String url, String oauthtoken,String id) {
		logger.debug("saveScmDet  method :"+type);
		boolean isSaved=false ;
		String userId=email.toLowerCase();
		boolean flag=convertToBoolean(switchval);
		int value =flag ? 1 : 0;
		EntityManager em = factory.createEntityManager();
		EntityTransaction tx =  em.getTransaction();
		tx.begin();
		boolean isExist=false ;
		isExist=checkScmRecordExist(userId,url,oauthtoken,value,type);
		boolean existprefer=false; 

		if(!isExist){
			try{	
				ScmDetails scmdet=new ScmDetails();
				if(id!=null && !id.equals("")){
					int tid=Integer.parseInt(id.trim());
					scmdet=em.find(ScmDetails.class,tid);
					deletepreference(scmdet.getUrl(),userId);
					scmdet.setId(tid);
					scmdet.setType(type);
					scmdet.setEmail(userId);
					scmdet.setOauthtoken(oauthtoken);
					scmdet.setUrl(url);
					scmdet.setDefaultValue(value);
					em.merge(scmdet);
					if(value > 0){
						int idRec=Integer.parseInt(id);
						updateScmDet(idRec,userId,em);
					}
					//added to check preferences are exist
					existprefer= checkpreference(userId,url);
					if(!existprefer){
						insertPreferences(url,em,userId);
					}
					isSaved=true;
				}else{
					if(value > 0){
						updateScmDet(userId,em);
					}
					scmdet.setType(type);
					scmdet.setEmail(userId);
					scmdet.setOauthtoken(oauthtoken);
					scmdet.setUrl(url);
					scmdet.setDefaultValue(value);
					try {
						em.persist(scmdet);
						insertPreferences(url,em,userId);
						isSaved=true;
					}catch (PersistenceException ex) {
						logger.debug("exception"+ex);
						isSaved=false;
					}
				}
				em.close();
				tx.commit();
			}catch(PersistenceException e){
				logger.debug(e.toString());
				isSaved=false;
			}
		}
		return isSaved;
	}

	/**
	 * @param url
	 * @param email
	 */
	@Transactional
	public void deletepreference(String url,String email) {
		EntityManager em1 = factory.createEntityManager();
		String scmurl= url.substring(url.lastIndexOf("/") + 1);;
		String[] urlsplit = scmurl.split("\\."); 
		String repository = urlsplit[0];
		String jpql = "DELETE FROM buildon_preferences  " + 
				"WHERE repository=:repository and email=:email";
		em1.getTransaction().begin();
		em1.createNativeQuery(jpql)
		.setParameter("repository",repository.trim())
		.setParameter("email",email.trim())
		.executeUpdate();
		em1.getTransaction().commit();
		em1.close();
	}

	/**
	 * @param userId
	 * @param url
	 * @return
	 */
	private boolean  checkpreference(String userId,String url){
		boolean isExist=false;
		String scmurl= url.substring(url.lastIndexOf('/') + 1);;
		String[] urlsplit = scmurl.split("\\."); 
		String repository = urlsplit[0];
		EntityManager em1 = factory.createEntityManager();
		try{
			Query query = em1.createQuery("select count(det) from Preferences det where det.email=:userId and det.repository=:repository " );
			query.setParameter("userId",userId);
			query.setParameter("repository",repository);
			long cnt = (long)query.getSingleResult();
			if(cnt >0){
				isExist=true;
			}
		}catch(NoResultException e){
			isExist=false;
		}
		em1.close();
		return isExist;
	}

	/**
	 * @param userId
	 * @param url
	 * @param oauthtoken
	 * @param defaultValue
	 * @param type 
	 * @return
	 */
	private boolean checkScmRecordExist(String userId,String url,String oauthtoken,int defaultValue, String type){
		boolean isExist=false;
		EntityManager em1 = factory.createEntityManager();
		try{
			Query query = em1.createQuery("select count(det) from ScmDetails det where det.email=:userId and det.url=:url and det.oauthtoken=:oauthtoken  and type=:type" );
			query.setParameter("userId",userId);
			query.setParameter("url",url);
			query.setParameter("oauthtoken",oauthtoken);
			query.setParameter("type",type);
			long cnt = (long)query.getSingleResult();
			if(cnt >0){
				isExist=true;
				if(defaultValue > 0){
					updateOtherDefaultval(userId,url,defaultValue);
					updateDefaultval(userId,url,defaultValue);
				}
			}
		}catch(NoResultException e){
			isExist=false;
		}

		em1.close();
		return isExist;
	}

	/**
	 * @param userId
	 * @param url
	 * @param defaultValue
	 */
	private  void updateOtherDefaultval(String userId,String url, int defaultValue) {
		logger.debug("updateOtherDefaultval...");
		String sql="UPDATE buildon_scmdetails  " + 
				"SET defaultvalue =0  " +
				"WHERE  email in (?)";

		try(Connection con=createConnection(); PreparedStatement statement=con.prepareStatement(sql)) {
			statement.setString(1,userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			logger.debug(e.toString());
		}

	}

	/**
	 * @param userId
	 * @param url
	 * @param defaultValue
	 */
	private void updateDefaultval(String userId, String url, int defaultValue) {
		String jpql = "UPDATE buildon_scmdetails  " + 
				"SET defaultvalue =?  " +
				"WHERE  email=?  and url=?  ";

		try(Connection con=createConnection();PreparedStatement statement=con.prepareStatement(jpql)) {
			statement.setInt(1,defaultValue);
			statement.setString(2,userId);
			statement.setString(3,url);
			statement.executeUpdate();
		}
		catch (SQLException e) {
			logger.debug(e.toString());
		}

	}


	/**
	 * @param url
	 * @param em
	 * @param email
	 */
	@Transactional
	private void insertPreferences(String url,EntityManager em, String email) {
		Preferences preference=new Preferences();
		String scmurl= url.substring(url.lastIndexOf('/') + 1);;
		String[] urlsplit = scmurl.split("\\."); 
		String repo = urlsplit[0];
		try {
			preference.setRepository(repo);
			preference.setWebhook(0);
			preference.setEmail(email);
			em.persist(preference);
		}
		catch(Exception ex) {
			logger.debug(ex.toString());
		}
	}

	/**
	 * @param userId
	 * @param em
	 */
	@Transactional
	private void updateScmDet(String userId, EntityManager em) {
		logger.debug("update scm details with default value 1");
		String jpql = "UPDATE buildon_scmdetails  " + 
				"SET defaultvalue = 0 " +
				"WHERE  email=?    and   defaultvalue = 1";

		try(Connection con=createConnection();PreparedStatement statement=con.prepareStatement(jpql)) {
			statement.setString(1,userId);
			statement.executeUpdate();
		}
		catch (SQLException e) {
			logger.debug(e.toString());
		}
	}

	/**
	 * @param id
	 * @param userId
	 * @param em
	 */
	@Transactional
	private void updateScmDet(int id,String userId, EntityManager em) {
		logger.debug(" updateScmDet(int id,String userId, EntityManager em) with default value 0");
		String jpql = "UPDATE buildon_scmdetails  " + 
				"SET defaultvalue = 0 " +
				"WHERE email=? and  id  not in(?)";

		try(Connection con=createConnection(); PreparedStatement statement=con.prepareStatement(jpql)) {
			statement.setString(1,userId);
			statement.setInt(2,id);
			statement.executeUpdate();
		}
		catch (SQLException e) {
			logger.debug(e.toString());
		}
	}

	/**
	 * @param value
	 * @return
	 */
	private boolean convertToBoolean(String value) {
		boolean returnValue = false;
		if ("1".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || 
				"true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value))
			returnValue = true;
		return returnValue;
	}

	/* (non-Javadoc)
	 * @see com.dao.BuildOnDAO#getScmDetails(java.lang.String, java.lang.String)
	 */
	@Override
	public  List<ScmDetails> getScmDetails(String userId ,String type){
		logger.debug("getscm details with userid and type start");
		EntityManager em = factory.createEntityManager();
		Query query =null;
		if(Constants.FIRST.equals(type)){
			query=  em.createQuery("select det from ScmDetails det where det.email=:uid  order by det.id  asc" );
			query.setParameter("uid",userId.toLowerCase());
		}else{
			query=  em.createQuery("select det from ScmDetails det where det.email=:uid and det.type= :type  order by det.id asc" );
			query.setParameter("uid",userId.toLowerCase());
			query.setParameter("type",type);	
		}
		List<ScmDetails> scmdetails = query.getResultList();
		em.close();
		return scmdetails;
	}


	/* (non-Javadoc)
	 * @see com.dao.BuildOnDAO#removeRecord(java.lang.String, java.lang.String)
	 */
	@Transactional
	@Override
	public   boolean removeRecord(String id,String email) {
		logger.debug("remove record from details and preferences start ");
		boolean isSuccess =false;
		int idRec = Integer.parseInt(id);
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		ScmDetails scmdetails=em.find(ScmDetails.class,idRec);
		if(scmdetails!=null){
			em.remove(scmdetails);
			isSuccess=true;
		}
		String scmurl= scmdetails.getUrl().substring(scmdetails.getUrl().lastIndexOf("/") + 1);;
		String[] urlsplit = scmurl.split("\\."); 
		String repository = urlsplit[0];
		String jpql = "DELETE FROM buildon_preferences   " + 
				"WHERE repository=:repository and email=:email";
		int updateCount=em.createNativeQuery(jpql).setParameter("repository",repository).setParameter("email",email).executeUpdate();
		em.getTransaction().commit();
		em.close();
		if(updateCount >0){
			isSuccess=true; 
		}
		return isSuccess;
	}


	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getUserScmDetails(java.lang.String, java.lang.String)
	 */
	@Override
	public  List<ScmDetails> getUserScmDetails(String email ,String type){
		int defaultval=1;
		EntityManager em = factory.createEntityManager();
		Query query = em.createQuery("select det from ScmDetails det where det.email=:email and det.type= :type"
				+ " and det.defaultvalue=:defaultValue");
		query.setParameter("email",email);
		query.setParameter("type",type);
		query.setParameter("defaultValue",defaultval);
		List<ScmDetails> scmdetails = query.getResultList();
		em.close();
		return scmdetails;
	}


	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getBuildtrends(java.lang.String)
	 */
	@Override
	public  JSONArray getBuildtrends(String userId)  {
		logger.info("getBuildtrends methods start.");
		JSONArray  jsonarray=new JSONArray();
		JSONObject jsonobj=null;
		String sql="SELECT  rep.startDate,sum(CASE WHEN rep.status = 'FAILURE' THEN 1 END) as failure, "
				+ " sum(CASE WHEN rep.status = 'SUCCESS' THEN 1 END) as success, "
				+ " sum(CASE WHEN rep.status = 'ABORTED' THEN 1 END) as Aborted "
				+ " FROM buildon_reports rep "
				+ " where rep.scmuser=?  "
				+ " GROUP BY rep.startDate  "
				+ " ORDER BY rep.startDate desc";

		Long countF=0L;
		Long countS=0L;
		Long countA=0L;
		Date startdate=null;
		try(Connection con=createConnection();PreparedStatement statement=con.prepareStatement(sql)) {
			statement.setString(1,userId);
			statement.setMaxRows(4);
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					jsonobj = new JSONObject();
					startdate = (Date)rs.getDate(1);
					countF = (Long) rs.getLong(2);
					countS = (Long) rs.getLong(3);
					countA = (Long) rs.getLong(4);
					try{
						jsonobj.put("Days",startdate);
						jsonobj.put(Constants.FAILURESTATUS,countF);
						jsonobj.put(Constants.SUCCESSSTATUS,countS);
						jsonobj.put(Constants.ABORTEDSTATUS,countA);
					} catch (JSONException e) {
						logger.debug(e.toString());
					}
					jsonarray.put(jsonobj);
				}
			}

		} catch (SQLException e) {
			logger.debug(e.toString());
		}

		return jsonarray;
	} 		


	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getProjectwiseBuild(java.lang.String)
	 */
	@Override
	public JSONArray getProjectwiseBuild(String userId) {
		logger.info("getProjectwiseBuild methods start.");
		JSONArray  jsonarray=new JSONArray();
		JSONObject jsonobj=null;
		String  sql="SELECT  rep.project,sum(CASE WHEN status = 'FAILURE' THEN 1 END) as failure,"
				+ " sum(CASE WHEN status = 'SUCCESS' THEN 1 END) as success,sum(CASE WHEN status = 'ABORTED' THEN 1 END) as Aborted  "
				+ "    FROM buildon_reports rep where rep.scmuser=?  GROUP BY rep.project order by rep.project desc";

		Long countF=0L;
		Long countS=0L;
		Long countA=0L;
		String project=null;
		try(Connection con=createConnection();PreparedStatement statement=con.prepareStatement(sql)) {
			statement.setString(1,userId);
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					jsonobj = new JSONObject();
					project = (String)rs.getString(1);
					countF = (Long) rs.getLong(2);
					countS = (Long) rs.getLong(3);
					countA = (Long) rs.getLong(4);
					try {
						jsonobj.put(Constants.PROJECTS,project);
						jsonobj.put(Constants.FAILURESTATUS,countF);
						jsonobj.put(Constants.SUCCESSSTATUS,countS);
						jsonobj.put(Constants.ABORTEDSTATUS,countA);
					} catch (JSONException e) {
						logger.debug(e.toString());
					}
					jsonarray.put(jsonobj);
				}
			}
		} catch (SQLException e) {
			logger.debug(e.toString());
		}
		return jsonarray;
	}


	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getLatestbuild(java.lang.String)
	 */
	@Override
	public JSONArray getLatestbuild(String userId) {
		logger.debug("getLatestbuild methods start.");    
		JSONArray  jsonarray=new JSONArray();
		JSONObject jsonobj=null;
		String sql="SELECT tmp.stdate, SUM( tmp.FAILURE) AS FAILURE ,SUM( tmp.SUCCESS) AS SUCCESS,SUM( tmp.ABORTED)  AS ABORTED   "
				+ "   FROM ( SELECT startdate stdate, SUM(CASE WHEN rep.status = 'FAILURE' THEN 1 END) AS FAILURE  ,"
				+ "   SUM(CASE WHEN rep.status = 'SUCCESS' THEN 1 END)  AS SUCCESS, "
				+ "   SUM(CASE WHEN rep.status = 'ABORTED' THEN 1 END)  AS ABORTED  FROM buildon_reports rep where rep.scmuser=?  "
				+ "   GROUP BY rep.status ,startDate ORDER BY rep.startDate desc)"
				+ "   AS tmp GROUP BY tmp.stdate  ORDER BY tmp.stdate desc   fetch first row only ";

		Long countF=0L;
		Long countS=0L;
		Long countA=0L;
		try(Connection con=createConnection();PreparedStatement statement=con.prepareStatement(sql)) {
			statement.setString(1,userId);
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					countF = (Long) rs.getLong(2);
					countS = (Long) rs.getLong(3);
					countA = (Long) rs.getLong(4);
				}
			}
		} catch (SQLException e) {
			logger.debug(e.toString());
		}


		try {
			jsonobj = new JSONObject();
			jsonobj.put(Constants.BUILD_STATUS,Constants.FAILURESTATUS);
			jsonobj.put(Constants.VALUE,countF);
			jsonobj.put(Constants.COLOR, "#f73333");
			jsonarray.put(jsonobj);

			jsonobj = new JSONObject();
			jsonobj.put(Constants.BUILD_STATUS,Constants.SUCCESSSTATUS);
			jsonobj.put(Constants.VALUE,countS);
			jsonobj.put(Constants.COLOR, "#84b761");
			jsonarray.put(jsonobj);

			jsonobj = new JSONObject();
			jsonobj.put(Constants.BUILD_STATUS,Constants.ABORTEDSTATUS);
			jsonobj.put(Constants.VALUE,countA);
			jsonobj.put(Constants.COLOR,"#fdd400");

			jsonarray.put(jsonobj);

		} catch (JSONException e) {
			logger.debug(e.toString());
		}

		return jsonarray;
	}


	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getCompareBuild(java.lang.String)
	 */
	@Override
	public  JSONArray getCompareBuild(String userId)  {
		logger.debug("getCompareBuild methods start."); 
		JSONArray  jsonarray=new JSONArray();
		JSONObject jsonobj=null;
		String sql="SELECT  'you'UID ,  r.PROJECT ,CASE WHEN r.status = 'SUCCESS' THEN 'Success' "
				+ "  WHEN r.status = 'FAILURE' THEN 'Failure'  "
				+ "  WHEN r.status = 'ABORTED' THEN 'Aborted' "
				+ " ELSE 'N/A'    END status  ,COUNT (1) cnt FROM  buildon_reports  r "
				+ " WHERE    scmuser = ?  GROUP BY project, STATUS  "
				+ "  UNION "
				+ " SELECT  'others'UID,  PROJECT , "
				+ " CASE WHEN status = 'SUCCESS' "
				+ " THEN 'Success'  WHEN status = 'FAILURE' THEN ' Failure'  "
				+ " WHEN  status = 'ABORTED'        THEN ' Aborted'     ELSE 'N/A'  "
				+ "  END status ,COUNT (1) cnt  FROM     buildon_reports T WHERE   scmuser <> ? "
				+ " AND      EXISTS (SELECT 1  FROM   buildon_reports T1 WHERE  T1.scmuser = ?  AND T1.project = T.project) "
				+ " GROUP BY project, STATUS  ORDER BY project,STATUS desc";

		String project =null;
		String person =null;
		String status=null;
		boolean flag=false;
		try(Connection con=createConnection();PreparedStatement statement=con.prepareStatement(sql)) {
			statement.setString(1, userId);
			statement.setString(2, userId);
			statement.setString(3, userId);
			String tempproj=null;;
			jsonobj = new JSONObject();
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					person =rs.getString(1).trim();
					project =rs.getString(2).trim();
					status = rs.getString(3).trim();
					Long cnt =(Long) rs.getLong(4);
					if( !project.equalsIgnoreCase(tempproj) ){
						jsonobj = new JSONObject();

					}
					try {
						if(person.equalsIgnoreCase(Constants.YOU) && status.equalsIgnoreCase(Constants.SUCCESSSTATUS)){
							jsonobj.put(Constants.YOU_SUCCESS, cnt);
						}
						if(person.equalsIgnoreCase(Constants.YOU)  && status.equalsIgnoreCase(Constants.FAILURESTATUS)){
							jsonobj.put(Constants.YOU_FAILURE, cnt);
						}
						if(person.equalsIgnoreCase(Constants.YOU)  && status.equalsIgnoreCase(Constants.ABORTEDSTATUS)){
							jsonobj.put(Constants.YOU_ABORTED, cnt);
						}
						if(person.equalsIgnoreCase(Constants.OTHERS) && status.equalsIgnoreCase(Constants.SUCCESSSTATUS)){
							jsonobj.put(Constants.OTHERS_SUCCESS, cnt);
						}
						if(person.equalsIgnoreCase(Constants.OTHERS)  && status.equalsIgnoreCase(Constants.FAILURESTATUS)){
							jsonobj.put(Constants.OTHERS_FAILURE, cnt);
						}
						if(person.equalsIgnoreCase(Constants.OTHERS)  && status.equalsIgnoreCase(Constants.ABORTEDSTATUS)){
							jsonobj.put(Constants.OTHERS_ABORTED, cnt);
						}
					} catch (JSONException e) {
						logger.debug(e.toString());
					}	
					flag=true;
					if(flag && !project.equalsIgnoreCase(tempproj)){
						try {	jsonobj.put("projects",project);
						if(!jsonobj.has(Constants.YOU_SUCCESS)){
							jsonobj.put(Constants.YOU_SUCCESS, 0);
						}
						if(!jsonobj.has(Constants.YOU_FAILURE)){
							jsonobj.put(Constants.YOU_FAILURE, 0);
						}
						if(!jsonobj.has(Constants.YOU_ABORTED)){
							jsonobj.put(Constants.YOU_ABORTED, 0);
						}
						if(!jsonobj.has(Constants.OTHERS_SUCCESS)){
							jsonobj.put(Constants.OTHERS_SUCCESS, 0);
						}

						if(!jsonobj.has(Constants.OTHERS_FAILURE)){
							jsonobj.put(Constants.OTHERS_FAILURE, 0);
						}

						if(!jsonobj.has(Constants.OTHERS_ABORTED)){
							jsonobj.put(Constants.OTHERS_ABORTED, 0);
						}

						} catch (JSONException e) {
							logger.debug(e.toString());
						}	

						jsonarray.put(jsonobj);
					}
					tempproj=project;			
				}
			}
		} catch(SQLException e){
			logger.debug(e.toString());
		}
		return jsonarray;
	}


	/**
	 * @return
	 */
	private  Connection  createConnection(){
		logger.debug("Create Connection");
		Connection connection=null;
		Properties props = readPropertyFile();
		String driver = props.getProperty("postgresql.driver");
		String url = props.getProperty("postgresql.url");
		String username = props.getProperty("postgresql.username");
		String password = props.getProperty("postgresql.password");
		//String pass=service.decrypt(password);
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(driver);
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		try {
			connection = dataSource.getConnection();
		} catch (SQLException e) {
			logger.debug(e.toString());
		}
		return connection;
	}

	/**
	 * @return
	 */
	private Properties readPropertyFile() {
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

	/* (non-Javadoc)
	 * @see com.dao.BuildOnDAO#getPeferenceDetails(java.lang.String, java.lang.String)
	 */
	@Override
	public  List<String> getPeferenceDetails(String userid,String type) {
		String email =userid.toLowerCase();
		List<String> list=new ArrayList<>();
		JSONObject json=null;
		EntityManager em = factory.createEntityManager();
		Query query = em.createQuery("select det from Preferences det  where det.email=:email");
		query.setParameter("email",email);
		List<Preferences> preference= query.getResultList();
		for(Preferences details : preference) {
			json=new JSONObject();
			String repo=details.getRepository();
			String webhook  = Integer.toString(details.getWebhook());
			boolean hookvalue=convertToBoolean(webhook);

			try {
				json.put("repo",repo);
				json.put("hookvalue", hookvalue);
			} catch (JSONException e) {
				logger.debug(e.toString());
			}


			list.add(json.toString());
		} 
		em.close();
		return list;
	}

	/* (non-Javadoc)
	 * @see com.dao.BuildOnDAO#savePeferenceDetails(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean savePeferenceDetails(String userid,String switchmode,String repository,String branch) {
		logger.debug("savePeferenceDetails method start");
		boolean flag=convertToBoolean(switchmode);
		int webhook =flag ? 1 : 0;
		Query qry=null;
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		if(null!=branch && !branch.equals("")){
			String jpql = "UPDATE buildon_preferences  " + 
					"  SET webhook = :webhook ,branch=:branch "  +
					"  WHERE repository=:repository" ;
			qry=em.createNativeQuery(jpql);
			qry.setParameter("webhook",webhook);
			qry.setParameter("branch",branch);
			qry.setParameter("repository",repository);
		}else{
			String jpql = "UPDATE buildon_preferences  " + 
					"  SET  webhook = :webhook "  +
					"  WHERE repository=:repository";
			qry=em.createNativeQuery(jpql);
			qry.setParameter("webhook",webhook);
			qry.setParameter("repository",repository);
		}
		qry.executeUpdate();
		em.getTransaction().commit();
		em.close();
		return true;
	}


	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getHistoricalReports(java.lang.String)
	 */
	@Override
	public JSONObject getHistoricalReports(String email) {
		logger.debug("getHistoricalReports method start");
		List<String> projectlist=new ArrayList<>();
		JSONObject json=new JSONObject();
		String query="select distinct rep from Reports rep where rep.scmuser=:email";
		EntityManager em = factory.createEntityManager();
		Query qry = em.createQuery(query);
		qry.setParameter("email",email);
		List<Reports> list = qry.getResultList();
		for(Reports report:list){
			projectlist.add(report.getProject());
		}
		logger.debug(" projectlist:" +projectlist);
		Set<String> hs = new HashSet<>();
		hs.addAll(projectlist);
		projectlist.clear();
		projectlist.addAll(hs);
		try {
			json.put("projectlist",projectlist);
		} catch (JSONException e) {
			logger.debug(e.toString());
		}
		logger.debug("getHistoricalReports method end");
		return json;
	}



	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getIndividualReports(java.lang.String)
	 */
	@Override
	public Reports getIndividualReports(String userid) {
		String email=userid.toLowerCase();
		logger.info("getIndividualReports "+email);
		String query="select rep from Reports rep where rep.scmuser=:email order by  start_timestamp  desc";
		EntityManager em = factory.createEntityManager();
		Query qry = em.createQuery(query);
		qry.setParameter("email",email);
		qry.setMaxResults(1);
		List<Reports> results = qry.getResultList();
		return results.size()==0 ? null : results.get(0);
	}

	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getIndividualstatusReports(java.lang.String, java.lang.String)
	 */
	@Override
	public Reports getIndividualstatusReports(String email, String commitid) {
		String scmuser=email.toLowerCase();
		ArrayList<String> list=new ArrayList<>();
		Reports rep=null;
		String query="select rep1 from Reports  rep1 where rep1.scmuser=:scmuser and  rep1.commitid=:commitid  "
				+ "  and exists (select rep2 from Reports  rep2  where rep2.jobname = rep1.jobname) "
				+ "  order by  start_timestamp  desc ";
		EntityManager em = factory.createEntityManager();
		Query qry = em.createQuery(query);
		qry.setParameter("scmuser",scmuser);
		qry.setParameter("commitid",commitid);
		List<Reports> results = qry.getResultList();
		for(Reports reports:results){
			rep=new Reports();
			rep.setJobname(reports.getJobname());
			rep.setBranch(reports.getBranch());
			rep.setProject(reports.getProject());
			rep.setScmuser(reports.getScmuser());
			rep.setCommitid(reports.getCommitid());
			rep.setStart_timestamp(reports.getStart_timestamp());
			rep.setEstimated_duration(reports.getEstimated_duration());
			rep.setLogdir(reports.getLogdir());
			rep.setTRIGGER_FROM(reports.getTRIGGER_FROM());
			list.add(reports.getStatus());
		}
		if(list.contains(Constants.FAILURE)){
			rep.setStatus(Constants.FAILURESTATUS);
		}else if(list.contains(Constants.ABORTED)){
			rep.setStatus(Constants.ABORTEDSTATUS);
		}else if(list.contains(Constants.INPROGRESS)){
			rep.setStatus(Constants.INPROGRESSSTATUS);
		}else if(list.contains(Constants.NOTSTARTED)){
			if( list.isEmpty() || list.stream().allMatch(list.get(0)::equals)){
				rep.setStatus(Constants.INITIATEDSTATUS);
			}else{
				rep.setStatus(Constants.INPROGRESSSTATUS);
			}
		}else if(list.contains(Constants.SUCCESS)){
			rep.setStatus(Constants.SUCCESSSTATUS);
		}
		return rep;
	}


	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getJsonData(java.lang.String)
	 */
	@Override
	public String getJsonData(String commitid) {
		logger.info("commitid:"+commitid);
		String json=null;
		EntityManager em = factory.createEntityManager();
		Query query = em.createQuery("select service from  Service  service where service.commitid=:commitid");
		query.setParameter("commitid",commitid);
		List<Service> list = query.getResultList();
		if(list.isEmpty()){
			json=null;	
		}else{
			json=list.get(0).getJson();
		}
		em.close();
		return json;
	}


	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getPodname(java.lang.String)
	 */
	@Override
	public  String getPodname(String commitId) {
		String pod=null;
		EntityManager em = factory.createEntityManager();
		Query query = em.createQuery("select service from  Service  service where service.commitid=:commitid");
		query.setParameter("commitid",commitId);
		List<Service> list = query.getResultList();
		if(list.isEmpty()){
			pod=null;	
		}else{
			pod=list.get(0).getPodname();
		}

		em.close();
		return pod;
	}


	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getServiceCommitId(java.lang.String)
	 */
	@Override
	public  String getServiceCommitId(String commitId) {
		String commitidresult=null;
		EntityManager em = factory.createEntityManager();
		Query query = em.createQuery("select service from  Service  service where service.commitid=:commitId");
		query.setParameter("commitId", commitId);
		List<Service>  list = query.getResultList();
		if(list.size() >0 ){
			commitidresult=list.get(0).getCommitid();
		}
		em.close();
		return commitidresult;
	}


	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getServiceData(java.lang.String)
	 */
	@Override
	public Service getServiceData(String commitId) {
		Service service=new Service();
		EntityManager em = factory.createEntityManager();
		Query query = em.createQuery("select service from  Service  service where service.commitid=:commitId");
		query.setParameter("commitId", commitId);
		List<Service>  list = query.getResultList();
		if(list.size() >0 ){
			service=list.get(0);
		}
		em.close();
		return service;
	}



	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getDBServiceInsert(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public  boolean getDBServiceInsert(String podIP, String podPort, String podNameValue, String resultJSON,
			String commitid) {
		boolean update=false;
		logger.debug("DBServiceInsert:"+resultJSON+":"+podNameValue);
		String insertTableSQL = "INSERT INTO buildon_service"
				+ "(COMMITID,PODIP,PODPORT,PODNAME,JSON) VALUES"
				+ "(?,?,?,?,?)";
		EntityManager em = factory.createEntityManager();
		try{
			em.getTransaction().begin();
			em.createNativeQuery(insertTableSQL).setParameter(1,commitid).setParameter(2,podIP)
			.setParameter(3, podPort).setParameter(4, podNameValue).setParameter(5,resultJSON)
			.executeUpdate();
			em.getTransaction().commit();
		}catch(Exception ex){
			logger.debug(ex.toString());	
		}finally{
			em.close();
		}
		return update;
	}	

	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getDBServiceUpdate(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public  boolean getDBServiceUpdate(String podIP, String podPort, String podNameValue, String resultJSON,
			String commitid) {
		EntityManager em = factory.createEntityManager();
		boolean update=false;
		int i=0;
		String sql = " UPDATE buildon_service  " + 
				"   SET  PODIP=?, PODPORT=?,PODNAME=?,JSON=?     WHERE commitid=? " ;
		try{
			em.getTransaction().begin();
			i= em.createNativeQuery(sql).setParameter(1,podIP).setParameter(2, podPort)
					.setParameter(3, podNameValue).setParameter(4,resultJSON).setParameter(5,commitid).executeUpdate();
			em.getTransaction().commit();
		}catch(Exception ex){
			logger.debug(ex.toString());	
		}finally{
			em.close();
		}
		logger.debug("getDBServiceUpdate :"+i);
		return update;
	}

	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getHistoricDBServiceInsert(java.lang.String, java.lang.String)
	 */
	@Override
	public  boolean getHistoricDBServiceInsert(String commitid,String resultJSON) {
		logger.debug("getHistoricDBServiceInsert start");
		boolean update=false;
		String insertTableSQL = "INSERT INTO buildon_service"
				+ "(COMMITID,JSON) VALUES"
				+ "(?,?)";
		EntityManager em = factory.createEntityManager();
		try{
		em.getTransaction().begin();
		em.createNativeQuery(insertTableSQL).setParameter(1,commitid).setParameter(2, resultJSON)
		.executeUpdate();
		em.getTransaction().commit();
		}catch(Exception ex){
			logger.debug(ex.toString());	
		}finally{
			em.close();
		}
		return update;

	}

	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getReportsStatus(java.lang.String, java.lang.String)
	 */
	@Override
	public String getReportsStatus(String commitId,String cijobname) {
		String status=null;
		EntityManager em = factory.createEntityManager();
		Query query = em.createQuery("select report from  Reports  report where report.commitid=:commitId  and  report.ci_jobname=:cijobname ");
		query.setParameter("commitId", commitId);
		query.setParameter("cijobname",cijobname);
		List<Reports>  list = query.getResultList();
		if(list.isEmpty()){
			status=null;
		}else{
			status=list.get(0).getStatus();

		}
		em.close();

		return status;
	}


	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getReportTriggerData(java.lang.String)
	 */
	@Override
	public String getReportTriggerData(String commitid) {
		logger.debug("commitid:"+commitid);		
		String trigger=null;		
		EntityManager em = factory.createEntityManager();
		Query query = em.createQuery("select report from  Reports  report where report.commitid=:commitid");
		query.setParameter("commitid", commitid);
		List<Reports>  list = query.getResultList();
		if(list.isEmpty()){
			trigger=null;
		}else{
			trigger=list.get(0).getTRIGGER_FROM();
		}
		em.close();

		return trigger;
	}


	/* (non-Javadoc)
	 * @see com.cognizant.buildon.dao.BuildOnDAO#getHistoricalURL(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ScmDetails> getHistoricalURL(String email, String repo) {
		String repository="%"+repo+"%";
		EntityManager em = factory.createEntityManager();
		Query query = em.createQuery("select det from ScmDetails det where det.email=:email and det.url like :repo");
		query.setParameter("email",email);
		query.setParameter("repo",repository);
		List<ScmDetails> scmdetails = query.getResultList();
		em.close();
		return scmdetails;
	}
}
