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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.esri.ges.adapter.AdapterDefinition;
import com.esri.ges.adapter.InboundAdapterBase;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.messaging.GeoEventCreator;

public class NmeaInboundAdapter extends InboundAdapterBase
{
  private static final BundleLogger                LOGGER        = BundleLoggerFactory.getLogger(NmeaInboundAdapter.class);

  private final Map<String, NMEAMessageTranslator> translators   = new HashMap<String, NMEAMessageTranslator>();
  StringBuilder                                    nameBuffer    = new StringBuilder();
  private Map<String, GeoEventDefinition>          definitionMap = new HashMap<String, GeoEventDefinition>();
  private ExecutorService                          threadPool;

  public NmeaInboundAdapter(AdapterDefinition definition) throws ComponentException
  {
    super(definition);
    translators.put("NMEAGPGGA", new NMEAGPGGAMessageTranslator());
    translators.put("NMEAGPGLL", new NMEAGPGLLMessageTranslator());
    translators.put("NMEAGPRMC", new NMEAGPRMCMessageTranslator());
    translators.put("NMEAGPGSA", new NMEAGPGSAMessageTranslator());
    translators.put("NMEAGPVTG", new NMEAGPVTGMessageTranslator());
    LOGGER.trace("Created NMEA adapter and translators");
  }

  private class GeoEventProducer implements Runnable
  {
    private String       channelId;
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
        if (Thread.interrupted())
          return;
        String msg = new String(messages.remove(0));
        if (msg != null && !msg.trim().isEmpty())
        {
          LOGGER.trace("Processing message: {0}", msg);
          String[] data = msg.split(",");
          if (data != null && data.length > 0)
          {
            LOGGER.trace("Message type {0} has {1} parts", data[0], data.length);
            String gedName = "NMEA" + data[0];
            if (translators.containsKey(gedName))
            {
              try
              {
                NMEAMessageTranslator translator = translators.get(gedName);
                translator.validate(data);
                GeoEvent geoEvent = geoEventCreator.create(getGeoEventDefinition(gedName).getGuid());
                geoEvent.setField(0, channelId);
                translator.translate(geoEvent, data);
                geoEventListener.receive(geoEvent);
              }
              catch (Exception error)
              {
                LOGGER.info("TRANSLATION_ERROR", error.getMessage());
                LOGGER.debug(error.getMessage(), error);
              }
            }
            else
            {
              LOGGER.info("Can't translate message type {0}: {1}", gedName, msg);
            }
          }else {
            LOGGER.info("Can't translate empty/null message array.");
          }
        }
        else
        {
          LOGGER.info("Can't translate empty/null message string");
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
    if (threadPool == null || threadPool.isShutdown() || threadPool.isTerminated())
      threadPool = Executors.newCachedThreadPool();
    threadPool.execute(new GeoEventProducer(channelId, index(buffer)));
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
    try
    {
      super.shutdown();
      translators.clear();
      definitionMap.clear();
      threadPool.shutdownNow();
    }
    catch (Exception e)
    {
      LOGGER.trace("Failed to shutdown gracefully. Continuing on...", e);
    }

    threadPool = null;
  }

  private void loadGeoEventDefinitions()
  {
    ((AdapterDefinition) definition).getGeoEventDefinitions().forEach((key, def) ->
      {
        getGeoEventDefinition(def.getName());
      });
  }

  private GeoEventDefinition getGeoEventDefinition(String gedName)
  {
    GeoEventDefinition result = null;
    try
    {
      LOGGER.trace("Getting GeoEvent Defintion for channel {0}", gedName);
      GeoEventDefinition adapterDef = ((AdapterDefinition) definition).getGeoEventDefinition(gedName);
      String guid = adapterDef.getGuid();
      LOGGER.trace("Getting GeoEvent Defintion for channel {0} using guid {1}", gedName, guid);
      result = definitionMap.get(guid);
      if (result == null)
      {
        LOGGER.trace("Getting GeoEvent Defintion from Definition Manager for channel {0} using guid {1}", gedName, guid);
        result = geoEventCreator.getGeoEventDefinitionManager().getGeoEventDefinition(guid);
        if (result == null)
        {
          String name = adapterDef.getName();
          String owner = adapterDef.getOwner();
          LOGGER.trace("Failed to find GeoEvent Defintion for channel {0} using guid, checking with name/owner: {1}/{2}", gedName, name, owner);
          result = geoEventCreator.getGeoEventDefinitionManager().searchGeoEventDefinition(name, owner);

          if (result == null)
          {
            LOGGER.trace("Failed to find GeoEvent Defintion channel {0} using name/owner, using definition returned with name = {0}", gedName, name);
            Collection<GeoEventDefinition> possibleDefs = geoEventCreator.getGeoEventDefinitionManager().searchGeoEventDefinitionByName(name);
            if (possibleDefs.size() > 0)
            {
              result = possibleDefs.iterator().next();
            }
          }
        }
        if (result != null)
        {
          definitionMap.put(guid, result);
        }
      }
    }
    catch (Exception e)
    {
      LOGGER.warn("Failed to get {0} GeoEvent Definition, cannot create new {0} event.", e, gedName);
    }
    return result;
  }

  @Override
  public void setGeoEventCreator(GeoEventCreator geoEventCreator)
  {
    super.setGeoEventCreator(geoEventCreator);
    loadGeoEventDefinitions();
  }
}
