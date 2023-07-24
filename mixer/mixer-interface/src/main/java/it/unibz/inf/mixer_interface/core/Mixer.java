package it.unibz.inf.mixer_interface.core;

/*
 * #%L
 * mixer-interface
 * %%
 * Copyright (C) 2014 Free University of Bozen-Bolzano
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

import java.util.Map;

/**
 * @author Davide Lanti
 */
public interface Mixer extends AutoCloseable {

    /**
     * Initializes the mixer, supplying configuration data. If the mixer embeds the OBDA system, this method can be
     * used to load the OBDA system and have the corresponding loading time tracked by OBDA Mixer.
     *
     * @param configuration {@code <key, value>} configuration properties, not expected to be modified
     */
    void init(Map<String, String> configuration) throws Exception;

    /**
     * Executes a query with an optional execution timeout, reporting query results and other execution events to the
     * supplied {@code Handler} object.
     *
     * @param query   query object including the query string and additional information (e.g., timeout, result ignored)
     *                that may be leveraged for query execution
     * @param handler handler object where to report query execution events and results
     */
    void execute(Query query, Handler handler) throws Exception;

    /**
     * Releases any resource allocated by the mixer. This method is guaranteed to be called at the end of the evaluation.
     */
    void close() throws Exception;

}
