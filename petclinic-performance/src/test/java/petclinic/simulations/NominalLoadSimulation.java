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
                    .disableFollowRedirect()
                    .formParam("firstName", "#{firstName}")
                    .formParam("lastName", "#{lastName}")
                    .formParam("address", "#{address}")
                    .formParam("city", "#{city}")
                    .formParam("telephone", "#{telephone}")
                    .check(status().in(302, 303)));

    private final ScenarioBuilder createPet = scenario("Creation animal")
            .exec(http("Formulaire creation animal")
                    .get("/owners/1/pets/new")
                    .check(status().is(200)))
            .exec(session -> {
                String id = UUID.randomUUID().toString().substring(0, 8);
                return session
                        .set("petName", "Pet" + id)
                        .set("birthDate", "2020-01-01")
                        .set("type", "dog");
            })
            .exec(http("POST creation animal")
                    .post("/owners/1/pets/new")
                    .disableFollowRedirect()
                    .formParam("name", "#{petName}")
                    .formParam("birthDate", "#{birthDate}")
                    .formParam("type", "#{type}")
                    .check(status().in(302, 303)));

    {
        setUp(
                consultationOwners.injectOpen(
                        rampUsersPerSec(5).to(20).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(20).during(Duration.ofMinutes(1)),
                        rampUsersPerSec(20).to(30).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(30).during(Duration.ofMinutes(6)),
                        rampUsersPerSec(30).to(5).during(Duration.ofMinutes(1))
                ),
                searchOwners.injectOpen(
                        rampUsersPerSec(5).to(15).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(15).during(Duration.ofMinutes(1)),
                        rampUsersPerSec(15).to(20).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(20).during(Duration.ofMinutes(6)),
                        rampUsersPerSec(20).to(5).during(Duration.ofMinutes(1))
                ),
                createOwner.injectOpen(
                        rampUsersPerSec(5).to(20).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(20).during(Duration.ofMinutes(1)),
                        rampUsersPerSec(20).to(30).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(30).during(Duration.ofMinutes(6)),
                        rampUsersPerSec(30).to(5).during(Duration.ofMinutes(1))
                ),
                createPet.injectOpen(
                        rampUsersPerSec(5).to(15).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(15).during(Duration.ofMinutes(1)),
                        rampUsersPerSec(15).to(20).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(20).during(Duration.ofMinutes(6)),
                        rampUsersPerSec(20).to(5).during(Duration.ofMinutes(1))
                )
        ).protocols(httpProtocol)
                .assertions(
                        global().successfulRequests().percent().gt(70.0),
                        global().failedRequests().percent().lt(30.0),
                        global().responseTime().percentile3().lt(15000),
                        global().responseTime().percentile4().lt(30000)
                );
    }
}