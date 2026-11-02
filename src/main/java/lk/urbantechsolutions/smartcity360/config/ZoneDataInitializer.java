package lk.urbantechsolutions.smartcity360.config;

import lk.urbantechsolutions.smartcity360.model.zone.Building;
import lk.urbantechsolutions.smartcity360.model.zone.City;
import lk.urbantechsolutions.smartcity360.model.zone.District;
import lk.urbantechsolutions.smartcity360.model.zone.Street;
import lk.urbantechsolutions.smartcity360.repository.CityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class ZoneDataInitializer {

    @Bean
    CommandLineRunner seedDatabase(CityRepository cityRepository) {
        return args -> {
            if (cityRepository.count() > 0) {
                return; // already seeded (matters when ddl-auto=update)
            }

            List<City> cities = List.of(
                    city("Colombo",
                            district("Fort",
                                    street("York Street",
                                            building("World Trade Center", "850.50", true),
                                            building("Grand Oriental Hotel", "320.75", true)),
                                    street("Chatham Street",
                                            building("Central Bank", "540.00", true),
                                            building("Old Post Office", "95.25", false))),
                            district("Pettah",
                                    street("Main Street",
                                            building("Pettah Market", "410.00", true),
                                            building("Floating Market Hall", "150.80", true)),
                                    street("Second Cross Street",
                                            building("Gold Traders Complex", "275.40", false))),
                            district("Cinnamon Gardens",
                                    street("Independence Avenue",
                                            building("Independence Hall", "120.00", true),
                                            building("National Museum", "230.60", true)),
                                    street("Wijerama Mawatha",
                                            building("University Admin Block", "310.90", true)))),

                    city("Kandy",
                            district("Kandy Central",
                                    street("Dalada Veediya",
                                            building("Temple of the Tooth Complex", "180.00", true),
                                            building("Kandy City Centre", "620.30", true)),
                                    street("Peradeniya Road",
                                            building("Kandy Railway Station", "210.45", true),
                                            building("Central Market", "145.10", false))),
                            district("Peradeniya",
                                    street("University Road",
                                            building("Faculty of Engineering", "455.75", true),
                                            building("Main Library", "190.20", true)),
                                    street("Botanical Garden Road",
                                            building("Royal Botanic Gardens Office", "75.60", true)))),

                    city("Galle",
                            district("Galle Fort",
                                    street("Church Street",
                                            building("Dutch Reformed Church", "40.00", true),
                                            building("Maritime Museum", "88.90", true)),
                                    street("Lighthouse Street",
                                            building("Galle Lighthouse Station", "35.50", true),
                                            building("Fort Boutique Hotel", "160.25", false))),
                            district("Unawatuna",
                                    street("Beach Road",
                                            building("Seaside Resort", "390.00", true),
                                            building("Dive Centre", "55.30", true)),
                                    street("Temple Road",
                                            building("Rumassala Visitor Centre", "62.80", false)))),

                    city("Jaffna",
                            district("Jaffna Town",
                                    street("Hospital Road",
                                            building("Teaching Hospital", "720.00", true),
                                            building("Public Library", "130.40", true)),
                                    street("Stanley Road",
                                            building("Jaffna Market", "260.15", true),
                                            building("Bus Terminal", "198.70", false))),
                            district("Nallur",
                                    street("Temple Street",
                                            building("Nallur Kandaswamy Kovil", "110.00", true),
                                            building("Heritage Hall", "48.25", false)))),

                    city("Negombo",
                            district("Negombo Town",
                                    street("Lewis Place",
                                            building("Beachfront Hotel A", "340.00", true),
                                            building("Beachfront Hotel B", "315.80", true)),
                                    street("Main Street",
                                            building("Negombo Fish Market", "205.90", true),
                                            building("Town Hall", "150.00", false))),
                            district("Katunayake",
                                    street("Airport Road",
                                            building("Cargo Terminal", "980.00", true),
                                            building("Logistics Warehouse", "540.60", true)),
                                    street("Free Trade Zone Road",
                                            building("Garment Factory 1", "670.35", true),
                                            building("Garment Factory 2", "645.10", false))))
            );

            cityRepository.saveAll(cities); // cascades to districts, streets, buildings

            cities.forEach(c -> System.out.println("Seeded " + c.getName()
                    + " | usage: " + c.getPowerUsage()
                    + " | power on: " + c.isPowerOn()));
        };
    }

    // ---------- helpers ----------

    private City city(String name, District... districts) {
        City c = new City(name);
        for (District d : districts) {
            c.addDistrict(d);
        }
        return c;
    }

    private District district(String name, Street... streets) {
        District d = new District(name);
        for (Street s : streets) {
            d.addStreet(s);
        }
        return d;
    }

    private Street street(String name, Building... buildings) {
        Street s = new Street(name);
        for (Building b : buildings) {
            s.addBuilding(b);
        }
        return s;
    }

    private Building building(String name, String usage, boolean powerOn) {
        Building b = new Building(name, new BigDecimal(usage));
        if (powerOn) {
            b.turnOnPower();
        }
        return b;
    }
}
