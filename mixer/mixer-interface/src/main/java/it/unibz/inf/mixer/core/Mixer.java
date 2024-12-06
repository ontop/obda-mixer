package it.unibz.inf.mixer.core;

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

/**
 * Interface of OBDA Mixer plugins responsible for running evaluation queries on the tested system.
 * <p>
 * A {@code Mixer} plugin is responsible to send the warm up / test {@link Query}/ies used in the evaluation to the
 * system under test, notifying a supplied {@link Handler} of relevant query execution events and query results.
 * </p>
 *
 * @author Davide Lanti
 */
public interface Mixer extends Plugin {

    /**
     * Prepares the execution of the supplied query, returning a {@code QueryExecution} object that can be used to
     * actually perform a single execution of the query, with possibility to interrupt it. This method is called
     * sequentially and does not have to be thread-safe.
     *
     * @param query query object including the query string and additional information (e.g., timeout, result ignored)
     *              that may be leveraged for query execution
     * @throws Throwable on failure
     */
    QueryExecution prepare(Query query) throws Throwable;

}
