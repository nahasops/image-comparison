package com.nahasops.imagecomparison.service.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nahasops.imagecomparison.service.ImageComparisonService;

@Service("ImageComparisionService")
public class ImageComparisonServiceImpl implements ImageComparisonService {

	private static final Logger logger = LoggerFactory.getLogger(ImageComparisonServiceImpl.class);

	@Async("imageComparisonExecutor")
	@Override
	public CompletableFuture<Void> compareAndCopy(final Path srcPath, final String comparisonPath,
			final File dstFolder) {

		final Path dstPath = Paths.get(comparisonPath.concat("/").concat(srcPath.getFileName().toString()));
			
		final double result = compare(srcPath, dstPath);

		//If percentage > 10 (found considerable differences between images)
		if (result > 10d ) {
			
			try {
			
				FileUtils.copyFileToDirectory(srcPath.toFile(), dstFolder, true);
			
			} catch (IOException e) {
				logger.error("Copying the file {} - Error: ", dstPath.getFileName().toString(),
						e.getMessage());
				e.printStackTrace();
			}
		}

		return CompletableFuture.completedFuture(null);
	}
	
	@Async("imageComparisonExecutor")
	@Override
	public CompletableFuture<Void> compare(final Path srcPath, final String comparisonPath) {

		final Path dstPath = Paths.get(comparisonPath.concat("/").concat(srcPath.getFileName().toString()));
			
		final double result = compare(srcPath, dstPath);

		logger.info("Percentage {} for image {} ", result, srcPath.getFileName().toString());

		return CompletableFuture.completedFuture(null);
	}

	private double compare(final Path srcPath, final Path dstPath) {

		BufferedImage imgA = null;
		BufferedImage imgB = null;

		File dstFile = dstPath.toFile();

		if (!dstFile.exists()) {
			logger.info("File not found on destination folder {} ", dstFile.getName());
			return -1;
		}

		try {

			imgA = ImageIO.read(srcPath.toFile());
			imgB = ImageIO.read(dstFile);

		} catch (final IOException e) {
			logger.error("Error#1 : reading image {} and {} Error: {} ", srcPath.getFileName().toString(),
					dstPath.getFileName().toString(), e.getMessage());

			return -2;
		}

		final int width1 = imgA.getWidth();
		final int width2 = imgB.getWidth();
		final int height1 = imgA.getHeight();
		final int height2 = imgB.getHeight();

		if ((width1 != width2) || (height1 != height2)) {
			logger.error("Error#2 : Images dimensions mismatch ");
			return -3;
		} else {
			long difference = 0;
			for (int y = 0; y < height1; y++) {
				for (int x = 0; x < width1; x++) {
					final int rgbA = imgA.getRGB(x, y);
					final int rgbB = imgB.getRGB(x, y);
					final int redA = (rgbA >> 16) & 0xff;
					final int greenA = (rgbA >> 8) & 0xff;
					final int blueA = (rgbA) & 0xff;
					final int redB = (rgbB >> 16) & 0xff;
					final int greenB = (rgbB >> 8) & 0xff;
					final int blueB = (rgbB) & 0xff;
					difference += Math.abs(redA - redB);
					difference += Math.abs(greenA - greenB);
					difference += Math.abs(blueA - blueB);
				}
			}

			// Total number of red pixels = width * height
			// Total number of blue pixels = width * height
			// Total number of green pixels = width * height
			// So total number of pixels = width * height * 3
			final double total_pixels = width1 * height1 * 3;

			// Normalizing the value of different pixels
			// for accuracy(average pixels per color
			// component)
			final double avg_different_pixels = difference / total_pixels;

			// There are 255 values of pixels in total
			final double percentage = (avg_different_pixels / 255) * 100;

			// System.out.println("Difference Percentage-->" + percentage);

			return percentage;

		}
	}
}