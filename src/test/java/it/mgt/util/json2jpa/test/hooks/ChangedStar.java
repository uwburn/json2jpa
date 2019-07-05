package it.mgt.util.json2jpa.test.hooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.util.json2jpa.ChangedHandler;
import it.mgt.util.json2jpa.Json2Jpa;
import it.mgt.util.json2jpa.Json2JpaFactory;
import it.mgt.util.json2jpa.test.config.SpringContext;
import it.mgt.util.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.util.json2jpa.test.hooks.entity.Country;
import it.mgt.util.json2jpa.test.hooks.entity.Region;
import it.mgt.util.json2jpa.test.hooks.util.Counter;
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
public class ChangedStar {

    private static final Logger logger = LoggerFactory.getLogger(ChangedStar.class);

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

    private Country italy;
    private Region piedmont;

    @Before
    @Transactional
    public void before() {
        italy = new Country("Italy", "IT", "+39");
        em.persist(italy);

        piedmont = new Region("Piedmont", italy);
        em.persist(piedmont);
    }

    @Test
    @Transactional
    public void test()  {
        logger.info("Testing changed basic nested");

        Counter handlerInvokedCount = new Counter();

        ObjectNode json = objectMapper.createObjectNode();
        ObjectNode countryJson = json.putObject("country");
        countryJson.put("id", italy.getId());
        countryJson.put("countryCode", "it");
        countryJson.put("phonePrefix", "0039");

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.onChanged("country/*", new ChangedHandler<String>() {

            public void handle(String oldValue, String newValue) {
                handlerInvokedCount.increase();
            }

        });
        json2Jpa.merge(piedmont, json);

        Assert.assertEquals(2, handlerInvokedCount.getValue());
    }

    @After
    @Transactional
    public void after() {
        logger.info("Restoring data");

        em.remove(piedmont);
        em.remove(italy);

        em.flush();
    }

}
