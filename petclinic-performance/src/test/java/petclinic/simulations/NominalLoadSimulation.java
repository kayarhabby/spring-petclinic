package petclinic.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.UUID;

public class NominalLoadSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(System.getProperty("baseUrl", "http://localhost:8080"))
            .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .acceptLanguageHeader("fr-FR,fr;q=0.9,en;q=0.8")
            .userAgentHeader("Gatling Petclinic Nominal Load Test");

    private final ScenarioBuilder consultationOwners = scenario("Consultation proprietaires")
            .exec(http("Accueil")
                    .get("/")
                    .check(status().is(200)))
            .exec(http("Page recherche proprietaires")
                    .get("/owners/find")
                    .check(status().is(200)))
            .exec(http("Liste proprietaires")
                    .get("/owners?lastName=")
                    .check(status().is(200)))
            .exec(http("Liste veterinaires")
                    .get("/vets.html")
                    .check(status().in(200, 301, 302)));

    private final ScenarioBuilder searchOwners = scenario("Recherche proprietaire")
            .exec(http("Page recherche proprietaire")
                    .get("/owners/find")
                    .check(status().is(200)))
            .exec(http("Recherche Davis")
                    .get("/owners?lastName=Davis")
                    .check(status().in(200, 302)));

    private final ScenarioBuilder createOwner = scenario("Creation proprietaire")
            .exec(session -> {
                String id = UUID.randomUUID().toString().substring(0, 8);
                return session
                        .set("firstName", "Test" + id)
                        .set("lastName", "Gatling" + id)
                        .set("address", "1 rue du test")
                        .set("city", "Paris")
                        .set("telephone", "0102030405");
            })
            .exec(http("Formulaire creation proprietaire")
                    .get("/owners/new")
                    .check(status().is(200)))
            .exec(http("POST creation proprietaire")
                    .post("/owners/new")
                    .formParam("firstName", "#{firstName}")
                    .formParam("lastName", "#{lastName}")
                    .formParam("address", "#{address}")
                    .formParam("city", "#{city}")
                    .formParam("telephone", "#{telephone}")
                    .check(status().in(200, 302, 303)));

    {
        setUp(
                consultationOwners.injectOpen(
                        rampUsersPerSec(1).to(18).during(Duration.ofMinutes(2)),
                        constantUsersPerSec(18).during(Duration.ofMinutes(7)),
                        rampUsersPerSec(18).to(1).during(Duration.ofMinutes(1))
                ),
                searchOwners.injectOpen(
                        rampUsersPerSec(1).to(8).during(Duration.ofMinutes(2)),
                        constantUsersPerSec(8).during(Duration.ofMinutes(7)),
                        rampUsersPerSec(8).to(1).during(Duration.ofMinutes(1))
                ),
                createOwner.injectOpen(
                        rampUsersPerSec(1).to(4).during(Duration.ofMinutes(2)),
                        constantUsersPerSec(4).during(Duration.ofMinutes(7)),
                        rampUsersPerSec(4).to(1).during(Duration.ofMinutes(1))
                )
        ).protocols(httpProtocol)
                .assertions(
                        global().successfulRequests().percent().gt(95.0),
                        global().failedRequests().percent().lt(5.0),
                        global().responseTime().percentile3().lt(3000),
                        global().responseTime().percentile4().lt(6000)
                );
    }
}