package it.unibz.inf.mixer.core;

import java.util.List;

/**
 * Interface of OBDA Mixer plugins responsible for generating the evaluation queries.
 * <p>
 * A {@code QuerySelector} plugin is responsible to generate the query mixes used for warm up and test during the
 * evaluation. which is achieved by calling method {@link #nextQueryMix()} whose implementation has to be thread-safe.
 * </p>
 */
public interface QuerySelector extends Plugin {

    /**
     * Returns the queries of the next query mix, typically using placeholder fillers different from the ones of previous
     * mixes. This method is called repeatedly and possibly by multiple threads simultaneously so it needs to be
     * thread-safe. The number of invocations depend on warmup and test configuration, and the method is expected to
     * always succeed.
     *
     * @return the next query mix
     */
    List<Query> nextQueryMix();

}
