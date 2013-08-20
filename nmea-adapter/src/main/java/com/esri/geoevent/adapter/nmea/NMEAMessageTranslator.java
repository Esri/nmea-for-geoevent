package com.esri.geoevent.adapter.nmea;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;

import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.spatial.Spatial;

public abstract class NMEAMessageTranslator
{
  protected abstract void translate(String trackId, ByteBuffer buffer, GeoEvent geoEvent, Spatial spatial) throws FieldException;

  protected Date toTime(String time, String date)
  {
    Calendar c = Calendar.getInstance();
    if (time != null)
    {
      c.set(Calendar.HOUR, Integer.parseInt(time.substring(0, 2)));
      c.set(Calendar.MINUTE, Integer.parseInt(time.substring(2, 4)));
      c.set(Calendar.SECOND, Integer.parseInt(time.substring(4, 6)));
    }
    if (date != null)
    {
      c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(0, 2)));
      c.set(Calendar.MONTH, Integer.parseInt(date.substring(2, 4)));
      c.set(Calendar.YEAR, 2000 + Integer.parseInt(date.substring(4, 6)));
    }
    return c.getTime();
  }
}
