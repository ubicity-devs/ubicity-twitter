/**
 * 
 */
package at.ac.ait.ubicity.twitterplugin.dto;

import at.ac.ait.ubicity.commons.templates.AbstractDTO;

import com.google.gson.annotations.SerializedName;

public class TwitterMetaDTO extends AbstractDTO {
	@SerializedName("retweeted")
	private final Boolean isRetweeted;

	@SerializedName("retweet_origin")
	private final String retweetOrigin;

	@SerializedName("reply_to_msg")
	private final String replyToMsg;

	public TwitterMetaDTO(Boolean isRetweeted, String retweetOrigin, String replyToMsg) {
		this.isRetweeted = isRetweeted;
		this.retweetOrigin = retweetOrigin;
		this.replyToMsg = replyToMsg;
	}

	public Boolean getIsRetweeted() {
		return this.isRetweeted;
	}

	public String getRetweetOrigint() {
		return this.retweetOrigin;
	}

	public String getReplyToMsg() {
		return this.replyToMsg;
	}
}
