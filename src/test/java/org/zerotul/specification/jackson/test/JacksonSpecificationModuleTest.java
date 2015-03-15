package org.zerotul.specification.jackson.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;
import org.zerotul.specificaion.jackson.SpecificationModule;
import org.zerotul.specification.Specification;
import org.zerotul.specification.SpecificationImpl;
import org.zerotul.specification.exception.BuildException;

import org.zerotul.specification.jackson.test.mock.Mock;
import org.zerotul.specification.predicate.PredicateOperation;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.zerotul.specification.Specifications.from;
import static org.zerotul.specification.order.Orders.desc;
import static org.zerotul.specification.restriction.Restrictions.equal;

/**
 * Created by zerotul on 15.03.15.
 */
public class JacksonSpecificationModuleTest {

    static String  json ="{  \n" +
            "   \"recordType\":\"org.zerotul.specification.jackson.test.mock.Mock\",\n" +
            "   \"where\":{  \n" +
            "        \"restriction\":{\n" +
            "            \"propertyName\": \"field1\",\n" +
            "            \"value\": \"value1\",\n" +
            "            \"operator\": \"=\"\n" +
            "         },\n" +
            "         \"predicate\":{\n" +
            "               \"operator\":\"and\",\n" +
            "                \"where\":{\n" +
            "                  \"restriction\":{\n" +
            "                     \"propertyName\": \"field4\",\n" +
            "                     \"value\": 33,\n" +
            "                     \"operator\": \"=\"\n" +
            "                   },\n" +
            "                   \"predicate\":{\n" +
            "                      \"operator\":\"and\",\n" +
            "                      \"where\":{\n" +
            "                          \"restriction\":{\n" +
            "                          \"propertyName\": \"mock\",\n" +
            "                          \"value\": {\"id\":3333},\n" +
            "                          \"operator\": \"=\"\n" +
            "                   }\n" +
            "                }\n" +
            "          }\n" +
            "                }\n" +
            "          }\n" +
            "   },\n" +
            "   \"order\":{  \n" +
            "      \"fields\": [\"field1\"],\n" +
            "      \"orderType\": \"desc\"\n" +
            "   }\n" +
            "}";

    @Test
    public void testJsonSpecificationDeserialize() throws IOException, BuildException {
        ObjectMapper mapper = new ObjectMapper();
        SpecificationModule module = new SpecificationModule();

        mapper.registerModule(module);
        Specification<Mock> readValue = mapper.readValue(json, SpecificationImpl.class);
        Mock mock =new Mock();
        mock.setId(3333l);
        Specification<Mock> specification = from(Mock.class)
                .where()
                 .restriction(equal(Mock::getField1, "value1"))
                .predicate(PredicateOperation.AND)
                 .restriction(equal(Mock::getField4, 33))
                .predicate(PredicateOperation.AND)
                 .restriction(equal(Mock::getMock, mock))
                .endWhere()
                .order(desc(Mock::getField1)).endFrom();
        assertEquals(readValue, specification);
    }

}
