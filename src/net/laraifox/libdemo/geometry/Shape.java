package net.laraifox.libdemo.geometry;

import net.laraifox.libdemo.graphics.Transform3D;
import net.laraifox.libdemo.graphics.Vertex3D;
import net.laraifox.libdemo.interfaces.IGeometricObject;
import net.laraifox.libdemo.math.Matrix4f;
import net.laraifox.libdemo.math.Quaternion;
import net.laraifox.libdemo.math.Vector2f;
import net.laraifox.libdemo.math.Vector3f;

public class Shape implements IGeometricObject {
	protected Transform3D transform;
	protected Vector3f[] vertices;
	protected Vector3f[] normals;

	public Shape(Vector3f[] vertices) {
		this(new Transform3D(), vertices);
	}

	public Shape(Transform3D transform, Vector3f[] points) {
		this.transform = transform;
		this.vertices = points;
		
		calculateEdgeNormals();
	}

	public Shape(Transform3D transform, Vertex3D[] vertices) {
		this.transform = transform;
		this.vertices = vertices;
		calculateEdgeNormals();
	}

	public Shape(Shape shape) {
		this.transform = shape.transform;
		this.vertices = shape.vertices;
		calculateEdgeNormals();
	}

	protected void calculateEdgeNormals() {
		for (int i = 0; i < vertices.length; i++) {
			Vector3f normal = Vector3f.subtract(vertices[i].getPosition(), vertices[i + 1 == vertices.length ? 0 : i + 1].getPosition());
			vertices[i].setNormal(Vector3f.normalize(normal.cross()));
			// TODO: Figure out a way to calculate normals; May have to override this method in subclasses;
		}
	}

	public void transform(Transform3D transformation) {
		transform.transform(transformation);
	}

	public void translate(float dx, float dy, float dz) {
		transform.translate(dx, dy, dz);
	}

	public void translate(Vector3f translation) {
		transform.translate(translation);
	}

	public void rotate(Quaternion rotation) {
		transform.rotate(rotation);
	}

	public void scale(float scale) {
		transform.scale(scale);
	}

	public void scale(float dx, float dy, float dz) {
		transform.scale(dx, dy, dz);
	}

	public void scale(Vector3f scale) {
		transform.scale(scale);
	}

	public Vertex3D[] getVertices() {
		return vertices;
	}

	public Vector3f[] getPoints() {
		Vector3f[] tranformedVertices = new Vector3f[vertices.length];
		Matrix4f tranformationMatrix = transform.getTransformationMatrix();
		for (int i = 0; i < vertices.length; i++) {
			tranformedVertices[i] = tranformationMatrix.multiply(vertices[i].getPosition());
		}

		return tranformedVertices;
	}

	public Vector3f[] getEdgeNormals() {
		Vector3f[] normals = new Vector3f[vertices.length];
		for (int i = 0; i < normals.length; i++) {
			normals[i] = Vector3f.rotate(vertices[i].getNormal(), transform.getRotation());
		}

		return normals;
	}

	public float getX() {
		return transform.getTranslation().getX();
	}

	public float getY() {
		return transform.getTranslation().getY();
	}

	public float getZ() {
		return transform.getTranslation().getZ();
	}

	public float getMinX() {
		Vector3f axis = Vector3f.Right();
		Vector3f[] verticesArray = this.getPoints();

		float result = axis.dot(verticesArray[0]);

		for (int i = 1; i < verticesArray.length; i++) {
			float dot = axis.dot(verticesArray[i]);

			if (dot < result)
				result = dot;
		}

		return result;
	}

	public float getMinY() {
		Vector3f axis = Vector3f.Up();
		Vector3f[] verticesArray = this.getPoints();

		float result = axis.dot(verticesArray[0]);

		for (int i = 1; i < verticesArray.length; i++) {
			float dot = axis.dot(verticesArray[i]);

			if (dot < result)
				result = dot;
		}

		return result;
	}

	public float getMinZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getMaxX() {
		Vector3f axis = Vector3f.Right();
		Vector3f[] verticesArray = this.getPoints();

		float result = axis.dot(verticesArray[0]);

		for (int i = 1; i < verticesArray.length; i++) {
			float dot = axis.dot(verticesArray[i]);

			if (dot > result)
				result = dot;
		}

		return result;
	}

	public float getMaxY() {
		Vector3f axis = Vector3f.Up();
		Vector3f[] verticesArray = this.getPoints();

		float result = axis.dot(verticesArray[0]);

		for (int i = 1; i < verticesArray.length; i++) {
			float dot = axis.dot(verticesArray[i]);

			if (dot > result)
				result = dot;
		}

		return result;
	}

	public float getMaxZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getWidth() {
		Vector3f axis = Vector3f.Right();
		Vector3f[] verticesArray = this.getPoints();

		float min = axis.dot(verticesArray[0]);
		float max = min;

		for (int i = 1; i < verticesArray.length; i++) {
			float dot = axis.dot(verticesArray[i]);

			if (dot < min)
				min = dot;
			else if (dot > max)
				max = dot;
		}

		return max - min;
	}

	public float getHeight() {
		Vector3f axis = Vector3f.Up();
		Vector3f[] verticesArray = this.getPoints();

		float min = axis.dot(verticesArray[0]);
		float max = min;

		for (int i = 1; i < verticesArray.length; i++) {
			float dot = axis.dot(verticesArray[i]);

			if (dot < min)
				min = dot;
			else if (dot > max)
				max = dot;
		}

		return max - min;
	}

	public float getLength() {
		Vector3f axis = Vector3f.Forward();
		Vector3f[] verticesArray = this.getPoints();

		float min = axis.dot(verticesArray[0]);
		float max = min;

		for (int i = 1; i < verticesArray.length; i++) {
			float dot = axis.dot(verticesArray[i]);

			if (dot < min)
				min = dot;
			else if (dot > max)
				max = dot;
		}

		return max - min;
	}
}
