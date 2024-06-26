/*-
 * #%L
 * Frappee
 * %%
 * Copyright (C) 2020 - 2024 i-Cell Mobilsoft Zrt.
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
package hu.icellmobilsoft.frappee.hibernate.batch.enums;

/**
 * Hibernate batch fault type
 *
 * @author attila-kiss-it
 * @since 1.0.0
 */
public enum HibernateBatchFaultType {

    /**
     * Entity save (INSERT/UPDATE) failure.
     */
    ENTITY_SAVE_FAILED,
    /**
     * Entity delete (DELETE) failure.
     */
    ENTITY_DELETE_FAILED,

}
