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
import it.mgt.util.json2jpa.test.hooks.entity.Region;
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
import java.util.Set;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = {StandaloneDataConfig.class, SpringContext.class })
public class ChangedOneToManyObj {

    private static final Logger logger = LoggerFactory.getLogger(ChangedOneToManyObj.class);

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
        logger.info("Testing changed many-to-one property");

        Flag basicHandlerInvoked = new Flag(false);
        Flag relationshipHandlerInvoked = new Flag(false);

        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode regionsJson = json.putArray("regions");
        ObjectNode italyJson = regionsJson.addObject();
        italyJson.put("id", piedmont.getId());
        italyJson.put("name", "Lombardy");
        ObjectNode tuscanyJson = regionsJson.addObject();
        tuscanyJson.put("name", "Tuscany");

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.onChanged("regions/name", new ChangedHandler<String>() {

            @Override
            public void handle(String oldValue, String newValue) {
                basicHandlerInvoked.setValue(true);

                if (oldValue != null)
                    Assert.assertEquals("Lombardy", newValue);
                else
                    Assert.assertEquals("Tuscany", newValue);
            }

        });
        json2Jpa.onChanged("regions", new ChangedHandler<Set<Region>>() {

            @Override
            public void handle(Set<Region> oldValue, Set<Region> newValue) {
                relationshipHandlerInvoked.setValue(true);
            }

        });
        json2Jpa.merge(italy, json);

        Assert.assertTrue("Basic handler has been invoked", basicHandlerInvoked.isValue());
        Assert.assertTrue("Relationship handler has been invoked", relationshipHandlerInvoked.isValue());
    }

    @After
    @Transactional
    public void after() {
        logger.info("Restoring data");

        em.createNamedQuery("Region.findByName", Region.class)
                .setParameter("name", "Tuscany")
                .getResultList()
                .stream()
                .findFirst()
                .ifPresent(r -> em.remove(r));

        em.remove(piedmont);
        em.remove(italy);

        em.flush();
    }

}
