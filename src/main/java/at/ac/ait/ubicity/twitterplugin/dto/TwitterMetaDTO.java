/**
 * 
 */
package at.ac.ait.ubicity.twitterplugin.dto;

import at.ac.ait.ubicity.commons.templates.AbstractDTO;

import com.google.gson.annotations.SerializedName;

public class TwitterMetaDTO extends AbstractDTO {
	@SerializedName("retweeted")
	private final Boolean isRetweeted;

	@SerializedName("retweet_count")
	private Integer retweetCount = 0;

	@SerializedName("reply_to_msg")
	private final String replyToMsg;

	public TwitterMetaDTO(Boolean isRetweeted, Integer retweetCount, String replyToMsg) {
		this.isRetweeted = isRetweeted;
		this.retweetCount = retweetCount;
		this.replyToMsg = replyToMsg;
	}

	public Boolean getIsRetweeted() {
		return this.isRetweeted;
	}

	public Integer getRetweetCount() {
		return this.retweetCount;
	}

	public String getReplyToMsg() {
		return this.replyToMsg;
	}
}
