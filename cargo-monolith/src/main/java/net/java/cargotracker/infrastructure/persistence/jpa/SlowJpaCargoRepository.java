package net.java.cargotracker.infrastructure.persistence.jpa;

import javax.enterprise.context.ApplicationScoped;
import net.java.cargotracker.application.util.demo.MakeSlow;
import net.java.cargotracker.application.util.demo.Slow;

@Slow
@ApplicationScoped
@MakeSlow
public class SlowJpaCargoRepository extends JpaCargoRepository {

}
