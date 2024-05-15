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
package hu.icellmobilsoft.frappee.hibernate.batch.provider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

class DateUtil {

    private DateUtil() {
    }

    /**
     * Creates a new calendar instance from a {@link Date}.
     *
     * @param date
     *            {@code Date} to convert
     * @return {@link Calendar} or null if {@code date} is null
     */
    static Calendar toCalendar(Date date) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    /**
     * {@link ZonedDateTime} to {@link Date} converter
     *
     * @param zonedDateTime
     *            {@code ZonedDateTime} to convert
     * @return {@code Date} instance or null if {@code zonedDateTime} is null
     */
    static Date toDate(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * {@link LocalDateTime} to {@link Date} converter with system default zone id
     *
     * @param localDateTime
     *            {@code LocalDateTime} to convert
     * @return {@code Date} instance or null if {@code localDateTime} is null
     */
    static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Returns a {@link Date} instance created from {@link LocalDate} with system default zone id
     *
     * @param localDate
     *            localDate to convert
     * @return {@code Date} instance or null if {@code localDate} is empty
     */
    static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
}
