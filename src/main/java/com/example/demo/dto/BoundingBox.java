package com.example.demo.dto;

public class BoundingBox {
    private int x;
    private int y;
    private int width;
    private int height;

    public BoundingBox(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Calculates the center of the bounding box.
     *
     * @return an array containing the [x, y] coordinates of the center.
     */
    public int[] getCenter() {
        return new int[]{x + width / 2, y + height / 2};
    }

    /**
     * Calculates the area of the bounding box.
     *
     * @return the area.
     */
    public int getArea() {
        return width * height;
    }

    /**
     * Checks if this bounding box overlaps with another one.
     *
     * @param other the other bounding box.
     * @return true if they overlap, false otherwise.
     */
    public boolean overlaps(BoundingBox other) {
        if (this.x >= (other.x + other.width) || other.x >= (this.x + this.width)) {
            return false;
        }
        if (this.y >= (other.y + other.height) || other.y >= (this.y + this.height)) {
            return false;
        }
        return true;
    }
}