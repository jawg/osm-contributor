/**
 * Copyright (C) 2016 eBusiness Information
 *
 * This file is part of OSM Contributor.
 *
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor.utils.ways;

import android.graphics.Rect;

import org.junit.Test;

import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.offset;


/**
 * ClipperTests, here is the used data
 * <pre>
 *
 *
 *                         M
 *
 *                                   L
 *             F
 *         0 . . . . 5 . . . . 10
 *         .                   .
 *         .                   .
 *         .                   .
 *         .   D               .
 *         5                   .
 *         .                   .
 *         .         A         Q       N
 *         .                   .
 * H       .   G               P
 *         10. . .C. . .K. O . .
 *      I
 *
 * E           B           J
 *
 *
 *
 * </pre>
 */
public class ClipperTest {


    Rect clippingRect = new Rect();

    /**
     * Use a init bloc to set values of rect as the modified version of the android.jar kills the constructor doing that
     */ {
        clippingRect.left = 0;
        clippingRect.top = 0;
        clippingRect.right = 10;
        clippingRect.bottom = 10;
    }

    XY pointA = new XY(5, 7);
    XY pointB = new XY(2, 13);
    XY pointC = new XY(3.5, 10);
    XY pointD = new XY(2, 4);
    XY pointE = new XY(-4, 13);
    XY pointF = new XY(2, -1);
    XY pointG = new XY(2, 9);
    XY pointH = new XY(-4, 9);
    XY pointI = new XY(-1, 11);
    XY pointJ = new XY(8, 13);
    XY pointK = new XY(6.5, 10);
    XY pointL = new XY(13, -2);
    XY pointM = new XY(8, -4);
    XY pointN = new XY(14, 7);
    XY pointO = new XY(8, 10);
    XY pointP = new XY(10, 9);
    XY pointQ = new XY(10, 7);

    Clipper testedClipper = new Clipper(clippingRect);

    private void checkPoint(XY actual, XY expected) {
        assertThat(actual.getX()).isEqualTo(expected.getX(), offset(0.000001));
        assertThat(actual.getY()).isEqualTo(expected.getY(), offset(0.000001));
    }

    @Test
    public void testIntersectVerticalFirstLine() {
        XY intersection = testedClipper.intersection(pointF, pointB, pointA, pointE);
        checkPoint(intersection, pointG);
    }


    @Test
    public void testIntersectVerticalSecondLine() {
        XY intersection = testedClipper.intersection(pointA, pointE, pointF, pointB);
        checkPoint(intersection, pointG);
    }

    @Test
    public void testIntersect() {
        XY intersection = testedClipper.intersection(pointB, pointH, pointA, pointE);
        checkPoint(intersection, pointI);
    }

    @Test
    public void clippingDoesNotModifyLineInBounds() {
        assertThat(new Clipper(clippingRect).clip(Arrays.asList(pointA, pointD), false)).containsExactly(pointA, pointD);
    }

    @Test
    public void clippingLineOutOfBoundsReturnsEmptyList() {
        assertThat(new Clipper(clippingRect).clip(Arrays.asList(pointH, pointE), false)).isEmpty();
        assertThat(new Clipper(clippingRect).clip(Arrays.asList(pointL, pointM), false)).isEmpty();
    }

    @Test
    public void clippingWorksOnLine() {
        assertThat(new Clipper(clippingRect).clip(Arrays.asList(pointA, pointB), false)).containsExactly(pointA, pointC);
    }

    @Test
    public void clippingWorksOnBasicTriangle() {
        assertThat(new Clipper(clippingRect).clip(Arrays.asList(pointA, pointB, pointJ), true)).containsExactly(pointK, pointA, pointC);
    }

    @Test
    public void clippingWorksOnTriangle() {
        assertThat(new Clipper(clippingRect).clip(Arrays.asList(pointA, pointB, pointN), true)).containsExactly(pointQ, pointA, pointC, pointO, pointP);
    }

    @Test
    public void clippingWorksMultipleTimes() {
        Clipper clipper = new Clipper(clippingRect);
        assertThat(clipper.clip(Arrays.asList(pointA, pointB, pointJ), true)).containsExactly(pointK, pointA, pointC);
        clipper.setClippingBounds(clippingRect);
        assertThat(clipper.clip(Arrays.asList(pointA, pointB, pointN), true)).containsExactly(pointQ, pointA, pointC, pointO, pointP);
    }


    /**
     * <pre>
     *
     *         0 . . . . 5 . . . . 10
     *         .                   .
     *         .                   .
     *         .                   .
     *         .                   .
     *         5                   .
     *         .                   .
     *         .                   .
     *         . a b           d e .
     *         .                   .
     *         10. . j i . h g . . .
     *                   c
     *                   f
     *
     * </pre>
     */
    @Test
    public void clippingV() {
        XY a = new XY(1, 8);
        XY b = new XY(2, 8);
        XY c = new XY(5, 11);
        XY d = new XY(8, 8);
        XY e = new XY(9, 8);
        XY f = new XY(5, 12);
        XY g = new XY(7, 10);
        XY h = new XY(6, 10);
        XY i = new XY(4, 10);
        XY j = new XY(3, 10);

        assertThat(new Clipper(clippingRect).clip(Arrays.asList(a, b, c, d, e, f), true)).containsExactly(j, a, b, i, h, d, e, g);
    }
}