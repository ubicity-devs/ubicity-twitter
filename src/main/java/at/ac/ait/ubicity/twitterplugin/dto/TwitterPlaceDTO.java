/**
 * 
 */
package at.ac.ait.ubicity.twitterplugin.dto;

import at.ac.ait.ubicity.commons.templates.AbstractDTO;

import com.google.gson.annotations.SerializedName;

public class TwitterPlaceDTO extends AbstractDTO {

	@SerializedName("geo_point")
	private double[] geoPoint;

	private String city;

	private String country;

	@SerializedName("country_code")
	private String countryCode;

	public void setPlace(String country, String countryCode, String city) {
		this.countryCode = countryCode.toUpperCase();
		this.country = country;
		this.city = city;
	}

	public void setGeo(double longitude, double latitude) {
		geoPoint = new double[] { longitude, latitude };
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