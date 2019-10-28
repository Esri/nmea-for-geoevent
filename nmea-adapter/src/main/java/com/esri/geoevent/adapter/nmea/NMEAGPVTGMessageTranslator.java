/*
  Copyright 1995-2019 Esri

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

import java.util.Date;

import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.validation.ValidationException;

public class NMEAGPVTGMessageTranslator extends NMEAMessageTranslator
{
  @Override
  public void translate(GeoEvent geoEvent, String[] data) throws FieldException
  {
    int i = 1;

    // VTG Velocity Made Good
    geoEvent.setField(i++, convertToDouble(getTrue(data)));// True
    geoEvent.setField(i++, convertToDouble(getMag(data)));// Mag
    geoEvent.setField(i++, convertToDouble(getKnots(data)));// Knots
    geoEvent.setField(i++, convertToDouble(getKPH(data)));// KPH
    geoEvent.setField(i++, new Date());// ReceiveTime

    if (LOGGER.isTraceEnabled())
      LOGGER.trace("Translated GPVTG message string [ {0} ] to geoevent: [ {1} ]", String.join(" ", data), geoEvent);
  }

  @Override
  protected void validate(String[] data) throws ValidationException
  {
    if (data == null || data.length < 9)
      throw new ValidationException(LOGGER.translate("INVALID_NMEAGPVTG_MSG"));
  }

  private String getTrue(String[] data)
  {
    return getValue(data, "T");
  }

  private String getMag(String[] data)
  {
    return getValue(data, "M");
  }

  private String getKnots(String[] data)
  {
    return getValue(data, "N");
  }

  private String getKPH(String[] data)
  {
    return getValue(data, "K");
  }

  private String getValue(String[] data, String key)
  {
    for (int i = 1; i < data.length; i++)
    {
      if (data[i].toLowerCase().startsWith(key.toLowerCase()))
      {
        return data[i - 1];
      }
    }
    return "";
  }
}
