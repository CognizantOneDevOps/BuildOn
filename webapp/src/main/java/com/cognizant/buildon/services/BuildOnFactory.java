package com.cognizant.buildon.services;

import com.cognizant.buildon.dao.BuildOnDAO;
import com.cognizant.buildon.dao.BuildOnDAOImpl;

public class BuildOnFactory {

	public static BuildOnService getInstance() { 
	    return new BuildOnServiceImpl( );
	}
	
	public static BuildOnDAO  getDAOInstance() { 
	    return new BuildOnDAOImpl( );
	}
}

