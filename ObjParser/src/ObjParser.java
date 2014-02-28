import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

public class ObjParser {
	private float[] coords;
	private float[] normals;
	private float[] blendedNormals;
	private float[] matAmb;
	private float[] matDiff;
	private float[] matSpec;
	private int[] shininess;
	private float scale;
	private HashMap matList = new HashMap<String, Material>();
	
	public static void main(String[] args) throws IOException {
		ObjParser obj = new ObjParser("rect.obj", false, false);
		obj.printData();

		ProcessBuilder pb = new ProcessBuilder("Notepad.exe", "output.txt");
		pb.start();
	}

	public ObjParser(String filename, boolean smoothing, boolean grouping) throws FileNotFoundException {
		readObj(filename, smoothing, grouping );
	}

	public void readObj(String filename, boolean smoothing, boolean grouping) throws FileNotFoundException {
		float minX = -1;
		float minY = -1;
		float minZ = -1;
		float maxX = 1;
		float maxY = 1;
		float maxZ = 1;

		ArrayList<Float> vertList = new ArrayList<Float>();
		ArrayList<Short> vertDrawList = new ArrayList<Short>();
		ArrayList<Float> normalList = new ArrayList<Float>();
		ArrayList<Short> normalDrawList = new ArrayList<Short>();
		ArrayList<Material> materialList = new ArrayList<Material>();
		Material currentMat = new Material();

		File file = new File(filename);
		Scanner fileScanner = new Scanner(new FileReader(file));
		while (fileScanner.hasNextLine()) {
			Scanner lineScanner = new Scanner(fileScanner.nextLine());
			if (lineScanner.hasNext()) {
				String title = lineScanner.next();
				if (title.equals("mtllib")) {
					readMtl(lineScanner.next()); // load material data
				} else if (title.equals("v")) {
					float x = lineScanner.nextFloat(); // retrieve vertex data
					float y = lineScanner.nextFloat();
					float z = lineScanner.nextFloat();
					vertList.add(x);
					vertList.add(y);
					vertList.add(z);

					if (x < minX) {
						minX = x;
					}
					if (y < minY) {
						minY = y;
					}
					if (z < minZ) {
						minZ = z;
					}
					if (x > maxX) {
						maxX = x;
					}
					if (y > maxY) {
						maxY = y;
					}
					if (z > maxZ) {
						maxZ = z;
					}
				} else if (title.equals("vn")) {
					// retrieve normal data
					normalList.add(lineScanner.nextFloat());
					normalList.add(lineScanner.nextFloat());
					normalList.add(lineScanner.nextFloat());

				} else if (title.equals("usemtl")) {
					// set current material for face group
					String s = lineScanner.next();
					currentMat = (Material) matList.get(s);
				} else if (title.equals("f")) {
					// update vert and normal draw lists from obj face data
					Scanner stringScanner = new Scanner(lineScanner.next());
					stringScanner.useDelimiter("//");
					vertDrawList.add((short) (stringScanner.nextShort() - 1));
					normalDrawList.add((short) (stringScanner.nextShort() - 1));
					stringScanner = new Scanner(lineScanner.next());
					stringScanner.useDelimiter("//");
					vertDrawList.add((short) (stringScanner.nextShort() - 1));
					normalDrawList.add((short) (stringScanner.nextShort() - 1));
					stringScanner = new Scanner(lineScanner.next());
					stringScanner.useDelimiter("//");
					vertDrawList.add((short) (stringScanner.nextShort() - 1));
					normalDrawList.add((short) (stringScanner.nextShort() - 1));
					// add material for each vertex
					materialList.add(currentMat);
					materialList.add(currentMat);
					materialList.add(currentMat);
				}
			}
		}

		float dX = maxX - minX;
		float dY = maxY - minY;
		float dZ = maxZ - minZ;
		float max = Math.max(Math.max(dX, dZ), dZ);
		float tx = -1 * (maxX + minX) / (2 * max);
		float ty = -1 * (maxY + minY) / (2 * max);
		float tz = -1 * (maxZ + minZ) / (2 * max);
		tx = 0;
		ty = 0;
		tz = 0;
		// max = 1;

		// populate material draw lists
		matAmb = new float[materialList.size() * 3];
		matDiff = new float[materialList.size() * 3];
		matSpec = new float[materialList.size() * 3];
		shininess = new int[materialList.size()];
		for (int n = 0; n < materialList.size(); n++) {
			matAmb[n * 3] = materialList.get(n).ka[0];
			matAmb[n * 3 + 1] = materialList.get(n).ka[1];
			matAmb[n * 3 + 2] = materialList.get(n).ka[2];

			matDiff[n * 3] = materialList.get(n).kd[0];
			matDiff[n * 3 + 1] = materialList.get(n).kd[1];
			matDiff[n * 3 + 2] = materialList.get(n).kd[2];

			matSpec[n * 3] = materialList.get(n).ks[0];
			matSpec[n * 3 + 1] = materialList.get(n).ks[1];
			matSpec[n * 3 + 2] = materialList.get(n).ks[2];

			shininess[n] = materialList.get(n).ns;
		}

		// populate coord data from vertList, vertDrawList look-up
		coords = new float[vertDrawList.size() * 3];
		for (int n = 0; n < vertDrawList.size(); n++) {
			coords[n * 3] = vertList.get(vertDrawList.get(n) * 3) / max;
			coords[n * 3 + 1] = vertList.get(vertDrawList.get(n) * 3 + 1) / max;
			coords[n * 3 + 2] = vertList.get(vertDrawList.get(n) * 3 + 2) / max;
		}

		ArrayList<ArrayList<Float>> blendMap = new ArrayList<ArrayList<Float>>();

		for (int n = 0; n < normalDrawList.size() * 3; n++) {
			blendMap.add(new ArrayList<Float>());
		}
		for (int n = 0; n < normalDrawList.size(); n++) {
			blendMap.get(vertDrawList.get(n)).add(
					normalList.get(normalDrawList.get(n) * 3));
		}
		for (ArrayList<Float> l : blendMap) {
			for (float f : l) {
				// System.out.print(f + " ");
			}
			// System.out.println();
		}

		//boolean condition = true;
		//condition = false;

		// populate normal data from normalList, normalDrawList look-up
		normals = new float[normalDrawList.size() * 3];
		if (smoothing == false) {
			for (int n = 0; n < normalDrawList.size(); n++) {
				normals[n * 3] = normalList.get(normalDrawList.get(n) * 3);
				normals[n * 3 + 1] = normalList
						.get(normalDrawList.get(n) * 3 + 1);
				normals[n * 3 + 2] = normalList
						.get(normalDrawList.get(n) * 3 + 2);
			}
		}

		// average normals if smoothing enabled
		if (smoothing == true) {
			ArrayList<ArrayList<Short>> tallyList = new ArrayList<ArrayList<Short>>();
			ArrayList<ArrayList<Short>> totalList = new ArrayList<ArrayList<Short>>();
			int maxValue = Collections.max(vertDrawList);
			for (int n = 0; n <= maxValue; n++) {
				tallyList.add(new ArrayList<Short>());
				totalList.add(new ArrayList<Short>());
			}
			float[] newNormalList = new float[vertList.size()];
			for (int n = 0; n < vertDrawList.size(); n++) {
				if (!tallyList.get(vertDrawList.get(n)).contains(
						normalDrawList.get(n)))
					tallyList.get(vertDrawList.get(n)).add(
							normalDrawList.get(n));
				totalList.get(vertDrawList.get(n)).add(normalDrawList.get(n));
			}
			for (int i = 0; i < tallyList.size(); i++) {
				ArrayList<Short> l = tallyList.get(i);
				ArrayList<Short> t = totalList.get(i);
				double average1 = 0.0;
				double average2 = 0.0;
				double average3 = 0.0;
				for (short s : l) {
					average1 += normalList.get(s * 3);
					average2 += normalList.get(s * 3 + 1);
					average3 += normalList.get(s * 3 + 2);
				}
				average1 = average1 / l.size();
				average2 = average2 / l.size();
				average3 = average3 / l.size();
				newNormalList[i * 3] = (float) average1;
				newNormalList[i * 3 + 1] = (float) average2;
				newNormalList[i * 3 + 2] = (float) average3;
				// System.out.println(average1 + " " + average2 + " " +
				// average3);
			}
			for (int n = 0; n < vertDrawList.size(); n++) {
				short s = vertDrawList.get(n);
				normals[n * 3] = (float) newNormalList[s * 3];
				normals[n * 3 + 1] = (float) newNormalList[s * 3 + 1];
				normals[n * 3 + 2] = (float) newNormalList[s * 3 + 2];
			}
		}
		// System.out.println(vertDrawList.size());

	}

