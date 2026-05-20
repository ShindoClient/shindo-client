package me.miki.shindo.management.shader

import me.miki.shindo.logger.ShindoLogger
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.Calendar

class ShaderManager {
    private val shaderCache = HashMap<File, Int>()
    private val resourceShaderCache = HashMap<ResourceLocation, Int>()
    private var quadVAO = -1
    private var initialized = false

    fun init() {
        if (initialized) return
        try {
            quadVAO = createQuad()
            initialized = true
        } catch (e: Exception) {
            ShindoLogger.error("Failed to initialize shader manager", e)
        }
    }

    fun loadShader(shaderResource: ResourceLocation): Int {
        if (!initialized) init()
        resourceShaderCache[shaderResource]?.let { return it }
        return try {
            var fragmentSource = readShaderResource(shaderResource)
            if (fragmentSource.isNullOrBlank()) fragmentSource = getDefaultFragmentShader()
            val program = createShaderProgram(DEFAULT_VERTEX_SHADER, fragmentSource!!)
            if (program != -1) resourceShaderCache[shaderResource] = program
            program
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load shader resource: $shaderResource", e)
            -1
        }
    }

    fun loadShader(fragmentShaderFile: File): Int {
        if (!initialized) init()
        shaderCache[fragmentShaderFile]?.let { return it }
        return try {
            var fragmentSource = readShaderFile(fragmentShaderFile)
            if (fragmentSource.isNullOrBlank()) fragmentSource = getDefaultFragmentShader()
            val program = createShaderProgram(DEFAULT_VERTEX_SHADER, fragmentSource!!)
            if (program != -1) shaderCache[fragmentShaderFile] = program
            program
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load shader: ${fragmentShaderFile.name}", e)
            -1
        }
    }

