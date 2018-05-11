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
@Table(name = "buildon_preferences")
public class Preferences {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	private String repository;
	private String branch;
	@Column( nullable=true)
	private int webhook;
	private String email;
	
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return the repository
	 */
	public String getRepository() {
		return repository;
	}
	/**
	 * @param repository the repository to set
	 */
	public void setRepository(String repository) {
		this.repository = repository;
	}
	/**
	 * @return the branch
	 */
	public String getBranch() {
		return branch;
	}
	/**
	 * @param branch the branch to set
	 */
	public void setBranch(String branch) {
		this.branch = branch;
	}

	/**
	 * @return the webhook
	 */
	public int getWebhook() {
		return webhook;
	}
	/**
	 * @param webhook the webhook to set
	 */
	public void setWebhook(int webhook) {
		this.webhook = webhook;
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
