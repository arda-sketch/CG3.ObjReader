package com.cgvsu;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.IncorrectFileException;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objreader.PathReadException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws PathReadException, IncorrectFileException {
        String fileContent = null;
        try {
            Path fileName = Path.of("Your.obj");
            fileContent = Files.readString(fileName);
        } catch (IOException e) {
            throw new PathReadException();
        }

        System.out.println("Loading model ...");
        Model model = ObjReader.read(fileContent);

        System.out.println("Vertices: " + model.vertices.size());
        System.out.println("Texture vertices: " + model.textureVertices.size());
        System.out.println("Normals: " + model.normals.size());
        System.out.println("Polygons: " + model.polygons.size());
    }

    public static final float eps = 1e-7f;
}