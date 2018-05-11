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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author 338143
 *
 */
public class LDAPAuthentication {
	private static  final Logger logger=LoggerFactory.getLogger(LDAPAuthentication.class);
	

	/**
	 * @param username
	 * @param password
	 * @return
	 */
	public static Users getEmpId(String email,String password){
		Users users=new Users();
		String empID=null;
		Properties props = readPropertyFile();
		String ldapAdServer = props.getProperty("ldap.server");
		String ldapSearchBase =props.getProperty("ldap.searchbase");
		String ldapUsername = props.getProperty("ldap.user");
		String ldapPassword = props.getProperty("ldap.password");
		boolean isOpenLDAP = Boolean.parseBoolean( props.getProperty("ldap.isopenLDAP"));
		LdapContext ctx=null;
		
		String searchFilter =null;
		if(isOpenLDAP){
			 ctx = intializeEnvForOpenLDAP(ldapAdServer,ldapUsername, ldapPassword);
			 searchFilter = "(mail=" + email + ")";
			 
		}else{
			 ctx = intializeEnvForLDAP(ldapAdServer, ldapUsername.trim(), ldapPassword.trim());
			 searchFilter =Constants.SEARCH_FILTER_MAIL + email + "))";
		}
		
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration<SearchResult> results=null;
		try {
			results = ctx.search(ldapSearchBase,searchFilter,searchControls);
		} catch (NamingException e1) {
			logger.debug(e1.toString());
		}
		SearchResult searchResult = null;
		if(results!= null && results.hasMoreElements()) {
			searchResult = (SearchResult) results.nextElement();
		}
		String[] attrIDs = { 
				Constants.LDAP_GN_NAME,Constants.LDAP_MAIL,Constants.ACCOUNT_NAME
		};
		searchControls.setReturningAttributes(attrIDs);
		try {

			if(searchResult!=null ){
				
				if(!isOpenLDAP){
					empID = searchResult.getAttributes().get(Constants.ACCOUNT_NAME).get().toString();
					if(empID!=null){
						boolean isvalid=authenticate(empID,password);
						if(isvalid){
							String uname=searchResult.getAttributes().get(Constants.LDAP_GN_NAME).get().toString();
							users.setEmail(email);
							users.setUname(uname);
							users.setId(Integer.parseInt(empID));
						}else{
							users.setEmail(Constants.NO_USER);	
	
						}
					}
				}else{
					empID = searchResult.getAttributes().get("uidnumber").get().toString();
					String uname=searchResult.getAttributes().get(Constants.LDAP_GN_NAME).get().toString();
					users.setEmail(email);
					users.setUname(uname);
					users.setId(Integer.parseInt(empID));
					
				}
					
			}else{
				users.setEmail(Constants.NO_USER);	
			}
		} catch (NamingException e) {
			logger.debug( e.toString());
		}
		try {
			ctx.close();
		} catch (NamingException e) {
			logger.debug( e.toString());
		}
		return users;

	}





	private static boolean authenticate(String empID, String password) {
		boolean isSuccess=false;
		String ldapUsername = empID.trim();
		String ldapPassword = password.trim();
		Properties props = readPropertyFile();
		String ldapAdServer = props.getProperty("ldap.server");
		String ldapSearchBase =props.getProperty("ldap.searchbase");
		LdapContext ctx = intializeEnvForLDAP(ldapAdServer, ldapUsername.trim(), ldapPassword.trim());
		String searchFilter = Constants.SERACH_ACCOUNT+ ldapUsername.trim() + "))";
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration<SearchResult> results=null;
		try {
			results = ctx.search(ldapSearchBase,searchFilter,searchControls);
		} catch (NamingException e1) {
			logger.debug(e1.toString());
		}
		SearchResult searchResult = null;
		if(null!=results && (results.hasMoreElements()) ){
			isSuccess=true;
			searchResult = (SearchResult) results.nextElement();
		}else{
			isSuccess=false;
		}

		return isSuccess;
	}

	/**
	 * @param ldapAdServer
	 * @param ldapUsername
	 * @param ldapPassword
	 * @return
	 */
	private static  LdapContext intializeEnvForLDAP(String ldapAdServer, String ldapUsername, String ldapPassword) {
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		String principalName = ldapUsername + Constants.LDAP_CONCAT;
		env.put(Context.SECURITY_AUTHENTICATION,Constants.SIMPLE);
		if(ldapUsername != null) {
			env.put(Context.SECURITY_PRINCIPAL,principalName);
		}
		if(ldapPassword != null) {
			env.put(Context.SECURITY_CREDENTIALS,ldapPassword.trim());
		}
		env.put(Context.INITIAL_CONTEXT_FACTORY,Constants.LDAP_CTX_FAC);
		env.put(Context.PROVIDER_URL,ldapAdServer);
		env.put(Constants.LDAP_SID_BIN,Constants.LDAP_SID);
		LdapContext ctx=null;
		try {
			ctx = new InitialLdapContext();
			ctx = new InitialLdapContext(env, null);
		} catch (NamingException e1) {
			logger.debug(e1.toString());
		}
		return ctx;
	}
	