	public void readMtl(String filename) throws FileNotFoundException {
		String name = "";

		float ka[] = { 0.0f, 0.0f, 0.0f };
		float kd[] = { 0.0f, 0.0f, 0.0f };
		float ks[] = { 0.0f, 0.0f, 0.0f };
		int ns = 0;
		File file = new File(filename);
		Scanner fileScanner = new Scanner(new FileReader(file));
		while (fileScanner.hasNextLine()) {

			Scanner lineScanner = new Scanner(fileScanner.nextLine());
			if (lineScanner.hasNext()) {
				String title = lineScanner.next();
				if (title.equals("newmtl")) {
					name = lineScanner.next();
					// System.out.println("Name " + name);
				}
				if (title.equals("Ns")) {
					ns = (int) lineScanner.nextFloat();
				}
				if (title.equals("Ka")) {
					ka = new float[] { lineScanner.nextFloat(),
							lineScanner.nextFloat(), lineScanner.nextFloat() };
				}
				if (title.equals("Kd")) {
					kd = new float[] { lineScanner.nextFloat(),
							lineScanner.nextFloat(), lineScanner.nextFloat() };
				}
				if (title.equals("Ks")) {
					ks = new float[] { lineScanner.nextFloat(),
							lineScanner.nextFloat(), lineScanner.nextFloat() };
					if (!name.equals("")) {
						// System.out.println(name + "" + ka + "" + kd + "" + ks
						// + "" + ns);
						Material m = new Material(ka, kd, ks, ns, name);

						matList.put(name, m);
					}
				}
			}
		}
	}

