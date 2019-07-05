package it.mgt.util.json2jpa.test.hooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.util.json2jpa.ChangedHandler;
import it.mgt.util.json2jpa.Json2Jpa;
import it.mgt.util.json2jpa.Json2JpaFactory;
import it.mgt.util.json2jpa.test.config.SpringContext;
import it.mgt.util.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.util.json2jpa.test.hooks.entity.Country;
import it.mgt.util.json2jpa.test.hooks.entity.PersonH;
import it.mgt.util.json2jpa.test.hooks.util.Flag;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Set;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = {StandaloneDataConfig.class, SpringContext.class })
public class ChangedManyToManyObj {

    private static final Logger logger = LoggerFactory.getLogger(ChangedManyToManyObj.class);

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

    private Country italy;
    private Country france;
    private Country germany;
    private PersonH john;
    private PersonH paul;

    @Before
    @Transactional
    public void before() {
        italy = new Country("Italy", "IT", "+39");
        em.persist(italy);

        france = new Country("France", "FR", "+33");
        em.persist(france);

        germany = new Country("Germany", "DE", "+49");
        em.persist(germany);

        john = new PersonH("John");
        em.persist(john);

        paul = new PersonH("Paul");
        em.persist(paul);

        italy.getVisitors().add(john);
        italy.getVisitors().add(paul);

        germany.getVisitors().add(john);

        france.getVisitors().add(paul);

        john.getVisitedCountries().add(italy);
        john.getVisitedCountries().add(germany);

        paul.getVisitedCountries().add(italy);
        paul.getVisitedCountries().add(france);
    }

    @Test
    @Transactional
    public void test()  {
        logger.info("Testing changed many-to-many object property");

        Flag basicHandlerInvoked = new Flag(false);
        Flag relationshipHandlerInvoked = new Flag(false);

        ObjectNode json = objectMapper.createObjectNode();
        json.put("id", john.getId());
        ArrayNode visitedCountriesJson = json.putArray("visitedCountries");
        ObjectNode italyJson = visitedCountriesJson.addObject();
        italyJson.put("id", italy.getId());
        italyJson.put("countryCode", "it");
        ObjectNode germanyJson = visitedCountriesJson.addObject();
        germanyJson.put("id", france.getId());

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.onChanged("visitedCountries/countryCode", new ChangedHandler<String>() {

            @Override
            public void handle(String oldValue, String newValue) {
                basicHandlerInvoked.setValue(true);

                Assert.assertEquals("it", newValue);
            }

        });
        json2Jpa.onChanged("visitedCountries", new ChangedHandler<Collection<Country>>() {

            @Override
            public void handle(Collection<Country> oldValue, Collection<Country> newValue) {
                relationshipHandlerInvoked.setValue(true);

                Assert.assertTrue(oldValue.contains(germany));
                Assert.assertFalse(oldValue.contains(france));
                Assert.assertTrue(newValue.contains(france));
                Assert.assertFalse(newValue.contains(germany));
            }

        });
        json2Jpa.merge(john, json);

        Assert.assertTrue("Basic handler has been invoked", basicHandlerInvoked.isValue());
        Assert.assertTrue("Relationship handler has been invoked", relationshipHandlerInvoked.isValue());
    }

    @After
    @Transactional
    public void after() {
        logger.info("Restoring data");

        em.remove(john);
        em.remove(paul);

        em.remove(germany);
        em.remove(france);
        em.remove(italy);

        em.flush();
    }

}
