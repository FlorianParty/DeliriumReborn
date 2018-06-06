package model;

import com.sun.javafx.geom.Vec2d;

public abstract class Bloc {
    Vec2d position;
    private boolean isFriable;

    public Vec2d getPosition() {
        return position;
    }

    public boolean isFriable() {
        return isFriable;
    }

    public void setPosition(Vec2d position) {
        this.position = position;
    }
}
