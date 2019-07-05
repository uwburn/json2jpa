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
public class ChangedOneToManyRef {

    private static final Logger logger = LoggerFactory.getLogger(ChangedOneToManyObj.class);

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

    private Country italy;
    private Region piedmont;
    private Region lombardy;

    @Before
    @Transactional
    public void before() {
        italy = new Country("Italy", "IT", "+39");
        em.persist(italy);

        piedmont = new Region("Piedmont", italy);
        italy.getRegions().add(piedmont);
        em.persist(piedmont);

        lombardy = new Region("Lombardy", italy);
        italy.getRegions().add(lombardy);
        em.persist(lombardy);
    }

    @Test
    @Transactional
    public void test()  {
        logger.info("Testing changed many-to-one property");

        Flag relationshipHandlerInvoked = new Flag(false);

        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode regionsJson = json.putArray("regions");
        regionsJson.add(piedmont.getId());

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.onChanged("regions", new ChangedHandler<Set<Region>>() {

            @Override
            public void handle(Set<Region> oldValue, Set<Region> newValue) {
                relationshipHandlerInvoked.setValue(true);
            }

        });
        json2Jpa.merge(italy, json);

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
