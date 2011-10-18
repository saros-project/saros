package de.fu_berlin.inf.nebula.utils;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

public class ImageUtils {
	public static Dimension getImageDimensions(final String path)
			throws IOException {
		Dimension result = null;
		String suffix = getFileSuffix(path);
		Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
		if (iter.hasNext()) {
			ImageReader reader = iter.next();
			try {
				ImageInputStream stream = new FileImageInputStream(new File(
						path));
				reader.setInput(stream);
				int width = reader.getWidth(reader.getMinIndex());
				int height = reader.getHeight(reader.getMinIndex());
				result = new Dimension(width, height);
			} catch (IOException e) {
				throw e;
			} finally {
				reader.dispose();
			}
		} else {
			System.err.println("No reader found for given format: " + suffix);
		}
		return result;
	}

	private static String getFileSuffix(final String path) {
		String result = null;
		if (path != null) {
			result = "";
			if (path.lastIndexOf('.') != -1) {
				result = path.substring(path.lastIndexOf('.'));
				if (result.startsWith(".")) {
					result = result.substring(1);
				}
			}
		}
		return result;
	}

	/**
	 * Returns a new scaled image. new Image must be disposed after use.
	 * 
	 * @param image
	 * @param width
	 * @param height
	 * @return
	 */
	public static Image resize(Image image, int width, int height) {
		if (image == null)
			return null;

		final Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, image.getBounds().width,
				image.getBounds().height, 0, 0, width, height);
		gc.dispose();

		return scaled;
	}

	/**
	 * Returns a new scaled image. new Image must be disposed after use.
	 * 
	 * @param image
	 * @param width
	 * @param height
	 * @return
	 */
	public static Image resize(ImageData imageData, int width, int height) {
		if (imageData == null)
			return null;

		if (imageData.width == width && imageData.height == height)
			return new Image(Display.getDefault(), imageData);

		Image tmpImage = null;
		Image fullImage = new Image(Display.getCurrent(), imageData);
		tmpImage = resize(fullImage, width, height);

		fullImage.dispose();
		return tmpImage;
	}

	public static ImageData resize(ImageData imageData, int width, int height,
			boolean antiAliasing) {
		if (antiAliasing) {
			Image tmpImage = resize(imageData, width, height);
			ImageData result = tmpImage.getImageData();
			tmpImage.dispose();
			return result;
		}
		return imageData.scaledTo(width, height);
	}
}
