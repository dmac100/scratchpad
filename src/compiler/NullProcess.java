package compiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A process that exits immediately with no output.
 */
public class NullProcess extends Process {
	public OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(new byte[0]);
	}

	public InputStream getErrorStream() {
		return new ByteArrayInputStream(new byte[0]);
	}

	public int waitFor() throws InterruptedException {
		return 0;
	}

	public int exitValue() {
		return 0;
	}

	public void destroy() {
	}
}
