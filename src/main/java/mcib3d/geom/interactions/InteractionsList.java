package mcib3d.geom.interactions;

import mcib3d.geom.Object3D;
import mcib3d.geom.PairColocalisation;

import java.util.HashMap;

public class InteractionsList {
    HashMap<String, PairColocalisation> interactions;

    public InteractionsList() {
        interactions = new HashMap<>();
    }

    public boolean contains(String inter) {
        return interactions.containsKey(inter);
    }

    public boolean contains(int a, int b) {
        return interactions.containsKey(buildKey(a, b));
    }

    public boolean addInteraction(PairColocalisation pair) {
        PairColocalisation inter = interactions.putIfAbsent(buildKey(pair.getObject3D1().getValue(), pair.getObject3D2().getValue()), pair);
        return (inter != null);
    }

    public boolean addInteraction(Object3D object3D1, Object3D object3D2) {
        return addInteraction(object3D1, object3D2, 0);
    }

    public boolean addInteraction(Object3D object3D1, Object3D object3D2, int volume) {
        PairColocalisation pair = new PairColocalisation(object3D1, object3D2, volume);
        PairColocalisation inter = interactions.putIfAbsent(buildKey(pair.getObject3D1().getValue(), pair.getObject3D2().getValue()), pair);

        return (inter != null);
    }


    public int incrementPairVolume(int a, int b) {
        return incrementPairVolume(a, b, 1);
    }

    public int incrementPairVolume(int a, int b, int vol) {
        PairColocalisation pair = getPair(a, b);
        pair.incrementVolumeColoc(vol);

        return pair.getVolumeColoc();
    }


    public PairColocalisation getPair(int a, int b) {
        return interactions.get(buildKey(a, b));
    }



    // to be removed
    @Deprecated
    public HashMap<String, PairColocalisation> getMap() {
        return interactions;
    }

    private String buildKey(int a, int b) {
        if (a == b) return null; // ERROR
        if (a < b) return a + "-" + b;
        else return b + "-" + a;
    }

    public String toString() {
        String res = "";
        for (String S : interactions.keySet()) {
            res = res.concat(S) + ", ";
        }

        return res;
    }

}
