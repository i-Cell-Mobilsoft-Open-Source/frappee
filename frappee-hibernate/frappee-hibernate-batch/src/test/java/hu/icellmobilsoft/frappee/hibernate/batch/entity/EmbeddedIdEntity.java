/*-
 * #%L
 * Frappee
 * %%
 * Copyright (C) 2026 i-Cell Mobilsoft Zrt.
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
package hu.icellmobilsoft.frappee.hibernate.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Test entity with an {@code @EmbeddedId} composite id.
 *
 * @author attila.almasi
 * @since 2.2.0
 */
@Entity
@Table(name = "EMBEDDED_ID_ENTITY")
public class EmbeddedIdEntity {

    @EmbeddedId
    private CompositeKey id;

    @Column(name = "NAME")
    private String name;

    @Version
    @Column(name = "VERSION")
    private Long version;

    /**
     * Getter for id.
     *
     * @return id
     */
    public CompositeKey getId() {
        return id;
    }

    /**
     * Setter for id.
     *
     * @param id
     *            id
     */
    public void setId(CompositeKey id) {
        this.id = id;
    }

    /**
     * Getter for name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name.
     *
     * @param name
     *            name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for version.
     *
     * @return version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Setter for version.
     *
     * @param version
     *            version
     */
    public void setVersion(Long version) {
        this.version = version;
    }
}
