package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.*;

public class ObjReader {

	private static final String OBJ_VERTEX_TOKEN = "v";
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	private static final String OBJ_NORMAL_TOKEN = "vn";
	private static final String OBJ_FACE_TOKEN = "f";

	public static Model read(String fileContent) throws IncorrectFileException {
		ArrayList<Vector3f> vertices = new ArrayList<>();
		ArrayList<Vector2f> textureVertices = new ArrayList<>();
		ArrayList<Vector3f> normals = new ArrayList<>();
		ArrayList<Polygon> polygons = new ArrayList<>();
		int lineInd = 0;
		Scanner scanner = new Scanner(fileContent);
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			ArrayList<String> wordsInLine = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
			if (wordsInLine.isEmpty()) {
				continue;
			}

			final String token = wordsInLine.get(0);
			wordsInLine.remove(0);

			++lineInd;
			switch (token) {
				// Для структур типа вершин методы написаны так, чтобы ничего не знать о внешней среде.
				// Они принимают только то, что им нужно для работы, а возвращают только то, что могут создать.
				// Исключение - индекс строки. Он прокидывается, чтобы выводить сообщение об ошибке.
				// Могло быть иначе. Например, метод parseVertex мог вместо возвращения вершины принимать вектор вершин
				// модели или сам класс модели, работать с ним.
				// Но такой подход может привести к большему количеству ошибок в коде. Например, в нем что-то может
				// тайно сделаться с классом модели.
				// А еще это портит читаемость
				// И не стоит забывать про тесты. Чем проще вам задать данные для теста, проверить, что метод рабочий,
				// тем лучше.
				case OBJ_VERTEX_TOKEN -> vertices.add(parseVertex(wordsInLine, lineInd));
				case OBJ_TEXTURE_TOKEN -> textureVertices.add(parseTextureVertex(wordsInLine, lineInd));
				case OBJ_NORMAL_TOKEN -> normals.add(parseNormal(wordsInLine, lineInd));
				case OBJ_FACE_TOKEN -> polygons.add(parseFace(wordsInLine, lineInd));
				default -> {}
			}
		}
		int lengthVertex = vertices.size();
		int lengthTextureVertex = textureVertices.size();
		int lengthNormal = normals.size();
		int lengthPolygons = polygons.size();
//		if (lengthVertex!=lengthTextureVertex){
//			throw new IncorrectFileException("The number of vertices does not match the number of textures");
//		}
//		if(lengthVertex==0){
//			throw new IncorrectFileException("There are no vertices in this file");
//		}
//		if(lengthPolygons==0){
//			throw new IncorrectFileException("There are no polygons in this file");
//		}
		format(polygons, lengthVertex, lengthTextureVertex, lengthNormal);
		return new Model(vertices, textureVertices, normals, polygons);
	}

	// Всем методам кроме основного я поставил модификатор доступа protected, чтобы обращаться к ним в тестах
	protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		if (wordsInLineWithoutToken.size()>3){
			throw new ObjReaderException("Too many vertex arguments.", lineInd);
		}
		try {
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));
		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few vertex arguments.", lineInd);
		}
	}

	protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		if (wordsInLineWithoutToken.size()>2){
			throw new ObjReaderException("Too many texture vertex arguments.", lineInd);
		}
		try {
			return new Vector2f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few texture vertex arguments.", lineInd);
		}
	}

	protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		if (wordsInLineWithoutToken.size()>3){
			throw new ObjReaderException("Too many normal arguments.", lineInd);
		}
		try {
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few normal arguments.", lineInd);
		}
	}

	protected static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		if (wordsInLineWithoutToken.size()<3){
			throw new ObjReaderException("Not enough vertex to create polygon.", lineInd);
		}
		ArrayList<Integer> onePolygonVertexIndices = new ArrayList<Integer>();
		ArrayList<Integer> onePolygonTextureVertexIndices = new ArrayList<Integer>();
		ArrayList<Integer> onePolygonNormalIndices = new ArrayList<Integer>();

		parseFaceWord(wordsInLineWithoutToken.get(0), onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd);
		int baseCountType = onePolygonTextureVertexIndices.size()+onePolygonNormalIndices.size()+onePolygonVertexIndices.size();
		for (int s=1; s<wordsInLineWithoutToken.size(); s++) {
			parseFaceWord(wordsInLineWithoutToken.get(s), onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd);
			if (baseCountType*(s+1)!=onePolygonTextureVertexIndices.size()+onePolygonNormalIndices.size()+onePolygonVertexIndices.size()){
				throw new ObjReaderException("Incorrect data for polygon.", lineInd);
			}
		}
		Set<Integer> set = new HashSet<>();
		for (Integer vertex: onePolygonVertexIndices) {
			if(!set.add(vertex)){
				throw new ObjReaderException("The vertices of the polygon must not be repeated.", lineInd);
			}
		}
		Polygon result = new Polygon();
		result.setVertexIndices(onePolygonVertexIndices);
		result.setTextureVertexIndices(onePolygonTextureVertexIndices);
		result.setNormalIndices(onePolygonNormalIndices);
		return result;
	}

	// Обратите внимание, что для чтения полигонов я выделил еще один вспомогательный метод.
	// Это бывает очень полезно и с точки зрения структурирования алгоритма в голове, и с точки зрения тестирования.
	// В радикальных случаях не бойтесь выносить в отдельные методы и тестировать код из одной-двух строчек.
	protected static void parseFaceWord(
			String wordInLine,
			ArrayList<Integer> onePolygonVertexIndices,
			ArrayList<Integer> onePolygonTextureVertexIndices,
			ArrayList<Integer> onePolygonNormalIndices,
			int lineInd) {
		try {
			String[] wordIndices = wordInLine.split("/");
			switch (wordIndices.length) {
				case 1 -> {
						onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]));
				}
				case 2 -> {
					onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]));
					onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]));
				}
				case 3 -> {
					onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]));
					onePolygonNormalIndices.add(Integer.parseInt(wordIndices[2]));
					if (!wordIndices[1].equals("")) {
						onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]));
					}
				}
				default -> {
					throw new ObjReaderException("Invalid element size.", lineInd);
				}
			}

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse int value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few arguments.", lineInd);
		}
	}
	protected static void format(ArrayList<Polygon> polygons, int lengthVertex, int lengthTextureVertex, int lengthNormal) throws IncorrectFileException {
		for (Polygon pol: polygons) {
			ArrayList<Integer> vertexIndices = pol.getVertexIndices();
			ArrayList<Integer> textureVertexIndices = pol.getTextureVertexIndices();
			ArrayList<Integer> normalIndices = pol.getNormalIndices();
			formatArray(vertexIndices, lengthVertex);
			formatArray(textureVertexIndices, lengthTextureVertex);
			formatArray(normalIndices, lengthNormal);

		}
	}
	protected static void formatArray(ArrayList<Integer> array, int n) throws IncorrectFileException {
		for (int i = 0; i < array.size(); i++) {
			if (array.get(i) > 0) {
				if (array.get(i)>n){
					throw new IncorrectFileException("Incorrect polygon parameters set.");
				}
				array.set(i, array.get(i) - 1);
			} else {
				if (Math.abs(array.get(i))>n){
					throw new IncorrectFileException("Incorrect polygon parameters set.");
				}
				array.set(i, array.get(i) + n);
			}
		}
	}
}