package net.java.pathfinder.internal;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Slow
public class SlowGraphDao extends GraphDao {

    @Override
    @MakeSlow
    public String getVoyageNumber(String from, String to) {
        return super.getVoyageNumber(from, to); //To change body of generated methods, choose Tools | Templates.
    }
    
}
