package it.mgt.util.json2jpa.test.hooks.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Component
public class HooksHelperImpl implements HooksHelper {

    private static final Logger logger = LoggerFactory.getLogger(HooksHelper.class);

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initialize() {
        logger.info("Initializing sample data");

    }

}
