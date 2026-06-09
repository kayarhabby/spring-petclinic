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
            .exec(http("Accueil").get("/").check(status().is(200)))
            .exec(http("Page recherche proprietaires").get("/owners/find").check(status().is(200)))
            .exec(http("Liste proprietaires").get("/owners?lastName=").check(status().is(200)))
            .exec(http("Liste veterinaires").get("/vets.html").check(status().in(200, 301, 302)));

    private final ScenarioBuilder searchOwners = scenario("Recherche proprietaire")
            .exec(http("Page recherche proprietaire").get("/owners/find").check(status().is(200)))
            .exec(http("Recherche Davis").get("/owners?lastName=Davis").check(status().in(200, 302)))
            .exec(http("Recherche vide").get("/owners?lastName=").check(status().is(200)));

    private final ScenarioBuilder readPages = scenario("Lectures pages")
            .exec(http("Accueil lecture").get("/").check(status().is(200)))
            .exec(http("Veterinaires lecture").get("/vets.html").check(status().in(200, 301, 302)))
            .exec(http("Recherche owners lecture").get("/owners/find").check(status().is(200)))
            .exec(http("Liste owners lecture").get("/owners?lastName=").check(status().is(200)));

    private final ScenarioBuilder readOwnersDetails = scenario("Lecture details proprietaires")
            .exec(http("Owner 1").get("/owners/1").check(status().is(200)))
            .exec(http("Owner 2").get("/owners/2").check(status().is(200)))
            .exec(http("Owner 3").get("/owners/3").check(status().is(200)))
            .exec(http("Owner 4").get("/owners/4").check(status().is(200)))
            .exec(http("Owner 5").get("/owners/5").check(status().is(200)));

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

    {
        setUp(
                consultationOwners.injectOpen(
                        rampUsersPerSec(10).to(40).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(40).during(Duration.ofMinutes(1)),
                        rampUsersPerSec(40).to(70).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(70).during(Duration.ofMinutes(6)),
                        rampUsersPerSec(70).to(10).during(Duration.ofMinutes(1))
                ),
                searchOwners.injectOpen(
                        rampUsersPerSec(10).to(35).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(35).during(Duration.ofMinutes(1)),
                        rampUsersPerSec(35).to(60).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(60).during(Duration.ofMinutes(6)),
                        rampUsersPerSec(60).to(10).during(Duration.ofMinutes(1))
                ),
                readPages.injectOpen(
                        rampUsersPerSec(10).to(40).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(40).during(Duration.ofMinutes(1)),
                        rampUsersPerSec(40).to(70).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(70).during(Duration.ofMinutes(6)),
                        rampUsersPerSec(70).to(10).during(Duration.ofMinutes(1))
                ),
                readOwnersDetails.injectOpen(
                        rampUsersPerSec(10).to(35).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(35).during(Duration.ofMinutes(1)),
                        rampUsersPerSec(35).to(60).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(60).during(Duration.ofMinutes(6)),
                        rampUsersPerSec(60).to(10).during(Duration.ofMinutes(1))
                ),
                createOwner.injectOpen(
                        rampUsersPerSec(5).to(20).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(20).during(Duration.ofMinutes(1)),
                        rampUsersPerSec(20).to(40).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(40).during(Duration.ofMinutes(6)),
                        rampUsersPerSec(40).to(5).during(Duration.ofMinutes(1))
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