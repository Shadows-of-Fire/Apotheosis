package shadows.apotheosis.util;

import java.util.Random;

public class FloatValueRange {

    public static final FloatValueRange ZERO = new FloatValueRange(0, 0) {
        @Override
        public float getRandomValue(Random rand) {
            return 0;
        }
    };

    private final float min;
    private final float max;

    public FloatValueRange(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public float getRandomValue(Random rand) {
        return this.min + rand.nextFloat() * (this.max - this.min);
    }


    public float getMin() {
        return this.min;
    }

    public float getMax() {
        return this.max;
    }
}
