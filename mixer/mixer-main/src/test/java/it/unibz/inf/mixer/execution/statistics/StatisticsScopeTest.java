package it.unibz.inf.mixer.execution.statistics;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class StatisticsScopeTest {

    @Test
    public void testToFromString() {
        var scope = StatisticsScope.forQuery(1, 2, "query.rq");
        var serializedScope = scope.toString();
        var parsedScope = StatisticsScope.fromString(serializedScope);
        Assert.assertEquals(scope, parsedScope);
    }

    @Test
    public void testToFromStringWithMarkers() {
        var scopeGlobal = StatisticsScope.global();
        var scopeClient = StatisticsScope.forClient(1);
        var scopeMix = StatisticsScope.forMix(1, 2);
        var scopeQuery = StatisticsScope.forQuery(1, 2, "query.rq");
        String text = "this is some query text " + scopeGlobal + " with embedding some scope such as" + scopeClient +
                "and" + scopeMix + "\nand also" + scopeQuery;
        List<StatisticsScope> parsedScopes = StatisticsScope.fromStringWithMarkers().apply(text);
        Assert.assertEquals(ImmutableList.of(scopeGlobal, scopeClient, scopeMix, scopeQuery), parsedScopes);
    }

}
