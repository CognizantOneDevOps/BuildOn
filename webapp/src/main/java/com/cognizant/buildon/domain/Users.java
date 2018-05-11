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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
/**
 * @author 338143
 *
 */
@Entity
@Table(name = "buildon_users")
public class Users implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@SequenceGenerator(name = "usersSequence", sequenceName = "users_id_seq", allocationSize = 1, initialValue = 100)
	private Integer id;
	private String uname;
	private String upass;
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
	public String getUname() {
		return uname;
	}
	/**
	 * @param uname
	 */
	public void setUname(String uname) {
		this.uname = uname;
	}
	/**
	 * @return
	 */
	public String getUpass() {
		return upass;
	}
	/**
	 * @param upass
	 */
	public void setUpass(String upass) {
		this.upass = upass;
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