	private static LdapContext intializeEnvForOpenLDAP(String openLDAPserver, String user, String pass) {
		LdapContext ctx=null;
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.SECURITY_AUTHENTICATION,"simple");
		if(!user.isEmpty() && !pass.isEmpty())
		{
		String principalName="cn="+user;
		env.put(Context.SECURITY_PRINCIPAL,principalName);
		env.put(Context.SECURITY_CREDENTIALS,pass);
		}else{
		env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
		}
		
		env.put(Context.PROVIDER_URL,openLDAPserver);
		
		try {
			ctx = new InitialLdapContext(env, null);
		} catch (NamingException e1) {
			logger.debug(e1.toString());
		}
		return ctx;
	}

	/**
	 * @return
	 */
	private static  Properties readPropertyFile() {
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
	 * @param id
	 * @return
	 */
	public static Users getEmailForLDAPUser(String id) {
		Users users=new Users();
		Properties props = readPropertyFile();
		String ldapAdServer = props.getProperty("ldap.server");
		String ldapSearchBase =props.getProperty("ldap.searchbase");
		String ldapUsername = props.getProperty("ldap.user");
		String ldapPassword = props.getProperty("ldap.password");
		
		LdapContext ctx = intializeEnvForLDAP(ldapAdServer, ldapUsername.trim(), ldapPassword);
		String searchFilter = Constants.SEARCH_FILTER_ACCOUNT + id + "))";
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration<SearchResult> results=null;
		try {
			results = ctx.search(ldapSearchBase,searchFilter,searchControls);
		} catch (NamingException e1) {
			logger.debug(e1.toString());
		}
		SearchResult searchResult = null;
		if(results.hasMoreElements()) {
			searchResult = (SearchResult) results.nextElement();
		}

		String[] attrIDs = {Constants.LDAP_GN_NAME,
				Constants.LDAP_MAIL,
				Constants.ACCOUNT_NAME,};
		searchControls.setReturningAttributes(attrIDs);
		searchFilter=Constants.ACCOUNT_NAME+"="+ldapUsername;
		try {

			String mail = searchResult.getAttributes().get(Constants.ACCOUNT_NAME).get().toString();
			String name=searchResult.getAttributes().get(Constants.LDAP_GN_NAME).get().toString();
			users.setEmail(mail);
			users.setUname(name);

		} catch (NamingException e) {
			logger.debug( e.toString());
		}
		try {
			ctx.close();
		} catch (NamingException e) {
			logger.debug( e.toString());
		}
		return users;

	}

		//new feature yet to  integrate in UI
		private boolean CheckDistributionList(String username,String password){
		String ldapUsername = username.trim();
		String ldapPassword = password.trim();
		Properties props = readPropertyFile();
		boolean isMember=false;
		String ldapAdServer = props.getProperty("ldap.server");
		String ldapSearchBase =props.getProperty("ldap.searchbase");
		LdapContext ctx = intializeEnvForLDAP(ldapAdServer,ldapUsername, ldapPassword);
		String searchFilter = Constants.SEARCH_FILTER_ACCOUNT + ldapUsername + "))";
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration<SearchResult> results=null;
		String[] attrIDs = { 
				"cn",
				Constants.MEMBER_OF
		};
		searchControls.setReturningAttributes(attrIDs);
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchFilter="sAMAccountName"+"="+ldapUsername;
		NamingEnumeration<SearchResult> answer;
		try {
			answer = ctx.search(ldapSearchBase, searchFilter,searchControls);
			if ( answer.hasMore()) {
				Attributes attrs = ((SearchResult) answer.next()).getAttributes();
				NamingEnumeration e =attrs.get(Constants.MEMBER_OF).getAll();
				while (e.hasMore()) {
					String value = (String) e.next();
					if ( value.indexOf(props.getProperty("ldap.distributionlist")) != -1 ) {
						isMember=true;
						break;
					}
					else{
						isMember=false;
					}

				}
			}else{
				logger.debug("no results");
			}
		} catch (NamingException e) {
			logger.debug( e.toString());
		}
		try {
			ctx.close();
		} catch (NamingException e) {
			logger.debug( e.toString());
		}


		return isMember;

	}
		
		

}
