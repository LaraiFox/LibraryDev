package laraifox.foxtail.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import laraifox.foxtail.core.math.Vector3f;
import laraifox.foxtail.rendering.Texture2D;
import laraifox.foxtail.rendering.Vertex;
import laraifox.foxtail.rendering.models.IndexedModel;
import laraifox.foxtail.rendering.models.Mesh;
import laraifox.foxtail.rendering.models.OBJModel;

public class ResourceManager {
	private static class ReferencedResource<T extends ICleanable> {
		private int referenceCount;
		private T resource;

		public ReferencedResource(T resource) {
			this.referenceCount = 1;
			this.resource = resource;
		}

		@Override
		protected void finalize() {
			resource.cleanUp();
		}

		public void addReference() {
			referenceCount += 1;
		}

		public boolean removeReference() {
			referenceCount -= 1;
			return referenceCount <= 0;
		}

		public T getResource() {
			return resource;
		}
	}

	public static final String FOXTAIL_RESOURCE_DIRECTORY;
	public static final String USERS_APPDATA_DIRECTORY;

	private static final String FILE_SEPARATOR = System.getProperty("file.separator");

	static {
		FOXTAIL_RESOURCE_DIRECTORY = new String("src/laraifox/foxtail/resources");

		String operatingSystem = System.getProperty("os.name").toLowerCase();
		if (operatingSystem.contains("win")) {
			USERS_APPDATA_DIRECTORY = new String(System.getProperty("user.home") + FILE_SEPARATOR + "AppData" + FILE_SEPARATOR + "Roaming");
		} else if (operatingSystem.contains("mac")) {
			USERS_APPDATA_DIRECTORY = new String(System.getProperty("user.home") + FILE_SEPARATOR + "Library");
		} else if (operatingSystem.contains("lin")) {
			USERS_APPDATA_DIRECTORY = new String(System.getProperty("user.home") + FILE_SEPARATOR + "Library");
		} else {
			USERS_APPDATA_DIRECTORY = new String(System.getProperty("user.home"));
		}
	}

	private static final Map<String, ResourceManager.ReferencedResource<Mesh>> MESH_RESOURCE_MAP = new HashMap<String, ResourceManager.ReferencedResource<Mesh>>();
	private static final Map<String, ResourceManager.ReferencedResource<Texture2D>> TEXTURE_2D_RESOURCE_MAP = new HashMap<String, ResourceManager.ReferencedResource<Texture2D>>();

	private static String programFolderName = new String("");
	private static String internalResourcePrefix = new String("");

	private ResourceManager() {

	}

	public static void initialize(String programFolderName, String internalPrefix) {
		ResourceManager.programFolderName = programFolderName;
		ResourceManager.internalResourcePrefix = internalPrefix;
	}

	public static Mesh getMeshResource(String filepath) {
		ReferencedResource<Mesh> result = MESH_RESOURCE_MAP.get(filepath);
		if (result == null) {
			result = new ReferencedResource<Mesh>(ResourceManager.loadModel(filepath));
			MESH_RESOURCE_MAP.put(filepath, result);
		} else {
			result.addReference();
		}

		return result.getResource();
	}

	public static void releaseMeshResource(String filepath) {
		ReferencedResource<Mesh> result = MESH_RESOURCE_MAP.get(filepath);
		if (result.removeReference()) {
			MESH_RESOURCE_MAP.remove(filepath);
		}
	}

	private static String getFormattedFilepath(String filepath) {
		if (!filepath.startsWith(FILE_SEPARATOR)) {
			return new String(FILE_SEPARATOR + filepath);
		}

		return filepath;
	}

	public static String getExternalResourcePath(String filepath) {
		return new String(USERS_APPDATA_DIRECTORY + programFolderName + ResourceManager.getFormattedFilepath(filepath));
	}

	public static String getFoxtailResourcePath(String filepath) {
		return new String(FOXTAIL_RESOURCE_DIRECTORY + ResourceManager.getFormattedFilepath(filepath));
	}

	public static String getInternalResourcePath(String filepath) {
		return new String(internalResourcePrefix + ResourceManager.getFormattedFilepath(filepath));
	}

	public static String loadShaderSource(String filepath) throws IOException {
		final String INCLUDE_DIRECTIVE = "#pragma include";

		// InputStream inputStream = ResourceManager.class.getResourceAsStream(filepath);
		// BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		File file = new File(filepath);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		StringBuilder result = new StringBuilder();

		String line = new String();
		while ((line = reader.readLine()) != null) {
			if (line.trim().startsWith(INCLUDE_DIRECTIVE)) {
				int includeFilepathStart = line.indexOf(INCLUDE_DIRECTIVE);
				while (line.charAt(includeFilepathStart - 1) != '\"')
					includeFilepathStart++;

				int includeFilepathEnd = line.length() - 1;
				while (line.charAt(includeFilepathEnd) != '\"')
					includeFilepathEnd--;

				result.append(loadShaderSource(file.getParent() + line.substring(includeFilepathStart, includeFilepathEnd)));
			} else {
				result.append(line).append("\n");
			}
		}

		reader.close();

		return result.toString();
	}

	public static String loadFile(String filepath) throws IOException {
		// InputStream inputStream = ResourceManager.class.getResourceAsStream(filepath);
		// BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		BufferedReader reader = new BufferedReader(new FileReader(new File(filepath)));

		StringBuilder result = new StringBuilder();

		String line = new String();
		while ((line = reader.readLine()) != null) {
			result.append(line).append("\n");
		}

		reader.close();

		return result.toString();
	}

	private static final Mesh loadModel(String filepath) {
		String extention = filepath.substring(filepath.lastIndexOf('.'));

		try {
			if (extention.endsWith("obj")) {
				return ResourceManager.loadOBJMesh(filepath);
			} else {
				Logger.log("File format not supported for model '" + filepath + "'", "ResourceManager", Logger.MESSAGE_LEVEL_ERROR);
				Logger.log("Supported file formats are: WaveFront OBJ (.obj)", "ResourceManager", Logger.MESSAGE_LEVEL_ERROR);
				System.exit(1);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}

		return null;
	}

	private static Mesh loadOBJMesh(String filepath) throws FileNotFoundException {
		// InputStream inputStream = ResourceManager.class.getResourceAsStream(filepath);
		// BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		BufferedReader reader = new BufferedReader(new FileReader(new File(filepath)));

		OBJModel objModel = new OBJModel(reader);
		IndexedModel indexedModel = objModel.toIndexedModel();

		ArrayList<Vertex> vertices = new ArrayList<Vertex>();

		for (int i = 0; i < indexedModel.getPositions().size(); i++) {
			vertices.add(new Vertex(indexedModel.getPositions().get(i), indexedModel.getTexCoords().get(i), indexedModel.getNormals().get(i), new Vector3f(), new Vector3f()));
		}
		Vertex[] vertexData = new Vertex[vertices.size()];
		vertices.toArray(vertexData);

		Integer[] indexData = new Integer[indexedModel.getIndices().size()];
		indexedModel.getIndices().toArray(indexData);

		return new Mesh(filepath, vertexData, ArrayUtils.toIntArray(indexData));
	}
}
