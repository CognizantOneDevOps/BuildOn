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
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * @author 338143
 *
 */
@Entity
@Table(name = "buildon_service")
public class Service implements Serializable{
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String commitid;
	@Column(columnDefinition = "TEXT")
	private String json;
	private String podip;
	private String podport;
	private String podname;
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
	 * @return the commitid
	 */
	public String getCommitid() {
		return commitid;
	}
	/**
	 * @param commitid the commitid to set
	 */
	public void setCommitid(String commitid) {
		this.commitid = commitid;
	}
	/**
	 * @return the json
	 */
	public String getJson() {
		return json;
	}
	/**
	 * @param json the json to set
	 */
	public void setJson(String json) {
		this.json = json;
	}
	/**
	 * @return the podip
	 */
	public String getPodip() {
		return podip;
	}
	/**
	 * @param podip the podip to set
	 */
	public void setPodip(String podip) {
		this.podip = podip;
	}
	/**
	 * @return the podport
	 */
	public String getPodport() {
		return podport;
	}
	/**
	 * @param podport the podport to set
	 */
	public void setPodport(String podport) {
		this.podport = podport;
	}
	/**
	 * @return the podname
	 */
	public String getPodname() {
		return podname;
	}
	/**
	 * @param podname the podname to set
	 */
	public void setPodname(String podname) {
		this.podname = podname;
	}
	

}
