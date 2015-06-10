/**
 * 
 */
package at.ac.ait.ubicity.twitterplugin.dto;

import at.ac.ait.ubicity.commons.templates.AbstractDTO;

import com.google.gson.annotations.SerializedName;

public class TwitterUserStatDTO extends AbstractDTO {

	@SerializedName("followers_count")
	private final Integer followersCount;

	@SerializedName("friends_count")
	private final Integer friendsCount;

	public TwitterUserStatDTO(Integer followers, Integer friends) {
		this.followersCount = followers;
		this.friendsCount = friends;
	}

	public Integer getFollowersCount() {
		return followersCount;
	}

	public Integer getFriendsCount() {
		return friendsCount;
	}
}
