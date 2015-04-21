/**
 * 
 */
package at.ac.ait.ubicity.twitterplugin.dto;

import java.util.List;

import at.ac.ait.ubicity.commons.templates.AbstractDTO;

import com.google.gson.annotations.SerializedName;

public class TwitterMsgDTO extends AbstractDTO {

	private final String text;
	private final String lang;

	@SerializedName("hash_tags")
	private List<String> hashTags;

	@SerializedName("mentioned_users")
	private List<TwitterUserDTO> mentionedUsers;

	public TwitterMsgDTO(String txt, String lang) {
		this.text = txt;
		this.lang = lang.toUpperCase();
	}

	public String getText() {
		return text;
	}

	public String getLang() {
		return lang;
	}

	public void setHashTags(List<String> hashTags) {
		this.hashTags = hashTags;
	}

	public List<String> getHashTags() {
		return hashTags;
	}

	public void setMentionedUsers(List<TwitterUserDTO> mentionedUsers) {
		this.mentionedUsers = mentionedUsers;
	}

	public List<TwitterUserDTO> getMentionedUsers() {
		return this.mentionedUsers;
	}
}
