/**
 * 
 */
package at.ac.ait.ubicity.twitterplugin.dto;

import java.util.Date;
import java.util.List;

import at.ac.ait.ubicity.commons.templates.AbstractDTO;

import com.google.gson.annotations.SerializedName;

public class TwitterDTO extends AbstractDTO {

	@SerializedName("id")
	private final String tweetId;

	@SerializedName("created_at")
	private final String createdAt;

	private TwitterUserDTO user;
	private final TwitterPlaceDTO place = new TwitterPlaceDTO();
	private TwitterMsgDTO msg;
	private TwitterMetaDTO meta;

	public TwitterDTO(String id, Date createdAt) {
		this.tweetId = id;
		this.createdAt = dateAsString(createdAt);
	}

	public void setUser(String userId, String userName, String screenName) {
		this.user = new TwitterUserDTO(userId, userName, screenName);
	}

	public void setMessage(String msg, String language, List<String> hashes, List<TwitterUserDTO> mentionedUsers) {
		this.msg = new TwitterMsgDTO(msg, language);
		this.msg.setHashTags(hashes);
		this.msg.setMentionedUsers(mentionedUsers);
	}

	public void setMetaData(Boolean isRetweeted, String retweetOrigin, String replyToMsg) {
		this.meta = new TwitterMetaDTO(isRetweeted, retweetOrigin, replyToMsg);
	}

	public void setPlace(String country, String countryCode, String city) {
		this.place.setPlace(country, countryCode, city);
	}

	public void setGeo(double longitude, double latitude) {
		this.place.setGeo(longitude, latitude);
	}

	public String getTweetId() {
		return tweetId;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public TwitterUserDTO getUser() {
		return user;
	}

	public TwitterPlaceDTO getPlace() {
		return place;
	}

	public TwitterMsgDTO getMsg() {
		return msg;
	}

	public TwitterMetaDTO getMeta() {
		return this.meta;
	}
}
