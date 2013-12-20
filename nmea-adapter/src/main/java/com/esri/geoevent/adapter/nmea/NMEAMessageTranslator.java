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

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.spatial.Spatial;

public abstract class NMEAMessageTranslator
{
  protected abstract void translate(String trackId, ByteBuffer buffer, GeoEvent geoEvent, Spatial spatial) throws FieldException;

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
}
