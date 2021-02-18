package org.heigit.ors.routing.traffic;

import com.graphhopper.routing.EdgeKeys;
import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.SpeedCalculator;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.VehicleFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.TrafficGraphStorage;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class TrafficSpeedCalculator implements SpeedCalculator {
//    public Map<Double, Double> changedSpeedCount = new HashMap();
//    public Map<Double, Double> changedSpeed = new HashMap();
    protected DecimalEncodedValue avSpeedEnc;
    // time-dependent stuff
    private TrafficGraphStorage trafficGraphStorage;
    private int timeZoneOffset;
    private VehicleFlagEncoder vehicleFlagEncoder;
    private boolean isVehicle = false;

    public TrafficSpeedCalculator() {
    }

    public void init(GraphHopperStorage graphHopperStorage, FlagEncoder flagEncoder) {
        setEncoder(flagEncoder);
        if (flagEncoder instanceof VehicleFlagEncoder) {
            this.vehicleFlagEncoder = (VehicleFlagEncoder) flagEncoder;
            isVehicle = true;
        }
        setTrafficGraphStorage(GraphStorageUtils.getGraphExtension(graphHopperStorage, TrafficGraphStorage.class));
    }

    public double getSpeed(EdgeIteratorState edge, boolean reverse, long time) {
        double speed = reverse ? edge.getReverse(avSpeedEnc) : edge.get(avSpeedEnc);
        if (time != -1) {
            int edgeId = EdgeKeys.getOriginalEdge(edge);
            double trafficSpeed = trafficGraphStorage.getSpeedValue(edgeId, edge.getBaseNode(), edge.getAdjNode(), time, timeZoneOffset);
            if (trafficSpeed != -1) {
                //TODO: This is a heuristic to provide expected results given traffic data and ORS internal speed calculations.
                if (isVehicle) {
                    trafficSpeed = vehicleFlagEncoder.adjustSpeedForAcceleration(edge.getDistance(), trafficSpeed);
                    speed = trafficSpeed;
                } else {
                    if (speed >= 45.0 && !(trafficSpeed > 1.1 * speed) || trafficSpeed < speed) {
                        speed = trafficSpeed;
                    }
                }
//                if (speed >= 45.0 && !(trafficSpeed > 1.1 * speed) || trafficSpeed < speed) {
//                if(trafficSpeed < speed){
//                changedSpeed.put(speed, changedSpeed.getOrDefault(speed, 0.0) + trafficSpeed * edge.getDistance());
//                changedSpeedCount.put(speed, changedSpeedCount.getOrDefault(speed, 0.0) + edge.getDistance());
            }
        }
        return speed;
    }

    public void setEncoder(FlagEncoder flagEncoder) {
        this.avSpeedEnc = flagEncoder.getAverageSpeedEnc();
    }

    public void setTrafficGraphStorage(TrafficGraphStorage trafficGraphStorage) {
        this.trafficGraphStorage = trafficGraphStorage;
    }

    public void setZonedDateTime(ZonedDateTime zdt) {

        this.timeZoneOffset = zdt.getOffset().getTotalSeconds() / 3600;
    }
}
