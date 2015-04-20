/**
 * 
 */
package at.ac.ait.ubicity.twitterplugin.dto;

import at.ac.ait.ubicity.commons.templates.AbstractDTO;

import com.google.gson.annotations.SerializedName;

public class TwitterUserDTO extends AbstractDTO {

	private final String id;
	private final String name;

	@SerializedName("screen_name")
	private final String screenName;

	public TwitterUserDTO(String id, String name, String screenName) {
		this.id = id;
		this.name = name;
		this.screenName = screenName;
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

}
