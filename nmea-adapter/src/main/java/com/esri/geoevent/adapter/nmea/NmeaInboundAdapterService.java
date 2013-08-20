package com.esri.geoevent.adapter.nmea;

import com.esri.ges.adapter.Adapter;
import com.esri.ges.adapter.AdapterServiceBase;
import com.esri.ges.adapter.util.XmlAdapterDefinition;
import com.esri.ges.core.component.ComponentException;

public class NmeaInboundAdapterService extends AdapterServiceBase
{
  public NmeaInboundAdapterService()
  {
    definition = new XmlAdapterDefinition(getResourceAsStream("adapter-definition.xml"));
  }

  @Override
  public Adapter createAdapter() throws ComponentException
  {
    return new NmeaInboundAdapter(definition);
  }
}
