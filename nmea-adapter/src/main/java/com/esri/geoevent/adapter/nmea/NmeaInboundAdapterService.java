package com.esri.geoevent.adapter.nmea;

import javax.xml.bind.JAXBException;

import com.esri.ges.adapter.Adapter;
import com.esri.ges.adapter.AdapterServiceBase;
import com.esri.ges.adapter.util.XmlAdapterDefinition;
import com.esri.ges.core.component.ComponentException;

public class NmeaInboundAdapterService extends AdapterServiceBase
{
  public NmeaInboundAdapterService()
  {
    XmlAdapterDefinition xmlAdapterDefinition = new XmlAdapterDefinition(getResourceAsStream("adapter-definition.xml"));
    try
    {
      xmlAdapterDefinition.loadConnector(getResourceAsStream("connector-definition.xml"));
    }
    catch (JAXBException e)
    {
      throw new RuntimeException(e);
    }
    definition = xmlAdapterDefinition;
  }

  @Override
  public Adapter createAdapter() throws ComponentException
  {
    return new NmeaInboundAdapter(definition);
  }
}