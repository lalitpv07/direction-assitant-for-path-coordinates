package com.lpv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {

    public static double R = 6371000.0;

    public static double degreeToRadians(double degrees) {
        return ((double) (degrees*Math.PI)/180.0);
    }

    public static double radiansToDegrees(double radians) {
        return ((double)radians*180.0)/Math.PI;
    }

    public static double kmphToMps (double speed) {
        return ((double) speed*1000.0)/3600.0;
    }

    public static final String distanceKey = "dist";
    public static final String angleKey = "angle";

    public static double distance (double lat1, double lon1, double lat2, double lon2) {
        double dLat = degreeToRadians(lat2-lat1);
        double dLon = degreeToRadians(lon2-lon1);
        lat1 = degreeToRadians(lat1);
        lat2 = degreeToRadians(lat1);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R*c;
    }

    public static double angleAtMid (double lat1, double lon1, double lat2, double lon2, double lat3, double lon3) {
        double theta1=0, theta2=0;
        if (lon2==lon1) {
            if (lat2>=lat1) theta1 = Math.PI/2;
            else theta1 = -Math.PI/2;
        } else {
            theta1 = Math.atan(degreeToRadians(lat2-lat1)/degreeToRadians(lon2-lon1));
        }
        if (lon3==lon2) {
            if (lat3>=lat2) theta2 = Math.PI/2;
            else theta2 = -Math.PI/2;
        } else {
            theta2 = Math.atan(degreeToRadians(lat3-lat2)/degreeToRadians(lon3-lon2));
        }
        return radiansToDegrees(theta1-theta2);
    }

    public static void printDirections(List<Double> lats, List<Double> lons, double speed) {
        Map<Date, Map<String, Double>> ret = new LinkedHashMap<Date, Map<String, Double>>();

        Calendar calendar = Calendar.getInstance();
        //ret.put(calendar.getTime(), "");
        for (int i=0; i<lats.size()-1; i++) {
            Map<String, Double> map = new HashMap<String, Double>();
            double dist = distance(lats.get(i), lons.get(i), lats.get(i+1), lons.get(i+1));
            int time = (int)(dist/speed);
            if (ret.get(calendar.getTime())!=null) {
                dist+=(ret.get(calendar.getTime()).get(distanceKey)==null)?0:(ret.get(calendar.getTime()).get(distanceKey));
            }
            map.put(distanceKey, dist);
            if (i>0) {
                map.put(angleKey, angleAtMid(
                        lats.get(i-1), lons.get(i-1),
                        lats.get(i), lons.get(i),
                        lats.get(i+1), lons.get(i+1))
                );
            }
            ret.put(calendar.getTime(), map);
            calendar.add(Calendar.SECOND, time);
        }

        for (Map.Entry<Date, Map<String, Double>> mapEntry : ret.entrySet()) {
            String dir = "";
            String dist = "";
            String message = "";
            if (mapEntry.getKey()!=null && mapEntry.getValue()!=null) {
                if (mapEntry.getValue().get(angleKey)!=null && mapEntry.getValue().get(angleKey)!=0) {
                    if (mapEntry.getValue().get(angleKey)<0) {
                        dir = "take "+ Math.abs(mapEntry.getValue().get(angleKey))+" degrees left (<-) and ";
                    } else {
                        dir = "take "+ Math.abs(mapEntry.getValue().get(angleKey))+" degrees right (->) and ";
                    }
                }
                if (mapEntry.getValue().get(distanceKey)!=null) {
                    dist = "go straight for "+mapEntry.getValue().get(distanceKey) + " meters.";
                }
                message = dir+dist;
            }
            System.out.println(mapEntry.getKey()+": "+message);
        }
        System.out.println(calendar.getTime() + ": Reached destination.");
    }

    public static void main(String[] args) {
	// write your code here
        String filename = "lat_long.csv";
        double speed = 30.0;
        String line = "";
        BufferedReader reader = null;
        List<Double> lats = new ArrayList<Double>();
        List<Double> lons = new ArrayList<Double>();
        try {
            reader = new BufferedReader(new FileReader(filename));
            while ((line = reader.readLine()) != null) {
                String[] row = line.split(",");
                if (row.length >= 2) {
                    lats.add(Double.valueOf(row[0]));
                    lons.add(Double.valueOf(row[1]));
                }
            }
            printDirections(lats, lons, kmphToMps(speed));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
