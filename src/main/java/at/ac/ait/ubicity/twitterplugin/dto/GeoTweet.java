/**
 * 
 */
package at.ac.ait.ubicity.twitterplugin.dto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.Status;

/**
 * @author ruggenthalerc
 *
 */
public class GeoTweet {

	private static SimpleDateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	{
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private final String tweetId;
	private final String createdAt;

	public class User {
		private final String name;

		public User(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public class Place {
		private double[] longLat;
		private double[] latLong;
		private String country;
		private String countryCode;

		public Place(GeoLocation loc, twitter4j.Place place) {

			if (loc != null) {
				latLong = new double[] { loc.getLatitude(), loc.getLongitude() };
				longLat = new double[] { loc.getLongitude(), loc.getLatitude() };
			}

			if (place != null) {
				this.country = place.getCountry();
				this.countryCode = place.getCountryCode().toUpperCase();
			}
		}

		public String getCountry() {
			return country;
		}

		public String getCountryCode() {
			return countryCode;
		}

		public double[] getLongLat() {
			return longLat;
		}

		public double[] getLatLong() {
			return latLong;
		}
	}

	public class Msg {
		private final String text;
		private final String lang;
		private final List<String> hashTags;

		public Msg(String txt, String lang, List<String> hashes) {
			this.text = txt;
			this.lang = lang.toUpperCase();
			this.hashTags = hashes;
		}

		public String getText() {
			return text;
		}

		public String getLang() {
			return lang;
		}

		public List<String> getHashTags() {
			return hashTags;
		}
	}

	private final User user;
	private final Place place;
	private final Msg msg;

	public GeoTweet(Status status) {

		this.tweetId = String.valueOf(status.getId());
		this.createdAt = df.format(status.getCreatedAt());

		this.user = new User(status.getUser().getName());
		this.msg = new Msg(status.getText(), status.getLang(),
				calcHashTags(status.getHashtagEntities()));
		this.place = new Place(status.getGeoLocation(), status.getPlace());
	}

	private List<String> calcHashTags(HashtagEntity[] entities) {

		List<String> hashes = new ArrayList<String>();

		if (entities != null) {
			for (int i = 0; i < entities.length; i++) {
				hashes.add(entities[i].getText());
			}
		}

		return hashes;
	}

	public String getTweetId() {
		return tweetId;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public User getUser() {
		return user;
	}

	public Place getPlace() {
		return place;
	}

	public Msg getMsg() {
		return msg;
	}
}
