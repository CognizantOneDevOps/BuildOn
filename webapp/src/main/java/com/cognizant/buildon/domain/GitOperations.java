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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportHttp;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognizant.buildon.services.BuildOnFactory;
import com.cognizant.buildon.services.BuildOnService;
import com.jcraft.jsch.Session;

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * @author 338143
 *
 */

public class GitOperations {

	private static Logger logger=LoggerFactory.getLogger(GitOperations.class);
	private static JSONArray  trendsarray=null;
	private static JSONArray  projwisearray=null;
	private static JSONArray latestbuildarray=null;
	private static JSONArray comparebuildarray=null;

	/**
	 * @param email
	 * @param type 
	 * @return
	 */
	public static JSONObject getRepodetails(String email, String type) {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		String userId=email.toLowerCase();
		ArrayList<String> repoList=new ArrayList<>();
		List<ScmDetails> scmdetails=buildonservice.getUserScmDetails(userId,type);
		if(scmdetails.size() >0){
			ScmDetails scm=scmdetails.get(0);
			logger.debug(scm.getUrl());
			String scmurl=scm.getUrl().substring(scm.getUrl().lastIndexOf('/') + 1);
			String[] url = scmurl.split("\\."); 
			String repo = url[0]; 
			repoList.add(repo);
		}
		ExecutorService executor =Executors.newFixedThreadPool(6);
		executor.submit(new Runnable() {
			public void run() {
				trendsarray=buildonservice.getBuildtrends(userId);
			}
		});
		executor.submit(new Runnable() {
			public void run() {
				projwisearray= buildonservice.getProjectwiseBuild(userId);
			}
		});
		executor.submit(new Runnable() {
			public void run() {
				latestbuildarray= buildonservice.getLatestbuild(userId);
			}
		});
		executor.submit(new Runnable() {
			public void run() {
				comparebuildarray=buildonservice.getCompareBuild(userId);
			}
		});
		executor.shutdownNow();
		try {
			executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			logger.debug(e1.toString());
			Thread.currentThread().interrupt();
		}
		JSONObject jsonobj = new JSONObject();
		try {
			jsonobj.put("repoList",repoList);
			jsonobj.put("trendsarray",trendsarray);
			jsonobj.put("projwisearray",projwisearray);
			jsonobj.put("latestbuildarray",latestbuildarray);
			jsonobj.put("comparebuildarray",comparebuildarray);
		} catch (JSONException e) {
			logger.debug(e.toString());
		}
		return jsonobj;
	}


	/**
	 * @param email
	 * @param repo
	 * @param type 
	 * @return
	 */
	public static ArrayList<String> getBranchDetails(String email,String repo, String type) {

		BuildOnService buildonservice=BuildOnFactory.getInstance();
		String userId=email.toLowerCase();
		ArrayList<String> branches=new ArrayList<>();
		List<ScmDetails> scmdetails=buildonservice.getUserScmDetails(userId,type);
		ScmDetails scmdet=scmdetails.get(0);
		String url=scmdet.getUrl();
		int index=url.lastIndexOf('/');
		String scmurl=url.substring(0,index);
		scmurl=scmurl+"/"+repo+"."+Constants.GIT;
		String username=Constants.OAUTH;
		String pass=scmdet.getOauthtoken();
		CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username,pass);
		Collection<Ref> refs;
		try {
			logger.debug("before list");
			refs = Git.lsRemoteRepository().setTransportConfigCallback(getTransportConfigCallback())
					.setUploadPack(Constants.GIT_UPLOAD)					
					.setHeads(true)
					.setTags(true)
					.setRemote(scmurl)
					.setCredentialsProvider(credentialsProvider)
					.call();
			for (Ref ref : refs) {
				String repobranch= ref.getName().substring( ref.getName().lastIndexOf("/") + 1);
				branches.add(repobranch);
			}
		} catch (GitAPIException e) {
			logger.debug(e.toString());
		} 



