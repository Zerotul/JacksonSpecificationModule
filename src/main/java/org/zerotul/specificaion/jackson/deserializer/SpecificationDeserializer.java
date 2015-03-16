package org.zerotul.specificaion.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.zerotul.specification.FromSpecification;
import org.zerotul.specification.Specification;
import org.zerotul.specification.SpecificationImpl;
import org.zerotul.specification.WhereSpecification;
import org.zerotul.specification.order.OrderType;
import org.zerotul.specification.order.SimpleOrder;
import org.zerotul.specification.predicate.PredicateOperation;
import org.zerotul.specification.predicate.PredicateSpecification;
import org.zerotul.specification.restriction.Operator;
import org.zerotul.specification.restriction.SimpleRestriction;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

import static org.zerotul.specification.Specifications.from;

/**
 * Created by zerotul on 13.03.15.
 */
public class SpecificationDeserializer<V extends Serializable, T extends Specification<V>> extends JsonDeserializer<T> {

    private Map<String, PropertyDescriptor> propertyMap;

    @Override
    public T deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException{
        JsonNode node = parser.getCodec().readTree(parser);
        node = node.get("specification");
        if(node==null) throw new JsonParseException("specification not found", parser.getCurrentLocation());
        String recordType = node.get("recordType").asText();
        try {
            Class<V> clazz = (Class<V>) Class.forName(recordType);
            FromSpecification<V> from = from(clazz);
            initPropertyMap(clazz);
            if(node.get("where")!=null){
                JsonNode whereNode = node.get("where");
                addWhere(parser, from.where(), whereNode);
            }

            if(node.get("order")!=null){
                JsonNode orderNode = node.get("order");
                addOrder(parser, from, orderNode);
            }

            return (T) from.endFrom();
        } catch (ClassNotFoundException e) {
            throw new JsonParseException("property recordType "+recordType, parser.getCurrentLocation(), e);
        } catch (IntrospectionException e) {
            throw new JsonParseException("property recordType "+recordType, parser.getCurrentLocation(), e);
        }
    }

    private void initPropertyMap(Class<V> clazz) throws IntrospectionException {
        propertyMap = new HashMap<>();
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        for (PropertyDescriptor descriptor: beanInfo.getPropertyDescriptors()){
            propertyMap.put(descriptor.getName().trim().toLowerCase(), descriptor);
        }
        propertyMap = Collections.unmodifiableMap(propertyMap);
    }

    private void addWhere(JsonParser parser,WhereSpecification<V> where, JsonNode whereNode) throws IOException {
        addRestriction(parser, where, whereNode);
    }

    private void addRestriction(JsonParser parser,WhereSpecification<V> where, JsonNode whereNode) throws IOException {
        JsonNode restrictionNode = whereNode.get("restriction");
        if(restrictionNode!=null) {
            String operator = restrictionNode.get("operator").asText().trim().toLowerCase();
            Operator rOperator;
            switch (operator) {
                case "=":
                    rOperator = Operator.EQUAL;
                    break;
                case "like":
                    rOperator = Operator.LIKE;
                    break;
                case "!=":
                    rOperator = Operator.NOT_EQUAL;
                    break;
                default:
                    rOperator = Operator.EQUAL;
                    break;
            }

            String propertyName = restrictionNode.get("propertyName").asText().trim().toLowerCase();
            PropertyDescriptor descriptor = getDescriptor(parser, propertyName);
            Function<V, Object> getter = createGetter(parser, propertyName);
            Object value = parser.getCodec().readValue(restrictionNode.get("value").traverse(), descriptor.getReadMethod().getReturnType());
            PredicateSpecification<V> predicate = where.restriction(new SimpleRestriction<>(getter, value, rOperator));
            if (whereNode.get("predicate")!=null){
                JsonNode predicateNode = whereNode.get("predicate");
                addPredicate(parser, predicate, predicateNode);
            }
        }
    }

    private PropertyDescriptor getDescriptor(JsonParser parser, String propertyName) throws JsonParseException {
        PropertyDescriptor descriptor = propertyMap.get(propertyName);
        if (descriptor == null)
            throw new JsonParseException("descriptor for " + propertyName + " not found", parser.getCurrentLocation());
        return descriptor;
    }

    private Function<V, Object> createGetter(JsonParser parser, String propertyName) throws JsonParseException {
        PropertyDescriptor descriptor = propertyMap.get(propertyName);
        if (descriptor == null)
            throw new JsonParseException("descriptior for " + propertyName + " not found", parser.getCurrentLocation());

        Function<V, Object> getter = (V reecord) -> {
            try {
                return   descriptor.getReadMethod().invoke(reecord);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("invoke read method failed, methd = " + descriptor.getReadMethod().getName()+ " class = "+descriptor.getReadMethod().getDeclaringClass().getName(), e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("invoke read method failed, methd = " + descriptor.getReadMethod().getName()+ " class = "+descriptor.getReadMethod().getDeclaringClass().getName(), e);
            }
        };
        return getter;
    }

    private void addPredicate(JsonParser parser, PredicateSpecification<V> predicate, JsonNode predicateNode) throws IOException {
        JsonNode operationNode = predicateNode.get("operator");
        if(operationNode!=null){
            PredicateOperation operation;
            switch (operationNode.asText().trim().toLowerCase()){
                case "and": operation = PredicateOperation.AND;break;
                case "or": operation = PredicateOperation.OR;break;
                default: operation = PredicateOperation.AND;break;
            }

            JsonNode whereNode = predicateNode.get("where");
            if(whereNode==null) throw new JsonParseException("in the predicate is missing a required property where",parser.getCurrentLocation());
            WhereSpecification<V> where = predicate.predicate(operation);
            addWhere(parser, where, whereNode);
        }
    }

   private void addOrder(JsonParser parser, FromSpecification<V> from, JsonNode orderNode) throws IOException {
       if(orderNode.get("fields")!=null){
           CollectionType collectionType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
           List<String> filedNames = parser.getCodec().readValue(orderNode.get("fields").traverse(), collectionType);
           Function[] getters = new Function[filedNames.size()];
           for(int i=0; i < filedNames.size(); i++){
             getters[i]=createGetter(parser, filedNames.get(i));
           }

           if(getters.length==0)return;

           if(orderNode.get("orderType")==null) throw new JsonParseException("orderType can not be null",parser.getCurrentLocation());
           OrderType type = OrderType.valueOf(orderNode.get("orderType").asText().trim().toUpperCase());

           from.order(new SimpleOrder<V>(type, getters));
       }

   }
}
