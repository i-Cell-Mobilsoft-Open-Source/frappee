/*-
 * #%L
 * Frappee
 * %%
 * Copyright (C) 2024 i-Cell Mobilsoft Zrt.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package hu.icellmobilsoft.frappee.hibernate.batch.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;

import hu.icellmobilsoft.frappee.hibernate.batch.HibernateBatchService;
import hu.icellmobilsoft.frappee.hibernate.util.HibernateEntityHelper;

/**
 * The default CDI producer of {@link HibernateBatchService}.
 *
 * @author attila-kiss-it
 * @since 1.0.0
 */
@ApplicationScoped
public class HibernateBatchServiceProducer {

    /**
     * The default producer method of the {@link HibernateBatchService}.
     *
     * @param em
     *            the {@link EntityManager}
     * @param hibernateEntityHelper
     *            the {@link HibernateEntityHelper}
     * @return a new {@link HibernateBatchService} instance
     */
    @Produces
    @Dependent
    public HibernateBatchService createHibernateBatchService(EntityManager em, HibernateEntityHelper hibernateEntityHelper) {
        return new HibernateBatchService(em, hibernateEntityHelper);
    }

}
