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
package hu.icellmobilsoft.frappee.hibernate.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

/**
 * Helper class for entity handling.
 *
 * @author attila-kiss-it
 * @since 1.0.0
 */
public class HibernateEntityHelper {

    /** Constant <code>MAX_PARAMETER_COUNT=1000</code> */
    public static final int MAX_PARAMETER_COUNT = 1000;

    private final EntityManager em;
    
    public HibernateEntityHelper(EntityManager em) {
        this.em = em;
    }

    /**
     * Can get Lazy loaded Entitys id, else org.hibernate.LazyInitializationException: could not initialize proxy - no Session will be thrown
     *
     * @param entity
     *            a E object.
     * @return a {@link java.lang.String} object.
     */
    public String getLazyId(Object entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof HibernateProxy) {
            LazyInitializer lazyInitializer = ((HibernateProxy) entity).getHibernateLazyInitializer();
            if (lazyInitializer.isUninitialized()) {
                return (String) lazyInitializer.getIdentifier();
            }
        }
        return getId(entity);
    }

    /**
     * Getting identifier value from entity (annotated {@link Id})
     *
     * @param entity
     *            a {@link java.lang.Object} object.
     * @return a {@link java.lang.String} object.
     */
    public String getId(Object entity) {
        if (entity == null) {
            return null;
        }
        return (String) em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
    }

}
