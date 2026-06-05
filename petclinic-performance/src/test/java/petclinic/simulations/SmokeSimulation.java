package petclinic.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;

public class SmokeSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .acceptLanguageHeader("fr-FR,fr;q=0.9,en;q=0.8")
            .userAgentHeader("Gatling Petclinic Smoke Test");

    private final ScenarioBuilder smokeScenario = scenario("Petclinic Smoke Test")
            .exec(
                    http("Accueil")
                            .get("/")
                            .check(status().is(200))
            )
            .pause(Duration.ofSeconds(1))
            .exec(
                    http("Page recherche propriétaires")
                            .get("/owners/find")
                            .check(status().is(200))
            )
            .pause(Duration.ofSeconds(1))
            .exec(
                    http("Liste vétérinaires")
                            .get("/vets.html")
                            .check(status().in(200, 301, 302))
            );

    {
        setUp(
                smokeScenario.injectOpen(
                        rampUsers(5).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(1).during(Duration.ofMinutes(4))
                )
        ).protocols(httpProtocol)
                .assertions(
                        global().successfulRequests().percent().gt(99.0),
                        global().responseTime().percentile3().lt(1500),
                        global().failedRequests().percent().lt(1.0)
                );
    }
}