		return branches;

	}

	/**
	 * @return
	 */
	public static TransportConfigCallback getTransportConfigCallback() {
		final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() { 
			@Override 
			protected void configure(OpenSshConfig.Host host, Session session) {
				// Do nothing  override method.
			} 
		}; 
		return new TransportConfigCallback() { 
			public void configure(Transport transport) { 
				if (transport instanceof TransportHttp) return; 
				SshTransport sshTransport = (SshTransport) transport; 
				sshTransport.setSshSessionFactory(sshSessionFactory); 
			} 
		}; 
	} 

	/**
	 * @param repoName
	 * @param branch
	 * @param email
	 * @param session
	 * @param type 
	 * @return
	 */
	public static String getJenkinsFile(String repoName,String branch,String email,HttpSession session, String type) {
		String userId=email.toLowerCase();
		Repository repository=null;
		String urlLocal=null; 
		session.setAttribute(Constants.PROJECT,repoName);
		File localPath=cloneRepo(userId,branch,repoName,session,type);
		urlLocal =localPath +"/."+Constants.GIT;
		byte[] content = null;
		String jenkinsContent=null;
		File gitDir = new File(urlLocal);
		try {
			repository = new FileRepository(gitDir);
			Ref head = repository.getRef("HEAD");
			RevWalk walk = new RevWalk(repository);
			RevCommit commit = walk.parseCommit(head.getObjectId());
			RevTree tree = commit.getTree();
			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(tree);
			treeWalk.setRecursive(true);
			while(treeWalk.next()) {
				if (treeWalk.getPathString().endsWith(Constants.JENKINS_FILE)) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					content=loader.getBytes();
					jenkinsContent = new String(content);
					break;
				}
				else  if(treeWalk.isSubtree()  &&  treeWalk.getPathString().equalsIgnoreCase(repoName) ) {
					treeWalk.enterSubtree();
					while(treeWalk.next()){
						if (treeWalk.getPathString().equals(Constants.JENKINS_FILE)) {
							ObjectId objectId = treeWalk.getObjectId(0);
							ObjectLoader loader = repository.open(objectId);
							content=loader.getBytes();
							jenkinsContent = new String(content);
							break;
						}

					}
				}
			}  

		} catch (IOException e) {
			logger.debug(e.toString());
		}
		repository.close();
		session.setAttribute(Constants.LOCALPATH,localPath);
		return jenkinsContent;
	} 

	/**
	 * @param url
	 * @param oauth
	 * @return
	 */
	public static boolean  checkrepo(String url,String oauth){
		boolean isSuccess=false;
		String username=Constants.OAUTH;
		String password=oauth;
		CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username,password);                               
		Collection<Ref> refs = null;
		try {
			refs = Git.lsRemoteRepository()
					.setTransportConfigCallback(getTransportConfigCallback())
					.setUploadPack(Constants.GIT_UPLOAD)                                                                   
					.setHeads(true)
					.setTags(true)
					.setRemote(url)
					.setCredentialsProvider(credentialsProvider)
					.call();
			for (Ref ref : refs) {
				isSuccess =true;
			} 

		} catch (InvalidRemoteException e) {
			logger.debug(e.toString());
			isSuccess =false;
		} catch (TransportException e) {
			logger.debug(e.toString());
			isSuccess =false;
		} catch (GitAPIException e) {
			logger.debug(e.toString());
			isSuccess =false;
		}

		return isSuccess;
	}

	/**
	 * @param email
	 * @param content
	 * @param session
	 * @param type 
	 * @return
	 */
	public static boolean saveJenkinsEdit(String email,String content,HttpSession session, String type) {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		String userId=email.toLowerCase();
		Object path=session.getAttribute(Constants.LOCALPATH);
		String urlLocal = path +"/."+Constants.GIT;
		boolean isSaved=false;
		Repository repository=null;
		try {
			File gitDir = new File(urlLocal);
			repository = new FileRepository(gitDir);
			Ref head = repository.getRef("HEAD");
			RevWalk walk = new RevWalk(repository);
			RevCommit commit = walk.parseCommit(head.getObjectId());
			RevTree tree = commit.getTree();
			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(tree);
			treeWalk.setRecursive(true);
			File file = new File(path+"/" +Constants.JENKINSFILE);
			while(treeWalk.next()) {
				if (treeWalk.getPathString().equals(Constants.JENKINS_FILE)) {
					try(Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path+"/" + treeWalk.getPathString()),"utf-8")) ) {   
						writer.write(content);
					}catch(IOException e){
						logger.debug(e.toString());	
					}
					isSaved=true;
					break;
				}else{
					try(FileOutputStream is = new FileOutputStream(file)){
						OutputStreamWriter osw = new OutputStreamWriter(is);    
						try(Writer writer = new BufferedWriter(osw)) {
							writer.write(content);

						}catch (IOException e) {
							logger.debug(e.toString());	
						}
					} catch (IOException e) {
						logger.debug(e.toString());	
					}
				}
			}

			Git git = Git.open(new File(urlLocal));
			repository = git.getRepository();
			git.add().addFilepattern(".").call();
			CommitCommand commitCommand = git.commit();
			commitCommand.setMessage(Constants.JENKINS_MOD_MSG);
			commitCommand.setAuthor(email,email);
			commitCommand.setCommitter(commitCommand.getAuthor());
			commitCommand.call();

			List<ScmDetails> scmdetails=buildonservice.getUserScmDetails(userId,type);
			ScmDetails scmdet=scmdetails.get(0);
			String username=Constants.OAUTH;
			String pass=scmdet.getOauthtoken();
			logger.debug(username+pass);
			CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username,pass);
			String currentBranch = git.getRepository().getBranch();
			List<RefSpec> specs = Arrays.asList(new RefSpec(currentBranch + ":" + currentBranch));
			PushCommand pushCommand = git.push();
			pushCommand.setCredentialsProvider(credentialsProvider);
			pushCommand.setPushAll();
			pushCommand.setRefSpecs(specs);
			pushCommand.setDryRun(false);
			pushCommand.call();
			repository.close();
		}catch (IOException e) {
			logger.debug(e.toString());
		} 
		catch (GitAPIException e) {
			logger.debug(e.toString());
		}

		return isSaved;
	}


	/**
	 * @param file
	 */
	public static void delete(File file){
		File fileDelete =null;
		if(file.isDirectory()){
			if(file.list().length==0){

				file.delete();
			}else{
				String files[] = file.list();
				for (String temp : files) {
					fileDelete=new File(file, temp);
					delete(fileDelete);
				}
				if(file.list().length==0){
					file.delete();

				}
			}

		}else{
			file.delete();
		}
	}



	/**
	 * @param userId
	 * @param branch
	 * @param repo
	 * @param session
	 * @param type 
	 * @return
	 * @throws IOException
	 * @throws InvalidRemoteException
	 * @throws TransportException
	 * @throws GitAPIException
	 */
	public static boolean callBuildon(String userId, String branch,String repo, HttpSession session, String type) 
			throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		String urlLocal=null;
		boolean isSuccess=false;
		File localPath=null;
		Object path=session.getAttribute("localPath");
		if(null!=path){
			urlLocal = path +"/."+Constants.GIT;
		}else{
			localPath=cloneRepo(userId,branch,repo,session,type);
			urlLocal =localPath +"/."+Constants.GIT;
		}
		File gitDir = new File(urlLocal);
		Repository repository = new FileRepository(gitDir);
		org.eclipse.jgit.lib.Config config = repository.getConfig();
		String url = config.getString("remote", "origin", "url");
		logger.debug(url);
		String[] urls = url.split("/", -1);
		String user=urls[urls.length-2];
		String full_name=user+"/"+repo;
		String id=RandomStringUtils.randomAlphanumeric(40);
		Ref headref = repository.getRef(branch);
		String ref=headref.getName();
		String namespace=userId.toLowerCase(); 
		JSONObject jsonobject=new JSONObject();
		JSONObject commitobject=new JSONObject();
		JSONArray commitarray=new JSONArray();
		JSONObject jsonrepo=new JSONObject();
		JSONObject jsonproject=new JSONObject(); 
		JSONObject authorobject=new JSONObject();

		if( null!= type && type.equals("gitlab")){
			try {
				commitobject.put("id",id.toLowerCase());
				authorobject.put("email",userId.toLowerCase());
				commitobject.put("author",authorobject);
				commitarray.put(commitobject); 
				jsonrepo.put("name",repo);
				jsonrepo.put("git_http_url","");
				jsonproject.put("path_with_namespace",full_name);
				jsonproject.put("namespace",namespace); 
				jsonproject.put("http_url",url); 
				jsonobject.put("project",jsonproject);
				jsonobject.put("ref", ref);
				jsonobject.put("commits", commitarray);
				jsonobject.put("repository", jsonrepo);
				logger.debug("callbuildon jsonrepo"+jsonobject.toString());

			} catch (JSONException e) {
				logger.debug(e.toString());
			}

		}else if(null!= type && type.equals("github")){
			try {
				JSONObject headcommit=new JSONObject();
				commitobject.put("id",id.toLowerCase());
				authorobject.put("email",userId.toLowerCase());
				commitobject.put("author",authorobject);
				commitarray.put(commitobject); 
				jsonrepo.put("name",repo);
				jsonrepo.put("full_name",full_name);
				jsonrepo.put("clone_url",url);
				jsonrepo.put("git_url","");
				jsonobject.put("ref", ref);
				jsonobject.put("commits", commitarray);
				jsonobject.put("repository", jsonrepo);
				jsonobject.put("head_commit", headcommit);
			} catch (JSONException e) {
				logger.debug(e.toString());
			}
		}
		logger.debug(jsonobject.toString());
		Properties props = readPropertymethod();
		String urlProps = props.getProperty("python.url");
		String urlPython = urlProps+"/setup";
		URL obj = new URL(urlPython);
		HttpURLConnection con = (HttpURLConnection)obj.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type","applictaion/json");
		con.setConnectTimeout(5000);
		OutputStreamWriter out =null;
		try{
			out= new OutputStreamWriter(con.getOutputStream());
			out.write(jsonobject.toString());
		}catch(Exception e){
			logger.debug(e.toString());
		}finally{
			out.close();
		}
		int responseCode = con.getResponseCode();
		logger.info("response"+responseCode);
		if(responseCode==200){
			isSuccess=true;
		}
		repository.close();
		File f=new File(path.toString());
		delete(f);
		return isSuccess;
	}
	private static Properties readPropertymethod() {
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
	 * @param email
	 * @param branch
	 * @param repo
	 * @param session
	 * @param type 
	 * @return
	 */
	private static File cloneRepo(String email, String branch, String repo, HttpSession session, String type) {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		String userId=email.toLowerCase();
		List<ScmDetails> scmdetails=buildonservice.getUserScmDetails(userId ,type);
		ScmDetails scmdet=scmdetails.get(0);
		String url=scmdet.getUrl();
		int index=url.lastIndexOf('/');
		String scmurl=url.substring(0,index);
		scmurl=scmurl+"/"+repo+".git";
		String username=Constants.OAUTH;
		String pass=scmdet.getOauthtoken();
		CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username,pass);
		File localPath =null;
		try {
			localPath = File.createTempFile("TestGitRepository", "");

			if(!localPath.delete()) {
				throw new IOException("Could not delete temporary file " + localPath);
			}
			try {
				Git.cloneRepository() 
				.setURI(scmurl)
				.setBranch(branch)
				.setCredentialsProvider(credentialsProvider)
				.setDirectory(localPath)
				.call();
			} catch (GitAPIException e) {
				logger.debug(e.toString());
			}
		} catch (IOException e) {
			logger.debug(e.toString());
		}
		return localPath;
	}


	/**
	 * @param commitId
	 * @return
	 */
	public static String getKuberneteslog(String commitId) {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		String podName=buildonservice.getPodname(commitId);
		Properties props = readPropertymethod();
		String url = props.getProperty("kubernetes.url");
		String namespace=props.getProperty("kubernetes.namespace");
		io.fabric8.kubernetes.client.Config config = new ConfigBuilder().withMasterUrl(url).build();
		KubernetesClient client = new DefaultKubernetesClient(config);
		String log = client.pods().inNamespace(namespace).withName(podName).getLog(true);
		client.close();
		return log;

	}


	/**
	 * @param commitId
	 * @return
	 */
	public static String getCIlog(String commitId) {
		logger.debug("CILOG start");
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		Service service=buildonservice.getServiceData(commitId);
		StringBuilder sb = new StringBuilder();
		BufferedReader br=null;
		String ciJob=null;
		String status=null;
		JSONArray jsonArray=null;
		String line = null;
		URL obj=null;
		try {
			jsonArray = new JSONArray(service.getJson());
			int length=jsonArray.length();
			if(jsonArray!=null && length>0){
				statusloop:
					for (int i = 0; i < length; i++) {
						JSONObject objectInArray = jsonArray.optJSONObject(i);
						status = objectInArray.getString(Constants.STATUS);
						if(null!= status && status.equals(Constants.INPROGRESS) ){
							ciJob=objectInArray.getString(Constants.JOBNAME);
							break;
						}else{
							continue statusloop;
						}
					}
			}
		} catch (JSONException e1) {
			logger.debug(e1.toString());
		}
		logger.debug(status);
		if(null!=status && status.equals(Constants.INPROGRESS)){
			String cilogsUrl="http://"+service.getPodip()+":"+service.getPodport()+"/job/"+"buildon-"+commitId+"/lastBuild/consoleText";
			logger.debug("cilogsUrl :"+cilogsUrl);
			try {
				obj = new URL(cilogsUrl);
				HttpURLConnection con = (HttpURLConnection)obj.openConnection();
				con.setRequestMethod("GET");
				con.connect();
				if (200==con.getResponseCode()) {
					logger.debug("response code 200");
					br = new BufferedReader(new InputStreamReader((con.getInputStream())));
				} else {
					logger.debug("response code not equal to 200");
					br = new BufferedReader(new InputStreamReader((con.getErrorStream())));
				}
			} catch ( IOException e1) {
				logger.debug(e1.toString());
			}
			try {
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
			} catch (IOException e) {
				logger.debug(e.toString());
			} finally {
				try {
					if (br != null)
						br.close();
				} catch (IOException e) {
					logger.debug(e.toString());
				}
			}
		}else{
			sb=new StringBuilder("next_job");
		}
		return sb.toString();
	}

	/**
	 * @param commitId
	 * @return
	 */
	public static String getHistoricKube(String commitId) {
		Properties props = readPropertymethod();
                String basepath=props.getProperty("kubernetes.logbasepath");
                String path = FilenameUtils.normalize(basepath+commitId+"/kube-"+commitId+".log");	
		StringBuilder sb = new StringBuilder();
		try(BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String line = null;         
			while ((line = reader.readLine()) != null){
				sb.append(line + "\n");   
			}               
		}catch (IOException e){
			logger.debug(e.toString());
		}
		return sb.toString();   
	}

	/**
	 * @param commitId
	 * @return
	 */
	public static String getHistoricCILogs(String commitId) {
		Properties props = readPropertymethod();
                String basepath=props.getProperty("kubernetes.logbasepath");
                String path = FilenameUtils.normalize(basepath+commitId+"/jenkins-"+commitId+".log");
		StringBuilder sb = new StringBuilder();
		try(BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String line = null;         
			while ((line = reader.readLine()) != null){
				sb.append(line + "\n");   
			} 

		}catch (IOException e){
			logger.debug(e.toString());
		}
		return sb.toString();   
	}


	/**
	 * @param commitid
	 * @param repo
	 * @param branch
	 * @return
	 * @throws JSONException 
	 * @throws IOException 
	 */
	public static boolean getDBServiceUpdate(String commitid, String repo, String branch)  {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		boolean statusupdate=false;
		String commitidresult=null;
		Properties props = readPropertymethod();
		String URI = props.getProperty("kubernetes.url");
		String namespace=props.getProperty("kubernetes.namespace");
		String podPort=props.getProperty("kubernetes.podport");
		String basepath=props.getProperty("kubernetes.logbasepath");
		String resultJSON=null;		
		io.fabric8.kubernetes.client.Config config = new ConfigBuilder().withMasterUrl(URI).build();
		String podNameValue =null;
		boolean statusflag=false;
		String podIP = null;
		int stage_node_inc=0;
		logger.debug("KubeClient Outside");
		try(KubernetesClient client = new DefaultKubernetesClient(config)){
			logger.debug("KubeClient Inside");			
			logger.debug("KubeClient Insider 1");			
			//PodList podList = client.pods().inNamespace(Constants.DEFAULTPOD).list();
			PodList podList = client.pods().inNamespace("default").list();
			KubernetesHelper.removeEmptyPods(podList);
			logger.debug("KubeClient Insider 2");
			for (Pod podname : podList.getItems()) {
				logger.debug("KubeClient Insider 3");
				podNameValue=podname.getMetadata().getName().toString();
				logger.debug("getpodNameValue:"+podNameValue);
				if(null!= podNameValue && podNameValue.contains(commitid) == true)
				{	
					logger.debug("Pod Name Match");
					statusflag=false;
					while(!statusflag)
					{
						String podStatus = client.pods().inNamespace("default").withName(podNameValue).get().getStatus().getPhase().toString();				
						if (podStatus.equalsIgnoreCase("running")) {
							podIP = client.pods().inNamespace("default").withName(podNameValue).get().getStatus().getPodIP().toString();
							statusflag=true;
						}
					}

				}
			}
			client.close();
		}catch(Exception e){ //kubernetes try
			logger.debug(e.toString());
		}

		logger.debug("getDBServiceUpdate:"+podNameValue+":"+commitid+":"+podIP);		
		String read = null;				
		ArrayList<String>  fetchcjob =  new ArrayList<String>();
		String cjobstatus =  null;			
		JSONArray jsontestarray=new JSONArray();				
		String fileName = basepath+commitid+"/Jenkinsfile";
		logger.debug("fileName..."+fileName+"commitid..."+commitid);
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))){
			while ((read = br.readLine()) != null) {
				if (read.contains("stage('") || read.contains("stage ('")) {
					String[] getcjob = read.split("'");
					fetchcjob.add(getcjob[1]);
					logger.debug("extractjcob count " + getcjob[1]);
				}	
			} 

		} catch (IOException e) {
			logger.debug(e.toString());
		}

		logger.debug("index value 1" + readFile(fileName).indexOf("stages(")+ "index value 1"+ readFile(fileName).indexOf("stages ("));			
		if ( readFile(fileName).indexOf("stages(") > -1 || readFile(fileName).indexOf("stages (") > -1)
		{
			stage_node_inc=1;
			logger.debug("if stage_node_inc value" + stage_node_inc);								   
		}
		else
		{
			stage_node_inc=0;
			logger.debug("else stage_node_inc value" + stage_node_inc);
		}

		for(int k = 0 ; k < fetchcjob.size() ; k++) {	
			JSONObject jsontest=new JSONObject();
			String currentcjob = fetchcjob.get(k);				
			logger.debug("currentcjob for NOTSTARTED" + currentcjob);
			try {
				jsontest.put(Constants.JOBNAME, currentcjob);
				jsontest.put(Constants.STATUS, "NOTSTARTED");
			} catch (JSONException e) {
				logger.debug(e.toString());
			}

			jsontestarray.put(jsontest);
		}
		resultJSON=jsontestarray.toString();
		logger.debug("resultJSON"+resultJSON);
		if(commitidresult==null)
		{
			statusupdate=buildonservice.getDBServiceInsert(podIP,podPort,podNameValue,resultJSON,commitid);
		}
		for(int i = 0 ; i < fetchcjob.size() ; i++) {
			JSONObject jsontest=new JSONObject();
			String currentcjob = fetchcjob.get(i);
			logger.debug("currentcjob if" + currentcjob);						
			boolean updateflag=false;
			boolean loopstatus=false;
			while(!updateflag)
			{
				String url = "http://" + podIP + ":" + podPort + "/job/" + "buildon-"+commitid+"/1/wfapi/describe";
				HttpURLConnection con=null;
				try{								
					URL obj = new URL(url);
					con = (HttpURLConnection) obj.openConnection();							
					con.setRequestMethod("GET");
					con.connect();							
					int responseCode = con.getResponseCode();
					logger.debug("Response Code : " + responseCode);							
					if(con.getResponseCode()==200)
					{
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String inputLine;
						StringBuilder response = new StringBuilder();
						while ((inputLine = in.readLine()) != null) {
							response.append(inputLine);
						}
						logger.debug("Response JSON : " + response.toString());		
						JSONObject stageobj = new JSONObject(response.toString());
						JSONArray jsonarray = stageobj.getJSONArray("stages");


						//JSONObject status = jsonarray.getJSONObject(i);
						JSONObject status = jsonarray.getJSONObject(stage_node_inc);						
						logger.debug("Response status : " +status.toString());	
						cjobstatus = status.getString("status");


						logger.debug("Response cjobstatus : " +cjobstatus +"loopstatus==>"+loopstatus);
						if (cjobstatus.equals("") || cjobstatus.equals("IN_PROGRESS"))
						{
							jsontest.put(Constants.JOBNAME, currentcjob);
							jsontest.put(Constants.STATUS,Constants.INPROGRESS);
							jsontestarray.put(i,jsontest);
							resultJSON=jsontestarray.toString();
							if (!loopstatus){
								statusupdate=buildonservice.getDBServiceUpdate(podIP,podPort,podNameValue,resultJSON,commitid);
								loopstatus=true;
							}

						}
						else
						{
							if(cjobstatus.equals("SUCCESS"))
							{	
								jsontest.put(Constants.JOBNAME, currentcjob);
								jsontest.put(Constants.STATUS, cjobstatus);
								updateflag=true;
								jsontestarray.put(i,jsontest);
								resultJSON=jsontestarray.toString();
								statusupdate=buildonservice.getDBServiceUpdate(podIP,podPort,podNameValue,resultJSON,commitid);
								stage_node_inc=stage_node_inc+1;
							}
							else if(cjobstatus.equals("FAILURE") || cjobstatus.equals("FAILED")){
								jsontest.put(Constants.JOBNAME, currentcjob);
								jsontest.put(Constants.STATUS,"FAILURE");
								updateflag=true;
								jsontestarray.put(i,jsontest);
								i=fetchcjob.size()+1;
								resultJSON=jsontestarray.toString();
								statusupdate=buildonservice.getDBServiceUpdate(podIP,podPort,podNameValue,resultJSON,commitid);
								stage_node_inc=stage_node_inc+1;
							}
							else
							{
								jsontest.put(Constants.JOBNAME, currentcjob);
								jsontest.put(Constants.STATUS, cjobstatus);
								updateflag=true;
								jsontestarray.put(i,jsontest);
								i=fetchcjob.size()+1;
								resultJSON=jsontestarray.toString();
								statusupdate=buildonservice.getDBServiceUpdate(podIP,podPort,podNameValue,resultJSON,commitid);
							}

						}



						in.close();
						con.disconnect();

					}
				}catch(Exception e)
				{
					logger.debug(e.toString());
				}
			}					 				
		}					
		return statusupdate;
	}//End of DBsevice update method

	public static StringBuilder readFile(String path) 
	{       
		// Assumes that a file article.rss is available on the SD card
		File file = new File(path);
		StringBuilder builder = new StringBuilder();
		if (!file.exists()) {
			throw new RuntimeException("File not found");
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return builder;
	}


	/**
	 * @param commitid
	 * @return
	 */
	public static boolean getHistoricDBService(String commitid){
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		boolean statusupdate=false;
		String commitidresult=null;
		Properties props = readPropertymethod();			
		String basepath=props.getProperty("kubernetes.logbasepath");
		String resultJSON=null;

		String read = null;		
		ArrayList<String>  fetchcjob =  new ArrayList<String>();;
		String cjobstatus =  null;			
		JSONArray jsontestarray=new JSONArray();
		String  fileName = FilenameUtils.normalize(basepath+commitid+"/Jenkinsfile");

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))){
			while ((read = br.readLine()) != null) {
				if (read.contains("stage('") || read.contains("stage ('")) {
					String[] getcjob = read.split("'");
					fetchcjob.add(getcjob[1]);
					logger.debug("extractjcob count " + getcjob[1]);
				}					
			} 

		} catch (IOException e) {
			logger.debug(e.toString());
		}
		for(int k = 0 ; k < fetchcjob.size() ; k++) {	
			JSONObject jsontest=new JSONObject();
			String currentcjob = fetchcjob.get(k);				
			logger.debug("currentcjob" + currentcjob);
			cjobstatus=buildonservice.getReportsStatus(commitid,currentcjob);
			logger.debug("cjobstatus" + cjobstatus);
			try {
				jsontest.put(Constants.JOBNAME, currentcjob);
				jsontest.put(Constants.STATUS,cjobstatus);
			} catch (JSONException e) {
				logger.debug(e.toString());
			}

			jsontestarray.put(jsontest);
		}
		resultJSON=jsontestarray.toString();
		logger.debug("resultJSON"+resultJSON);
		commitidresult=buildonservice.getServiceCommitId(commitid);
		logger.debug(commitidresult);

		if(commitidresult==null)
		{
			logger.debug("Inside if commitidresult historic"+commitidresult);
			statusupdate=buildonservice.getHistoricDBServiceInsert(commitid,resultJSON);
		}
		return statusupdate;
	}

	/**
	 * @param email
	 * @param repo
	 * @return
	 */
	public static ArrayList<String> gethistoricalBranch(String email, String repo) {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		String userId=email.toLowerCase();
		ArrayList<String> branches=new ArrayList<>();
		String repobranch=null;
		List<ScmDetails> scmdetails=buildonservice.getHistoricalURL(userId,repo);
		ScmDetails scmdet=scmdetails.get(0);
		String url=scmdet.getUrl();
		int index=url.lastIndexOf('/');
		String scmurl=url.substring(0,index);
		scmurl=scmurl+"/"+repo+"."+Constants.GIT;
		String username=Constants.OAUTH;
		String pass=scmdet.getOauthtoken();
		CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username,pass);
		Collection<Ref> refs;
		try {
			refs = Git.lsRemoteRepository().setTransportConfigCallback(getTransportConfigCallback())
					.setUploadPack(Constants.GIT_UPLOAD)					
					.setHeads(true)
					.setTags(true)
					.setRemote(scmurl)
					.setCredentialsProvider(credentialsProvider)
					.call();
			for (Ref ref : refs) {
				repobranch = ref.getName().substring( ref.getName().lastIndexOf("/") + 1);
				branches.add(repobranch);
			}
			return branches;
		} catch (GitAPIException e) {
			logger.debug(e.toString());
		} 
		return null;
	}


}

