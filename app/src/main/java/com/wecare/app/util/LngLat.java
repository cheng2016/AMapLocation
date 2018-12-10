package com.wecare.app.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class LngLat implements Cloneable {
	/**
	 * 纬度 (垂直方向)
	 */
	public final double latitude;
	/**
	 * 经度 (水平方向)
	 */
	public final double longitude;
	/**
	 * 格式化
	 */
	private static DecimalFormat format = new DecimalFormat("0.000000", new DecimalFormatSymbols(Locale.US));

	/**
	 * 使用传入的经纬度构造LatLng 对象，一对经纬度值代表地球上一个地点。
	 * 
	 * @param longitude
	 *            地点的经度，在-180 与180 之间的double 型数值。
	 * @param latitude
	 *            地点的纬度，在-90 与90 之间的double 型数值。
	 */
	public LngLat(double longitude, double latitude) {
		this(longitude, latitude, true);
	}

	/**
	 * 使用传入的经纬度构造LatLng 对象，一对经纬度值代表地球上一个地点
	 * 
	 * @param longitude
	 *            地点的经度，在-180 与180 之间的double 型数值。
	 * 
	 * @param latitude
	 *            地点的纬度，在-90 与90 之间的double 型数值。
	 * @param isCheck
	 *            是否需要检查经纬度的合理性，建议填写true
	 */
	public LngLat(double longitude, double latitude, boolean isCheck) {
		if (isCheck) {
			if ((-180.0D <= longitude) && (longitude < 180.0D))
				this.longitude = parse(longitude);
			else {
				throw new IllegalArgumentException("the longitude range [-180, 180].");
				// this.longitude = parse(((longitude - 180.0D) % 360.0D + 360.0D) %
				// 360.0D - 180.0D);
			}

			if ((latitude < -90.0D) || (latitude > 90.0D)) {
				throw new IllegalArgumentException("the latitude range [-90, 90].");
			}
			this.latitude = latitude;
			// this.latitude = parse(Math.max(-90.0D, Math.min(90.0D, latitude)));
		} else {
			this.latitude = latitude;
			this.longitude = longitude;
		}
	}

	/**
	 * 解析
	 * 
	 * @param d
	 * @return
	 */
	private static double parse(double d) {
		return Double.parseDouble(format.format(d));
	}

	public LngLat clone() {
		return new LngLat(this.latitude, this.longitude);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LngLat other = (LngLat) obj;
		if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
			return false;
		return true;
	}

	public String toString() {
		return "lat/lng: (" + this.latitude + "," + this.longitude + ")";
	}
}
