/**
 * 
 */
package at.ac.ait.ubicity.twitterplugin.dto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.lucene.search.highlight.SimpleHTMLEncoder;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.Status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

/**
 * @author ruggenthalerc
 *
 */
public class TwitterDTO {
	private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

	private static SimpleDateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZZ");
	{
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@SerializedName("id")
	private final String tweetId;

	@SerializedName("created_at")
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

		@SerializedName("geo_point")
		private double[] geoPoint;

		private String city;

		private String country;

		@SerializedName("country_code")
		private String countryCode;

		public Place(GeoLocation loc, twitter4j.Place place) {

			if (loc != null) {
				geoPoint = new double[] { loc.getLongitude(), loc.getLatitude() };
			}
			if (place != null) {
				this.city = place.getName();
				this.country = place.getCountry();
				this.countryCode = place.getCountryCode().toUpperCase();
			}
		}

		public String getCity() {
			return city;
		}

		public String getCountry() {
			return country;
		}

		public String getCountryCode() {
			return countryCode;
		}

		public double[] getGeoPoint() {
			return geoPoint;
		}
	}

	public class Msg {
		private final String text;
		private final String lang;
		@SerializedName("hash_tags")
		private final List<String> hashTags;

		public Msg(String txt, String lang, List<String> hashes) {
			this.text = SimpleHTMLEncoder.htmlEncode(txt);
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

	public TwitterDTO(Status status) {

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

	public String toJson() {
		return gson.toJson(this);
	}
}