	public void printData() throws FileNotFoundException,
			UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter("output.txt", "UTF-8");

		writer.println("public float[] setCoords() {");
		StringBuilder strCoords = new StringBuilder();
		strCoords.append("float coords[] = { ");
		for (int n = 0; n < coords.length; n++) {
			float f = coords[n];
			if (n < coords.length - 1)
				strCoords.append(f + "f, ");
			else
				strCoords.append(f + "f };");
		}
		writer.println(strCoords.toString());
		writer.println("return coords; }");

		writer.println("public float[] setNormals() {");
		StringBuilder strNormals = new StringBuilder();
		strNormals.append("float normals[] = { ");
		for (int n = 0; n < normals.length; n++) {
			float f = normals[n];
			if (n < normals.length - 1)
				strNormals.append(f + "f, ");
			else
				strNormals.append(f + "f };");
		}
		writer.println(strNormals.toString());
		writer.println("return normals; }");

		writer.println("public float[] setAmb() {");
		StringBuilder strAmb = new StringBuilder();
		strAmb.append("float amb[] = { ");
		for (int n = 0; n < matAmb.length; n++) {
			float f = matAmb[n];
			// f = 1.0f;
			if (n < matAmb.length - 1)
				strAmb.append(f + "f, ");
			else
				strAmb.append(f + "f };");
		}
		writer.println(strAmb);
		writer.println("return amb; }");

		writer.println("public float[] setDiff() {");
		StringBuilder strDiff = new StringBuilder();
		strDiff.append("float diff[] = { ");
		for (int n = 0; n < matDiff.length; n++) {
			float f = matDiff[n];
			if (n < matDiff.length - 1)
				strDiff.append(f + "f, ");
			else
				strDiff.append(f + "f };");
		}
		writer.println(strDiff);
		writer.println("return diff; }");

		writer.println("public float[] setSpec() {");
		StringBuilder strSpec = new StringBuilder();
		strSpec.append("float spec[] = { ");
		for (int n = 0; n < matSpec.length; n++) {
			float f = matSpec[n];
			if (n < matSpec.length - 1)
				strSpec.append(f + "f, ");
			else
				strSpec.append(f + "f };");
		}
		writer.println(strSpec);
		writer.println("return spec; }");

		writer.println("public float[] setShine() {");
		StringBuilder strShine = new StringBuilder();
		strShine.append("float shine[] = { ");
		for (int n = 0; n < shininess.length; n++) {
			int f = shininess[n];
			if (n < shininess.length - 1)
				strShine.append(f + ", ");
			else
				strShine.append(f + " };");
		}
		writer.println(strShine);
		writer.println("return shine; }");

		// System.out.println(coords.length + " " + matSpec.length + " " +
		// matDiff.length + " " + matSpec.length + " " + shininess.length);
		writer.close();
	}


}
