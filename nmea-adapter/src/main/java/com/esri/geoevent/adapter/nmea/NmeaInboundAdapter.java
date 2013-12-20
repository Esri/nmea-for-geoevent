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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.adapter.AdapterDefinition;
import com.esri.ges.adapter.InboundAdapterBase;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.messaging.MessagingException;

public class NmeaInboundAdapter extends InboundAdapterBase
{
  private static final Log                         LOG         = LogFactory.getLog(NmeaInboundAdapter.class);
  private final Map<String, NMEAMessageTranslator> translators = new HashMap<String, NMEAMessageTranslator>();
  StringBuilder                                    nameBuffer  = new StringBuilder();

  public NmeaInboundAdapter(AdapterDefinition definition) throws ComponentException
  {
    super(definition);
    translators.put("NMEAGPGGA", new NMEAGPGGAMessageTranslator());
    translators.put("NMEAGPGLL", new NMEAGPGLLMessageTranslator());
    translators.put("NMEAGPRMC", new NMEAGPRMCMessageTranslator());
  }

  @Override
  public GeoEvent adapt(ByteBuffer buffer, String channelId)
  {
    if (buffer != null && buffer.remaining() > 6)
    {
      try
      {
        scanForSentenceBeginning(buffer);
      }
      catch (BufferUnderflowException ex)
      {
        LOG.error("Could not find a NMEA Sentence.");
      }
      byte[] chars = new byte[6];
      buffer.get(chars);
      String edName = parseEventDefinitionName(new String(chars));
      if (translators.containsKey(edName))
        try
        {
          GeoEvent geoEvent = geoEventCreator.create(((AdapterDefinition) definition).getGeoEventDefinition(edName).getGuid());
          translators.get(edName).translate(channelId, buffer, geoEvent, spatial);
          return geoEvent;
        }
        catch (MessagingException e)
        {
          LOG.error("Exception while translating a NMEA message : " + e.getMessage());
        }
        catch (FieldException e)
        {
          LOG.error("Exception while translating a NMEA message : " + e.getMessage());
        }
    }
    return null;
  }

  private void scanForSentenceBeginning(ByteBuffer buffer) throws BufferUnderflowException
  {
    buffer.mark();
    nameBuffer.setLength(0);
    for (int i = 0; i < 6; i++)
      nameBuffer.append((char) buffer.get());
    while (true)
    {
      String nameAsString = nameBuffer.toString();
      if ("$GPGGA".equals(nameAsString))
      {
        buffer.position(buffer.position() - 6);
        buffer.mark();
        return;
      }
      else if ("$GPRMC".equals(nameAsString))
      {
        buffer.position(buffer.position() - 6);
        buffer.mark();
        return;
      }
      else if ("$GPGLL".equals(nameAsString))
      {
        buffer.position(buffer.position() - 6);
        buffer.mark();
        return;
      }
      nameBuffer.deleteCharAt(0);
      nameBuffer.append((char) buffer.get());
    }

  }

  private String parseEventDefinitionName(String msgFormat)
  {
    if ("$GPGGA".equals(msgFormat))
      return "NMEAGPGGA";
    else if ("$GPRMC".equals(msgFormat))
      return "NMEAGPRMC";
    else if ("$GPGLL".equals(msgFormat))
      return "NMEAGPGLL";
    return null;
  }
}
