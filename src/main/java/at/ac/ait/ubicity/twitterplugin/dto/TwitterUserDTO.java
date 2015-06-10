/**
 * 
 */
package at.ac.ait.ubicity.twitterplugin.dto;

import at.ac.ait.ubicity.commons.templates.AbstractDTO;

import com.google.gson.annotations.SerializedName;

public class TwitterUserDTO extends AbstractDTO {

	private final String id;
	private final String name;
	private String location;
	private String lang;

	@SerializedName("screen_name")
	private final String screenName;

	private TwitterUserStatDTO stats;

	public TwitterUserDTO(String id, String name, String screenName) {
		this.id = id;
		this.name = name;
		this.screenName = screenName;
	}

	public TwitterUserDTO(String id, String name, String screenName, String location, String lang) {
		this(id, name, screenName);
		this.location = location;
		this.lang = lang;
	}

	public String getName() {
		return name;
	}

	public String getScreenName() {
		return screenName;
	}

	public String getId() {
		return id;
	}

	public String getLocation() {
		return location;
	}

	public String getLang() {
		return lang;
	}

	public void setStats(Integer followerCount, Integer friendsCount) {
		stats = new TwitterUserStatDTO(followerCount, friendsCount);
	}

	public TwitterUserStatDTO getStats() {
		return stats;
	}
}
