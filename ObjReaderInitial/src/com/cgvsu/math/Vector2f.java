package com.cgvsu.math;

import java.util.Objects;

import static com.cgvsu.Main.eps;

// Это заготовка для собственной библиотеки для работы с линейной алгеброй
public class Vector2f {
    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public boolean equals(Vector2f other) {
        return Math.abs(x - other.x) < eps && Math.abs(y - other.y) < eps;
    }

    float x, y;
}
