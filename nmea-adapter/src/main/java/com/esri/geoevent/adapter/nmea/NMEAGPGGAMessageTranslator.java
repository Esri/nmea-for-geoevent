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

import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.validation.ValidationException;

public class NMEAGPGGAMessageTranslator extends NMEAMessageTranslator
{
  @Override
  public void translate(GeoEvent geoEvent, String[] data) throws FieldException
  {
    int i = 1;
    geoEvent.setField(i++, toTime(data[1], null));
    geoEvent.setField(i++, toPoint(data[2], data[4], "N".equals(data[3]), "E".equals(data[5])));
    geoEvent.setField(i++, convertToShort(data[6]));
    geoEvent.setField(i++, convertToShort(data[7]));
    geoEvent.setField(i++, convertToDouble(data[8]));
    geoEvent.setField(i++, convertToDouble(data[9]));
    geoEvent.setField(i++, data[10]);
    geoEvent.setField(i++, convertToDouble(data[11]));
    geoEvent.setField(i++, data[12]);
    geoEvent.setField(i++, data[13]);
    geoEvent.setField(i++, data[14].split("\\*")[0]);

    if (LOGGER.isTraceEnabled() && data != null && data.length > 0)
      LOGGER.trace("Translated GPGGA {0} to {1}", String.join(" ", data), geoEvent);
  }

  @Override
  protected void validate(String[] data) throws ValidationException
  {
    if (data == null || data.length != 15)
      throw new ValidationException(LOGGER.translate("INVALID_NMEAGPGG_MSG"));
  }
}
