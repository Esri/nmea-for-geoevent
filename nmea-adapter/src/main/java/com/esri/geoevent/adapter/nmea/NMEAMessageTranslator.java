/*
  Copyright 1995-2013 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
 */

package com.esri.geoevent.adapter.nmea;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.validation.ValidationException;

public abstract class NMEAMessageTranslator
{
	public NMEAMessageTranslator()
	{
	}

	protected abstract void translate(GeoEvent geoEvent, String[] data) throws FieldException;

	protected abstract void validate(String[] data) throws ValidationException;

	protected Date toTime(String time, String date)
	{
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		if (time != null)
		{
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2)));
			c.set(Calendar.MINUTE, Integer.parseInt(time.substring(2, 4)));
			c.set(Calendar.SECOND, Integer.parseInt(time.substring(4, 6)));
		}
		if (date != null)
		{
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(0, 2)));
			c.set(Calendar.MONTH, Integer.parseInt(date.substring(2, 4)) - 1);
			c.set(Calendar.YEAR, 2000 + Integer.parseInt(date.substring(4, 6)));
		}
		return c.getTime();
	}

	protected MapGeometry toPoint(String latitude, String longitude, boolean isNorth, boolean isEast)
	{
		double lat_d = convertToDouble(latitude.substring(0, 2), 0d);
		double lat_m = convertToDouble(latitude.substring(2, latitude.length()), 0d);
		double lat = lat_d + (lat_m / 60.0);
		double lon_d = convertToDouble(longitude.substring(0, 3), 0d);
		double lon_m = convertToDouble(longitude.substring(3, longitude.length()), 0d);
		double lon = lon_d + (lon_m / 60.0);
		lat = isNorth ? lat : -lat;
		lon = isEast ? lon : -lon;
		return new MapGeometry(new Point(lon, lat), SpatialReference.create(4326));
	}

	public boolean isEmpty(String s)
	{
		return (s == null || s.length() == 0);
	}

	public Double convertToDouble(String s, Double defaultValue)
	{
		if (!isEmpty(s))
		{
			try
			{
				return Double.parseDouble(s.replaceAll(",", "."));
			}
			catch (Exception e)
			{
				;
			}
		}
		return defaultValue;
	}

	public Double convertToDouble(Object value)
	{
		return (value != null) ? (value instanceof Double) ? (Double) value : convertToDouble(value.toString(), null) : null;
	}

	public Short convertToShort(Object value)
	{
		if (value != null)
		{
			if (value instanceof Short)
				return (Short) value;
			else
			{
				Double doubleValue = convertToDouble(value);
				if (doubleValue != null)
					return ((Long) Math.round(doubleValue)).shortValue();
			}
		}
		return null;
	}
}
