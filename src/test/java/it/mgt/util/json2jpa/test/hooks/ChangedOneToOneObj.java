package it.mgt.util.json2jpa.test.hooks;

import com.fasterxml.jackson.databind.ObjectMapper;
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


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = {StandaloneDataConfig.class, SpringContext.class })
public class ChangedOneToOneObj {

    private static final Logger logger = LoggerFactory.getLogger(ChangedOneToOneObj.class);

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

    private Country italy;
    private PersonH serge;

    @Before
    @Transactional
    public void before() {
        serge = new PersonH("Serge");
        em.persist(serge);

        italy = new Country("Italy", "IT", "+39", serge);
        em.persist(italy);

        serge.setCountryByPresident(italy);
    }

    @Test
    @Transactional
    public void test()  {
        logger.info("Testing changed many-to-one property");

        Flag handlerInvoked = new Flag(false);

        ObjectNode json = objectMapper.createObjectNode();
        ObjectNode countryJson = json.putObject("president");
        countryJson.put("id", serge.getId());
        countryJson.put("name", "Sergio");

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.onChanged("president/name", new ChangedHandler<String>() {

            public void handle(String oldValue, String newValue) {
                handlerInvoked.setValue(true);

                Assert.assertEquals("Sergio", newValue);
            }

        });
        json2Jpa.merge(italy, json);

        Assert.assertTrue("Handler has been invoked", handlerInvoked.isValue());
    }

    @After
    @Transactional
    public void after() {
        logger.info("Restoring data");

        serge.setCountryByPresident(null);
        italy.setPresident(null);

        em.remove(serge);
        em.remove(italy);

        em.flush();
    }

}