    fun renderShader(
        shaderId: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ) {
        if (shaderId == -1 || !initialized) return
        val mc = Minecraft.getMinecraft()
        val sr = ScaledResolution(mc)
        try {
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT or GL11.GL_TEXTURE_BIT or GL11.GL_COLOR_BUFFER_BIT)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL20.glUseProgram(shaderId)
            setShaderUniforms(shaderId, x, y, width, height, sr)
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
            GL11.glOrtho(0.0, sr.scaledWidth.toDouble(), sr.scaledHeight.toDouble(), 0.0, -1.0, 1.0)
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            val positionAttrib = GL20.glGetAttribLocation(shaderId, "position")
            if (positionAttrib != -1) GL20.glEnableVertexAttribArray(positionAttrib)
            GL11.glBegin(GL11.GL_QUADS)
            if (positionAttrib != -1) GL20.glVertexAttrib2f(positionAttrib, -1f, -1f)
            GL11.glTexCoord2f(0f, 1f)
            GL11.glVertex2f(x, y + height)
            if (positionAttrib != -1) GL20.glVertexAttrib2f(positionAttrib, 1f, -1f)
            GL11.glTexCoord2f(1f, 1f)
            GL11.glVertex2f(x + width, y + height)
            if (positionAttrib != -1) GL20.glVertexAttrib2f(positionAttrib, 1f, 1f)
            GL11.glTexCoord2f(1f, 0f)
            GL11.glVertex2f(x + width, y)
            if (positionAttrib != -1) GL20.glVertexAttrib2f(positionAttrib, -1f, 1f)
            GL11.glTexCoord2f(0f, 0f)
            GL11.glVertex2f(x, y)
            GL11.glEnd()
            if (positionAttrib != -1) GL20.glDisableVertexAttribArray(positionAttrib)
            GL11.glPopMatrix()
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPopMatrix()
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
        } catch (e: Exception) {
            ShindoLogger.error("Error rendering shader", e)
        } finally {
            GL20.glUseProgram(0)
            GL11.glPopAttrib()
        }
    }

    private fun setShaderUniforms(
        shaderId: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        sr: ScaledResolution,
    ) {
        val currentTime = (System.currentTimeMillis() % 100000) / 1000f
        GL20.glGetUniformLocation(shaderId, "time").takeIf { it != -1 }?.let { GL20.glUniform1f(it, currentTime) }
        GL20.glGetUniformLocation(shaderId, "iTime").takeIf { it != -1 }?.let { GL20.glUniform1f(it, currentTime) }
        GL20
            .glGetUniformLocation(shaderId, "resolution")
            .takeIf { it != -1 }
            ?.let { GL20.glUniform2f(it, width, height) }
        GL20
            .glGetUniformLocation(shaderId, "iResolution")
            .takeIf { it != -1 }
            ?.let { GL20.glUniform3f(it, width, height, 1f) }
        GL20.glGetUniformLocation(shaderId, "mouse").takeIf { it != -1 }?.let { GL20.glUniform2f(it, 0.5f, 0.5f) }
        GL20
            .glGetUniformLocation(shaderId, "iMouse")
            .takeIf { it != -1 }
            ?.let { GL20.glUniform4f(it, width * 0.5f, height * 0.5f, 0f, 0f) }
        GL20
            .glGetUniformLocation(shaderId, "iFrame")
            .takeIf { it != -1 }
            ?.let { GL20.glUniform1i(it, (currentTime * 60).toInt()) }
        GL20.glGetUniformLocation(shaderId, "iDate").takeIf { it != -1 }?.let { loc ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = System.currentTimeMillis()
            val year = cal.get(Calendar.YEAR).toFloat()
            val month = (cal.get(Calendar.MONTH) + 1).toFloat()
            val day = cal.get(Calendar.DAY_OF_MONTH).toFloat()
            val secondsInDay =
                (
                    cal.get(
                        Calendar.HOUR_OF_DAY,
                    ) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND)
                ).toFloat()
            GL20.glUniform4f(loc, year, month, day, secondsInDay)
        }
    }

    private fun readShaderResource(resource: ResourceLocation): String? {
        return try {
            val stream =
                Minecraft
                    .getMinecraft()
                    .resourceManager
                    .getResource(resource)
                    .inputStream ?: return null
            stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to read shader resource: $resource", e)
            null
        }
    }

    private fun readShaderFile(file: File): String? =
        try {
            if (!file.exists()) null else String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
        } catch (e: IOException) {
            ShindoLogger.error("Failed to read shader file: ${file.name}", e)
            null
        }

    private fun getDefaultFragmentShader(): String =
        "#version 120\n" +
            "uniform float time;\n" +
            "uniform vec2 resolution;\n" +
            "varying vec2 fragCoord;\n" +
            "void main() {\n" +
            "    vec2 uv = fragCoord;\n" +
            "    vec3 color = vec3(0.5 + 0.5 * cos(time + uv.xyx + vec3(0, 2, 4)));\n" +
            "    gl_FragColor = vec4(color, 1.0);\n" +
            "}"

    private fun createShaderProgram(
        vertexSource: String,
        fragmentSource: String,
    ): Int {
        val vertexShader = compileShader(GL20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == -1) return -1
        val fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragmentShader == -1) {
            GL20.glDeleteShader(vertexShader)
            return -1
        }
        val program = GL20.glCreateProgram()
        GL20.glAttachShader(program, vertexShader)
        GL20.glAttachShader(program, fragmentShader)
        GL20.glLinkProgram(program)
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            ShindoLogger.error("Shader program linking failed: " + GL20.glGetProgramInfoLog(program, 1024))
            GL20.glDeleteProgram(program)
            GL20.glDeleteShader(vertexShader)
            GL20.glDeleteShader(fragmentShader)
            return -1
        }
        GL20.glDeleteShader(vertexShader)
        GL20.glDeleteShader(fragmentShader)
        return program
    }

    private fun compileShader(
        type: Int,
        source: String,
    ): Int {
        val shader = GL20.glCreateShader(type)
        GL20.glShaderSource(shader, source)
        GL20.glCompileShader(shader)
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            ShindoLogger.error("Shader compilation failed: " + GL20.glGetShaderInfoLog(shader, 1024))
            GL20.glDeleteShader(shader)
            return -1
        }
        return shader
    }

    private fun createQuad(): Int = 0

    fun cleanup() {
        shaderCache.values.forEach { if (it != -1) GL20.glDeleteProgram(it) }
        resourceShaderCache.values.forEach { if (it != -1) GL20.glDeleteProgram(it) }
        shaderCache.clear()
        resourceShaderCache.clear()
        initialized = false
    }

    companion object {
        private const val DEFAULT_VERTEX_SHADER =
            "#version 120\n" +
                "attribute vec2 position;\n" +
                "varying vec2 fragCoord;\n" +
                "varying vec2 vTexCoord;\n" +
                "uniform vec2 resolution;\n" +
                "void main() {\n" +
                "    fragCoord = (position * 0.5 + 0.5);\n" +
                "    vTexCoord = fragCoord;\n" +
                "    gl_Position = vec4(position, 0.0, 1.0);\n" +
                "}"
    }
}
