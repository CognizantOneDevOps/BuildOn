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
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
/**
 * @author 338143
 *
 */
@Entity
@Table(name = "buildon_reports")
public class Reports implements Serializable {
	
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String branch;
	private String  commitid;
	private String commitLog;
	private String project;
	private String scmuser;
    @Temporal(TemporalType.DATE)
	private Date startDate;
    @Temporal(TemporalType.DATE)
	private Date endDate;
	private String status;
    private String estimated_duration;
	private Integer duration;
	private Timestamp end_timestamp;
	private Timestamp start_timestamp;
	private String jobname; 
	private Timestamp ci_job_timestamp;
	private String ci_jobname; 
	private String logdir;
	private String TRIGGER_FROM;
	
	/**
	 * @return the logdir
	 */
	public String getLogdir() {
		return logdir;
	}

	/**
	 * @param logdir the logdir to set
	 */
	public void setLogdir(String logdir) {
		this.logdir = logdir;
	}

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
	 * @return
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return
	 */
	public String getProject() {
		return project;
	}

	/**
	 * @param project
	 */
	public void setProject(String project) {
		this.project = project;
	}

	/**
	 * @return
	 */
	public String getBranch() {
		return branch;
	}

	/**
	 * @param branch
	 */
	public void setBranch(String branch) {
		this.branch = branch;
	}


	/**
	 * @return
	 */
	public String getCommitid() {
		return commitid;
	}

	/**
	 * @param commitid
	 */
	public void setCommitid(String commitid) {
		this.commitid = commitid;
	}

	/**
	 * @return
	 */
	public String getCommitLog() {
		return commitLog;
	}

	/**
	 * @param commitLog
	 */
	public void setCommitLog(String commitLog) {
		this.commitLog = commitLog;
	}

	/**
	 * @return
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @return
	 */
	public String getScmuser() {
		return scmuser;
	}

	/**
	 * @param scmuser
	 */
	public void setScmuser(String scmuser) {
		this.scmuser = scmuser;
	}

	/**
	 * @param endDate
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the estimated_duration
	 */
	public String getEstimated_duration() {
		return estimated_duration;
	}

	/**
	 * @param estimated_duration the estimated_duration to set
	 */
	public void setEstimated_duration(String estimated_duration) {
		this.estimated_duration = estimated_duration;
	}

	/**
	 * @return the duration
	 */
	public Integer getDuration() {
		return duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	/**
	 * @return the end_timestamp
	 */
	public Timestamp getEnd_timestamp() {
		return end_timestamp;
	}

	/**
	 * @param end_timestamp the end_timestamp to set
	 */
	public void setEnd_timestamp(Timestamp end_timestamp) {
		this.end_timestamp = end_timestamp;
	}

	/**
	 * @return the start_timestamp
	 */
	public Timestamp getStart_timestamp() {
		return start_timestamp;
	}

	/**
	 * @param start_timestamp the start_timestamp to set
	 */
	public void setStart_timestamp(Timestamp start_timestamp) {
		this.start_timestamp = start_timestamp;
	}

	/**
	 * @return the jobname
	 */
	public String getJobname() {
		return jobname;
	}

	/**
	 * @param jobname the jobname to set
	 */
	public void setJobname(String jobname) {
		this.jobname = jobname;
	}

	/**
	 * @return the ci_job_timestamp
	 */
	public Timestamp getCi_job_timestamp() {
		return ci_job_timestamp;
	}

	/**
	 * @param ci_job_timestamp the ci_job_timestamp to set
	 */
	public void setCi_job_timestamp(Timestamp ci_job_timestamp) {
		this.ci_job_timestamp = ci_job_timestamp;
	}

	/**
	 * @return the ci_jobname
	 */
	public String getCi_jobname() {
		return ci_jobname;
	}

	/**
	 * @param ci_jobname the ci_jobname to set
	 */
	public void setCi_jobname(String ci_jobname) {
		this.ci_jobname = ci_jobname;
	}

	 /**
	 * @return the tRIGGER_FROM
	 */
	public String getTRIGGER_FROM() {
		return TRIGGER_FROM;
	}

	/**
	 * @param tRIGGER_FROM the tRIGGER_FROM to set
	 */
	public void setTRIGGER_FROM(String TRIGGER_FROM) {
		this.TRIGGER_FROM = TRIGGER_FROM;
	}



	

}

