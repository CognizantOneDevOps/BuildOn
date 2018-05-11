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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * @author 338143
 *
 */
@Entity
@Table(name = "buildon_scmdetails")
public class ScmDetails implements Serializable{

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column( nullable=true)
	private int defaultvalue;
	private String type;
	private String url;
	private String oauthtoken;
	private String email;


	/**
	 * @return
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return
	 */
	public int getDefaultValue() {
		return defaultvalue;
	}
	/**
	 * @param defaultValue
	 */
	public void setDefaultValue(int defaultValue) {
		this.defaultvalue = defaultValue;
	}
	/**
	 * @return
	 */
	public String getOauthtoken() {
		return oauthtoken;
	}
	/**
	 * @param oauthtoken
	 */
	public void setOauthtoken(String oauthtoken) {
		this.oauthtoken = oauthtoken;
	}
	/**
	 * @return
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	
}
