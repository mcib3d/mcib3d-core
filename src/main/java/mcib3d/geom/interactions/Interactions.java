package mcib3d.geom.interactions;

import mcib3d.image3d.ImageHandler;

public class Interactions {
    public static InteractionsList computeLines(ImageHandler image) {
        return new InteractionsComputeDamLines().compute(image);
    }

    public static InteractionsList computeContours(ImageHandler image) {
        return new InteractionsComputeContours().compute(image);
    }

    public static InteractionsList computeDilate(ImageHandler image, float rx, float ry, float rz) {
        return new InteractionsComputeDilate(rx, ry, rz).compute(image);
    }
}
