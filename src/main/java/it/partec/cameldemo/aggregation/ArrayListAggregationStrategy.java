package it.partec.cameldemo.aggregation;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import java.util.ArrayList;

public class ArrayListAggregationStrategy implements AggregationStrategy {

  @Override
  public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
    Object newBody = newExchange.getIn().getBody();
    ArrayList<Object> list;
    if (oldExchange == null) {
      list = new ArrayList<>();
      list.add(newBody);
      newExchange.getIn().setBody(list);
      return newExchange;
    } else {
      list = oldExchange.getIn().getBody(ArrayList.class);
      list.add(newBody);
      return oldExchange;
    }
  }
}
