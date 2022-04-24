package raymarcher.window;

import static org.lwjgl.opengl.GL45.*;

public class Render {
	
	public static void render(int vaoID, Display model) {
		glBindVertexArray(vaoID);
		glBindBuffer(GL_ARRAY_BUFFER, model.getVboID());
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, model.getIboID());
		
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		model.setPointers();
		
		glDrawElements(GL_TRIANGLES, model.getIndexCount(), GL_UNSIGNED_INT, 0);
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);

		glBindVertexArray(0);
		

	}

}
