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

import java.sql.Time;
/**
 * @author 338143
 *
 */
public class SearchResults {
	private int jobId;
	private String status;
	private String project;
	private String branch;
	private String time ;
	private Time duration;
	private int commitid;
	private String commitLog;

	/**
	 * @return
	 */
	public int getJobId() {
		return jobId;
	}
	/**
	 * @param jobId
	 */
	public void setJobId(int jobId) {
		this.jobId = jobId;
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
	public String getTime() {
		return time;
	}
	/**
	 * @param time
	 */
	public void setTime(String time) {
		this.time = time;
	}
	/**
	 * @return
	 */
	public Time getDuration() {
		return duration;
	}
	/**
	 * @param duration
	 */
	public void setDuration(Time duration) {
		this.duration = duration;
	}
	/**
	 * @return
	 */
	public int getCommitid() {
		return commitid;
	}
	/**
	 * @param commitid
	 */
	public void setCommitid(int commitid) {
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

}
