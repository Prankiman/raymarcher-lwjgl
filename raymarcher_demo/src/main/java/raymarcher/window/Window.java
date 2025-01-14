package raymarcher.window;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;

import java.io.File;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.opengl.GL;
import raymarcher.inputs.KeyInput;
import raymarcher.inputs.MouseInput;

public class Window {

	private static Window instance = null;

	private int width = 1600;
	private int height = 1000;

	public static int tex_output;

    public static int tex_output_temp;

	private int vaoID, uniformID, uniform2ID, uniform3ID, uniform4ID, uniform5ID;
	public static float xx = 0;

	public static float camx = 0, camy = 0, camz = 0;

	public static Vector3f cam = new Vector3f();
	public static Vector2f rot = new Vector2f();

	float speed = 0.002f;

	private Display display;
	private Shader shader;
	private ComputeShader cs;
	public static int texBuff, tex_out, sampler;

	//private float[] vertexArray;

	private GLFWCursorPosCallback cursor = new MouseInput();

	KeyInput keyboard;

	private long window;

	int screenTex;

	public static Texture skybox, normal, sphere_tex, displace, metal, roughness, output;

	public static float dx, dy, res = 16;

	private Window() {}

	public static Window get() {
		if (instance == null) {
			instance = new Window();
		}
		return instance;
	}

	public void run() {
		init();
		loop();
	}

	public void init() {
		glfwInit();

		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);

		window = glfwCreateWindow(width, height, "Window", 0, 0);
		glfwSetWindowPos(window, 500, 200);
		glfwMakeContextCurrent(window);
		glfwSwapInterval(0);//disable vsync
		GL.createCapabilities();

		glfwSetCursorPosCallback(window, cursor);
		glfwSetKeyCallback(window, keyboard = new KeyInput());

		cs = new ComputeShader();
		cs.create();
		cs.init();
		shader = new Shader();
		shader.create();

		output = new Texture();
		vaoID = glGenVertexArrays();
		//images must use 3+ color channels as grayscale image usage has not been implemented yet
		skybox = new Texture( new File("./resources/skyboxes/street.JPG").getAbsolutePath());
		normal  = new Texture( new File("./resources/Gravel020_1K-JPG/Gravel020_1K_NormalGL.jpg").getAbsolutePath());
		sphere_tex =  new Texture( new File("./resources/Gravel020_1K-JPG/Gravel020_1K_Color.jpg").getAbsolutePath());
		displace =  new Texture( new File("./resources/Gravel020_1K-JPG/Gravel020_1K_Displacement.jpg").getAbsolutePath());
		metal =  new Texture( new File("./resources/Facade018B_1K-JPG/Facade018B_1K_Metalness.jpg").getAbsolutePath());
		roughness =  new Texture( new File("./resources/Gravel020_1K-JPG/Gravel020_1K_Roughness.jpg").getAbsolutePath());

		glBindVertexArray(vaoID);

		display = new Display();
		display.create();


		shader.use();
		uniform2ID = glGetUniformLocation(cs.programID, "xx");
		uniformID = glGetUniformLocation(cs.programID, "mouse_xy");
		uniform3ID = glGetUniformLocation(cs.programID, "orig");
		uniform4ID = glGetUniformLocation(shader.programID, "res");
		uniform5ID = glGetUniformLocation(cs.programID, "res");

		shader.stop();

		glfwShowWindow(window);
	}
	double crntTime = 0, deltaTime, counter, prevTime = 0;

	public void loop() {

		while (!glfwWindowShouldClose(window)) {

			// Updates counter and times
		crntTime = glfwGetTime();
		deltaTime = crntTime - prevTime;
		counter++;

		if (deltaTime >= 1.0 / 30.0)
		{
			// Creates new title
			int FPS = (int)((1.0 / deltaTime) * counter);
			int  ms = (int)((deltaTime / counter) * 1000);
			String newTitle = "Use P to increase resolution and O to decrease resolution- " + FPS + "FPS / " + ms + "ms";
			glfwSetWindowTitle(window, newTitle);

			// Resets times and counter
			prevTime = crntTime;
			counter = 0;
		}

			glfwPollEvents();

			xx += speed;
			if (xx > 0 || xx < -4)
				speed = -speed;

			cs.use();
			glUniform1f(uniform2ID, xx);
			glUniform2f(uniformID, dx*2, dy*2);
			glUniform3f(uniform3ID, cam.x, cam.y, cam.z);
			glUniform1f(uniform5ID, res);

			glActiveTexture(GL_TEXTURE0);
			glBindImageTexture(0, output.texID, 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);
			glBindTexture(GL_TEXTURE_2D, output.texID);
			glBindTextureUnit(0, output.texID);

			glActiveTexture(GL_TEXTURE0+1);
			glBindTexture(GL_TEXTURE_2D, skybox.texID);
			glBindImageTexture(1, skybox.texID, 0, false, 0, GL_READ_ONLY, GL_RGBA);
			glBindTextureUnit(1, skybox.texID);

			glActiveTexture(GL_TEXTURE0+2);
			glBindTexture(GL_TEXTURE_2D, normal.texID);
			glBindImageTexture(2, normal.texID, 0, false, 0, GL_READ_ONLY, GL_RGBA);
			glBindTextureUnit(2, normal.texID);

			glActiveTexture(GL_TEXTURE0+3);
			glBindTexture(GL_TEXTURE_2D, sphere_tex.texID);
			glBindImageTexture(3, sphere_tex.texID, 0, false, 0, GL_READ_ONLY, GL_RGBA);
			glBindTextureUnit(3, sphere_tex.texID);

			glActiveTexture(GL_TEXTURE0+4);
			glBindTexture(GL_TEXTURE_2D, displace.texID);
			glBindImageTexture(4, displace.texID, 0, false, 0, GL_READ_ONLY, GL_LUMINANCE);
			glBindTextureUnit(4, displace.texID);

			glActiveTexture(GL_TEXTURE0+5);
			glBindTexture(GL_TEXTURE_2D, metal.texID);
			glBindImageTexture(5, metal.texID, 0, false, 0, GL_READ_ONLY, GL_LUMINANCE);
			glBindTextureUnit(5, metal.texID);

			glActiveTexture(GL_TEXTURE0+6);
			glBindTexture(GL_TEXTURE_2D, roughness.texID);
			glBindImageTexture(6, roughness.texID, 0, false, 0, GL_READ_ONLY, GL_LUMINANCE);
			glBindTextureUnit(6, roughness.texID);

			cs.disp();

			cs.stop();

			glClear(GL_COLOR_BUFFER_BIT);
			shader.use();
			Render.render(vaoID, display);

			glUniform1f(uniform4ID, res);

			shader.stop();
			glfwSwapBuffers(window);

		}

		glDeleteVertexArrays(vaoID);
		display.delete();
		// cs.delete();
		shader.delete();
		// tex.unbind();
		// tex.delete();

		glfwTerminate();
	}

}
