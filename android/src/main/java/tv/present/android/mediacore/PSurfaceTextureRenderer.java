package tv.present.android.mediacore;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import tv.present.android.util.PLog;

/**
 * Present Surface Texture Renderer
 *
 * This class renders a texture onto a surface using OpenGL ES 2.00.
 *
 * June 19, 2014
 * @author Kyle Weisel (kyle@present.tv)
 *
 */
public final class PSurfaceTextureRenderer {

    private static final String TAG = "tv.present.android.recorder.SurfaceTextureRenderer";

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";
    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +      // highp here doesn't seem to matter
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    /* ==== Suppressing field can be local warning ====
     * Reason:  The mTriangleVeriticiesData float[] can be local but remains a member variable for
     * future reference or to enable future extensibility.  Unused member warnings are not
     * dangerous.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f, 1.0f, 0, 0.f, 1.f,
            1.0f, 1.0f, 0, 1.f, 1.f,
    };

    private FloatBuffer mTriangleVertices;
    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];
    private int mProgram;
    private int mTextureID = -12345;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    /**
     * Constructs a SurfaceTextureRenderer object.
     */
    public PSurfaceTextureRenderer() {
        this.mTriangleVertices = ByteBuffer.allocateDirect(this.mTriangleVerticesData.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mTriangleVertices.put(this.mTriangleVerticesData).position(0);
        Matrix.setIdentityM(this.mSTMatrix, 0);
    }

    /**
     * Gets the Id of the texture.
     * @return
     */
    public final int getTextureId() {
        return this.mTextureID;
    }

    /**
     * Draws a frame to a SurfaceTexture.
     * @param surfaceTexture is the SurfaceTexture to draw the frame to.
     */
    public final void drawFrame(SurfaceTexture surfaceTexture) {

        this.checkGlError("onDrawFrame() start");
        surfaceTexture.getTransformMatrix(mSTMatrix);

        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        checkGlError("After glUseProgram() call");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);

        this.mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(this.maPositionHandle, 3, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, this.mTriangleVertices);
        checkGlError("After glVertexAttribPointer(Position) call");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("After glEnableVertexAttribArray(PositionHandle) call");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(this.maTextureHandle, 2, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, this.mTriangleVertices);
        checkGlError("After glVertexAttribPointer(TextureHandle) call");
        GLES20.glEnableVertexAttribArray(this.maTextureHandle);
        checkGlError("After glEnableVertexAttribArray(TextureHandle) call");

        Matrix.setIdentityM(this.mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(this.muMVPMatrixHandle, 1, false, this.mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(this.muSTMatrixHandle, 1, false, this.mSTMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("After glDrawArrays() call");
        GLES20.glFinish();

    }

    /**
     * Initializes GL state.  This method should be called after the EGL surface has been created
     * and made current.
     */
    public final void surfaceCreated() {

        this.mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);

        if (this.mProgram == 0) {
            throw new RuntimeException("Failed creating program");
        }

        this.maPositionHandle = GLES20.glGetAttribLocation(this.mProgram, "aPosition");
        checkGlError("After glGetAttribLocation(Position) call");

        if (this.maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition!");
        }

        this.maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("After glGetAttribLocation(TextureCoord)");

        if (this.maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        this.muMVPMatrixHandle = GLES20.glGetUniformLocation(this.mProgram, "uMVPMatrix");
        checkGlError("After glGetUniformLocation(uMVPMatrix) call");

        if (this.muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix!");
        }

        this.muSTMatrixHandle = GLES20.glGetUniformLocation(this.mProgram, "uSTMatrix");
        checkGlError("After glGetUniformLocation(uSTMatrix) call");

        if (this.muSTMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uSTMatrix");
        }

        final int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        this.mTextureID = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, this.mTextureID);
        checkGlError("After glBindTexture(mTextureID) call");

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("After glTexParameter() call");

    }

    /**
     * Replaces the fragment shader.  Pass in null to resetWithChunk to default.
     */
    public final void changeFragmentShader(String fragmentShader) {

        if (fragmentShader == null) {
            fragmentShader = FRAGMENT_SHADER;
        }

        GLES20.glDeleteProgram(this.mProgram);

        this.mProgram = createProgram(VERTEX_SHADER, fragmentShader);

        if (this.mProgram == 0) {
            throw new RuntimeException("Failed creating program!");
        }

    }

    /**
     * Loads a shader.
     * @param shaderType is the integer representation of a shader type.
     * @param source is the shader source as a String.
     * @return an integer that defines the shader.
     */
    private int loadShader(final int shaderType, final String source) {
        
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("After glCreateShader type=" + shaderType + " call");
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        
        final int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        
        if (compiled[0] == 0) {
            PLog.logError(TAG, "Could not compile shader " + shaderType + " : " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        return shader;
        
    }

    /**
     * Creates the program.
     * @param vertexSource is a String.
     * @param fragmentSource is a String.
     * @return an integer.
     */
    private int createProgram(final String vertexSource, final String fragmentSource) {

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);

        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);

        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");

        if (program == 0) {
            PLog.logError(TAG, "Could not create program");
        }

        GLES20.glAttachShader(program, vertexShader);
        checkGlError("After glAttachShader() call");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("After glAttachShader() call");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);

        if (linkStatus[0] != GLES20.GL_TRUE) {
            PLog.logError(TAG, "Could not link program : " + GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }

        return program;

    }

    /**
     * Checks for a GL error.
     * @param operation is the string message that represents the operation.  This is only used for
     *                  logging purposes.
     * @throws RuntimeException when there is any error.
     */
    public final void checkGlError(final String operation) throws RuntimeException {
        final int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            PLog.logError(TAG, operation + ": glError " + error);
            throw new RuntimeException(operation + ": glError " + error);
        }
    }

}