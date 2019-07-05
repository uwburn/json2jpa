package it.mgt.util.json2jpa.test.hooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.util.json2jpa.ChangedHandler;
import it.mgt.util.json2jpa.Json2Jpa;
import it.mgt.util.json2jpa.Json2JpaFactory;
import it.mgt.util.json2jpa.test.hooks.entity.Country;
import it.mgt.util.json2jpa.test.hooks.util.Flag;
import it.mgt.util.json2jpa.test.config.SpringContext;
import it.mgt.util.json2jpa.test.config.StandaloneDataConfig;
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
public class ChangedBasic {

    private static final Logger logger = LoggerFactory.getLogger(ChangedBasic.class);

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

    private Country italy;

    @Before
    @Transactional
    public void before() {
        italy = new Country("Italy", "IT", "+39");
        em.persist(italy);
    }

    @Test
    @Transactional
    public void test()  {
        logger.info("Testing changed basic property");

        Flag handlerInvoked = new Flag(false);

        ObjectNode json = objectMapper.createObjectNode();
        json.put("countryCode", "it");

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.onChanged("countryCode", new ChangedHandler<String>() {

            @Override
            public void handle(String oldValue, String newValue) {
                handlerInvoked.setValue(true);

                Assert.assertEquals("IT", oldValue);
                Assert.assertEquals("it", newValue);
            }

        });
        json2Jpa.merge(italy, json);

        Assert.assertTrue("Handler has been invoked", handlerInvoked.isValue());
    }

    @After
    @Transactional
    public void after() {
        logger.info("Restoring data");

        em.remove(italy);

        em.flush();
    }

}
