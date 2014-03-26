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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.adapter.AdapterDefinition;
import com.esri.ges.adapter.InboundAdapterBase;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.spatial.Spatial;

public class NmeaInboundAdapter extends InboundAdapterBase
{
  private static final Log LOG = LogFactory.getLog(NmeaInboundAdapter.class);
  private final Map<String, NMEAMessageTranslator> translators = new HashMap<String, NMEAMessageTranslator>();
  StringBuilder nameBuffer = new StringBuilder();

  public NmeaInboundAdapter(AdapterDefinition definition) throws ComponentException
  {
    super(definition);
  }
  
  private class GeoEventProducer implements Runnable
  {
    private String channelId;
    private List<byte[]> messages;

    public GeoEventProducer(String channelId, List<byte[]> messages)
    {
      this.channelId = channelId;
      this.messages = messages;
    }

    @Override
    public void run()
    {
      while (!messages.isEmpty())
      {
        String[] data = new String(messages.remove(0)).split(",");
        if (data.length > 0)
        {
          String gedName = "NMEA" + data[0];
          if (translators.containsKey(gedName))
          {
            try
            {
              NMEAMessageTranslator translator = translators.get(gedName);
              translator.validate(data);
              GeoEvent geoEvent = geoEventCreator.create(((AdapterDefinition)definition).getGeoEventDefinition(gedName).getGuid());
              geoEvent.setField(0, channelId);
              translator.translate(geoEvent, data);
              geoEventListener.receive(geoEvent);
            }
            catch (Exception e)
            {
              LOG.error("Exception while translating a NMEA message : " + e.getMessage());
            }
          }
        }
      }
    }
  }
  
  @Override
  public GeoEvent adapt(ByteBuffer buffer, String channelId)
  {
    // We don't need to implement anything in here because this method will
    // never get called. It would normally be called
    // by the base class's receive() method. However, we are overriding that
    // method, and our new implementation does not call
    // the adapter's adapt() method.
    return null;
  }

  @Override
  public void receive(ByteBuffer buffer, String channelId)
  {
    new Thread(new GeoEventProducer(channelId, index(buffer))).start();
  }
  
  private static List<byte[]> index(ByteBuffer in)
  {
    List<byte[]> messages = new ArrayList<byte[]>();
    for (int i = -1; in.hasRemaining();)
    {
      byte b = in.get();
      if (b == ((byte) '$')) // bom
      {
        i = in.position();
        in.mark();
      }
      else if (b == ((byte) '\r') || b == ((byte) '\n')) // eom
      {
        if (i != -1)
        {
          byte[] message = new byte[in.position() - 1 - i];
          System.arraycopy(in.array(), i, message, 0, message.length);
          messages.add(message);
        }
        i = -1;
        in.mark();
      }
      else if (messages.isEmpty() && i == -1)
        in.mark();
    }
    return messages;
  }
  
  @Override
  public void shutdown()
  {
    super.shutdown();
    translators.clear();
  }
  
  @Override
  public void setSpatial(Spatial spatial)
  {
    super.setSpatial(spatial);
    translators.put("NMEAGPGGA", new NMEAGPGGAMessageTranslator(spatial));
    translators.put("NMEAGPGLL", new NMEAGPGLLMessageTranslator(spatial));
    translators.put("NMEAGPRMC", new NMEAGPRMCMessageTranslator(spatial));
  }
}
