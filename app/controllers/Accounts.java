package controllers;

import play.*;
import play.mvc.*;
import java.util.*;

import Driver.RecommenderAPI;
import models.*;

public class Accounts extends Controller {
	
	static RecommenderAPI rec;
	
	
	
	public static RecommenderAPI getRecommender() {
		return rec;
	}
	
	public static void signup()
	  {
		 
		    if (session.contains("logged_in_userid"))
		    {
		    	User user = Accounts.getLoggedInUser();
		    	user.logged_in = false;
		        user.save();
		        session.clear();
		        render();
		    }
		    else
		    {
		      render();
		    }
	  }

	public static void login()
	  {
		    if (session.contains("logged_in_userid")&& session.get("logged_in_user") != null)
		    {
		    	String userId = session.get("logged_in_userid");
		    	User user = RecommenderAPI.users.get(Integer.parseInt(userId));
		    	user.logged_in = false;
		        user.save();
		        session.clear();
		        render();
		    }
		    else
		    {
		      render();
		    }
	  }

	  public static void logout() throws Exception
	  {
		User user = Accounts.getLoggedInUser();
		user.logged_in = false;
	    session.clear();
	    index();
	  }

	public static void index() throws Exception
	  {
		rec = new RecommenderAPI();
		if(!rec.users.isEmpty()) {
			rec.store();
		}
		session.clear();
		rec.load();
		
		if (session.contains("logged_in_userid") && session.get("logged_in_userid") != null)
	    {
	    	User user = getLoggedInUser();
	    	user.logged_in = false;
	        user.save();
	        session.clear();
	        render();
	    }
	    else
	    {
	    	Collection<User> users = rec.users.values();
	        render(users);
	    }
		    
	  }

	  public static User getLoggedInUser()
	  {
	    User user = null;
	    if (session.contains("logged_in_userid")&& session.get("logged_in_userid") != null)
	    {
	      String userId = session.get("logged_in_userid");
	      Logger.info("userid " + userId);
	      user = rec.users.get(Integer.parseInt(userId));
	    }
	    else
	    {
	      login();
	    }
	    return user;
	  }
	  
	  public static void register(String firstName, String lastName, int age, String gender, String occupation, String email, String password) throws Exception
	  {
	    
	    rec.addUser(firstName, lastName, age, gender, occupation);
	    User thisUser = rec.users.get(rec.getUserCounter()-1);
	    rec.emailIndex.remove(thisUser.getEmail());
	    thisUser.setPassword(password);
	    thisUser.setEmail(email);
	    rec.emailIndex.put(thisUser.getEmail(), thisUser);
	    
	    index();
	  }

	  public static void authenticate(String email, String password) throws Exception
	  {
	    Logger.info("Attempting to authenticate with " + email + " " + password);
	    User user = rec.emailIndex.get(email);
	    if ((user != null) && (user.checkPassword(password) == true))
	    {
	      Logger.info("Authentication successful + " + user.getUserID());
	      Logger.info(user.getUserID() + " ");
	      session.put("logged_in_userid", user.getUserID());
	      user.logged_in = true;
	      Home.index();
	    }
	    else
	    {
	      Logger.info("Authentication failed");
	      login();
	    }
	  }

  
  
}