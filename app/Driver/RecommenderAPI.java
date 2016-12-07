package Driver;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import edu.princeton.cs.introcs.Stopwatch;
import models.Movie;
import models.Rating;
import models.User;

public class RecommenderAPI {
	
	static File file = new File("data.xml");
	private static XMLSerializer xml = new XMLSerializer(file);

	static Data data = new Data();
	private static int userCounter = 944;
	private static int movieCounter = 1684;
	
	//public static List<Map> dataMap = new ArrayList<Map>();
	//public static List<Rating> allRatings = new ArrayList<Rating>();
	public static Map<Integer, List<Rating>> userRatings = new HashMap<Integer, List<Rating>>();
	
	public static Map<Integer, User> users = new Hashtable<>();
	//public static Map<String, User>   emailIndex      = new HashMap<>();
	public static Map<Integer, Movie> movies = new HashMap<Integer, Movie>();
	public static Map<Integer, String> genres = new HashMap<Integer, String>();
	
	public static void main(String[] args) throws Exception {
				
//		data.loadOriginalData();
//    	displayUserRatings(2);
//    	store();
		
		//takes about 9 seconds 
		load(); 
		
		displayUserRatings(2);
		
		List<Movie> recommendations = getUserRecommendations(2,20);
		System.out.println("\n"+users.get(2).getFirstName() + "'s Recommendations:");
		for(Movie movie : recommendations) {
			System.out.println("Movie: " + movie.getTitle());
		}
//		
	}
	
	public RecommenderAPI() {
		
	}	

	public  void clearDatabase() 
	  {
	    users.clear();
	    emailIndex.clear();
	    movies.clear();
	    genres.clear();
	  }
	
	public static void addUser(String firstName, String lastName,int age,String gender,String occupation) {
	    User user = new User(firstName, lastName, age, gender, occupation);
	    user.save();
	    user.setUserID(userCounter);
	    if(!users.containsKey(userCounter)) {
		    users.put(userCounter, user);
		    userCounter++; 
	    }
	}
	
	public static void removeUser(int userID) {
		users.remove(userID);
	}
	
	public static void addMovie(String title,int year, String releaseDate, String url, String genre) {
		Movie movie = new Movie(title, year, releaseDate, url, genre);
		movie.setMovieID(movieCounter);
		movie.save();
		if(!movies.containsKey(movieCounter)) {
			
			movies.put(movieCounter, movie);
			movieCounter++;
		}
	}
	
	public static void addRating(int userID,int movieID,int rating) {
		 data.removeDuplicateRatings(userID, movieID, rating, users.get(userID).getRatings());
		 Rating thisRating = new Rating(userID, movieID, rating);
		 users.get(userID).addRating(thisRating);
	}
	
	public static Movie getMovie(int movieID) {
		return movies.get(movieID);
	}
	
	public static List<Rating> getUserRatings(int userID) {
		return users.get(userID).getRatings();
	}
	
	public static List<Movie> getUserRecommendations(int userID, int howMany) {
		double similarity = 0;
		List<Double> similarities = new ArrayList<>();
		
		User thisUser = users.get(userID);
		List<Rating> thisUserRatings = thisUser.getRatings();
		
		for(User thatUser : users.values()) {
			List<Rating> differentRatings = thatUser.getRatings();
			similarity = 0;
			if(!thisUser.equals(thatUser)) {
				for(Rating thisrating : thisUserRatings) {
					for(Rating thatrating : thatUser.getRatings()) {
						if(thisrating.getObject2() == thatrating.getObject2()) {
							similarity = similarity + (thisrating.getRating()*thatrating.getRating());
							differentRatings.remove(thatrating);
							break;
						} 
					}
				}
			}
			similarities.add(similarity);
			thatUser.setSimilarity(similarity);
			thatUser.setDifferentRatings(differentRatings);
				
		}
		Collections.sort(similarities);
		Collections.reverse(similarities);
		
		List<Movie> recommendations = new ArrayList<Movie>();
		
		for(Double thisSimilarity : similarities) {
			for(User user : users.values()) {
				if(!thisUser.equals(user) && user.getSimilarity() == thisSimilarity && !user.differentRatings.isEmpty()) {
					for(Rating rating : user.getDifferentRatings()) {
						if(recommendations.size() < howMany) {
							if(!recommendations.contains(movies.get(rating.getObject2()))) {
								recommendations.add(movies.get(rating.getObject2()));
							}
						} else { 
							return recommendations;
						}
					}
				}
			}
		}
		
		return recommendations;
	}
	
	
	public static List<Movie> getTopTenMovies(int userID) {
		
		List<Movie> recs = getUserRecommendations(userID, 10);
		return recs;
	}
	
	
//	public void addUser(String firstName, String lastName,int age,String gender,String occupation);
//	public void removeUser(int userID);
//	public void addMovie(String title,int year, String releaseDate, String url);
//	public void addRating(int userID,int movieID,int rating);
//	public Movie getMovie(int movieID);
//	public Map<Integer, Integer> getUserRatings(int userID);
//	public User getUserRecommendations(int userID);
//	public List<Movie> getTopTenMovies();
//	public Map<Integer,List<Map>>load();
//	public void write();
	
	public static void displayUserRatings(int userID) {
		System.out.println(users.get(userID).getFirstName() + " rated the following movies: ");
		for(Rating rating : users.get(userID).getRatings()) {
			System.out.println("Movie: " + movies.get(rating.getObject2()).getTitle());
			System.out.println("Rating: " + rating.getRating());
		}
	}
	
	public static void displayMovieRatings(int movieID) {
		System.out.println("\n" + movies.get(movieID).getTitle()+" was rated by the following users:");
		for(Rating rating : movies.get(movieID).getRatings()) {
			System.out.println("User: " + users.get(rating.getObject1()).getFirstName() + " " + users.get(rating.getObject2()).getLastName());
			System.out.println("Rating: " + rating.getRating());
		}
	}
	
	
	
	//XML//
		@SuppressWarnings("unchecked")
		public static void load() throws Exception {
			xml.read();
			//allRatings = (List<Rating>) xml.pop();
			genres = (Map<Integer, String>) xml.pop();
			movies = (Map<Integer, Movie>) xml.pop();
			//emailIndex = (Map<String, User>) xml.pop();
			users = (Map<Integer, User>) xml.pop();
			movieCounter = (int) xml.pop();
			userCounter = (int) xml.pop();
			
			
		}
		
		static void store() throws Exception {
			xml.push(userCounter);
			xml.push(movieCounter);
			xml.push(users);
			//xml.push(emailIndex);
			xml.push(movies);
			xml.push(genres);
			//xml.push(allRatings);
			xml.write();
		}
}
