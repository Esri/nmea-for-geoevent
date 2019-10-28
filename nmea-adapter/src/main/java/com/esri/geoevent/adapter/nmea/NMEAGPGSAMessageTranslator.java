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

public class NMEAGPGSAMessageTranslator extends NMEAMessageTranslator
{
  @Override
  public void translate(GeoEvent geoEvent, String[] data) throws FieldException
  {
    int i = 1;

    // GSA Satellite status
    geoEvent.setField(i++, data[1]); // Auto selection (A or M)
    geoEvent.setField(i++, data[2]);// 3D Fix (1=none,2=2D,3=3D)
    geoEvent.setField(i++, data[3]);// PRN 01
    geoEvent.setField(i++, data[4]);// PRN 02
    geoEvent.setField(i++, data[5]);// PRN 03
    geoEvent.setField(i++, data[6]);// PRN 04
    geoEvent.setField(i++, data[7]);// PRN 05
    geoEvent.setField(i++, data[8]);// PRN 06
    geoEvent.setField(i++, data[9]);// PRN 07
    geoEvent.setField(i++, data[10]);// PRN 08
    geoEvent.setField(i++, data[11]);// PRN 09
    geoEvent.setField(i++, data[12]);// PRN 10
    geoEvent.setField(i++, data[13]);// PRN 11
    geoEvent.setField(i++, data[14]);// PRN 12
    geoEvent.setField(i++, convertToDouble(data[15]));// PDOP
    geoEvent.setField(i++, convertToDouble(data[16]));// HDOP
    geoEvent.setField(i++, convertToDouble(data[17].split("\\*")[0]));// VDOP
    geoEvent.setField(i++, new Date());// Timestamp

    if (LOGGER.isTraceEnabled() && data != null && data.length > 0)
      LOGGER.trace("Translated GPGSA message string [ {0} ] to geoevent: [ {1} ]", String.join(" ", data), geoEvent);
  }

  @Override
  protected void validate(String[] data) throws ValidationException
  {
    if (data == null || data.length != 18)
      throw new ValidationException(LOGGER.translate("INVALID_NMEAGPGSA_MSG"));
  }
}
