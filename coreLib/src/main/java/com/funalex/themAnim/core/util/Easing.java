package com.funalex.themAnim.core.util;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Utility class for various easing functions
 * Matched to GeckoLib's EasingType implementation
 */
public class Easing {

    /**
     * Easing functions from easings.net
     * All function have a string codename
     * EasingFromString
     * <p>
     * All function needs an input between 0 and 1
     *
     * @deprecated Just use {@link Ease#invoke(float)}
     */
    @Deprecated
    public static float easingFromEnum(@Nullable Ease type, float f) {
        return type != null ? type.invoke(f) : f;
    }

    /**
     * @param string ease name
     * @return ease
     */
    public static Ease easeFromString(String string) {
        return Ease.valueOf(string);
    }

    /**
     * A linear function, equivalent to a null-operation
     * <p>
     * {@code f(n) = n}
     */
    public static float linear(float n) {
        return n;
    }

    /**
     * A sinusoidal function, equivalent to a sine curve output
     * <p>
     * {@code f(n) = 1 - cos(n * π / 2)}
     */
    public static float sine(float n) {
        return 1 - (float) Math.cos(n * Math.PI / 2f);
    }

    /**
     * A cubic function, equivalent to cube (n^3) of elapsed time
     * <p>
     * {@code f(n) = n^3}
     */
    public static float cubic(float n) {
        return n * n * n;
    }

    /**
     * A quadratic function, equivalent to the square (n^2) of elapsed time
     * <p>
     * {@code f(n) = n^2}
     */
    public static float quadratic(float n) {
        return n * n;
    }

    /**
     * An exponential function, equivalent to an exponential curve to the n root
     * <p>
     * {@code f(t) = t^n}
     *
     * @param n The exponent
     */
    public static Function<Float, Float> pow(float n) {
        return t -> (float) Math.pow(t, n);
    }

    /**
     * An exponential function, equivalent to an exponential curve
     * <p>
     * {@code f(n) = 2^(10 * (n - 1))}
     */
    public static float exp(float n) {
        return (float) Math.pow(2, 10 * (n - 1));
    }

    /**
     * A circular function, equivalent to a normally symmetrical curve
     * <p>
     * {@code f(n) = 1 - sqrt(1 - n^2)}
     */
    public static float circle(float n) {
        return 1 - (float) Math.sqrt(1 - n * n);
    }

    /**
     * A negative elastic function, equivalent to inverting briefly before increasing
     * <p>
     * {@code f(t) = t^2 * ((n * 1.70158 + 1) * t - n * 1.70158)}
     */
    public static Function<Float, Float> back(Float n) {
        final float n2 = n == null ? 1.70158F : n * 1.70158F;

        return t -> t * t * ((n2 + 1) * t - n2);
    }

    /**
     * An elastic function, equivalent to an oscillating curve
     * <p>
     * n defines the elasticity of the output
     * <p>
     * {@code f(t) = 1 - (cos(t * π) / 2))^3 * cos(t * n * π)}
     */
    public static Function<Float, Float> elastic(Float n) {
        float n2 = n == null ? 1 : n;

        return t -> (float) (1 - Math.pow(Math.cos(t * Math.PI / 2f), 3) * Math.cos(t * n2 * Math.PI));
    }

    /**
     * A bouncing function, equivalent to a bouncing ball curve
     * <p>
     * n defines the bounciness of the output
     */
    public static Function<Float, Float> bounce(Float n) {
        final float n2 = n == null ? 0.5F : n;

        Function<Float, Float> one = x -> 121f / 16f * x * x;
        Function<Float, Float> two = x -> (float) (121f / 4f * n2 * Math.pow(x - 6f / 11f, 2) + 1 - n2);
        Function<Float, Float> three = x -> (float) (121 * n2 * n2 * Math.pow(x - 9f / 11f, 2) + 1 - n2 * n2);
        Function<Float, Float> four = x -> (float) (484 * n2 * n2 * n2 * Math.pow(x - 10.5f / 11f, 2) + 1 - n2 * n2 * n2);

        return t -> Math.min(Math.min(one.apply(t), two.apply(t)), Math.min(three.apply(t), four.apply(t)));
    }

    /**
     * A stepped value based on the nearest step to the input value.
     * <p>
     * The size (grade) of the steps depends on the provided value of n
     */
    public static Function<Float, Float> step(Float n) {
        float n2 = n == null ? 2 : n;

        if (n2 < 2)
            throw new IllegalArgumentException("Steps must be >= 2, got: " + n2);

        final int steps = (int)n2;

        return t -> {
            float result = 0;

            if (t < 0)
                return result;

            float stepLength = (1 / (float)steps);

            if (t > (result = (steps - 1) * stepLength))
                return result;

            int testIndex;
            int leftBorderIndex = 0;
            int rightBorderIndex = steps - 1;

            while (rightBorderIndex - leftBorderIndex != 1) {
                testIndex = leftBorderIndex + (rightBorderIndex - leftBorderIndex) / 2;

                if (t >= testIndex * stepLength) {
                    leftBorderIndex = testIndex;
                }
                else {
                    rightBorderIndex = testIndex;
                }
            }

            return leftBorderIndex * stepLength;
        };
    }

    /**
     * Performs an approximation of Catmull-Rom interpolation
     * <p>
     * Given that by necessity, this only accepts a single argument, making this only technically a spline interpolation for n=1
     */
    public static float catmullRom(float n) {
        return (0.5f * (2.0f * (n + 1) + (2.0f * n - 5.0f * (n + 1) + 4.0f * (n + 2) - (n + 3)) * n * n
                + (3.0f * (n + 1) - n - 3.0f * (n + 2) + (n + 3)) * n * n * n));
    }

    /**
     * Generates a value from a given Catmull-Rom spline range with Centripetal parameterization (alpha=0.5)
     * <p>
     * Per standard implementation, this generates a spline curve over control points p1-p2, with p0 and p3
     * acting as curve anchors.<br>
     * We then apply the delta to determine the point on the generated spline to return.
     * <p>
     * Functionally equivalent to Minecraft's {@code Mth.catmullrom}
     *
     * @see <a href="https://en.wikipedia.org/wiki/Centripetal_Catmull%E2%80%93Rom_spline">Wikipedia</a>
     */
    public static double getPointOnSpline(double delta, double p0, double p1, double p2, double p3) {
        return 0.5d * (2d * p1 + (p2 - p0) * delta +
                (2d * p0 - 5d * p1 + 4d * p2 - p3) * delta * delta +
                (3d * p1 - p0 - 3d * p2 + p3) * delta * delta * delta);
    }
}